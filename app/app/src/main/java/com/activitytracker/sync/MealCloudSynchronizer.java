package com.activitytracker.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.activitytracker.model.PlannedMeal;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles periodic asynchronous upload of committed meals to Google Cloud Storage
 * bucket "georgeruiz-activity-tracker-meal-v4" as TSV files (FR 1.3).
 *
 * Authenticates via OAuth 2.0 using Google's standard security utilities (GoogleOAuthManager).
 */
public class MealCloudSynchronizer {

    public static final long DEFAULT_SYNC_INTERVAL_MINUTES = 15;

    private static MealCloudSynchronizer sInstance;

    private final Context context;
    private final CloudSyncStore store;
    private final CloudStorageClient storageClient;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> periodicTask;

    public static class SyncResult {
        public final int uploadedCount;
        public final String fileName;
        public final String message;
        public final boolean success;
        public final Intent recoveryIntent;

        public SyncResult(int uploadedCount, String fileName, String message, boolean success) {
            this(uploadedCount, fileName, message, success, null);
        }

        public SyncResult(int uploadedCount, String fileName, String message, boolean success, Intent recoveryIntent) {
            this.uploadedCount = uploadedCount;
            this.fileName = fileName != null ? fileName : "";
            this.message = message != null ? message : "";
            this.success = success;
            this.recoveryIntent = recoveryIntent;
        }

        public boolean isUserRecoverable() {
            return recoveryIntent != null;
        }

        @Override
        public String toString() {
            return String.format("SyncResult: %d uploaded to %s (success=%b): %s",
                    uploadedCount, fileName, success, message);
        }
    }

    public interface SyncCallback {
        void onSuccess(SyncResult result);
        void onError(String errorMessage);
        default void onRecoverableAuth(Intent recoveryIntent) {
            onError("NeedRemoteConsent");
        }
    }

    public static synchronized MealCloudSynchronizer getInstance(Context context) {
        if (sInstance == null) {
            Context appContext = context.getApplicationContext();
            sInstance = new MealCloudSynchronizer(appContext,
                    new DefaultCloudSyncStore(appContext),
                    new CloudStorageClient());
        }
        return sInstance;
    }

    public MealCloudSynchronizer(Context context, CloudSyncStore store, CloudStorageClient storageClient) {
        this.context = context;
        this.store = store;
        this.storageClient = storageClient;
        this.executor = Executors.newSingleThreadExecutor();

        Looper looper = null;
        try {
            looper = Looper.getMainLooper();
        } catch (Throwable ignored) {}
        this.mainHandler = (looper != null) ? new Handler(looper) : null;
    }

    /**
     * Starts periodic sync at regular intervals if cloud sync is enabled.
     */
    public synchronized void startPeriodicSyncIfEnabled() {
        if (!store.isCloudSyncEnabled()) {
            stopPeriodicSync();
            return;
        }

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        if (periodicTask == null || periodicTask.isCancelled() || periodicTask.isDone()) {
            periodicTask = scheduler.scheduleWithFixedDelay(
                    this::syncPendingMeals,
                    0,
                    DEFAULT_SYNC_INTERVAL_MINUTES,
                    TimeUnit.MINUTES
            );
        }
    }

    /**
     * Stops the periodic sync scheduler.
     */
    public synchronized void stopPeriodicSync() {
        if (periodicTask != null) {
            periodicTask.cancel(true);
            periodicTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Triggers asynchronous push of all unuploaded committed meals.
     */
    public void syncPendingMealsAsync(SyncCallback callback) {
        executor.execute(() -> {
            try {
                SyncResult result = syncPendingMeals();
                if (result.success) {
                    postSuccess(callback, result);
                } else if (result.isUserRecoverable()) {
                    postRecoverableAuth(callback, result.recoveryIntent);
                } else {
                    postError(callback, result.message);
                }
            } catch (Throwable t) {
                postError(callback, t.getMessage());
            }
        });
    }

    /**
     * Performs synchronous synchronization: retrieves unuploaded committed meals,
     * formats TSV, fetches OAuth 2.0 token, uploads to Cloud Storage, and marks meals as uploaded.
     */
    public synchronized SyncResult syncPendingMeals() {
        try {
            List<PlannedMeal> pendingMeals = store.getUnuploadedCommittedMeals();
            if (pendingMeals == null || pendingMeals.isEmpty()) {
                String msg = "No pending committed meals to upload";
                store.setLastSyncStatus(msg);
                return new SyncResult(0, "", msg, true);
            }

            // 1. Acquire OAuth 2.0 Bearer access token
            String accountName = store.getCloudAccountName();
            String accessToken = GoogleOAuthManager.getAccessToken(context, accountName);
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new IllegalStateException("Failed to obtain OAuth 2.0 access token for account: " + accountName);
            }

            // 2. Format meals into 13-column TSV
            String tsvData = MealTsvFormatter.formatMealsToTsv(pendingMeals);

            // 3. Generate unique TSV object name: meals_yyyyMMdd_HHmmss_xxx.tsv
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String timestamp = sdf.format(new Date());
            String fileName = String.format("meals_%s_%d.tsv", timestamp, pendingMeals.size());

            // 4. Upload to Cloud Storage bucket
            storageClient.uploadTsv(accessToken, fileName, tsvData);

            // 5. Mark meals as uploaded in SQLite
            List<Long> uploadedIds = new ArrayList<>();
            for (PlannedMeal m : pendingMeals) {
                uploadedIds.add(m.getId());
            }
            store.markMealsAsUploaded(uploadedIds);

            // 6. Record status & timestamp
            long now = System.currentTimeMillis();
            store.setLastSyncTimestamp(now);
            String successMsg = String.format(Locale.US, "Uploaded %d meal(s) successfully as %s",
                    pendingMeals.size(), fileName);
            store.setLastSyncStatus(successMsg);

            return new SyncResult(pendingMeals.size(), fileName, successMsg, true);

        } catch (UserRecoverableAuthException urae) {
            Intent recoveryIntent = urae.getIntent();
            String errorMsg = "Google authorization required: " + (urae.getMessage() != null ? urae.getMessage() : "NeedRemoteConsent");
            store.setLastSyncStatus(errorMsg);
            return new SyncResult(0, "", errorMsg, false, recoveryIntent);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof UserRecoverableAuthException) {
                    Intent recoveryIntent = ((UserRecoverableAuthException) cause).getIntent();
                    String errorMsg = "Google authorization required: " + (cause.getMessage() != null ? cause.getMessage() : "NeedRemoteConsent");
                    store.setLastSyncStatus(errorMsg);
                    return new SyncResult(0, "", errorMsg, false, recoveryIntent);
                }
                cause = cause.getCause();
            }
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            store.setLastSyncStatus("Upload error: " + errorMsg);
            return new SyncResult(0, "", errorMsg, false);
        }
    }

    private void postSuccess(SyncCallback callback, SyncResult result) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        } else {
            callback.onSuccess(result);
        }
    }

    private void postRecoverableAuth(SyncCallback callback, Intent recoveryIntent) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onRecoverableAuth(recoveryIntent));
        } else {
            callback.onRecoverableAuth(recoveryIntent);
        }
    }

    private void postError(SyncCallback callback, String errorMessage) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onError(errorMessage));
        } else {
            callback.onError(errorMessage);
        }
    }
}

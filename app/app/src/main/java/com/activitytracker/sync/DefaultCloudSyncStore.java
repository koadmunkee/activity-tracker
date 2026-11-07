package com.activitytracker.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.activitytracker.db.MealRepository;
import com.activitytracker.model.PlannedMeal;

import java.util.List;

/**
 * Default implementation of CloudSyncStore bridging MealRepository SQLite operations
 * and SharedPreferences configuration flags.
 */
public class DefaultCloudSyncStore implements CloudSyncStore {

    public static final String PREF_NAME = "cloud_meal_sync_prefs";
    public static final String KEY_ENABLED = "cloud_sync_enabled";
    public static final String KEY_ACCOUNT = "cloud_account_name";
    public static final String KEY_LAST_SYNC_TIME = "last_sync_timestamp";
    public static final String KEY_LAST_STATUS = "last_sync_status";

    private final MealRepository mealRepository;
    private final SharedPreferences prefs;

    public DefaultCloudSyncStore(Context context) {
        this.mealRepository = new MealRepository(context);
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public DefaultCloudSyncStore(MealRepository mealRepository, SharedPreferences prefs) {
        this.mealRepository = mealRepository;
        this.prefs = prefs;
    }

    @Override
    public List<PlannedMeal> getUnuploadedCommittedMeals() {
        return mealRepository.getUnuploadedCommittedMeals();
    }

    @Override
    public void markMealsAsUploaded(List<Long> mealIds) {
        mealRepository.markMealsAsUploaded(mealIds);
    }

    @Override
    public boolean isCloudSyncEnabled() {
        return prefs != null && prefs.getBoolean(KEY_ENABLED, false);
    }

    @Override
    public void setCloudSyncEnabled(boolean enabled) {
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
        }
    }

    @Override
    public String getCloudAccountName() {
        return prefs != null ? prefs.getString(KEY_ACCOUNT, "") : "";
    }

    @Override
    public void setCloudAccountName(String accountName) {
        if (prefs != null) {
            prefs.edit().putString(KEY_ACCOUNT, accountName != null ? accountName : "").apply();
        }
    }

    @Override
    public long getLastSyncTimestamp() {
        return prefs != null ? prefs.getLong(KEY_LAST_SYNC_TIME, 0L) : 0L;
    }

    @Override
    public void setLastSyncTimestamp(long timestamp) {
        if (prefs != null) {
            prefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply();
        }
    }

    @Override
    public String getLastSyncStatus() {
        return prefs != null ? prefs.getString(KEY_LAST_STATUS, "") : "";
    }

    @Override
    public void setLastSyncStatus(String status) {
        if (prefs != null) {
            prefs.edit().putString(KEY_LAST_STATUS, status != null ? status : "").apply();
        }
    }
}

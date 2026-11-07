package com.activitytracker.ui.config;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.db.ConfigRepository;
import com.activitytracker.db.FoodRepository;
import com.activitytracker.sync.CloudSyncStore;
import com.activitytracker.sync.DefaultCloudSyncStore;
import com.activitytracker.sync.FoodLibrarySynchronizer;
import com.activitytracker.sync.GoogleOAuthManager;
import com.activitytracker.sync.MealCloudSynchronizer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;

/**
 * Configuration View - enables defining the number of meals in a day and custom names (FR 1.1),
 * synchronizing the food library with the master food library from Google Sheets (FR 1.2),
 * and pushing committed meal data to Google Cloud Storage (FR 1.3).
 */
public class ConfigFragment extends Fragment {

    private TextView textMealCount;
    private MaterialButton btnDecrease;
    private MaterialButton btnIncrease;
    private RecyclerView recyclerMealConfigs;
    private TextView textError;
    private MaterialButton btnSave;

    // Food library synchronization UI (FR 1.2)
    private TextInputEditText etSyncUrl;
    private MaterialButton btnSync;
    private View progressSync;
    private TextView textSyncStatus;
    private FoodLibrarySynchronizer foodSynchronizer;

    // Cloud storage push UI (FR 1.3)
    private MaterialSwitch switchCloudSync;
    private TextView textCloudAccount;
    private TextView textCloudSyncStatus;
    private View progressCloudSync;
    private MaterialButton btnCloudSyncNow;
    private CloudSyncStore cloudSyncStore;
    private MealCloudSynchronizer mealCloudSynchronizer;
    private ActivityResultLauncher<Intent> accountPickerLauncher;
    private ActivityResultLauncher<Intent> authConsentLauncher;

    private ConfigRepository configRepository;
    private final List<String> currentMealNames = new ArrayList<>();
    private MealNameAdapter adapter;

    private static final int MIN_MEALS = 1;
    private static final int MAX_MEALS = 8;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String accountName = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null && !accountName.trim().isEmpty()) {
                            if (cloudSyncStore != null) {
                                cloudSyncStore.setCloudAccountName(accountName.trim());
                                cloudSyncStore.setCloudSyncEnabled(true);
                            }
                            updateCloudSyncUi();
                            if (mealCloudSynchronizer != null) {
                                mealCloudSynchronizer.startPeriodicSyncIfEnabled();
                            }
                            triggerCloudSyncNow();
                            return;
                        }
                    }
                    if (cloudSyncStore != null && cloudSyncStore.getCloudAccountName().isEmpty()) {
                        cloudSyncStore.setCloudSyncEnabled(false);
                    }
                    updateCloudSyncUi();
                }
        );

        authConsentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        Toast.makeText(requireContext(), "Permission granted. Resuming upload…", Toast.LENGTH_SHORT).show();
                        triggerCloudSyncNow();
                    } else {
                        Toast.makeText(requireContext(), "Cloud storage permission was not granted.", Toast.LENGTH_LONG).show();
                        updateCloudSyncUi();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();
        configRepository = new ConfigRepository(context);

        // Daily meal configuration views
        textMealCount = view.findViewById(R.id.text_meal_count_display);
        btnDecrease = view.findViewById(R.id.btn_decrease_meal_count);
        btnIncrease = view.findViewById(R.id.btn_increase_meal_count);
        recyclerMealConfigs = view.findViewById(R.id.recycler_meal_configs);
        textError = view.findViewById(R.id.text_config_error);
        btnSave = view.findViewById(R.id.btn_save_config);

        recyclerMealConfigs.setLayoutManager(new LinearLayoutManager(context));
        adapter = new MealNameAdapter(context, currentMealNames);
        recyclerMealConfigs.setAdapter(adapter);

        btnDecrease.setOnClickListener(v -> decreaseMealCount());
        btnIncrease.setOnClickListener(v -> increaseMealCount());
        btnSave.setOnClickListener(v -> saveConfiguration());

        // Setup master food library sync (FR 1.2)
        FoodRepository foodRepository = new FoodRepository(context);
        foodSynchronizer = new FoodLibrarySynchronizer(foodRepository);

        etSyncUrl = view.findViewById(R.id.et_sync_url);
        btnSync = view.findViewById(R.id.btn_sync_food_library);
        progressSync = view.findViewById(R.id.progress_sync);
        textSyncStatus = view.findViewById(R.id.text_sync_status);

        if (etSyncUrl != null && (etSyncUrl.getText() == null || etSyncUrl.getText().toString().trim().isEmpty())) {
            etSyncUrl.setText(FoodLibrarySynchronizer.DEFAULT_MASTER_URL);
        }

        if (btnSync != null) {
            btnSync.setOnClickListener(v -> startFoodSync());
        }

        // Setup Cloud Storage push (FR 1.3)
        cloudSyncStore = new DefaultCloudSyncStore(context);
        mealCloudSynchronizer = MealCloudSynchronizer.getInstance(context);

        switchCloudSync = view.findViewById(R.id.switch_cloud_sync);
        textCloudAccount = view.findViewById(R.id.text_cloud_account);
        textCloudSyncStatus = view.findViewById(R.id.text_cloud_sync_status);
        progressCloudSync = view.findViewById(R.id.progress_cloud_sync);
        btnCloudSyncNow = view.findViewById(R.id.btn_cloud_sync_now);

        if (switchCloudSync != null) {
            switchCloudSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    String account = cloudSyncStore.getCloudAccountName();
                    if (account == null || account.trim().isEmpty()) {
                        try {
                            Intent chooseIntent = GoogleOAuthManager.createChooseAccountIntent(requireContext());
                            accountPickerLauncher.launch(chooseIntent);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Account selection error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            switchCloudSync.setChecked(false);
                        }
                    } else {
                        cloudSyncStore.setCloudSyncEnabled(true);
                        updateCloudSyncUi();
                        mealCloudSynchronizer.startPeriodicSyncIfEnabled();
                        triggerCloudSyncNow();
                    }
                } else {
                    cloudSyncStore.setCloudSyncEnabled(false);
                    mealCloudSynchronizer.stopPeriodicSync();
                    updateCloudSyncUi();
                }
            });
        }

        if (btnCloudSyncNow != null) {
            btnCloudSyncNow.setOnClickListener(v -> triggerCloudSyncNow());
        }

        loadConfiguration();
        updateCloudSyncUi();
    }

    public void loadConfiguration() {
        currentMealNames.clear();
        currentMealNames.addAll(configRepository.getConfiguredMealNames());
        updateCountDisplay();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateCountDisplay() {
        textMealCount.setText(String.valueOf(currentMealNames.size()));
        btnDecrease.setEnabled(currentMealNames.size() > MIN_MEALS);
        btnIncrease.setEnabled(currentMealNames.size() < MAX_MEALS);
    }

    private void decreaseMealCount() {
        if (currentMealNames.size() > MIN_MEALS) {
            int removeIndex = currentMealNames.size() - 1;
            currentMealNames.remove(removeIndex);
            updateCountDisplay();
            adapter.notifyItemRemoved(removeIndex);
        }
    }

    private void increaseMealCount() {
        if (currentMealNames.size() < MAX_MEALS) {
            int newIndex = currentMealNames.size() + 1;
            String defaultName;
            switch (newIndex) {
                case 1: defaultName = "Breakfast"; break;
                case 2: defaultName = "Lunch"; break;
                case 3: defaultName = "Dinner"; break;
                case 4: defaultName = "Snack"; break;
                default: defaultName = "Meal " + newIndex; break;
            }
            currentMealNames.add(defaultName);
            updateCountDisplay();
            adapter.notifyItemInserted(currentMealNames.size() - 1);
        }
    }

    private void saveConfiguration() {
        textError.setVisibility(View.GONE);

        // Validation: verify no empty meal names
        for (int i = 0; i < currentMealNames.size(); i++) {
            String name = currentMealNames.get(i);
            if (name == null || name.trim().isEmpty()) {
                textError.setVisibility(View.VISIBLE);
                textError.setText(String.format("Please enter a valid name for Meal %d.", i + 1));
                return;
            }
        }

        configRepository.saveConfiguredMealNames(currentMealNames);
        Toast.makeText(requireContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
    }

    private void startFoodSync() {
        String url = (etSyncUrl != null && etSyncUrl.getText() != null)
                ? etSyncUrl.getText().toString().trim()
                : FoodLibrarySynchronizer.DEFAULT_MASTER_URL;
        if (url.isEmpty()) {
            url = FoodLibrarySynchronizer.DEFAULT_MASTER_URL;
            if (etSyncUrl != null) etSyncUrl.setText(url);
        }

        if (btnSync != null) btnSync.setEnabled(false);
        if (progressSync != null) progressSync.setVisibility(View.VISIBLE);
        if (textSyncStatus != null) {
            textSyncStatus.setVisibility(View.VISIBLE);
            textSyncStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.clinical_teal));
            textSyncStatus.setText(R.string.sync_in_progress);
        }

        foodSynchronizer.syncFromUrlAsync(url, new FoodLibrarySynchronizer.SyncCallback() {
            @Override
            public void onProgress(String message) {
                if (isAdded() && textSyncStatus != null) {
                    textSyncStatus.setText(message);
                }
            }

            @Override
            public void onSuccess(FoodLibrarySynchronizer.SyncResult result) {
                if (!isAdded()) return;
                if (btnSync != null) btnSync.setEnabled(true);
                if (progressSync != null) progressSync.setVisibility(View.GONE);
                String successMsg = getString(R.string.sync_success,
                        result.addedCount, result.updatedCount, result.unchangedCount);
                if (textSyncStatus != null) {
                    textSyncStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.clinical_teal));
                    textSyncStatus.setText(successMsg);
                }
                Toast.makeText(requireContext(), successMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                if (btnSync != null) btnSync.setEnabled(true);
                if (progressSync != null) progressSync.setVisibility(View.GONE);
                String errorMsg = getString(R.string.sync_error, errorMessage);
                if (textSyncStatus != null) {
                    textSyncStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.clinical_coral));
                    textSyncStatus.setText(errorMsg);
                }
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateCloudSyncUi() {
        if (switchCloudSync == null || cloudSyncStore == null) return;

        boolean enabled = cloudSyncStore.isCloudSyncEnabled();
        String account = cloudSyncStore.getCloudAccountName();

        switchCloudSync.setChecked(enabled);

        if (enabled && account != null && !account.trim().isEmpty()) {
            if (textCloudAccount != null) {
                textCloudAccount.setVisibility(View.VISIBLE);
                textCloudAccount.setText(getString(R.string.cloud_account_label, account.trim()));
            }
            if (btnCloudSyncNow != null) {
                btnCloudSyncNow.setVisibility(View.VISIBLE);
            }
            if (textCloudSyncStatus != null) {
                String lastStatus = cloudSyncStore.getLastSyncStatus();
                if (lastStatus != null && !lastStatus.isEmpty()) {
                    textCloudSyncStatus.setText(lastStatus);
                } else {
                    textCloudSyncStatus.setText(R.string.cloud_sync_enabled);
                }
            }
        } else {
            if (textCloudAccount != null) {
                textCloudAccount.setVisibility(View.GONE);
            }
            if (btnCloudSyncNow != null) {
                btnCloudSyncNow.setVisibility(View.GONE);
            }
            if (textCloudSyncStatus != null) {
                textCloudSyncStatus.setText(R.string.cloud_sync_disabled);
            }
        }
    }

    private void triggerCloudSyncNow() {
        if (mealCloudSynchronizer == null) return;
        if (progressCloudSync != null) progressCloudSync.setVisibility(View.VISIBLE);
        if (btnCloudSyncNow != null) btnCloudSyncNow.setEnabled(false);
        if (textCloudSyncStatus != null) {
            textCloudSyncStatus.setText(R.string.cloud_sync_in_progress);
        }

        mealCloudSynchronizer.syncPendingMealsAsync(new MealCloudSynchronizer.SyncCallback() {
            @Override
            public void onSuccess(MealCloudSynchronizer.SyncResult result) {
                if (!isAdded()) return;
                if (progressCloudSync != null) progressCloudSync.setVisibility(View.GONE);
                if (btnCloudSyncNow != null) btnCloudSyncNow.setEnabled(true);
                updateCloudSyncUi();
                if (result.uploadedCount > 0) {
                    Toast.makeText(requireContext(),
                            getString(R.string.cloud_sync_success, result.uploadedCount),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), R.string.cloud_sync_no_pending, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRecoverableAuth(Intent recoveryIntent) {
                if (!isAdded()) return;
                if (progressCloudSync != null) progressCloudSync.setVisibility(View.GONE);
                if (btnCloudSyncNow != null) btnCloudSyncNow.setEnabled(true);
                updateCloudSyncUi();
                if (recoveryIntent != null && authConsentLauncher != null) {
                    authConsentLauncher.launch(recoveryIntent);
                } else {
                    onError("NeedRemoteConsent");
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                if (progressCloudSync != null) progressCloudSync.setVisibility(View.GONE);
                if (btnCloudSyncNow != null) btnCloudSyncNow.setEnabled(true);
                updateCloudSyncUi();
                Toast.makeText(requireContext(),
                        getString(R.string.cloud_sync_error, errorMessage),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConfiguration();
        updateCloudSyncUi();
    }
}

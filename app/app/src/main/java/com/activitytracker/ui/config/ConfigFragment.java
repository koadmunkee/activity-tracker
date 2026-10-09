package com.activitytracker.ui.config;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.db.ConfigRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration View - enables defining the number of meals in a day and custom names (FR 1.1).
 */
public class ConfigFragment extends Fragment {

    private TextView textMealCount;
    private MaterialButton btnDecrease;
    private MaterialButton btnIncrease;
    private RecyclerView recyclerMealConfigs;
    private TextView textError;
    private MaterialButton btnSave;

    private ConfigRepository configRepository;
    private final List<String> currentMealNames = new ArrayList<>();
    private MealNameAdapter adapter;

    private static final int MIN_MEALS = 1;
    private static final int MAX_MEALS = 8;

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

        loadConfiguration();
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

    @Override
    public void onResume() {
        super.onResume();
        loadConfiguration();
    }
}

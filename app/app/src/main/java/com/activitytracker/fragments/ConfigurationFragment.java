package com.activitytracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.activitytracker.R;
import com.activitytracker.db.DatabaseHelper;
import com.activitytracker.models.Meal;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextInputEditText editNumMeals;
    private LinearLayout containerMealNames;
    private List<TextInputEditText> nameInputs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);
        dbHelper = DatabaseHelper.getInstance();

        editNumMeals = view.findViewById(R.id.edit_num_meals);
        containerMealNames = view.findViewById(R.id.container_meal_names);

        Button btnSetCount = view.findViewById(R.id.btn_update_meal_count);
        Button btnSave = view.findViewById(R.id.btn_save_config);

        List<String> currentConfig = dbHelper.getMealNamesConfig();
        editNumMeals.setText(String.valueOf(currentConfig.size()));
        buildNameInputs(currentConfig);

        btnSetCount.setOnClickListener(v -> {
            String val = editNumMeals.getText().toString().trim();
            if (!val.isEmpty()) {
                int count = Integer.parseInt(val);
                List<String> current = getCurrentInputNames();
                while (current.size() < count) current.add("Meal " + (current.size() + 1));
                while (current.size() > count) current.remove(current.size() - 1);
                buildNameInputs(current);
            }
        });

        btnSave.setOnClickListener(v -> {
            List<String> newNames = getCurrentInputNames();
            dbHelper.updateMealConfig(newNames);

            // Re-populate uncommitted meals based on updated config
            dbHelper.getUncommittedMeals().clear();
            for (String name : newNames) {
                dbHelper.getUncommittedMeals().add(new Meal(name));
            }

            Toast.makeText(getContext(), "Configuration Saved", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void buildNameInputs(List<String> names) {
        containerMealNames.removeAllViews();
        nameInputs.clear();

        for (int i = 0; i < names.size(); i++) {
            TextInputLayout layout = new TextInputLayout(requireContext(), null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
            layout.setHint("Meal " + (i + 1) + " Name");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 8);
            layout.setLayoutParams(params);

            TextInputEditText editText = new TextInputEditText(layout.getContext());
            editText.setText(names.get(i));
            layout.addView(editText);

            containerMealNames.addView(layout);
            nameInputs.add(editText);
        }
    }

    private List<String> getCurrentInputNames() {
        List<String> list = new ArrayList<>();
        for (TextInputEditText et : nameInputs) {
            list.add(et.getText().toString().trim());
        }
        return list;
    }
}

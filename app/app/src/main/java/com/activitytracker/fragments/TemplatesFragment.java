package com.activitytracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.adapters.FoodRowAdapter;
import com.activitytracker.adapters.TemplateAdapter;
import com.activitytracker.db.DatabaseHelper;
import com.activitytracker.models.FoodWeightPair;
import com.activitytracker.models.MealTemplate;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TemplatesFragment extends Fragment implements FoodRowAdapter.OnRowChangeListener {

    private DatabaseHelper dbHelper;
    private TextInputEditText editTemplateName, editTemplateInsulin;
    private TextView txtNutrients;
    private RecyclerView recyclerFoodRows, recyclerSavedTemplates;

    private List<FoodWeightPair> templateItems = new ArrayList<>();
    private FoodRowAdapter foodAdapter;
    private TemplateAdapter templateAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_templates, container, false);
        dbHelper = DatabaseHelper.getInstance();

        editTemplateName = view.findViewById(R.id.edit_template_name);
        editTemplateInsulin = view.findViewById(R.id.edit_template_insulin);
        txtNutrients = view.findViewById(R.id.txt_template_nutrients);

        recyclerFoodRows = view.findViewById(R.id.recycler_template_food_rows);
        recyclerFoodRows.setLayoutManager(new LinearLayoutManager(getContext()));
        foodAdapter = new FoodRowAdapter(getContext(), templateItems, this);
        recyclerFoodRows.setAdapter(foodAdapter);

        recyclerSavedTemplates = view.findViewById(R.id.recycler_saved_templates);
        recyclerSavedTemplates.setLayoutManager(new LinearLayoutManager(getContext()));
        setupSavedTemplatesAdapter();

        Button btnAddRow = view.findViewById(R.id.btn_template_add_row);
        Button btnSave = view.findViewById(R.id.btn_save_template);

        btnAddRow.setOnClickListener(v -> {
            templateItems.add(new FoodWeightPair(null, 0));
            foodAdapter.notifyItemInserted(templateItems.size() - 1);
            updateRollup();
        });

        btnSave.setOnClickListener(v -> saveTemplate());

        return view;
    }

    private void setupSavedTemplatesAdapter() {
        templateAdapter = new TemplateAdapter(getContext(), dbHelper.getTemplates(), template -> {
            dbHelper.deleteTemplate(template);
            templateAdapter.notifyDataSetChanged();
        });
        recyclerSavedTemplates.setAdapter(templateAdapter);
    }

    private void saveTemplate() {
        String name = editTemplateName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a template name", Toast.LENGTH_SHORT).show();
            return;
        }

        MealTemplate template = new MealTemplate(name);
        try {
            template.setInsulinDosage(Double.parseDouble(editTemplateInsulin.getText().toString()));
        } catch (NumberFormatException e) {
            template.setInsulinDosage(0.0);
        }

        template.setItems(new ArrayList<>(templateItems));
        dbHelper.saveTemplate(template);

        templateItems.clear();
        editTemplateName.setText("");
        editTemplateInsulin.setText("");
        foodAdapter.notifyDataSetChanged();
        templateAdapter.notifyDataSetChanged();
        updateRollup();

        Toast.makeText(getContext(), "Template Saved", Toast.LENGTH_SHORT).show();
    }

    private void updateRollup() {
        double cal = 0, digCarbs = 0;
        for (FoodWeightPair p : templateItems) {
            cal += p.getCalories();
            digCarbs += p.getDigestibleCarbs();
        }
        txtNutrients.setText(String.format(Locale.US, "Template Rollup -> Cal: %.0f | Dig.Carbs: %.1fg", cal, digCarbs));
    }

    @Override
    public void onRowChanged() {
        updateRollup();
    }

    @Override
    public void onRowRemoved(int position) {
        templateItems.remove(position);
        foodAdapter.notifyItemRemoved(position);
        foodAdapter.notifyItemRangeChanged(position, templateItems.size() - position);
        updateRollup();
    }
}

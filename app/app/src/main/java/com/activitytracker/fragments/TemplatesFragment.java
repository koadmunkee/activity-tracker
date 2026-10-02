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
    private TextView txtPrimaryNutrients, txtSecondaryNutrients;
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
        txtPrimaryNutrients = view.findViewById(R.id.txt_template_primary_nutrients);
        txtSecondaryNutrients = view.findViewById(R.id.txt_template_secondary_nutrients);

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
        templateAdapter = new TemplateAdapter(getContext(), dbHelper.getTemplates(), new TemplateAdapter.OnTemplateClickListener() {
            @Override
            public void onEditClick(MealTemplate template) {
                editTemplateName.setText(template.getTemplateName());
                editTemplateInsulin.setText(template.getInsulinDosage() > 0 ? String.valueOf(template.getInsulinDosage()) : "");

                templateItems.clear();
                for (FoodWeightPair p : template.getItems()) {
                    templateItems.add(new FoodWeightPair(p.getFoodItem(), p.getWeightGrams()));
                }

                foodAdapter.notifyDataSetChanged();
                updateRollup();
                Toast.makeText(getContext(), "Loaded " + template.getTemplateName() + " for editing", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(MealTemplate template) {
                dbHelper.deleteTemplate(template);
                templateAdapter.notifyDataSetChanged();
            }
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
        double cal = 0, digCarbs = 0, satFat = 0, fat = 0, protein = 0;
        double fiber = 0, chol = 0, totalCarbs = 0, omega3 = 0, omega6 = 0;

        for (FoodWeightPair p : templateItems) {
            cal += p.getCalories();
            digCarbs += p.getDigestibleCarbs();
            satFat += p.getSaturatedFat();
            fat += p.getTotalFat();
            protein += p.getProtein();

            fiber += p.getFiber();
            chol += p.getCholesterol();
            totalCarbs += p.getTotalCarbs();
            omega3 += p.getOmega3();
            omega6 += p.getOmega6();
        }

        txtPrimaryNutrients.setText(String.format(Locale.US,
                "Template Primary -> Cal: %.0f | Dig.Carbs: %.1fg | Sat Fat: %.1fg | Fat: %.1fg | Prot: %.1fg",
                cal, digCarbs, satFat, fat, protein));

        txtSecondaryNutrients.setText(String.format(Locale.US,
                "Template Secondary -> Fiber: %.1fg | Chol: %.0fmg | Total Carbs: %.1fg | Ω3: %.2fg | Ω6: %.2fg",
                fiber, chol, totalCarbs, omega3, omega6));
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

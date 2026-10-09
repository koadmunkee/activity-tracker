package com.activitytracker.ui.foods;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.activitytracker.R;
import com.activitytracker.db.FoodRepository;
import com.activitytracker.model.FoodItem;
import com.activitytracker.util.NutrientCalculator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

/**
 * Dialog for creating or editing a food with full nutrient specification (FR 5.4, 5.5, 5.6, 5.7).
 */
public class FoodEditDialog extends Dialog {

    public interface OnFoodSavedListener {
        void onFoodSaved(FoodItem food);
    }

    private final FoodItem food;
    private final boolean isEditMode;
    private final FoodRepository foodRepository;
    private final OnFoodSavedListener listener;

    private TextInputEditText etName;
    private TextInputEditText etRefWeight;
    private TextInputEditText etCalories;
    private TextInputEditText etDigCarbs;
    private TextInputEditText etSatFat;
    private TextInputEditText etFat;
    private TextInputEditText etProtein;
    private TextInputEditText etFiber;
    private TextInputEditText etCholesterol;
    private TextInputEditText etCarbohydrates;
    private TextInputEditText etOmega3;
    private TextInputEditText etOmega6;
    private TextView textError;

    private boolean isAutoCalculatingDigCarbs = false;

    public FoodEditDialog(@NonNull Context context, FoodItem foodToEdit, OnFoodSavedListener listener) {
        super(context);
        this.foodRepository = new FoodRepository(context);
        this.listener = listener;
        this.isEditMode = (foodToEdit != null);
        this.food = (foodToEdit != null) ? foodToEdit : new FoodItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_food);
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView textTitle = findViewById(R.id.text_dialog_food_title);
        textTitle.setText(isEditMode ? R.string.edit_food : R.string.add_food);

        etName = findViewById(R.id.et_edit_food_name);
        etRefWeight = findViewById(R.id.et_edit_ref_weight);
        etCalories = findViewById(R.id.et_edit_calories);
        etDigCarbs = findViewById(R.id.et_edit_digestable_carbs);
        etSatFat = findViewById(R.id.et_edit_sat_fat);
        etFat = findViewById(R.id.et_edit_fat);
        etProtein = findViewById(R.id.et_edit_protein);
        etFiber = findViewById(R.id.et_edit_fiber);
        etCholesterol = findViewById(R.id.et_edit_cholesterol);
        etCarbohydrates = findViewById(R.id.et_edit_carbohydrates);
        etOmega3 = findViewById(R.id.et_edit_omega3);
        etOmega6 = findViewById(R.id.et_edit_omega6);
        textError = findViewById(R.id.text_edit_food_error);

        MaterialButton btnCancel = findViewById(R.id.btn_edit_food_cancel);
        MaterialButton btnSave = findViewById(R.id.btn_edit_food_save);

        populateInitialFields();
        setupAutoDigCarbCalculation();

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> handleSave());
    }

    private void populateInitialFields() {
        if (isEditMode) {
            etName.setText(food.getName());
            etRefWeight.setText(formatNum(food.getReferenceWeight()));
            etCalories.setText(formatNum(food.getCalories()));
            etDigCarbs.setText(formatNum(food.getDigestableCarbs()));
            etSatFat.setText(formatNum(food.getSaturatedFat()));
            etFat.setText(formatNum(food.getFat()));
            etProtein.setText(formatNum(food.getProtein()));
            etFiber.setText(formatNum(food.getFiber()));
            etCholesterol.setText(formatNum(food.getCholesterol()));
            etCarbohydrates.setText(formatNum(food.getCarbohydrates()));
            etOmega3.setText(formatNum(food.getOmega3()));
            etOmega6.setText(formatNum(food.getOmega6()));
        } else {
            etRefWeight.setText("100");
        }
    }

    private void setupAutoDigCarbCalculation() {
        TextWatcher carbFiberWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isAutoCalculatingDigCarbs) return;
                try {
                    String carbStr = etCarbohydrates.getText() != null ? etCarbohydrates.getText().toString().trim() : "";
                    String fiberStr = etFiber.getText() != null ? etFiber.getText().toString().trim() : "";
                    if (!carbStr.isEmpty()) {
                        double carbs = Double.parseDouble(carbStr);
                        double fiber = fiberStr.isEmpty() ? 0.0 : Double.parseDouble(fiberStr);
                        double digCarbs = NutrientCalculator.calculateDigestableCarbs(carbs, fiber);
                        isAutoCalculatingDigCarbs = true;
                        etDigCarbs.setText(formatNum(digCarbs));
                        isAutoCalculatingDigCarbs = false;
                    }
                } catch (NumberFormatException ignored) {}
            }
        };

        etCarbohydrates.addTextChangedListener(carbFiberWatcher);
        etFiber.addTextChangedListener(carbFiberWatcher);
    }

    private void handleSave() {
        textError.setVisibility(View.GONE);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            showError("Food name cannot be empty.");
            return;
        }

        // FR 5.7 (first): Required to have unique name
        if (!foodRepository.isNameUnique(name, food.getId())) {
            showError(getContext().getString(R.string.error_unique_food_name));
            return;
        }

        // FR 5.7 (second): Not allowed to have undefined values for any of the food's nutrients
        Double refWeight = parseDoubleField(etRefWeight, "Reference Weight");
        Double calories = parseDoubleField(etCalories, "Calories");
        Double protein = parseDoubleField(etProtein, "Protein");
        Double fat = parseDoubleField(etFat, "Total Fat");
        Double satFat = parseDoubleField(etSatFat, "Saturated Fat");
        Double carbs = parseDoubleField(etCarbohydrates, "Total Carbohydrates");
        Double digCarbs = parseDoubleField(etDigCarbs, "Digestable Carbohydrates");
        Double fiber = parseDoubleField(etFiber, "Fiber");
        Double cholesterol = parseDoubleField(etCholesterol, "Cholesterol");
        Double omega3 = parseDoubleField(etOmega3, "Omega-3 Fats");
        Double omega6 = parseDoubleField(etOmega6, "Omega-6 Fats");

        if (refWeight == null || calories == null || protein == null || fat == null ||
                satFat == null || carbs == null || digCarbs == null || fiber == null ||
                cholesterol == null || omega3 == null || omega6 == null) {
            showError(getContext().getString(R.string.error_undefined_nutrients));
            return;
        }

        if (refWeight <= 0) {
            showError("Reference weight must be greater than zero.");
            return;
        }

        food.setName(name);
        food.setReferenceWeight(refWeight);
        food.setCalories(calories);
        food.setProtein(protein);
        food.setFat(fat);
        food.setSaturatedFat(satFat);
        food.setCarbohydrates(carbs);
        food.setDigestableCarbs(digCarbs);
        food.setFiber(fiber);
        food.setCholesterol(cholesterol);
        food.setOmega3(omega3);
        food.setOmega6(omega6);

        if (isEditMode) {
            foodRepository.updateFood(food);
        } else {
            long newId = foodRepository.insertFood(food);
            food.setId(newId);
        }

        if (listener != null) {
            listener.onFoodSaved(food);
        }
        dismiss();
    }

    private Double parseDoubleField(TextInputEditText et, String fieldName) {
        if (et == null || et.getText() == null) return null;
        String text = et.getText().toString().trim();
        if (text.isEmpty()) return null;
        try {
            double val = Double.parseDouble(text);
            return val >= 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showError(String msg) {
        textError.setVisibility(View.VISIBLE);
        textError.setText(msg);
    }

    private String formatNum(double num) {
        if (num == Math.round(num)) {
            return String.valueOf((long) num);
        }
        return String.format(Locale.US, "%.2f", num);
    }
}

package com.activitytracker.fragments;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
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
import com.activitytracker.db.DatabaseHelper;
import com.activitytracker.models.FoodWeightPair;
import com.activitytracker.models.Meal;
import com.activitytracker.models.MealTemplate;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlanningFragment extends Fragment implements FoodRowAdapter.OnRowChangeListener {

    private DatabaseHelper dbHelper;
    private List<Meal> uncommittedMeals;
    private int currentMealIndex = 0;

    private TextView txtDayTotals, txtMealTotals, txtSecondaryNutrients, txtCurrentMealTitle;
    private TextInputEditText editMealTime, editInsulinDosage, editInsulinTime;
    private RecyclerView recyclerFoodRows;
    private FoodRowAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planning, container, false);
        dbHelper = DatabaseHelper.getInstance();
        uncommittedMeals = dbHelper.getUncommittedMeals();

        txtDayTotals = view.findViewById(R.id.txt_day_totals);
        txtMealTotals = view.findViewById(R.id.txt_meal_totals);
        txtSecondaryNutrients = view.findViewById(R.id.txt_secondary_nutrients);
        txtCurrentMealTitle = view.findViewById(R.id.txt_current_meal_title);

        editMealTime = view.findViewById(R.id.edit_meal_time);
        editInsulinDosage = view.findViewById(R.id.edit_insulin_dosage);
        editInsulinTime = view.findViewById(R.id.edit_insulin_time);

        recyclerFoodRows = view.findViewById(R.id.recycler_food_rows);
        recyclerFoodRows.setLayoutManager(new LinearLayoutManager(getContext()));

        Button btnPrev = view.findViewById(R.id.btn_prev_meal);
        Button btnNext = view.findViewById(R.id.btn_next_meal);
        Button btnAddRow = view.findViewById(R.id.btn_add_food_row);
        Button btnCommit = view.findViewById(R.id.btn_commit_meal);
        Button btnPopulateRecent = view.findViewById(R.id.btn_populate_recent);
        Button btnPopulateTemplate = view.findViewById(R.id.btn_populate_template);

        btnPrev.setOnClickListener(v -> navigateMeal(-1));
        btnNext.setOnClickListener(v -> navigateMeal(1));

        btnAddRow.setOnClickListener(v -> {
            if (!uncommittedMeals.isEmpty()) {
                uncommittedMeals.get(currentMealIndex).getItems().add(new FoodWeightPair(null, 0));
                adapter.notifyItemInserted(uncommittedMeals.get(currentMealIndex).getItems().size() - 1);
                updateAggregations();
            }
        });

        btnPopulateRecent.setOnClickListener(v -> copyFromMostRecentMeal());
        btnPopulateTemplate.setOnClickListener(v -> copyFromTemplateDialog());

        btnCommit.setOnClickListener(v -> showCommitDatePicker());

        loadCurrentMeal();
        return view;
    }

    private void navigateMeal(int direction) {
        if (uncommittedMeals.isEmpty()) return;
        saveCurrentInputState();
        currentMealIndex += direction;
        if (currentMealIndex < 0) currentMealIndex = 0;
        if (currentMealIndex >= uncommittedMeals.size()) currentMealIndex = uncommittedMeals.size() - 1;
        loadCurrentMeal();
    }

    private void saveCurrentInputState() {
        if (uncommittedMeals.isEmpty()) return;
        Meal current = uncommittedMeals.get(currentMealIndex);
        current.setMealTime(editMealTime.getText().toString());
        current.setInsulinTime(editInsulinTime.getText().toString());
        try {
            current.setInsulinDosage(Double.parseDouble(editInsulinDosage.getText().toString()));
        } catch (NumberFormatException e) {
            current.setInsulinDosage(0.0);
        }
    }

    private void loadCurrentMeal() {
        if (uncommittedMeals.isEmpty()) {
            txtCurrentMealTitle.setText("No Uncommitted Meals");
            recyclerFoodRows.setAdapter(null);
            return;
        }

        Meal meal = uncommittedMeals.get(currentMealIndex);
        txtCurrentMealTitle.setText(String.format(Locale.US, "Meal %d of %d: %s",
                currentMealIndex + 1, uncommittedMeals.size(), meal.getName()));

        editMealTime.setText(meal.getMealTime());
        editInsulinTime.setText(meal.getInsulinTime());
        editInsulinDosage.setText(meal.getInsulinDosage() > 0 ? String.valueOf(meal.getInsulinDosage()) : "");

        adapter = new FoodRowAdapter(getContext(), meal.getItems(), this);
        recyclerFoodRows.setAdapter(adapter);

        updateAggregations();
    }

    private void updateAggregations() {
        if (uncommittedMeals.isEmpty()) return;
        Meal current = uncommittedMeals.get(currentMealIndex);

        // Meal Level Aggregation
        txtMealTotals.setText(String.format(Locale.US,
                "MEAL TOTALS -> Cal: %.0f | Dig.Carbs: %.1fg | Sat.Fat: %.1fg | Fat: %.1fg | Prot: %.1fg",
                current.getTotalCalories(), current.getTotalDigestibleCarbs(),
                current.getTotalSaturatedFat(), current.getTotalFat(), current.getTotalProtein()));

        // Secondary Macronutrients
        txtSecondaryNutrients.setText(String.format(Locale.US,
                "Secondary: Fiber: %.1fg | Chol: %.0fmg | Total Carbs: %.1fg | Ω3: %.2fg | Ω6: %.2fg",
                current.getTotalFiber(), current.getTotalCholesterol(), current.getTotalCarbs(),
                current.getTotalOmega3(), current.getTotalOmega6()));

        // Day Level Rollup across all uncommitted meals
        double dayCal = 0, dayDigCarbs = 0, daySatFat = 0, dayFat = 0, dayProt = 0;
        for (Meal m : uncommittedMeals) {
            dayCal += m.getTotalCalories();
            dayDigCarbs += m.getTotalDigestibleCarbs();
            daySatFat += m.getTotalSaturatedFat();
            dayFat += m.getTotalFat();
            dayProt += m.getTotalProtein();
        }

        txtDayTotals.setText(String.format(Locale.US,
                "DAY TOTALS -> Cal: %.0f | Dig.Carbs: %.1fg | Sat.Fat: %.1fg | Fat: %.1fg | Prot: %.1fg",
                dayCal, dayDigCarbs, daySatFat, dayFat, dayProt));
    }

    private void copyFromMostRecentMeal() {
        if (uncommittedMeals.isEmpty()) return;
        Meal current = uncommittedMeals.get(currentMealIndex);
        Meal recent = dbHelper.getMostRecentCommittedMeal(current.getName());

        if (recent == null) {
            Toast.makeText(getContext(), "No committed meal found with name: " + current.getName(), Toast.LENGTH_SHORT).show();
            return;
        }

        current.getItems().clear();
        for (FoodWeightPair p : recent.getItems()) {
            current.getItems().add(new FoodWeightPair(p.getFoodItem(), p.getWeightGrams()));
        }
        current.setInsulinDosage(recent.getInsulinDosage());

        loadCurrentMeal();
        Toast.makeText(getContext(), "Populated from recent " + current.getName(), Toast.LENGTH_SHORT).show();
    }

    private void copyFromTemplateDialog() {
        List<MealTemplate> templates = dbHelper.getTemplates();
        if (templates.isEmpty()) {
            Toast.makeText(getContext(), "No templates available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[templates.size()];
        for (int i = 0; i < templates.size(); i++) names[i] = templates.get(i).getTemplateName();

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Template")
                .setItems(names, (dialog, which) -> {
                    MealTemplate t = templates.get(which);
                    Meal current = uncommittedMeals.get(currentMealIndex);
                    current.getItems().clear();
                    for (FoodWeightPair pair : t.getItems()) {
                        current.getItems().add(new FoodWeightPair(pair.getFoodItem(), pair.getWeightGrams()));
                    }
                    current.setInsulinDosage(t.getInsulinDosage());
                    loadCurrentMeal();
                })
                .show();
    }

    private void showCommitDatePicker() {
        if (uncommittedMeals.isEmpty()) return;
        saveCurrentInputState();

        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            Meal committedMeal = uncommittedMeals.get(currentMealIndex);
            dbHelper.commitMeal(committedMeal, dateStr);

            Toast.makeText(getContext(), "Committed " + committedMeal.getName() + " for " + dateStr, Toast.LENGTH_SHORT).show();

            if (currentMealIndex >= uncommittedMeals.size()) {
                currentMealIndex = Math.max(0, uncommittedMeals.size() - 1);
            }
            loadCurrentMeal();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dialog.setTitle("Select Date to Commit Meal");
        dialog.show();
    }

    @Override
    public void onRowChanged() {
        updateAggregations();
    }

    @Override
    public void onRowRemoved(int position) {
        if (uncommittedMeals.isEmpty()) return;
        uncommittedMeals.get(currentMealIndex).getItems().remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, uncommittedMeals.get(currentMealIndex).getItems().size() - position);
        updateAggregations();
    }
}

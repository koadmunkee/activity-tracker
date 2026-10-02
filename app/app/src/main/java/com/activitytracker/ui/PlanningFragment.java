package com.activitytracker.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.adapter.FoodPairAdapter;
import com.activitytracker.database.MockDatabase;
import com.activitytracker.model.FoodWeightPair;
import com.activitytracker.model.Meal;
import com.activitytracker.model.MealTemplate;
import com.activitytracker.model.NutrientAggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlanningFragment extends Fragment implements FoodPairAdapter.OnDataChangeListener {

    private static final String ARG_MEAL_INDEX = "meal_index";
    private int mealIndex;

    private Meal meal;
    private FoodPairAdapter adapter;
    private TextView textPrimaryNutrients, textSecondaryNutrients;
    private EditText editMealTime, editInsulinDosage, editInsulinTime;

    public static PlanningFragment newInstance(int mealIndex) {
        PlanningFragment fragment = new PlanningFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEAL_INDEX, mealIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealIndex = getArguments().getInt(ARG_MEAL_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planning, container, false);

        MockDatabase db = MockDatabase.getInstance();
        if (mealIndex < db.getActiveUncommittedMeals().size()) {
            meal = db.getActiveUncommittedMeals().get(mealIndex);
        }

        textPrimaryNutrients = view.findViewById(R.id.textMealPrimaryNutrients);
        textSecondaryNutrients = view.findViewById(R.id.textMealSecondaryNutrients);
        editMealTime = view.findViewById(R.id.editMealTime);
        editInsulinDosage = view.findViewById(R.id.editInsulinDosage);
        editInsulinTime = view.findViewById(R.id.editInsulinTime);

        if (meal != null) {
            editMealTime.setText(meal.getMealTime());
            editInsulinDosage.setText(meal.getInsulinDosage() > 0 ? String.valueOf(meal.getInsulinDosage()) : "");
            editInsulinTime.setText(meal.getInsulinTime());
        }

        setupTextChangeListeners();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerFoodPairs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        if (meal != null) {
            adapter = new FoodPairAdapter(meal.getItems(), db.getFoodList(), this);
            recyclerView.setAdapter(adapter);
        }

        Button btnAddRow = view.findViewById(R.id.btnAddFoodRow);
        btnAddRow.setOnClickListener(v -> {
            if (meal != null) {
                meal.getItems().add(new FoodWeightPair(null, 0));
                adapter.notifyItemInserted(meal.getItems().size() - 1);
                onDataChanged();
            }
        });

        Button btnPopulateLast = view.findViewById(R.id.btnPopulateLast);
        btnPopulateLast.setOnClickListener(v -> populateFromLastMeal());

        Button btnPopulateTemplate = view.findViewById(R.id.btnPopulateTemplate);
        btnPopulateTemplate.setOnClickListener(v -> showTemplateSelectionDialog());

        updateNutrientUI();
        return view;
    }

    private void setupTextChangeListeners() {
        editMealTime.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                if (meal != null) meal.setMealTime(s.toString());
            }
        });

        editInsulinDosage.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                if (meal != null) {
                    try {
                        meal.setInsulinDosage(Double.parseDouble(s.toString()));
                    } catch (NumberFormatException e) {
                        meal.setInsulinDosage(0.0);
                    }
                }
            }
        });

        editInsulinTime.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                if (meal != null) meal.setInsulinTime(s.toString());
            }
        });
    }

    private void populateFromLastMeal() {
        if (meal == null) return;
        Meal last = MockDatabase.getInstance().getLastCommittedMealByName(meal.getMealName());
        if (last == null) {
            Toast.makeText(getContext(), "No committed meal found for: " + meal.getMealName(), Toast.LENGTH_SHORT).show();
            return;
        }
        meal.getItems().clear();
        for (FoodWeightPair p : last.getItems()) {
            meal.getItems().add(new FoodWeightPair(p.getFoodItem(), p.getWeightGrams()));
        }
        adapter.notifyDataSetChanged();
        onDataChanged();
        Toast.makeText(getContext(), "Populated from last " + meal.getMealName(), Toast.LENGTH_SHORT).show();
    }

    private void showTemplateSelectionDialog() {
        List<MealTemplate> templates = MockDatabase.getInstance().getTemplates();
        if (templates.isEmpty()) {
            Toast.makeText(getContext(), "No templates created yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[templates.size()];
        for (int i = 0; i < templates.size(); i++) {
            names[i] = templates.get(i).getTemplateName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Template")
                .setItems(names, (dialog, which) -> {
                    MealTemplate selected = templates.get(which);
                    meal.getItems().clear();
                    for (FoodWeightPair p : selected.getItems()) {
                        meal.getItems().add(new FoodWeightPair(p.getFoodItem(), p.getWeightGrams()));
                    }
                    adapter.notifyDataSetChanged();
                    onDataChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDataChanged() {
        updateNutrientUI();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateDayAggregationUI();
        }
    }

    private void updateNutrientUI() {
        if (meal == null) return;
        NutrientAggregation agg = meal.getAggregatedNutrients();
        textPrimaryNutrients.setText(String.format(Locale.US,
                "Cal: %.0f kcal | Dig.Carbs: %.1fg | Sat.Fat: %.1fg | Fat: %.1fg | Pro: %.1fg",
                agg.calories, agg.digestibleCarbs, agg.saturatedFat, agg.totalFat, agg.protein));

        textSecondaryNutrients.setText(String.format(Locale.US,
                "Fiber: %.1fg | Chol: %.0fmg | Total Carbs: %.1fg | Omega-3: %.2fg | Omega-6: %.2fg",
                agg.fiber, agg.cholesterolMg, agg.totalCarbs, agg.omega3g, agg.omega6g));
    }
}

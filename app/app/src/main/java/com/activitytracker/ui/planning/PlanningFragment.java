package com.activitytracker.ui.planning;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.db.ConfigRepository;
import com.activitytracker.db.FoodRepository;
import com.activitytracker.db.MealRepository;
import com.activitytracker.db.TemplateRepository;
import com.activitytracker.model.FoodItem;
import com.activitytracker.model.MealItem;
import com.activitytracker.model.MealTemplate;
import com.activitytracker.model.NutrientSummary;
import com.activitytracker.model.PlannedMeal;
import com.activitytracker.model.TemplateItem;
import com.activitytracker.util.DateTimeUtil;
import com.activitytracker.util.NutrientCalculator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Meal Planning View - default screen of Activity Tracker.
 * Manages daily meal planning, food/weight items, insulin & timings,
 * live aggregations, template/history population, and committing.
 */
public class PlanningFragment extends Fragment implements FoodRowAdapter.OnMealItemChangeListener {

    private ChipGroup chipGroupMeals;
    private TextView textDayPrimary;
    private TextView textDaySecondary;
    private TextView textMealAggregationLabel;
    private TextView textMealPrimary;
    private TextView textMealSecondary;

    private TextInputEditText etMealStartTime;
    private TextInputEditText etInsulinDose;
    private TextInputEditText etInsulinTime;

    private MaterialButton btnPopulateLastMeal;
    private MaterialButton btnPopulateTemplate;
    private MaterialButton btnAddFoodRow;
    private MaterialButton btnCommitDay;

    private RecyclerView recyclerFoodRows;
    private TextView textEmptyFoodRows;

    private FoodRepository foodRepository;
    private MealRepository mealRepository;
    private TemplateRepository templateRepository;
    private ConfigRepository configRepository;

    private List<PlannedMeal> uncommittedMeals = new ArrayList<>();
    private PlannedMeal activeMeal;
    private List<FoodItem> availableFoods = new ArrayList<>();
    private FoodRowAdapter foodRowAdapter;

    private boolean isUpdatingUi = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();
        foodRepository = new FoodRepository(context);
        mealRepository = new MealRepository(context);
        templateRepository = new TemplateRepository(context);
        configRepository = new ConfigRepository(context);

        initViews(view);
        setupListeners();
        loadPlanningData();
    }

    private void initViews(View root) {
        chipGroupMeals = root.findViewById(R.id.chip_group_meals);
        textDayPrimary = root.findViewById(R.id.text_day_primary_summary);
        textDaySecondary = root.findViewById(R.id.text_day_secondary_summary);
        textMealAggregationLabel = root.findViewById(R.id.text_meal_aggregation_label);
        textMealPrimary = root.findViewById(R.id.text_meal_primary_summary);
        textMealSecondary = root.findViewById(R.id.text_meal_secondary_summary);

        etMealStartTime = root.findViewById(R.id.et_meal_start_time);
        etInsulinDose = root.findViewById(R.id.et_insulin_dose);
        etInsulinTime = root.findViewById(R.id.et_insulin_time);

        btnPopulateLastMeal = root.findViewById(R.id.btn_populate_last_meal);
        btnPopulateTemplate = root.findViewById(R.id.btn_populate_template);
        btnAddFoodRow = root.findViewById(R.id.btn_add_food_row);
        btnCommitDay = root.findViewById(R.id.btn_commit_day);

        recyclerFoodRows = root.findViewById(R.id.recycler_food_rows);
        textEmptyFoodRows = root.findViewById(R.id.text_empty_food_rows);

        recyclerFoodRows.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupListeners() {
        // Meal Start Time picker
        etMealStartTime.setOnClickListener(v -> showTimePicker((hour, minute) -> {
            String timeStr = DateTimeUtil.formatTime(hour, minute);
            etMealStartTime.setText(timeStr);
            if (activeMeal != null) {
                activeMeal.setStartTime(timeStr);
                mealRepository.saveOrUpdateMeal(activeMeal);
            }
        }));

        // Insulin Injection Time picker
        etInsulinTime.setOnClickListener(v -> showTimePicker((hour, minute) -> {
            String timeStr = DateTimeUtil.formatTime(hour, minute);
            etInsulinTime.setText(timeStr);
            if (activeMeal != null) {
                activeMeal.setInsulinTime(timeStr);
                mealRepository.saveOrUpdateMeal(activeMeal);
            }
        }));

        // Insulin dose input
        etInsulinDose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingUi || activeMeal == null) return;
                String text = s != null ? s.toString().trim() : "";
                double dose = 0.0;
                if (!text.isEmpty()) {
                    try {
                        dose = Double.parseDouble(text);
                    } catch (NumberFormatException ignored) {}
                }
                activeMeal.setInsulinDose(dose);
                mealRepository.saveOrUpdateMeal(activeMeal);
            }
        });

        // Add food row
        btnAddFoodRow.setOnClickListener(v -> {
            if (activeMeal != null) {
                MealItem newItem = new MealItem();
                newItem.setMealId(activeMeal.getId());
                activeMeal.addItem(newItem);
                if (foodRowAdapter != null) {
                    foodRowAdapter.notifyItemInserted(activeMeal.getItems().size() - 1);
                }
                updateEmptyState();
                updateAggregationCards();
                mealRepository.saveOrUpdateMeal(activeMeal);
            }
        });

        // Populate from last committed meal with same name (FR 2.6)
        btnPopulateLastMeal.setOnClickListener(v -> populateFromMostRecentCommittedMeal());

        // Populate from template (FR 2.7)
        btnPopulateTemplate.setOnClickListener(v -> showTemplateSelectionDialog());

        // Commit Day's Meals (FR 2.3 & FR 2.8)
        btnCommitDay.setOnClickListener(v -> handleCommitDaysMeals());
    }

    public void loadPlanningData() {
        List<String> configuredMealNames = configRepository.getConfiguredMealNames();
        availableFoods = foodRepository.getUnarchivedFoods();
        uncommittedMeals = mealRepository.getUncommittedMeals(configuredMealNames);

        setupMealSelectorTabs();

        if (!uncommittedMeals.isEmpty()) {
            selectMeal(uncommittedMeals.get(0));
        } else {
            activeMeal = null;
            updateEmptyState();
            updateAggregationCards();
        }
    }

    private void setupMealSelectorTabs() {
        chipGroupMeals.removeAllViews();
        for (int i = 0; i < uncommittedMeals.size(); i++) {
            PlannedMeal meal = uncommittedMeals.get(i);
            Chip chip = new Chip(requireContext());
            chip.setText(meal.getMealName());
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            final PlannedMeal targetMeal = meal;
            chip.setOnClickListener(v -> selectMeal(targetMeal));
            chipGroupMeals.addView(chip);

            if (i == 0) {
                chip.setChecked(true);
            }
        }
    }

    private void selectMeal(PlannedMeal meal) {
        this.activeMeal = meal;
        isUpdatingUi = true;

        btnPopulateLastMeal.setText(String.format(Locale.US, "Copy Last %s", meal.getMealName()));
        textMealAggregationLabel.setText(String.format(Locale.US, "MEAL TOTAL (%s):", meal.getMealName()));

        etMealStartTime.setText(meal.getStartTime());
        etInsulinTime.setText(meal.getInsulinTime());
        if (meal.getInsulinDose() > 0) {
            etInsulinDose.setText(String.format(Locale.US, "%.1f", meal.getInsulinDose()));
        } else {
            etInsulinDose.setText("");
        }

        foodRowAdapter = new FoodRowAdapter(requireContext(), meal.getItems(), availableFoods, foodRepository, this);
        recyclerFoodRows.setAdapter(foodRowAdapter);

        updateEmptyState();
        updateAggregationCards();
        isUpdatingUi = false;
    }

    private void updateEmptyState() {
        if (activeMeal == null || activeMeal.getItems().isEmpty()) {
            textEmptyFoodRows.setVisibility(View.VISIBLE);
            recyclerFoodRows.setVisibility(View.GONE);
        } else {
            textEmptyFoodRows.setVisibility(View.GONE);
            recyclerFoodRows.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMealItemChanged() {
        if (activeMeal != null) {
            updateAggregationCards();
            mealRepository.saveOrUpdateMeal(activeMeal);
        }
    }

    @Override
    public void onMealItemDeleted(int position) {
        if (activeMeal != null && position >= 0 && position < activeMeal.getItems().size()) {
            activeMeal.removeItem(position);
            if (foodRowAdapter != null) {
                foodRowAdapter.notifyItemRemoved(position);
            }
            updateEmptyState();
            updateAggregationCards();
            mealRepository.saveOrUpdateMeal(activeMeal);
        }
    }

    /**
     * Updates both day level and meal level aggregations (FR 2.4).
     * Pinned at the top to be always visible.
     */
    private void updateAggregationCards() {
        // Day level aggregation
        NutrientSummary daySummary = NutrientCalculator.aggregateDayNutrients(uncommittedMeals);
        textDayPrimary.setText(String.format(Locale.US,
                "%.0f kcal | C: %.1fg | P: %.1fg | F: %.1fg (Sat: %.1fg)",
                daySummary.getCalories(),
                daySummary.getDigestableCarbs(),
                daySummary.getProtein(),
                daySummary.getFat(),
                daySummary.getSaturatedFat()));

        textDaySecondary.setText(String.format(Locale.US,
                "Day Sec: Fib %s • Chol %s • Carbs %s • Ω3 %s • Ω6 %s",
                daySummary.getFormattedFiber(),
                daySummary.getFormattedCholesterol(),
                daySummary.getFormattedCarbohydrates(),
                daySummary.getFormattedOmega3(),
                daySummary.getFormattedOmega6()));

        // Meal level aggregation
        if (activeMeal != null) {
            NutrientSummary mealSummary = activeMeal.getNutrientSummary();
            textMealPrimary.setText(String.format(Locale.US,
                    "%.0f kcal | C: %.1fg | P: %.1fg | F: %.1fg (Sat: %.1fg)",
                    mealSummary.getCalories(),
                    mealSummary.getDigestableCarbs(),
                    mealSummary.getProtein(),
                    mealSummary.getFat(),
                    mealSummary.getSaturatedFat()));

            textMealSecondary.setText(String.format(Locale.US,
                    "Meal Sec: Fib %s • Chol %s • Carbs %s • Ω3 %s • Ω6 %s",
                    mealSummary.getFormattedFiber(),
                    mealSummary.getFormattedCholesterol(),
                    mealSummary.getFormattedCarbohydrates(),
                    mealSummary.getFormattedOmega3(),
                    mealSummary.getFormattedOmega6()));
        } else {
            textMealPrimary.setText("0 kcal | C: 0g | P: 0g | F: 0g (Sat: 0g)");
            textMealSecondary.setText("Meal Sec: Fib 0g • Chol 0mg • Carbs 0g • Ω3 0g • Ω6 0g");
        }
    }

    /**
     * FR 2.6: Populates food & weight pairs and insulin dosage amount from
     * the most recently committed meal with the same name.
     */
    private void populateFromMostRecentCommittedMeal() {
        if (activeMeal == null) return;
        PlannedMeal lastCommitted = mealRepository.getMostRecentCommittedMealByName(activeMeal.getMealName());
        if (lastCommitted == null) {
            Snackbar.make(requireView(),
                    String.format(getString(R.string.no_recent_meal_found), activeMeal.getMealName()),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        activeMeal.getItems().clear();
        for (MealItem sourceItem : lastCommitted.getItems()) {
            MealItem copyItem = new MealItem();
            copyItem.setMealId(activeMeal.getId());
            copyItem.setFoodId(sourceItem.getFoodId());
            copyItem.setFoodName(sourceItem.getFoodName());
            copyItem.setWeight(sourceItem.getWeight());
            copyItem.setFoodItem(sourceItem.getFoodItem());
            activeMeal.addItem(copyItem);
        }
        activeMeal.setInsulinDose(lastCommitted.getInsulinDose());

        mealRepository.saveOrUpdateMeal(activeMeal);
        selectMeal(activeMeal);

        Snackbar.make(requireView(),
                String.format("Populated %s from committed meal on %s", activeMeal.getMealName(), lastCommitted.getDate()),
                Snackbar.LENGTH_SHORT).show();
    }

    /**
     * FR 2.7: Populates food & weight pairs and insulin dosage amount from a template.
     */
    private void showTemplateSelectionDialog() {
        if (activeMeal == null) return;
        List<MealTemplate> templates = templateRepository.getAllTemplates();
        if (templates.isEmpty()) {
            Toast.makeText(requireContext(), "No templates available. Create one in the Templates tab.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] templateNames = new String[templates.size()];
        for (int i = 0; i < templates.size(); i++) {
            templateNames[i] = templates.get(i).getTemplateName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_template_title)
                .setItems(templateNames, (dialog, which) -> {
                    MealTemplate selected = templates.get(which);
                    applyTemplateToActiveMeal(selected);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    public void applyTemplateToActiveMeal(MealTemplate template) {
        if (activeMeal == null || template == null) return;
        activeMeal.getItems().clear();
        for (TemplateItem tItem : template.getItems()) {
            MealItem mItem = new MealItem();
            mItem.setMealId(activeMeal.getId());
            mItem.setFoodId(tItem.getFoodId());
            mItem.setFoodName(tItem.getFoodName());
            mItem.setWeight(tItem.getWeight());
            mItem.setFoodItem(tItem.getFoodItem());
            activeMeal.addItem(mItem);
        }
        activeMeal.setInsulinDose(template.getInsulinDose());

        mealRepository.saveOrUpdateMeal(activeMeal);
        selectMeal(activeMeal);

        Snackbar.make(requireView(), "Loaded template: " + template.getTemplateName(), Snackbar.LENGTH_SHORT).show();
    }

    /**
     * FR 2.3 & FR 2.8: Committing the day's meals.
     * Validates that all food rows have recognized food and defined weight.
     * Prompts for date associated with meals (defaulted to today).
     */
    private void handleCommitDaysMeals() {
        // Validation check (FR 2.8)
        StringBuilder errorMsg = new StringBuilder();
        for (PlannedMeal meal : uncommittedMeals) {
            for (int i = 0; i < meal.getItems().size(); i++) {
                MealItem item = meal.getItems().get(i);
                if (item.getFoodItem() == null || item.getFoodItem().getId() <= 0) {
                    errorMsg.append("• ").append(meal.getMealName())
                            .append(" row ").append(i + 1)
                            .append(" has unrecognized food (\"")
                            .append(item.getFoodName())
                            .append("\").\n");
                }
                if (item.getWeight() <= 0) {
                    errorMsg.append("• ").append(meal.getMealName())
                            .append(" row ").append(i + 1)
                            .append(" has undefined or zero weight.\n");
                }
            }
        }

        if (errorMsg.length() > 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cannot Commit Meals")
                    .setMessage(getString(R.string.error_commit_validation, "\n\n" + errorMsg.toString()))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        // Prompt for date (FR 2.3: defaulted to today's date)
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = DateTimeUtil.formatDate(year, month, dayOfMonth);
                    commitMealsForDate(selectedDate);
                },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setTitle(R.string.prompt_commit_date_title);
        datePickerDialog.show();
    }

    private void commitMealsForDate(String date) {
        mealRepository.commitDayMeals(date, uncommittedMeals);

        // On successful commit, the meals disappear from the meal planning view (FR 2.3).
        loadPlanningData();

        Snackbar.make(requireView(),
                String.format(getString(R.string.commit_success), DateTimeUtil.formatDisplayDate(date)),
                Snackbar.LENGTH_LONG).show();
    }

    private interface OnTimeSelectedListener {
        void onTimeSelected(int hourOfDay, int minute);
    }

    private void showTimePicker(OnTimeSelectedListener listener) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> listener.onTimeSelected(hourOfDay, minute),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlanningData();
    }
}

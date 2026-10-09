package com.activitytracker.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.model.CommittedDay;
import com.activitytracker.model.MealItem;
import com.activitytracker.model.NutrientSummary;
import com.activitytracker.model.PlannedMeal;
import com.activitytracker.util.DateTimeUtil;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for historical committed days in History View (FR 4.1).
 */
public class HistoryDayAdapter extends RecyclerView.Adapter<HistoryDayAdapter.DayViewHolder> {

    public interface OnAdjustDayClickListener {
        void onAdjustDay(String date);
    }

    private final Context context;
    private final List<CommittedDay> days;
    private final OnAdjustDayClickListener adjustListener;
    private final LayoutInflater inflater;

    public HistoryDayAdapter(Context context, List<CommittedDay> days, OnAdjustDayClickListener adjustListener) {
        this.context = context;
        this.days = days;
        this.adjustListener = adjustListener;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_history_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days != null ? days.size() : 0;
    }

    class DayViewHolder extends RecyclerView.ViewHolder {

        private final TextView textDate;
        private final TextView textDayPrimary;
        private final TextView textDaySecondary;
        private final TextView textInsulinTotal;
        private final MaterialButton btnAdjustDay;
        private final LinearLayout layoutMealsContainer;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_history_date);
            textDayPrimary = itemView.findViewById(R.id.text_history_day_primary);
            textDaySecondary = itemView.findViewById(R.id.text_history_day_secondary);
            textInsulinTotal = itemView.findViewById(R.id.text_history_insulin_total);
            btnAdjustDay = itemView.findViewById(R.id.btn_adjust_day);
            layoutMealsContainer = itemView.findViewById(R.id.layout_history_meals_container);
        }

        public void bind(CommittedDay day) {
            textDate.setText(DateTimeUtil.formatDisplayDate(day.getDate()));

            NutrientSummary daySummary = day.getDayNutrientSummary();
            textDayPrimary.setText(String.format(Locale.US,
                    "Calories: %.0f kcal | Dig.Carb: %.1fg | Protein: %.1fg | Total Fat: %.1fg (Sat: %.1fg)",
                    daySummary.getCalories(),
                    daySummary.getDigestableCarbs(),
                    daySummary.getProtein(),
                    daySummary.getFat(),
                    daySummary.getSaturatedFat()));

            textDaySecondary.setText(String.format(Locale.US,
                    "Fiber: %s • Chol: %s • Carbs: %s • Ω3: %s • Ω6: %s",
                    daySummary.getFormattedFiber(),
                    daySummary.getFormattedCholesterol(),
                    daySummary.getFormattedCarbohydrates(),
                    daySummary.getFormattedOmega3(),
                    daySummary.getFormattedOmega6()));

            textInsulinTotal.setText(String.format(Locale.US, "Total Insulin: %.1f units across %d meal(s)",
                    day.getTotalInsulinDose(), day.getMeals().size()));

            btnAdjustDay.setOnClickListener(v -> {
                if (adjustListener != null) {
                    adjustListener.onAdjustDay(day.getDate());
                }
            });

            // Populate meals inside day
            layoutMealsContainer.removeAllViews();
            for (PlannedMeal meal : day.getMeals()) {
                View mealView = inflater.inflate(R.layout.item_history_meal, layoutMealsContainer, false);

                TextView textMealName = mealView.findViewById(R.id.text_hist_meal_name);
                TextView textMealTime = mealView.findViewById(R.id.text_hist_meal_time);
                TextView textMealInsulin = mealView.findViewById(R.id.text_hist_meal_insulin);
                TextView textMealPrimary = mealView.findViewById(R.id.text_hist_meal_primary);
                TextView textMealSecondary = mealView.findViewById(R.id.text_hist_meal_secondary);
                TextView textMealItems = mealView.findViewById(R.id.text_hist_meal_items);

                String displayName = meal.getMealName();
                if (meal.isAdHoc()) {
                    displayName += " (" + context.getString(R.string.ad_hoc_badge) + ")";
                }
                textMealName.setText(displayName);
                textMealTime.setText(meal.getStartTime().isEmpty() ? "Time not set" : meal.getStartTime());

                String insulinDesc = String.format(Locale.US, "Insulin: %.1f units", meal.getInsulinDose());
                if (!meal.getInsulinTime().isEmpty()) {
                    insulinDesc += " at " + meal.getInsulinTime();
                }
                textMealInsulin.setText(insulinDesc);

                NutrientSummary mealSummary = meal.getNutrientSummary();
                textMealPrimary.setText(String.format(Locale.US,
                        "%.0f kcal | C: %.1fg | P: %.1fg | F: %.1fg (Sat: %.1fg)",
                        mealSummary.getCalories(),
                        mealSummary.getDigestableCarbs(),
                        mealSummary.getProtein(),
                        mealSummary.getFat(),
                        mealSummary.getSaturatedFat()));

                textMealSecondary.setText(String.format(Locale.US,
                        "Fiber: %s • Chol: %s • Carbs: %s • Ω3: %s • Ω6: %s",
                        mealSummary.getFormattedFiber(),
                        mealSummary.getFormattedCholesterol(),
                        mealSummary.getFormattedCarbohydrates(),
                        mealSummary.getFormattedOmega3(),
                        mealSummary.getFormattedOmega6()));

                StringBuilder itemsBuilder = new StringBuilder();
                for (int i = 0; i < meal.getItems().size(); i++) {
                    MealItem item = meal.getItems().get(i);
                    if (i > 0) itemsBuilder.append("\n");
                    itemsBuilder.append("• ").append(item.getFoodName()).append(" (")
                            .append(String.format(Locale.US, "%.0fg", item.getWeight())).append(")");
                }
                textMealItems.setText(itemsBuilder.length() > 0 ? itemsBuilder.toString() : "No food items recorded.");

                layoutMealsContainer.addView(mealView);
            }
        }
    }
}

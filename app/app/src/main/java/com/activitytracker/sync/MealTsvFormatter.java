package com.activitytracker.sync;

import com.activitytracker.model.MealItem;
import com.activitytracker.model.NutrientSummary;
import com.activitytracker.model.PlannedMeal;

import java.util.List;
import java.util.Locale;

/**
 * Formats committed meals into TSV format for Google Cloud Storage and BigQuery reporting (FR 1.3).
 *
 * 14 Columns in exact order:
 * 1. local date
 * 2. local time
 * 3. calorie
 * 4. carbohydrate
 * 5. saturated fat
 * 6. fat
 * 7. protein
 * 8. fiber
 * 9. cholesterol
 * 10. digestable carbohydrate
 * 11. description (formatted as weight followed by a single space followed by food name, separated by semi-colon and ending with a semi-colon)
 * 12. insulin units
 * 13. insulin time
 * 14. timezone
 */
public class MealTsvFormatter {

    public static final String[] COLUMNS = new String[] {
            "local date",
            "local time",
            "calorie",
            "carbohydrate",
            "saturated fat",
            "fat",
            "protein",
            "fiber",
            "cholesterol",
            "digestable carbohydrate",
            "description",
            "insulin units",
            "insulin time",
            "timezone"
    };

    public static final String HEADER = String.join("\t", COLUMNS);

    /**
     * Determines whether a meal has any food/weight pairs (FR 1.3).
     * Meals that don't have any food/weight pairs should not be written to the TSV file.
     */
    public static boolean hasFoodWeightPairs(PlannedMeal meal) {
        return meal != null && meal.hasFoodWeightPairs();
    }

    public static String formatMealsToTsv(List<PlannedMeal> meals) {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER).append("\n");
        if (meals != null) {
            for (PlannedMeal meal : meals) {
                if (hasFoodWeightPairs(meal)) {
                    sb.append(formatMealRow(meal)).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public static String formatMealRow(PlannedMeal meal) {
        if (meal == null) return "";
        NutrientSummary summary = meal.getNutrientSummary();

        String localDate = meal.getDate() != null ? meal.getDate().trim() : "";
        String localTime = meal.getStartTime() != null ? meal.getStartTime().trim() : "";
        String calorie = formatNumber(summary.getCalories());
        String carb = formatNumber(summary.getCarbohydrates());
        String satFat = formatNumber(summary.getSaturatedFat());
        String fat = formatNumber(summary.getFat());
        String protein = formatNumber(summary.getProtein());
        String fiber = formatNumber(summary.getFiber());
        String cholesterol = formatNumber(summary.getCholesterol());
        String digestableCarb = formatNumber(summary.getDigestableCarbs());
        String description = formatDescription(meal.getItems());
        String insulinUnits = formatNumber(meal.getInsulinDose());
        String insulinTime = meal.getInsulinTime() != null ? meal.getInsulinTime().trim() : "";
        String timezone = meal.getTimeZone() != null ? meal.getTimeZone().trim() : "";

        return String.join("\t",
                localDate,
                localTime,
                calorie,
                carb,
                satFat,
                fat,
                protein,
                fiber,
                cholesterol,
                digestableCarb,
                description,
                insulinUnits,
                insulinTime,
                timezone
        );
    }

    public static String formatDescription(List<MealItem> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (MealItem item : items) {
            if (item == null) continue;
            String foodName = item.getFoodName();
            if ((foodName == null || foodName.trim().isEmpty()) && item.getFoodItem() != null) {
                foodName = item.getFoodItem().getName();
            }
            foodName = foodName != null ? foodName.trim() : "";
            foodName = foodName.replace("\t", " ").replace("\n", " ").replace("\r", " ");
            if (foodName.isEmpty()) continue;
            if (item.getWeight() <= 0 || Double.isNaN(item.getWeight())) continue;

            String weightStr = formatNumber(item.getWeight());
            sb.append(weightStr).append(" ").append(foodName).append(";");
        }
        return sb.toString();
    }

    public static String formatNumber(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        if (Math.abs(value - Math.round(value)) < 0.0001) {
            return String.valueOf(Math.round(value));
        }
        String str = String.format(Locale.US, "%.1f", value);
        if (str.endsWith(".0")) {
            return str.substring(0, str.length() - 2);
        }
        return str;
    }
}

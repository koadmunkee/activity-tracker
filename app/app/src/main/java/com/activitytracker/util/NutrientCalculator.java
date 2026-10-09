package com.activitytracker.util;

import com.activitytracker.model.FoodItem;
import com.activitytracker.model.MealItem;
import com.activitytracker.model.NutrientSummary;
import com.activitytracker.model.PlannedMeal;

import java.util.List;

/**
 * Pure calculation utilities for macronutrients and aggregations.
 */
public class NutrientCalculator {

    /**
     * Calculates digestable carbohydrates = total carbohydrates - fiber (FR 5.5, 5.6).
     */
    public static double calculateDigestableCarbs(double totalCarbohydrates, double fiber) {
        return Math.max(0.0, totalCarbohydrates - fiber);
    }

    /**
     * Scales nutrient value according to ratio: actualWeight / referenceWeight.
     */
    public static double scaleNutrient(double baseNutrientValue, double actualWeight, double referenceWeight) {
        if (actualWeight <= 0 || referenceWeight <= 0) {
            return 0.0;
        }
        return (baseNutrientValue * actualWeight) / referenceWeight;
    }

    /**
     * Computes NutrientSummary for a specific food and target weight.
     */
    public static NutrientSummary calculateNutrientsForFood(FoodItem food, double targetWeight) {
        if (food == null || targetWeight <= 0) {
            return NutrientSummary.empty();
        }
        return food.getNutrientsForWeight(targetWeight);
    }

    /**
     * Sums nutrients across all food rows in a meal.
     */
    public static NutrientSummary aggregateMealNutrients(List<MealItem> items) {
        NutrientSummary summary = NutrientSummary.empty();
        if (items == null) return summary;
        for (MealItem item : items) {
            summary = summary.plus(item.computeNutrients());
        }
        return summary;
    }

    /**
     * Sums nutrients across all meals of the day (FR 2.4).
     */
    public static NutrientSummary aggregateDayNutrients(List<PlannedMeal> meals) {
        NutrientSummary summary = NutrientSummary.empty();
        if (meals == null) return summary;
        for (PlannedMeal meal : meals) {
            summary = summary.plus(meal.getNutrientSummary());
        }
        return summary;
    }
}

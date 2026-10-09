package com.activitytracker.model;

import java.io.Serializable;
import java.util.Locale;

/**
 * Encapsulates the primary and secondary macronutrients for a food item,
 * meal, or entire day.
 */
public class NutrientSummary implements Serializable {

    // Primary macronutrients (FR 2.4)
    private final double calories;
    private final double digestableCarbs;
    private final double saturatedFat;
    private final double fat;
    private final double protein;

    // Secondary macronutrients (FR 2.4)
    private final double fiber;
    private final double cholesterol;
    private final double carbohydrates;
    private final double omega3;
    private final double omega6;

    public NutrientSummary(double calories, double digestableCarbs, double saturatedFat,
                           double fat, double protein, double fiber, double cholesterol,
                           double carbohydrates, double omega3, double omega6) {
        this.calories = Math.max(0, calories);
        this.digestableCarbs = Math.max(0, digestableCarbs);
        this.saturatedFat = Math.max(0, saturatedFat);
        this.fat = Math.max(0, fat);
        this.protein = Math.max(0, protein);
        this.fiber = Math.max(0, fiber);
        this.cholesterol = Math.max(0, cholesterol);
        this.carbohydrates = Math.max(0, carbohydrates);
        this.omega3 = Math.max(0, omega3);
        this.omega6 = Math.max(0, omega6);
    }

    public static NutrientSummary empty() {
        return new NutrientSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public NutrientSummary plus(NutrientSummary other) {
        if (other == null) return this;
        return new NutrientSummary(
                this.calories + other.calories,
                this.digestableCarbs + other.digestableCarbs,
                this.saturatedFat + other.saturatedFat,
                this.fat + other.fat,
                this.protein + other.protein,
                this.fiber + other.fiber,
                this.cholesterol + other.cholesterol,
                this.carbohydrates + other.carbohydrates,
                this.omega3 + other.omega3,
                this.omega6 + other.omega6
        );
    }

    public double getCalories() { return calories; }
    public double getDigestableCarbs() { return digestableCarbs; }
    public double getSaturatedFat() { return saturatedFat; }
    public double getFat() { return fat; }
    public double getProtein() { return protein; }
    public double getFiber() { return fiber; }
    public double getCholesterol() { return cholesterol; }
    public double getCarbohydrates() { return carbohydrates; }
    public double getOmega3() { return omega3; }
    public double getOmega6() { return omega6; }

    public static String formatOneDecimal(double val) {
        if (Math.abs(val - Math.round(val)) < 0.05) {
            return String.valueOf(Math.round(val));
        }
        return String.format(Locale.US, "%.1f", val);
    }

    public String getFormattedCalories() {
        return formatOneDecimal(calories) + " kcal";
    }

    public String getFormattedDigestableCarbs() {
        return formatOneDecimal(digestableCarbs) + "g";
    }

    public String getFormattedSaturatedFat() {
        return formatOneDecimal(saturatedFat) + "g";
    }

    public String getFormattedFat() {
        return formatOneDecimal(fat) + "g";
    }

    public String getFormattedProtein() {
        return formatOneDecimal(protein) + "g";
    }

    public String getFormattedFiber() {
        return formatOneDecimal(fiber) + "g";
    }

    public String getFormattedCholesterol() {
        return formatOneDecimal(cholesterol) + "mg";
    }

    public String getFormattedCarbohydrates() {
        return formatOneDecimal(carbohydrates) + "g";
    }

    public String getFormattedOmega3() {
        return formatOneDecimal(omega3) + "g";
    }

    public String getFormattedOmega6() {
        return formatOneDecimal(omega6) + "g";
    }
}

package com.activitytracker.model;

import java.io.Serializable;

/**
 * Represents a Food Item in the database with reference weight and nutrient values.
 */
public class FoodItem implements Serializable {
    private long id;
    private String name;
    private double referenceWeight; // Reference weight (typically 100g or serving)
    private double calories;
    private double digestableCarbs; // total carbohydrates minus fiber
    private double saturatedFat;
    private double fat; // total fat
    private double protein;
    private double fiber;
    private double cholesterol;
    private double carbohydrates; // total carbohydrates
    private double omega3;
    private double omega6;
    private boolean isArchived;

    public FoodItem() {
        this.referenceWeight = 100.0;
        this.isArchived = false;
    }

    public FoodItem(long id, String name, double referenceWeight, double calories,
                    double digestableCarbs, double saturatedFat, double fat, double protein,
                    double fiber, double cholesterol, double carbohydrates, double omega3,
                    double omega6, boolean isArchived) {
        this.id = id;
        this.name = name != null ? name.trim() : "";
        this.referenceWeight = referenceWeight > 0 ? referenceWeight : 100.0;
        this.calories = calories;
        this.digestableCarbs = digestableCarbs;
        this.saturatedFat = saturatedFat;
        this.fat = fat;
        this.protein = protein;
        this.fiber = fiber;
        this.cholesterol = cholesterol;
        this.carbohydrates = carbohydrates;
        this.omega3 = omega3;
        this.omega6 = omega6;
        this.isArchived = isArchived;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name.trim() : ""; }

    public double getReferenceWeight() { return referenceWeight; }
    public void setReferenceWeight(double referenceWeight) { this.referenceWeight = referenceWeight; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getDigestableCarbs() { return digestableCarbs; }
    public void setDigestableCarbs(double digestableCarbs) { this.digestableCarbs = digestableCarbs; }

    public double getSaturatedFat() { return saturatedFat; }
    public void setSaturatedFat(double saturatedFat) { this.saturatedFat = saturatedFat; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }

    public double getCholesterol() { return cholesterol; }
    public void setCholesterol(double cholesterol) { this.cholesterol = cholesterol; }

    public double getCarbohydrates() { return carbohydrates; }
    public void setCarbohydrates(double carbohydrates) { this.carbohydrates = carbohydrates; }

    public double getOmega3() { return omega3; }
    public void setOmega3(double omega3) { this.omega3 = omega3; }

    public double getOmega6() { return omega6; }
    public void setOmega6(double omega6) { this.omega6 = omega6; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    /**
     * Compute nutrients for a given weight using the food's reference weight.
     */
    public NutrientSummary getNutrientsForWeight(double weightGrams) {
        if (weightGrams <= 0 || referenceWeight <= 0) {
            return NutrientSummary.empty();
        }
        double factor = weightGrams / referenceWeight;
        return new NutrientSummary(
                calories * factor,
                digestableCarbs * factor,
                saturatedFat * factor,
                fat * factor,
                protein * factor,
                fiber * factor,
                cholesterol * factor,
                carbohydrates * factor,
                omega3 * factor,
                omega6 * factor
        );
    }

    @Override
    public String toString() {
        return name;
    }
}

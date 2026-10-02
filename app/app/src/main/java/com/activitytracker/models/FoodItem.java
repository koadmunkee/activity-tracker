package com.activitytracker.models;

public class FoodItem {
    private String name;
    private double caloriesPer100g;
    private double totalCarbsPer100g;
    private double fiberPer100g;
    private double saturatedFatPer100g;
    private double totalFatPer100g;
    private double proteinPer100g;
    private double cholesterolPer100g; // in mg
    private double omega3Per100g;     // in g
    private double omega6Per100g;     // in g

    public FoodItem(String name, double calories, double totalCarbs, double fiber,
                    double saturatedFat, double totalFat, double protein,
                    double cholesterol, double omega3, double omega6) {
        this.name = name;
        this.caloriesPer100g = calories;
        this.totalCarbsPer100g = totalCarbs;
        this.fiberPer100g = fiber;
        this.saturatedFatPer100g = saturatedFat;
        this.totalFatPer100g = totalFat;
        this.proteinPer100g = protein;
        this.cholesterolPer100g = cholesterol;
        this.omega3Per100g = omega3;
        this.omega6Per100g = omega6;
    }

    public String getName() { return name; }
    public double getCaloriesPer100g() { return caloriesPer100g; }
    public double getTotalCarbsPer100g() { return totalCarbsPer100g; }
    public double getFiberPer100g() { return fiberPer100g; }
    public double getDigestibleCarbsPer100g() { return Math.max(0, totalCarbsPer100g - fiberPer100g); }
    public double getSaturatedFatPer100g() { return saturatedFatPer100g; }
    public double getTotalFatPer100g() { return totalFatPer100g; }
    public double getProteinPer100g() { return proteinPer100g; }
    public double getCholesterolPer100g() { return cholesterolPer100g; }
    public double getOmega3Per100g() { return omega3Per100g; }
    public double getOmega6Per100g() { return omega6Per100g; }

    @Override
    public String toString() {
        return name;
    }
}

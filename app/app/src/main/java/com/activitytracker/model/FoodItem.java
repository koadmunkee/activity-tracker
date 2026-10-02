package com.activitytracker.model;

public class FoodItem {
    private String id;
    private String name;
    private double caloriesPer100g;
    private double totalCarbsPer100g;
    private double fiberPer100g;
    private double saturatedFatPer100g;
    private double totalFatPer100g;
    private double proteinPer100g;
    private double cholesterolMgPer100g;
    private double omega3gPer100g;
    private double omega6gPer100g;

    public FoodItem(String id, String name, double calories, double totalCarbs, double fiber,
                    double satFat, double totalFat, double protein, double cholesterol,
                    double omega3, double omega6) {
        this.id = id;
        this.name = name;
        this.caloriesPer100g = calories;
        this.totalCarbsPer100g = totalCarbs;
        this.fiberPer100g = fiber;
        this.saturatedFatPer100g = satFat;
        this.totalFatPer100g = totalFat;
        this.proteinPer100g = protein;
        this.cholesterolMgPer100g = cholesterol;
        this.omega3gPer100g = omega3;
        this.omega6gPer100g = omega6;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getCaloriesPer100g() { return caloriesPer100g; }
    public double getTotalCarbsPer100g() { return totalCarbsPer100g; }
    public double getFiberPer100g() { return fiberPer100g; }
    public double getDigestibleCarbsPer100g() { return Math.max(0, totalCarbsPer100g - fiberPer100g); }
    public double getSaturatedFatPer100g() { return saturatedFatPer100g; }
    public double getTotalFatPer100g() { return totalFatPer100g; }
    public double getProteinPer100g() { return proteinPer100g; }
    public double getCholesterolMgPer100g() { return cholesterolMgPer100g; }
    public double getOmega3gPer100g() { return omega3gPer100g; }
    public double getOmega6gPer100g() { return omega6gPer100g; }

    @Override
    public String toString() {
        return name;
    }
}

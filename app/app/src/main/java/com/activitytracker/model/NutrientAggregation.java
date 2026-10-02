package com.activitytracker.model;

public class NutrientAggregation {
    public double calories;
    public double digestibleCarbs;
    public double saturatedFat;
    public double totalFat;
    public double protein;
    public double fiber;
    public double cholesterolMg;
    public double totalCarbs;
    public double omega3g;
    public double omega6g;

    public NutrientAggregation() {}

    public NutrientAggregation(double calories, double digestibleCarbs, double saturatedFat,
                               double totalFat, double protein, double fiber,
                               double cholesterolMg, double totalCarbs,
                               double omega3g, double omega6g) {
        this.calories = calories;
        this.digestibleCarbs = digestibleCarbs;
        this.saturatedFat = saturatedFat;
        this.totalFat = totalFat;
        this.protein = protein;
        this.fiber = fiber;
        this.cholesterolMg = cholesterolMg;
        this.totalCarbs = totalCarbs;
        this.omega3g = omega3g;
        this.omega6g = omega6g;
    }

    public void add(NutrientAggregation other) {
        if (other == null) return;
        this.calories += other.calories;
        this.digestibleCarbs += other.digestibleCarbs;
        this.saturatedFat += other.saturatedFat;
        this.totalFat += other.totalFat;
        this.protein += other.protein;
        this.fiber += other.fiber;
        this.cholesterolMg += other.cholesterolMg;
        this.totalCarbs += other.totalCarbs;
        this.omega3g += other.omega3g;
        this.omega6g += other.omega6g;
    }
}

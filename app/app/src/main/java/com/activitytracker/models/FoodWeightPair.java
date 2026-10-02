package com.activitytracker.models;

public class FoodWeightPair {
    private FoodItem foodItem;
    private double weightGrams;

    public FoodWeightPair(FoodItem foodItem, double weightGrams) {
        this.foodItem = foodItem;
        this.weightGrams = weightGrams;
    }

    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }

    public double getWeightGrams() { return weightGrams; }
    public void setWeightGrams(double weightGrams) { this.weightGrams = weightGrams; }

    public double getCalories() { return foodItem != null ? (foodItem.getCaloriesPer100g() * weightGrams) / 100.0 : 0; }
    public double getDigestibleCarbs() { return foodItem != null ? (foodItem.getDigestibleCarbsPer100g() * weightGrams) / 100.0 : 0; }
    public double getSaturatedFat() { return foodItem != null ? (foodItem.getSaturatedFatPer100g() * weightGrams) / 100.0 : 0; }
    public double getTotalFat() { return foodItem != null ? (foodItem.getTotalFatPer100g() * weightGrams) / 100.0 : 0; }
    public double getProtein() { return foodItem != null ? (foodItem.getProteinPer100g() * weightGrams) / 100.0 : 0; }
    
    // Secondary nutrients
    public double getFiber() { return foodItem != null ? (foodItem.getFiberPer100g() * weightGrams) / 100.0 : 0; }
    public double getCholesterol() { return foodItem != null ? (foodItem.getCholesterolPer100g() * weightGrams) / 100.0 : 0; }
    public double getTotalCarbs() { return foodItem != null ? (foodItem.getTotalCarbsPer100g() * weightGrams) / 100.0 : 0; }
    public double getOmega3() { return foodItem != null ? (foodItem.getOmega3Per100g() * weightGrams) / 100.0 : 0; }
    public double getOmega6() { return foodItem != null ? (foodItem.getOmega6Per100g() * weightGrams) / 100.0 : 0; }
}

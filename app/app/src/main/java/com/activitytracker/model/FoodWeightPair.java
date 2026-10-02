package com.activitytracker.model;

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

    public NutrientAggregation getNutrients() {
        if (foodItem == null || weightGrams <= 0) {
            return new NutrientAggregation();
        }
        double factor = weightGrams / 100.0;
        return new NutrientAggregation(
            foodItem.getCaloriesPer100g() * factor,
            foodItem.getDigestibleCarbsPer100g() * factor,
            foodItem.getSaturatedFatPer100g() * factor,
            foodItem.getTotalFatPer100g() * factor,
            foodItem.getProteinPer100g() * factor,
            foodItem.getFiberPer100g() * factor,
            foodItem.getCholesterolMgPer100g() * factor,
            foodItem.getTotalCarbsPer100g() * factor,
            foodItem.getOmega3gPer100g() * factor,
            foodItem.getOmega6gPer100g() * factor
        );
    }
}

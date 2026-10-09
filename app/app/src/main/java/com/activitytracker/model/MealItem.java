package com.activitytracker.model;

import java.io.Serializable;

/**
 * Represents a food & weight pair row within a planned meal.
 */
public class MealItem implements Serializable {
    private long id;
    private long mealId;
    private long foodId;
    private String foodName;
    private double weight; // grams
    private FoodItem foodItem; // Populated food definition

    public MealItem() {
        this.weight = 0.0;
        this.foodName = "";
    }

    public MealItem(long id, long mealId, long foodId, String foodName, double weight) {
        this.id = id;
        this.mealId = mealId;
        this.foodId = foodId;
        this.foodName = foodName != null ? foodName.trim() : "";
        this.weight = weight;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMealId() { return mealId; }
    public void setMealId(long mealId) { this.mealId = mealId; }

    public long getFoodId() { return foodId; }
    public void setFoodId(long foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName != null ? foodName.trim() : ""; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
        if (foodItem != null) {
            this.foodId = foodItem.getId();
            this.foodName = foodItem.getName();
        }
    }

    public NutrientSummary computeNutrients() {
        if (foodItem != null && weight > 0) {
            return foodItem.getNutrientsForWeight(weight);
        }
        return NutrientSummary.empty();
    }

    /**
     * Check if this food row is valid: recognized food and weight > 0 (FR 2.8).
     */
    public boolean isValid() {
        return foodItem != null && foodItem.getId() > 0 && weight > 0;
    }
}

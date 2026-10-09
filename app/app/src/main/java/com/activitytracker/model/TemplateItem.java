package com.activitytracker.model;

import java.io.Serializable;

/**
 * Represents a food & weight pair row within a meal template.
 */
public class TemplateItem implements Serializable {
    private long id;
    private long templateId;
    private long foodId;
    private String foodName;
    private double weight;
    private FoodItem foodItem;

    public TemplateItem() {
        this.foodName = "";
        this.weight = 0.0;
    }

    public TemplateItem(long id, long templateId, long foodId, String foodName, double weight) {
        this.id = id;
        this.templateId = templateId;
        this.foodId = foodId;
        this.foodName = foodName != null ? foodName.trim() : "";
        this.weight = weight;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTemplateId() { return templateId; }
    public void setTemplateId(long templateId) { this.templateId = templateId; }

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

    public boolean isValid() {
        return foodItem != null && foodItem.getId() > 0 && weight > 0;
    }
}

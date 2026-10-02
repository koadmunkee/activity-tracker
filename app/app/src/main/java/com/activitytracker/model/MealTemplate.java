package com.activitytracker.model;

import java.util.ArrayList;
import java.util.List;

public class MealTemplate {
    private String id;
    private String templateName;
    private List<FoodWeightPair> items;

    public MealTemplate(String id, String templateName) {
        this.id = id;
        this.templateName = templateName;
        this.items = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public List<FoodWeightPair> getItems() { return items; }
    public void setItems(List<FoodWeightPair> items) { this.items = items; }

    public NutrientAggregation getAggregatedNutrients() {
        NutrientAggregation total = new NutrientAggregation();
        for (FoodWeightPair pair : items) {
            total.add(pair.getNutrients());
        }
        return total;
    }
}

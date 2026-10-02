package com.activitytracker.models;

import java.util.ArrayList;
import java.util.List;

public class MealTemplate {
    private long id;
    private String templateName;
    private double insulinDosage;
    private List<FoodWeightPair> items;

    public MealTemplate(String templateName) {
        this.templateName = templateName;
        this.insulinDosage = 0.0;
        this.items = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public double getInsulinDosage() { return insulinDosage; }
    public void setInsulinDosage(double insulinDosage) { this.insulinDosage = insulinDosage; }

    public List<FoodWeightPair> getItems() { return items; }
    public void setItems(List<FoodWeightPair> items) { this.items = items; }
}

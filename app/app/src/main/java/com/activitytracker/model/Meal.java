package com.activitytracker.model;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    private String id;
    private String mealName;
    private List<FoodWeightPair> items;
    private String mealTime;
    private double insulinDosage;
    private String insulinTime;
    private boolean committed;
    private String committedDate;

    public Meal(String id, String mealName) {
        this.id = id;
        this.mealName = mealName;
        this.items = new ArrayList<>();
        this.mealTime = "";
        this.insulinDosage = 0.0;
        this.insulinTime = "";
        this.committed = false;
        this.committedDate = "";
    }

    public String getId() { return id; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public List<FoodWeightPair> getItems() { return items; }
    public void setItems(List<FoodWeightPair> items) { this.items = items; }
    public String getMealTime() { return mealTime; }
    public void setMealTime(String mealTime) { this.mealTime = mealTime; }
    public double getInsulinDosage() { return insulinDosage; }
    public void setInsulinDosage(double insulinDosage) { this.insulinDosage = insulinDosage; }
    public String getInsulinTime() { return insulinTime; }
    public void setInsulinTime(String insulinTime) { this.insulinTime = insulinTime; }
    public boolean isCommitted() { return committed; }
    public void setCommitted(boolean committed) { this.committed = committed; }
    public String getCommittedDate() { return committedDate; }
    public void setCommittedDate(String committedDate) { this.committedDate = committedDate; }

    public NutrientAggregation getAggregatedNutrients() {
        NutrientAggregation total = new NutrientAggregation();
        for (FoodWeightPair pair : items) {
            total.add(pair.getNutrients());
        }
        return total;
    }
}

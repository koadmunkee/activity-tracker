package com.activitytracker.models;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    private long id;
    private String name;
    private String mealTime;
    private double insulinDosage;
    private String insulinTime;
    private boolean isCommitted;
    private String committedDate;
    private List<FoodWeightPair> items;

    public Meal(String name) {
        this.name = name;
        this.mealTime = "";
        this.insulinDosage = 0.0;
        this.insulinTime = "";
        this.isCommitted = false;
        this.committedDate = "";
        this.items = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMealTime() { return mealTime; }
    public void setMealTime(String mealTime) { this.mealTime = mealTime; }

    public double getInsulinDosage() { return insulinDosage; }
    public void setInsulinDosage(double insulinDosage) { this.insulinDosage = insulinDosage; }

    public String getInsulinTime() { return insulinTime; }
    public void setInsulinTime(String insulinTime) { this.insulinTime = insulinTime; }

    public boolean isCommitted() { return isCommitted; }
    public void setCommitted(boolean committed) { isCommitted = committed; }

    public String getCommittedDate() { return committedDate; }
    public void setCommittedDate(String committedDate) { this.committedDate = committedDate; }

    public List<FoodWeightPair> getItems() { return items; }
    public void setItems(List<FoodWeightPair> items) { this.items = items; }

    // Nutrient Aggregations
    public double getTotalCalories() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getCalories();
        return sum;
    }

    public double getTotalDigestibleCarbs() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getDigestibleCarbs();
        return sum;
    }

    public double getTotalSaturatedFat() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getSaturatedFat();
        return sum;
    }

    public double getTotalFat() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getTotalFat();
        return sum;
    }

    public double getTotalProtein() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getProtein();
        return sum;
    }

    public double getTotalFiber() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getFiber();
        return sum;
    }

    public double getTotalCholesterol() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getCholesterol();
        return sum;
    }

    public double getTotalCarbs() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getTotalCarbs();
        return sum;
    }

    public double getTotalOmega3() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getOmega3();
        return sum;
    }

    public double getTotalOmega6() {
        double sum = 0;
        for (FoodWeightPair p : items) sum += p.getOmega6();
        return sum;
    }
}

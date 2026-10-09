package com.activitytracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a committed day with all its committed meals and day-level totals.
 */
public class CommittedDay implements Serializable {
    private String date; // YYYY-MM-DD
    private List<PlannedMeal> meals = new ArrayList<>();

    public CommittedDay(String date, List<PlannedMeal> meals) {
        this.date = date;
        this.meals = meals != null ? meals : new ArrayList<>();
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<PlannedMeal> getMeals() { return meals; }
    public void setMeals(List<PlannedMeal> meals) {
        this.meals = meals != null ? meals : new ArrayList<>();
    }

    public NutrientSummary getDayNutrientSummary() {
        NutrientSummary total = NutrientSummary.empty();
        for (PlannedMeal meal : meals) {
            total = total.plus(meal.getNutrientSummary());
        }
        return total;
    }

    public double getTotalInsulinDose() {
        double sum = 0.0;
        for (PlannedMeal meal : meals) {
            sum += meal.getInsulinDose();
        }
        return sum;
    }
}

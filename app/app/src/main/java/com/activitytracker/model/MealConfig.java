package com.activitytracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents daily meal configuration: number of meals and custom names.
 */
public class MealConfig implements Serializable {
    private List<String> mealNames = new ArrayList<>();

    public MealConfig() {
        this.mealNames = new ArrayList<>(Arrays.asList("Breakfast", "Lunch", "Dinner"));
    }

    public MealConfig(List<String> mealNames) {
        if (mealNames != null && !mealNames.isEmpty()) {
            this.mealNames = new ArrayList<>(mealNames);
        } else {
            this.mealNames = new ArrayList<>(Arrays.asList("Breakfast", "Lunch", "Dinner"));
        }
    }

    public List<String> getMealNames() {
        return mealNames;
    }

    public void setMealNames(List<String> mealNames) {
        this.mealNames = mealNames != null ? mealNames : new ArrayList<>();
    }

    public int getMealCount() {
        return mealNames.size();
    }
}

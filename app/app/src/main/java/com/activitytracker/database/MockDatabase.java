package com.activitytracker.database;

import com.activitytracker.model.FoodItem;
import com.activitytracker.model.Meal;
import com.activitytracker.model.MealTemplate;

import java.util.ArrayList;
import java.util.List;

public class MockDatabase {
    private static MockDatabase instance;
    private List<FoodItem> foodList;
    private List<MealTemplate> templates;
    private List<Meal> activeUncommittedMeals;
    private List<Meal> committedHistory;
    private List<String> configuredMealNames;

    private MockDatabase() {
        foodList = new ArrayList<>();
        templates = new ArrayList<>();
        activeUncommittedMeals = new ArrayList<>();
        committedHistory = new ArrayList<>();
        configuredMealNames = new ArrayList<>();

        // Default Configured Meal Names
        configuredMealNames.add("Breakfast");
        configuredMealNames.add("Lunch");
        configuredMealNames.add("Dinner");
        configuredMealNames.add("Snack");

        initSeedData();
    }

    public static synchronized MockDatabase getInstance() {
        if (instance == null) {
            instance = new MockDatabase();
        }
        return instance;
    }

    private void initSeedData() {
        // Populate standard foods (Per 100g)
        foodList.add(new FoodItem("f1", "Oatmeal", 68, 12, 1.7, 0.2, 1.4, 2.4, 0, 0.03, 0.4));
        foodList.add(new FoodItem("f2", "Chicken Breast", 165, 0, 0, 1.0, 3.6, 31.0, 85, 0.03, 0.4));
        foodList.add(new FoodItem("f3", "Brown Rice", 112, 24, 1.8, 0.2, 0.9, 2.6, 0, 0.01, 0.2));
        foodList.add(new FoodItem("f4", "Broccoli", 34, 7, 2.6, 0.0, 0.4, 2.8, 0, 0.09, 0.04));
        foodList.add(new FoodItem("f5", "Salmon", 208, 0, 0, 3.1, 13.0, 20.0, 55, 2.2, 0.2));
        foodList.add(new FoodItem("f6", "Avocado", 160, 9, 7.0, 2.1, 15.0, 2.0, 0, 0.1, 1.7));
        foodList.add(new FoodItem("f7", "Egg", 155, 1.1, 0, 3.3, 11.0, 13.0, 373, 0.1, 1.1));

        // Initialize active uncommitted day's meals based on configuration
        reloadUncommittedMeals();
    }

    public void reloadUncommittedMeals() {
        activeUncommittedMeals.clear();
        for (int i = 0; i < configuredMealNames.size(); i++) {
            activeUncommittedMeals.add(new Meal("m_" + System.currentTimeMillis() + "_" + i, configuredMealNames.get(i)));
        }
    }

    public List<FoodItem> getFoodList() { return foodList; }
    public List<MealTemplate> getTemplates() { return templates; }
    public List<Meal> getActiveUncommittedMeals() { return activeUncommittedMeals; }
    public List<Meal> getCommittedHistory() { return committedHistory; }
    public List<String> getConfiguredMealNames() { return configuredMealNames; }

    public void setConfiguredMealNames(List<String> newNames) {
        this.configuredMealNames = new ArrayList<>(newNames);
    }

    public Meal getLastCommittedMealByName(String mealName) {
        for (int i = committedHistory.size() - 1; i >= 0; i--) {
            Meal m = committedHistory.get(i);
            if (m.getMealName().equalsIgnoreCase(mealName)) {
                return m;
            }
        }
        return null;
    }

    public void commitAllActiveMeals(String dateString) {
        for (Meal m : activeUncommittedMeals) {
            m.setCommitted(true);
            m.setCommittedDate(dateString);
            committedHistory.add(m);
        }
        reloadUncommittedMeals();
    }
}

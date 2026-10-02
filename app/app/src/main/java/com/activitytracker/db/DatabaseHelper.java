package com.activitytracker.db;

import android.content.Context;
import com.activitytracker.models.FoodItem;
import com.activitytracker.models.Meal;
import com.activitytracker.models.MealTemplate;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static DatabaseHelper instance;
    private List<FoodItem> foodDatabase;
    private List<Meal> uncommittedMeals;
    private List<Meal> committedMeals;
    private List<MealTemplate> templates;
    private List<String> mealNamesConfig;

    private DatabaseHelper() {
        foodDatabase = new ArrayList<>();
        uncommittedMeals = new ArrayList<>();
        committedMeals = new ArrayList<>();
        templates = new ArrayList<>();
        mealNamesConfig = new ArrayList<>();

        initDefaults();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private void initDefaults() {
        // Default Configurable Meal Names
        mealNamesConfig.add("Breakfast");
        mealNamesConfig.add("Lunch");
        mealNamesConfig.add("Dinner");
        mealNamesConfig.add("Snack");

        // Pre-populate Master Food List
        foodDatabase.add(new FoodItem("Chicken Breast", 165, 0, 0, 1.0, 3.6, 31, 85, 0.03, 0.5));
        foodDatabase.add(new FoodItem("Brown Rice", 112, 23.5, 1.8, 0.2, 0.9, 2.6, 0, 0.01, 0.3));
        foodDatabase.add(new FoodItem("Avocado", 160, 8.5, 6.7, 2.1, 14.7, 2, 0, 0.11, 1.7));
        foodDatabase.add(new FoodItem("Salmon", 208, 0, 0, 3.1, 13, 20, 55, 2.3, 0.2));
        foodDatabase.add(new FoodItem("Broccoli", 34, 6.6, 2.6, 0.04, 0.4, 2.8, 0, 0.09, 0.05));
        foodDatabase.add(new FoodItem("Egg (Whole)", 155, 1.1, 0, 3.3, 11, 13, 373, 0.1, 1.1));
        foodDatabase.add(new FoodItem("Oats", 389, 66.3, 10.6, 1.2, 6.9, 16.9, 0, 0.11, 2.4));
        foodDatabase.add(new FoodItem("Greek Yogurt", 59, 3.6, 0, 0.1, 0.4, 10, 5, 0.01, 0.03));

        // Create default uncommitted meals
        for (String name : mealNamesConfig) {
            uncommittedMeals.add(new Meal(name));
        }
    }

    public List<FoodItem> getFoodDatabase() { return foodDatabase; }
    public List<String> getMealNamesConfig() { return mealNamesConfig; }
    public List<Meal> getUncommittedMeals() { return uncommittedMeals; }
    public List<MealTemplate> getTemplates() { return templates; }

    public void updateMealConfig(List<String> newNames) {
        this.mealNamesConfig = new ArrayList<>(newNames);
    }

    public Meal getMostRecentCommittedMeal(String mealName) {
        for (int i = committedMeals.size() - 1; i >= 0; i--) {
            if (committedMeals.get(i).getName().equalsIgnoreCase(mealName)) {
                return committedMeals.get(i);
            }
        }
        return null;
    }

    public void commitMeal(Meal meal, String dateStr) {
        meal.setCommitted(true);
        meal.setCommittedDate(dateStr);
        committedMeals.add(meal);
        uncommittedMeals.remove(meal);
    }

    public void saveTemplate(MealTemplate template) {
        templates.removeIf(t -> t.getTemplateName().equalsIgnoreCase(template.getTemplateName()));
        templates.add(template);
    }

    public void deleteTemplate(MealTemplate template) {
        templates.remove(template);
    }
}

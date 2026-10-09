package com.activitytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activitytracker.model.CommittedDay;
import com.activitytracker.model.FoodItem;
import com.activitytracker.model.MealItem;
import com.activitytracker.model.PlannedMeal;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository handling PlannedMeal, MealItem, commit/uncommit workflows,
 * history tracking, and recent-meal retrieval.
 */
public class MealRepository {

    private final DatabaseHelper dbHelper;
    private final FoodRepository foodRepository;

    public MealRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.foodRepository = new FoodRepository(context);
    }

    /**
     * Retrieves or initializes uncommitted meals according to configured meal names.
     */
    public synchronized List<PlannedMeal> getUncommittedMeals(List<String> configuredMealNames) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<PlannedMeal> meals = loadMealsFromDb("is_committed = 0", null, "meal_order ASC");

        if (meals.isEmpty() && configuredMealNames != null && !configuredMealNames.isEmpty()) {
            for (int i = 0; i < configuredMealNames.size(); i++) {
                String name = configuredMealNames.get(i);
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_MEAL_NAME, name);
                cv.put(DatabaseHelper.COL_MEAL_ORDER, i);
                cv.put(DatabaseHelper.COL_MEAL_START_TIME, "");
                cv.put(DatabaseHelper.COL_MEAL_INSULIN_DOSE, 0.0);
                cv.put(DatabaseHelper.COL_MEAL_INSULIN_TIME, "");
                cv.put(DatabaseHelper.COL_MEAL_IS_COMMITTED, 0);
                long newId = db.insert(DatabaseHelper.TABLE_MEALS, null, cv);

                PlannedMeal meal = new PlannedMeal(newId, null, name, i, "", 0.0, "", false);
                meals.add(meal);
            }
        }
        return meals;
    }

    /**
     * Saves changes to a meal (timings, insulin, and food/weight item rows).
     */
    public synchronized void saveOrUpdateMeal(PlannedMeal meal) {
        if (meal == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_MEAL_NAME, meal.getMealName());
            cv.put(DatabaseHelper.COL_MEAL_ORDER, meal.getMealOrder());
            cv.put(DatabaseHelper.COL_MEAL_START_TIME, meal.getStartTime());
            cv.put(DatabaseHelper.COL_MEAL_INSULIN_DOSE, meal.getInsulinDose());
            cv.put(DatabaseHelper.COL_MEAL_INSULIN_TIME, meal.getInsulinTime());
            cv.put(DatabaseHelper.COL_MEAL_IS_COMMITTED, meal.isCommitted() ? 1 : 0);
            if (meal.getDate() != null) {
                cv.put(DatabaseHelper.COL_MEAL_DATE, meal.getDate());
            }

            if (meal.getId() <= 0) {
                long newId = db.insert(DatabaseHelper.TABLE_MEALS, null, cv);
                meal.setId(newId);
            } else {
                db.update(DatabaseHelper.TABLE_MEALS, cv,
                        DatabaseHelper.COL_MEAL_ID + " = ?", new String[]{String.valueOf(meal.getId())});
            }

            // Sync meal items
            db.delete(DatabaseHelper.TABLE_MEAL_ITEMS,
                    DatabaseHelper.COL_ITEM_MEAL_ID + " = ?", new String[]{String.valueOf(meal.getId())});

            for (MealItem item : meal.getItems()) {
                ContentValues itemCv = new ContentValues();
                itemCv.put(DatabaseHelper.COL_ITEM_MEAL_ID, meal.getId());
                itemCv.put(DatabaseHelper.COL_ITEM_FOOD_ID, item.getFoodId());
                itemCv.put(DatabaseHelper.COL_ITEM_FOOD_NAME, item.getFoodName());
                itemCv.put(DatabaseHelper.COL_ITEM_WEIGHT, item.getWeight());
                long itemId = db.insert(DatabaseHelper.TABLE_MEAL_ITEMS, null, itemCv);
                item.setId(itemId);
                item.setMealId(meal.getId());
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Commits the day's meals with the specified date (FR 2.3).
     */
    public synchronized void commitDayMeals(String date, List<PlannedMeal> meals) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (PlannedMeal meal : meals) {
                meal.setDate(date);
                meal.setCommitted(true);

                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_MEAL_DATE, date);
                cv.put(DatabaseHelper.COL_MEAL_IS_COMMITTED, 1);
                cv.put(DatabaseHelper.COL_MEAL_START_TIME, meal.getStartTime());
                cv.put(DatabaseHelper.COL_MEAL_INSULIN_DOSE, meal.getInsulinDose());
                cv.put(DatabaseHelper.COL_MEAL_INSULIN_TIME, meal.getInsulinTime());

                db.update(DatabaseHelper.TABLE_MEALS, cv,
                        DatabaseHelper.COL_MEAL_ID + " = ?", new String[]{String.valueOf(meal.getId())});

                // Update items
                db.delete(DatabaseHelper.TABLE_MEAL_ITEMS,
                        DatabaseHelper.COL_ITEM_MEAL_ID + " = ?", new String[]{String.valueOf(meal.getId())});

                for (MealItem item : meal.getItems()) {
                    ContentValues itemCv = new ContentValues();
                    itemCv.put(DatabaseHelper.COL_ITEM_MEAL_ID, meal.getId());
                    itemCv.put(DatabaseHelper.COL_ITEM_FOOD_ID, item.getFoodId());
                    itemCv.put(DatabaseHelper.COL_ITEM_FOOD_NAME, item.getFoodName());
                    itemCv.put(DatabaseHelper.COL_ITEM_WEIGHT, item.getWeight());
                    db.insert(DatabaseHelper.TABLE_MEAL_ITEMS, null, itemCv);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Retrieves the most recently committed meal matching the given meal name (FR 2.6).
     */
    public synchronized PlannedMeal getMostRecentCommittedMealByName(String mealName) {
        if (mealName == null) return null;
        String selection = "is_committed = 1 AND meal_name = ? COLLATE NOCASE";
        String[] selectionArgs = new String[]{mealName.trim()};
        List<PlannedMeal> meals = loadMealsFromDb(selection, selectionArgs, "date DESC, id DESC LIMIT 1");
        return meals.isEmpty() ? null : meals.get(0);
    }

    /**
     * Retrieves all committed days with meals (FR 4.1).
     */
    public synchronized List<CommittedDay> getCommittedDays() {
        List<CommittedDay> days = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT DISTINCT date FROM " + DatabaseHelper.TABLE_MEALS
                + " WHERE is_committed = 1 AND date IS NOT NULL ORDER BY date DESC", null);
        List<String> dates = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                dates.add(cursor.getString(0));
            }
            cursor.close();
        }

        for (String date : dates) {
            List<PlannedMeal> dayMeals = loadMealsFromDb(
                    "is_committed = 1 AND date = ?", new String[]{date}, "meal_order ASC, id ASC");
            days.add(new CommittedDay(date, dayMeals));
        }
        return days;
    }

    /**
     * Adjusts a committed day (FR 4.2):
     * Clears all food/weight pairs from uncommitted meals in the planning view,
     * makes the selected day's meals uncommitted, and returns them for planning.
     */
    public synchronized List<PlannedMeal> uncommitDayMeals(String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete all current uncommitted meals & items
            Cursor uncommittedCursor = db.query(DatabaseHelper.TABLE_MEALS,
                    new String[]{DatabaseHelper.COL_MEAL_ID},
                    "is_committed = 0", null, null, null, null);
            if (uncommittedCursor != null) {
                while (uncommittedCursor.moveToNext()) {
                    long uncommittedId = uncommittedCursor.getLong(0);
                    db.delete(DatabaseHelper.TABLE_MEAL_ITEMS,
                            DatabaseHelper.COL_ITEM_MEAL_ID + " = ?", new String[]{String.valueOf(uncommittedId)});
                }
                uncommittedCursor.close();
            }
            db.delete(DatabaseHelper.TABLE_MEALS, "is_committed = 0", null);

            // Mark the selected day's meals as uncommitted
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_MEAL_IS_COMMITTED, 0);
            cv.putNull(DatabaseHelper.COL_MEAL_DATE);
            db.update(DatabaseHelper.TABLE_MEALS, cv,
                    "is_committed = 1 AND date = ?", new String[]{date});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // Return newly uncommitted meals
        return loadMealsFromDb("is_committed = 0", null, "meal_order ASC, id ASC");
    }

    /**
     * Clears all food & weight pairs from the current uncommitted meals.
     */
    public synchronized void clearUncommittedFoodPairs() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_MEALS,
                    new String[]{DatabaseHelper.COL_MEAL_ID},
                    "is_committed = 0", null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long mealId = cursor.getLong(0);
                    db.delete(DatabaseHelper.TABLE_MEAL_ITEMS,
                            DatabaseHelper.COL_ITEM_MEAL_ID + " = ?", new String[]{String.valueOf(mealId)});
                }
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private List<PlannedMeal> loadMealsFromDb(String selection, String[] selectionArgs, String orderBy) {
        List<PlannedMeal> meals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_MEALS, null, selection, selectionArgs,
                null, null, orderBy);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long mealId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_DATE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_NAME));
                int order = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_ORDER));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_START_TIME));
                double insulinDose = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_INSULIN_DOSE));
                String insulinTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_INSULIN_TIME));
                boolean committed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEAL_IS_COMMITTED)) == 1;

                PlannedMeal meal = new PlannedMeal(mealId, date, name, order, startTime, insulinDose, insulinTime, committed);

                // Load meal items
                List<MealItem> items = loadMealItems(db, mealId);
                meal.setItems(items);

                meals.add(meal);
            }
            cursor.close();
        }
        return meals;
    }

    private List<MealItem> loadMealItems(SQLiteDatabase db, long mealId) {
        List<MealItem> items = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_MEAL_ITEMS, null,
                DatabaseHelper.COL_ITEM_MEAL_ID + " = ?", new String[]{String.valueOf(mealId)},
                null, null, DatabaseHelper.COL_ITEM_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_ID));
                long foodId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_FOOD_ID));
                String foodName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_FOOD_NAME));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_WEIGHT));

                MealItem item = new MealItem(itemId, mealId, foodId, foodName, weight);
                FoodItem foodItem = foodRepository.getFoodById(foodId);
                if (foodItem == null && foodName != null && !foodName.isEmpty()) {
                    foodItem = foodRepository.getFoodByName(foodName);
                }
                item.setFoodItem(foodItem);
                items.add(item);
            }
            cursor.close();
        }
        return items;
    }
}

package com.activitytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activitytracker.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for FoodItem persistence operations.
 */
public class FoodRepository {

    private final DatabaseHelper dbHelper;

    public FoodRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<FoodItem> getUnarchivedFoods() {
        return queryFoods(DatabaseHelper.COL_FOOD_IS_ARCHIVED + " = 0", null);
    }

    public List<FoodItem> getArchivedFoods() {
        return queryFoods(DatabaseHelper.COL_FOOD_IS_ARCHIVED + " = 1", null);
    }

    public List<FoodItem> getAllFoods() {
        return queryFoods(null, null);
    }

    public FoodItem getFoodById(long id) {
        List<FoodItem> list = queryFoods(DatabaseHelper.COL_FOOD_ID + " = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public FoodItem getFoodByName(String name) {
        if (name == null) return null;
        List<FoodItem> list = queryFoods(DatabaseHelper.COL_FOOD_NAME + " = ? COLLATE NOCASE", new String[]{name.trim()});
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean isNameUnique(String name, long excludeId) {
        if (name == null || name.trim().isEmpty()) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_FOOD_NAME + " = ? COLLATE NOCASE AND " + DatabaseHelper.COL_FOOD_ID + " != ?";
        String[] selectionArgs = new String[]{name.trim(), String.valueOf(excludeId)};
        Cursor cursor = db.query(DatabaseHelper.TABLE_FOODS, new String[]{DatabaseHelper.COL_FOOD_ID},
                selection, selectionArgs, null, null, null);
        boolean isUnique = (cursor.getCount() == 0);
        cursor.close();
        return isUnique;
    }

    public long insertFood(FoodItem food) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = foodToContentValues(food);
        return db.insert(DatabaseHelper.TABLE_FOODS, null, cv);
    }

    public boolean updateFood(FoodItem food) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = foodToContentValues(food);
        int rows = db.update(DatabaseHelper.TABLE_FOODS, cv,
                DatabaseHelper.COL_FOOD_ID + " = ?", new String[]{String.valueOf(food.getId())});
        return rows > 0;
    }

    public boolean setArchived(long foodId, boolean archived) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_FOOD_IS_ARCHIVED, archived ? 1 : 0);
        int rows = db.update(DatabaseHelper.TABLE_FOODS, cv,
                DatabaseHelper.COL_FOOD_ID + " = ?", new String[]{String.valueOf(foodId)});
        return rows > 0;
    }

    private ContentValues foodToContentValues(FoodItem food) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_FOOD_NAME, food.getName());
        cv.put(DatabaseHelper.COL_FOOD_REF_WEIGHT, food.getReferenceWeight());
        cv.put(DatabaseHelper.COL_FOOD_CALORIES, food.getCalories());
        cv.put(DatabaseHelper.COL_FOOD_DIGESTABLE_CARBS, food.getDigestableCarbs());
        cv.put(DatabaseHelper.COL_FOOD_SAT_FAT, food.getSaturatedFat());
        cv.put(DatabaseHelper.COL_FOOD_FAT, food.getFat());
        cv.put(DatabaseHelper.COL_FOOD_PROTEIN, food.getProtein());
        cv.put(DatabaseHelper.COL_FOOD_FIBER, food.getFiber());
        cv.put(DatabaseHelper.COL_FOOD_CHOLESTEROL, food.getCholesterol());
        cv.put(DatabaseHelper.COL_FOOD_CARBS, food.getCarbohydrates());
        cv.put(DatabaseHelper.COL_FOOD_OMEGA3, food.getOmega3());
        cv.put(DatabaseHelper.COL_FOOD_OMEGA6, food.getOmega6());
        cv.put(DatabaseHelper.COL_FOOD_IS_ARCHIVED, food.isArchived() ? 1 : 0);
        return cv;
    }

    private List<FoodItem> queryFoods(String selection, String[] selectionArgs) {
        List<FoodItem> foods = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FOODS, null, selection, selectionArgs,
                null, null, DatabaseHelper.COL_FOOD_NAME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FoodItem item = new FoodItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_REF_WEIGHT)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_CALORIES)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_DIGESTABLE_CARBS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_SAT_FAT)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_FAT)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_PROTEIN)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_FIBER)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_CHOLESTEROL)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_CARBS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_OMEGA3)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_OMEGA6)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FOOD_IS_ARCHIVED)) == 1
                );
                foods.add(item);
            }
            cursor.close();
        }
        return foods;
    }
}

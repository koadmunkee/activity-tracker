package com.activitytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository for managing daily meal configuration (FR 1.1).
 */
public class ConfigRepository {

    private final DatabaseHelper dbHelper;

    public ConfigRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<String> getConfiguredMealNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_MEAL_CONFIGS, null, null, null,
                null, null, DatabaseHelper.COL_CONFIG_ORDER + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONFIG_NAME));
                names.add(name);
            }
            cursor.close();
        }
        if (names.isEmpty()) {
            return new ArrayList<>(Arrays.asList("Breakfast", "Lunch", "Dinner"));
        }
        return names;
    }

    public void saveConfiguredMealNames(List<String> names) {
        if (names == null || names.isEmpty()) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_MEAL_CONFIGS, null, null);
            for (int i = 0; i < names.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_CONFIG_ORDER, i);
                cv.put(DatabaseHelper.COL_CONFIG_NAME, names.get(i).trim());
                db.insert(DatabaseHelper.TABLE_MEAL_CONFIGS, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}

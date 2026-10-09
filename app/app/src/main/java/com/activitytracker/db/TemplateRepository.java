package com.activitytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activitytracker.model.FoodItem;
import com.activitytracker.model.MealTemplate;
import com.activitytracker.model.TemplateItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for MealTemplate persistence operations.
 */
public class TemplateRepository {

    private final DatabaseHelper dbHelper;
    private final FoodRepository foodRepository;

    public TemplateRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.foodRepository = new FoodRepository(context);
    }

    public List<MealTemplate> getAllTemplates() {
        List<MealTemplate> templates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_TEMPLATES, null, null, null,
                null, null, DatabaseHelper.COL_TEMPLATE_NAME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long tplId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_NAME));
                double insulinDose = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_INSULIN_DOSE));

                MealTemplate template = new MealTemplate(tplId, name, insulinDose);
                template.setItems(loadTemplateItems(db, tplId));
                templates.add(template);
            }
            cursor.close();
        }
        return templates;
    }

    public MealTemplate getTemplateById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TEMPLATES, null,
                DatabaseHelper.COL_TEMPLATE_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        MealTemplate template = null;
        if (cursor != null) {
            if (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_NAME));
                double insulinDose = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TEMPLATE_INSULIN_DOSE));
                template = new MealTemplate(id, name, insulinDose);
                template.setItems(loadTemplateItems(db, id));
            }
            cursor.close();
        }
        return template;
    }

    public boolean isTemplateNameUnique(String name, long excludeId) {
        if (name == null || name.trim().isEmpty()) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TEMPLATES, new String[]{DatabaseHelper.COL_TEMPLATE_ID},
                DatabaseHelper.COL_TEMPLATE_NAME + " = ? COLLATE NOCASE AND " + DatabaseHelper.COL_TEMPLATE_ID + " != ?",
                new String[]{name.trim(), String.valueOf(excludeId)}, null, null, null);
        boolean isUnique = (cursor.getCount() == 0);
        cursor.close();
        return isUnique;
    }

    public long saveOrUpdateTemplate(MealTemplate template) {
        if (template == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_TEMPLATE_NAME, template.getTemplateName());
            cv.put(DatabaseHelper.COL_TEMPLATE_INSULIN_DOSE, template.getInsulinDose());

            long tplId;
            if (template.getId() <= 0) {
                tplId = db.insert(DatabaseHelper.TABLE_TEMPLATES, null, cv);
                template.setId(tplId);
            } else {
                tplId = template.getId();
                db.update(DatabaseHelper.TABLE_TEMPLATES, cv,
                        DatabaseHelper.COL_TEMPLATE_ID + " = ?", new String[]{String.valueOf(tplId)});
            }

            // Sync items
            db.delete(DatabaseHelper.TABLE_TEMPLATE_ITEMS,
                    DatabaseHelper.COL_TPL_ITEM_TEMPLATE_ID + " = ?", new String[]{String.valueOf(tplId)});

            for (TemplateItem item : template.getItems()) {
                ContentValues itemCv = new ContentValues();
                itemCv.put(DatabaseHelper.COL_TPL_ITEM_TEMPLATE_ID, tplId);
                itemCv.put(DatabaseHelper.COL_TPL_ITEM_FOOD_ID, item.getFoodId());
                itemCv.put(DatabaseHelper.COL_TPL_ITEM_FOOD_NAME, item.getFoodName());
                itemCv.put(DatabaseHelper.COL_TPL_ITEM_WEIGHT, item.getWeight());
                long itemId = db.insert(DatabaseHelper.TABLE_TEMPLATE_ITEMS, null, itemCv);
                item.setId(itemId);
                item.setTemplateId(tplId);
            }

            db.setTransactionSuccessful();
            return tplId;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteTemplate(long templateId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_TEMPLATE_ITEMS,
                    DatabaseHelper.COL_TPL_ITEM_TEMPLATE_ID + " = ?", new String[]{String.valueOf(templateId)});
            int rows = db.delete(DatabaseHelper.TABLE_TEMPLATES,
                    DatabaseHelper.COL_TEMPLATE_ID + " = ?", new String[]{String.valueOf(templateId)});
            db.setTransactionSuccessful();
            return rows > 0;
        } finally {
            db.endTransaction();
        }
    }

    private List<TemplateItem> loadTemplateItems(SQLiteDatabase db, long templateId) {
        List<TemplateItem> items = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TEMPLATE_ITEMS, null,
                DatabaseHelper.COL_TPL_ITEM_TEMPLATE_ID + " = ?", new String[]{String.valueOf(templateId)},
                null, null, DatabaseHelper.COL_TPL_ITEM_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TPL_ITEM_ID));
                long foodId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TPL_ITEM_FOOD_ID));
                String foodName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TPL_ITEM_FOOD_NAME));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TPL_ITEM_WEIGHT));

                TemplateItem item = new TemplateItem(id, templateId, foodId, foodName, weight);
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

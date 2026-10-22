package com.activitytracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database helper managing database tables, indexes, and initial seeding.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "activity_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_FOODS = "foods";
    public static final String TABLE_MEALS = "meals";
    public static final String TABLE_MEAL_ITEMS = "meal_items";
    public static final String TABLE_TEMPLATES = "templates";
    public static final String TABLE_TEMPLATE_ITEMS = "template_items";
    public static final String TABLE_MEAL_CONFIGS = "meal_configs";

    // Column names for Foods
    public static final String COL_FOOD_ID = "id";
    public static final String COL_FOOD_NAME = "name";
    public static final String COL_FOOD_REF_WEIGHT = "ref_weight";
    public static final String COL_FOOD_CALORIES = "calories";
    public static final String COL_FOOD_DIGESTABLE_CARBS = "digestable_carbs";
    public static final String COL_FOOD_SAT_FAT = "saturated_fat";
    public static final String COL_FOOD_FAT = "fat";
    public static final String COL_FOOD_PROTEIN = "protein";
    public static final String COL_FOOD_FIBER = "fiber";
    public static final String COL_FOOD_CHOLESTEROL = "cholesterol";
    public static final String COL_FOOD_CARBS = "carbohydrates";
    public static final String COL_FOOD_OMEGA3 = "omega3";
    public static final String COL_FOOD_OMEGA6 = "omega6";
    public static final String COL_FOOD_IS_ARCHIVED = "is_archived";
    public static final String COL_FOOD_VERSION = "version";

    // Column names for Meals
    public static final String COL_MEAL_ID = "id";
    public static final String COL_MEAL_DATE = "date";
    public static final String COL_MEAL_NAME = "meal_name";
    public static final String COL_MEAL_ORDER = "meal_order";
    public static final String COL_MEAL_START_TIME = "start_time";
    public static final String COL_MEAL_INSULIN_DOSE = "insulin_dose";
    public static final String COL_MEAL_INSULIN_TIME = "insulin_time";
    public static final String COL_MEAL_TIME_ZONE = "time_zone";
    public static final String COL_MEAL_IS_COMMITTED = "is_committed";
    public static final String COL_MEAL_IS_AD_HOC = "is_ad_hoc";

    // Column names for Meal Items
    public static final String COL_ITEM_ID = "id";
    public static final String COL_ITEM_MEAL_ID = "meal_id";
    public static final String COL_ITEM_FOOD_ID = "food_id";
    public static final String COL_ITEM_FOOD_NAME = "food_name";
    public static final String COL_ITEM_WEIGHT = "weight";

    // Column names for Templates
    public static final String COL_TEMPLATE_ID = "id";
    public static final String COL_TEMPLATE_NAME = "name";
    public static final String COL_TEMPLATE_INSULIN_DOSE = "insulin_dose";

    // Column names for Template Items
    public static final String COL_TPL_ITEM_ID = "id";
    public static final String COL_TPL_ITEM_TEMPLATE_ID = "template_id";
    public static final String COL_TPL_ITEM_FOOD_ID = "food_id";
    public static final String COL_TPL_ITEM_FOOD_NAME = "food_name";
    public static final String COL_TPL_ITEM_WEIGHT = "weight";

    // Column names for Meal Configs
    public static final String COL_CONFIG_ID = "id";
    public static final String COL_CONFIG_ORDER = "meal_order";
    public static final String COL_CONFIG_NAME = "meal_name";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Foods table
        db.execSQL("CREATE TABLE " + TABLE_FOODS + " ("
                + COL_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FOOD_NAME + " TEXT UNIQUE NOT NULL, "
                + COL_FOOD_REF_WEIGHT + " REAL NOT NULL, "
                + COL_FOOD_CALORIES + " REAL NOT NULL, "
                + COL_FOOD_DIGESTABLE_CARBS + " REAL NOT NULL, "
                + COL_FOOD_SAT_FAT + " REAL NOT NULL, "
                + COL_FOOD_FAT + " REAL NOT NULL, "
                + COL_FOOD_PROTEIN + " REAL NOT NULL, "
                + COL_FOOD_FIBER + " REAL NOT NULL, "
                + COL_FOOD_CHOLESTEROL + " REAL NOT NULL, "
                + COL_FOOD_CARBS + " REAL NOT NULL, "
                + COL_FOOD_OMEGA3 + " REAL NOT NULL, "
                + COL_FOOD_OMEGA6 + " REAL NOT NULL, "
                + COL_FOOD_IS_ARCHIVED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_FOOD_VERSION + " INTEGER NOT NULL DEFAULT 0)");

        // Create Meals table
        db.execSQL("CREATE TABLE " + TABLE_MEALS + " ("
                + COL_MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MEAL_DATE + " TEXT, "
                + COL_MEAL_NAME + " TEXT NOT NULL, "
                + COL_MEAL_ORDER + " INTEGER NOT NULL, "
                + COL_MEAL_START_TIME + " TEXT, "
                + COL_MEAL_INSULIN_DOSE + " REAL DEFAULT 0.0, "
                + COL_MEAL_INSULIN_TIME + " TEXT, "
                + COL_MEAL_TIME_ZONE + " TEXT, "
                + COL_MEAL_IS_COMMITTED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_MEAL_IS_AD_HOC + " INTEGER NOT NULL DEFAULT 0)");

        // Create Meal Items table
        db.execSQL("CREATE TABLE " + TABLE_MEAL_ITEMS + " ("
                + COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ITEM_MEAL_ID + " INTEGER NOT NULL, "
                + COL_ITEM_FOOD_ID + " INTEGER NOT NULL, "
                + COL_ITEM_FOOD_NAME + " TEXT NOT NULL, "
                + COL_ITEM_WEIGHT + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COL_ITEM_MEAL_ID + ") REFERENCES " + TABLE_MEALS + "(" + COL_MEAL_ID + ") ON DELETE CASCADE)");

        // Create Templates table
        db.execSQL("CREATE TABLE " + TABLE_TEMPLATES + " ("
                + COL_TEMPLATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TEMPLATE_NAME + " TEXT UNIQUE NOT NULL, "
                + COL_TEMPLATE_INSULIN_DOSE + " REAL DEFAULT 0.0)");

        // Create Template Items table
        db.execSQL("CREATE TABLE " + TABLE_TEMPLATE_ITEMS + " ("
                + COL_TPL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TPL_ITEM_TEMPLATE_ID + " INTEGER NOT NULL, "
                + COL_TPL_ITEM_FOOD_ID + " INTEGER NOT NULL, "
                + COL_TPL_ITEM_FOOD_NAME + " TEXT NOT NULL, "
                + COL_TPL_ITEM_WEIGHT + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COL_TPL_ITEM_TEMPLATE_ID + ") REFERENCES " + TABLE_TEMPLATES + "(" + COL_TEMPLATE_ID + ") ON DELETE CASCADE)");

        // Create Meal Configs table
        db.execSQL("CREATE TABLE " + TABLE_MEAL_CONFIGS + " ("
                + COL_CONFIG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CONFIG_ORDER + " INTEGER NOT NULL, "
                + COL_CONFIG_NAME + " TEXT NOT NULL)");

        // Seed initial data
        seedDefaultMealConfigs(db);
        seedInitialTemplates(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void seedDefaultMealConfigs(SQLiteDatabase db) {
        String[] defaultMeals = new String[]{"Breakfast", "Lunch", "Dinner"};
        for (int i = 0; i < defaultMeals.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_CONFIG_ORDER, i);
            cv.put(COL_CONFIG_NAME, defaultMeals[i]);
            db.insert(TABLE_MEAL_CONFIGS, null, cv);
        }
    }

    private void seedInitialTemplates(SQLiteDatabase db) {
        // Create an example template: "Power Oatmeal & Berries"
        ContentValues cv = new ContentValues();
        cv.put(COL_TEMPLATE_NAME, "High Protein Oatmeal");
        cv.put(COL_TEMPLATE_INSULIN_DOSE, 3.0);
        long tplId = db.insert(TABLE_TEMPLATES, null, cv);
        if (tplId > 0) {
            insertSeedTemplateItem(db, tplId, 5, "Oatmeal (Rolled Oats, Dry)", 60);
            insertSeedTemplateItem(db, tplId, 14, "Whey Protein Powder", 30);
            insertSeedTemplateItem(db, tplId, 11, "Almonds", 15);
        }
    }

    private void insertSeedTemplateItem(SQLiteDatabase db, long tplId, long foodId, String foodName, double weight) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TPL_ITEM_TEMPLATE_ID, tplId);
        cv.put(COL_TPL_ITEM_FOOD_ID, foodId);
        cv.put(COL_TPL_ITEM_FOOD_NAME, foodName);
        cv.put(COL_TPL_ITEM_WEIGHT, weight);
        db.insert(TABLE_TEMPLATE_ITEMS, null, cv);
    }
}

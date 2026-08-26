-- Recreate meal_v2 table using data from q3 2025 to q2 2026 - before stopping hourly glucose snapshots
CREATE OR REPLACE EXTERNAL TABLE `activity_tracker_dataset.meal_v2_raw`(
    date DATE,
    time STRING,
    calorie FLOAT64,
    carbohydrate FLOAT64,
    saturated_fat FLOAT64,
    fat FLOAT64,
    protein FLOAT64,
    fiber FLOAT64,
    cholesterol FLOAT64,
    digestable_cabohydrate FLOAT64,
    description STRING,
    insulin_units FLOAT64,
    insulin_time STRING,
    glucose_1h FLOAT64,
    glucose_2h FLOAT64,
    glucose_3h FLOAT64)
    OPTIONS (
        format = 'GOOGLE_SHEETS',
        uris =
            [
                'https://docs.google.com/spreadsheets/d/198gIigiCvKRCpXN2XY_ICrWYusB901h9HmCrB9gGVKI'],
        sheet_range = 'meal!A13:P1394');

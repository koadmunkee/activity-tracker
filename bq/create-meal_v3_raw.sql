-- Recreate meal_v3 table using data from q2 2026
CREATE OR REPLACE EXTERNAL TABLE `activity_tracker_dataset.meal_v3_raw`(
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
    timezone STRING)
    OPTIONS (
        format = 'GOOGLE_SHEETS',
        uris =
            [
                'https://docs.google.com/spreadsheets/d/1YshkLkE1drK7Fe7L6btxHcviXBf6GyTao1kWB2vBicM'],
        sheet_range = 'meal!A:N',
        skip_leading_rows = 1);

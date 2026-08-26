-- Recreate meal_v1 table using data from q2 2024 to q3 2025 - incomplete nutrition data
CREATE OR REPLACE EXTERNAL TABLE `activity_tracker_dataset.meal_v1_raw`(
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
                'https://docs.google.com/spreadsheets/d/1HXq8OhKD7tH8Q1PHpuRzo-i5RjD0OPaxZtaIefo5c98'],
        sheet_range = 'meal!A13:P2541');

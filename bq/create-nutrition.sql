-- Recreate nutrition table representing food and associated macro nutrients
CREATE OR REPLACE EXTERNAL TABLE `activity_tracker_dataset.nutrition`(
    primary_nutrient STRING,
    food STRING,
    weight_g FLOAT64,
    calorie_kcal FLOAT64,
    carb_g FLOAT64,
    saturated_fat_g FLOAT64,
    fat_g FLOAT64,
    protein_g FLOAT64,
    fiber_g FLOAT64,
    cholesterol_mg FLOAT64,
    digestable_carb_g FLOAT64,
    omega_3_g FLOAT64,
    omega_6_g FLOAT64,
    inventory_status STRING,
    tag STRING)
    OPTIONS (
        format = 'GOOGLE_SHEETS',
        uris =
            [
                'https://docs.google.com/spreadsheets/d/1YshkLkE1drK7Fe7L6btxHcviXBf6GyTao1kWB2vBicM'],
        sheet_range = 'nutrition!A:O',
        skip_leading_rows = 1);

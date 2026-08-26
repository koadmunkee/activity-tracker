CREATE OR REPLACE VIEW `activity_tracker_dataset.meal`
AS
SELECT
    TIMESTAMP(
        DATETIME(date, PARSE_TIME('%H:%M', time)),
        IFNULL(timezone, 'America/Los_Angeles')) AS meal_time,
    calorie,
    carbohydrate,
    saturated_fat,
    fat,
    protein,
    fiber,
    cholesterol,
    digestable_cabohydrate,
    description,
    insulin_units,
    IF(
        insulin_time IS NULL,
        NULL,
        TIMESTAMP(
            DATETIME(date, PARSE_TIME('%H:%M', insulin_time)),
            IFNULL(timezone, 'America/Los_Angeles'))) AS insulin_time,
FROM `activity_tracker_dataset.meal_v3_raw`
WHERE
    date IS NOT NULL;

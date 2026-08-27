CREATE OR REPLACE VIEW `activity_tracker_dataset.meal_legacy`
AS
SELECT date AS local_meal_date, description
FROM `activity_tracker_dataset.meal_v1_raw`
WHERE date >= DATE('2024-07-13')
UNION ALL
SELECT date AS local_meal_date, description
FROM `activity_tracker_dataset.meal_v2_raw`
UNION ALL
SELECT date AS local_meal_date, description
FROM `activity_tracker_dataset.meal_v3_raw`;

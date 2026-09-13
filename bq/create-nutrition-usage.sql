-- Usage of nutrition entries across meals
CREATE OR REPLACE VIEW `activity_tracker_dataset.nutrition_usage`
AS
SELECT
  nutrition.food, MAX(meal_legacy.local_meal_date) AS last_eaten_date, count(*) AS meal_count
FROM `activity_tracker_dataset.nutrition` AS nutrition
LEFT OUTER JOIN `activity_tracker_dataset.meal_legacy` AS meal_legacy
  ON meal_legacy.description LIKE CONCAT('%', nutrition.food, '%')
WHERE nutrition.food IS NOT NULL
GROUP BY nutrition.food;

-- Food library with reference weight standardized to 100g
CREATE OR REPLACE VIEW `activity_tracker_dataset.nutrition_100g`
AS
SELECT
  food,
  round(100 / weight_g * calorie_kcal) AS calorie_kcal_100g,
  round(100 / weight_g * saturated_fat_g, 2) AS saturated_fat_g_100g,
  round(100 / weight_g * fat_g, 2) AS fat_g_100g,
  round(100 / weight_g * protein_g, 2) AS protein_g_100g,
  round(100 / weight_g * fiber_g, 2) AS fiber_g_100g,
  round(100 / weight_g * cholesterol_mg, 2) AS cholesterol_g_100g,
  round(100 / weight_g * digestable_carb_g, 2) AS digestable_carb_g_100g,
  round(100 / weight_g * omega_3_g, 2) AS omega_3_g_100g,
  round(100 / weight_g * omega_6_g, 2) AS omega_6_g_100g,
  round(safe_divide(saturated_fat_g, fat_g), 4) AS sat_fat_ratio,
FROM `activity_tracker_dataset.nutrition`;

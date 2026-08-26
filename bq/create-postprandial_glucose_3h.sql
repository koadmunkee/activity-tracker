-- Average glucose up to 3 hours after meal time
CREATE OR REPLACE VIEW `activity_tracker_dataset.postprandial_glucose_3h`
AS
SELECT
  DATETIME(m.meal_time, 'America/Los_Angeles') meal_time,
  m.digestable_cabohydrate / m.insulin_units AS carb_insulin_ratio,
  AVG(bg.level) AS avg_blood_glucose_3h
FROM
  `activity_tracker_dataset.meal` AS m
LEFT JOIN
  `activity_tracker_dataset.blood_glucose` AS bg
  ON
    bg.time >= m.meal_time
    AND bg.time <= TIMESTAMP_ADD(m.meal_time, INTERVAL 3 HOUR)
WHERE m.insulin_units > 0
GROUP BY
  meal_time,
  carb_insulin_ratio;

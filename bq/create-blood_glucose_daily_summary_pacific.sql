CREATE OR REPLACE VIEW `activity_tracker_dataset.blood_glucose_daily_summary_pacific`
AS
SELECT
  DATE(time, 'America/Los_Angeles') AS pacific_date,
  AVG(level) average_level,
FROM `activity_tracker_dataset.blood_glucose`
GROUP BY pacific_date;

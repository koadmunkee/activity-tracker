CREATE OR REPLACE EXTERNAL TABLE `activity_tracker_dataset.meal_v4_raw`(
  local_date DATE,
  local_time STRING,
  calorie FLOAT64,
  carbohydrate FLOAT64,
  saturated_fat FLOAT64,
  fat FLOAT64,
  protein FLOAT64,
  fiber FLOAT64,
  cholesterol FLOAT64,
  digestable_carbohydrate FLOAT64,
  description STRING,
  insulin_units FLOAT64,
  insulin_local_time STRING,
  timezone STRING)
  OPTIONS (
    format = "CSV",
    field_delimiter = '\t',
    skip_leading_rows = 1,
    uris = ["gs://georgeruiz-activity-tracker-meal-v4/*.tsv"]);

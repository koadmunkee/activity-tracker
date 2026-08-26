CREATE OR REPLACE EXTERNAL TABLE `activity_tracker_dataset.blood_glucose_delta`(
  physicalTime STRING,
  bloodGlucoseMilligramsPerDeciliter FLOAT64)
  OPTIONS (
    format = "CSV",
    uris = ["gs://georgeruiz-activity-tracker/20*.csv"]);

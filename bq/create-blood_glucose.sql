CREATE OR REPLACE VIEW `activity_tracker_dataset.blood_glucose`
AS
SELECT *
FROM
    (
        SELECT
            level*18 AS level,
            TIMESTAMP_MILLIS(local_date_time) AS time
        FROM `activity_tracker_dataset.blood_glucose_initial_dump`
    )
UNION ALL
SELECT
    bloodGlucoseMilligramsPerDeciliter AS level,
    SAFE_CAST(physicalTime AS TIMESTAMP) AS time
FROM `activity_tracker_dataset.blood_glucose_delta`;

CREATE OR REPLACE EXTERNAL TABLE `activity_tracker_dataset.blood_glucose_initial_dump`(
    row_id INT64,
    uuid STRING,
    last_modified_time INT64,
    client_record_id STRING,
    client_record_version STRING,
    device_info_id INT64,
    app_info_id INT64,
    recording_method INT64,
    dedupe_hash STRING,
    time INT64,
    zone_offset INT64,
    local_date INT64,
    specimen_source STRING,
    level FLOAT64,
    relation_to_meal STRING,
    meal_type STRING,
    local_date_time INT64,
    device_data_provider_id INT64)
    OPTIONS (
        format = 'CSV',
        uris = ['gs://georgeruiz-activity-tracker/blood_glucose_record_table.csv']);

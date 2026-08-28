#!/bin/bash

set -euo pipefail

PROJECT_ID=georgeruiz-experimental

SQL_FILES=(
    "create-blood_glucose_initial_dump.sql"
    "create-blood_glucose_delta.sql"
    "create-blood_glucose.sql"
    "create-blood_glucose_daily_summary_pacific.sql"
    "create-meal_v1_raw.sql"
    "create-meal_v2_raw.sql"
    "create-meal_v3_raw.sql"
    "create-meal.sql"
    "create-meal_legacy.sql"
    "create-postprandial_glucose_3h.sql"
)

for SQL_FILE in "${SQL_FILES[@]}"
do
    echo "Deploying $SQL_FILE to project: ${PROJECT_ID}..."
    bq query \
        --use_legacy_sql=false \
        --project_id="${PROJECT_ID}" \
        < "${SQL_FILE}"
    echo "Done"
done

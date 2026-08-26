#!/bin/bash

set -euo pipefail

PROJECT_ID=georgeruiz-experimental

SQL_FILES=("create-blood_glucose_initial_dump.sql" "create-blood_glucose_delta.sql" "create-blood_glucose.sql")

for SQL_FILE in "${SQL_FILES[@]}"
do
    echo "Deploying $SQL_FILE to project: ${PROJECT_ID}..."
    bq query \
        --use_legacy_sql=false \
        --project_id="${PROJECT_ID}" \
        < "${SQL_FILE}"
    echo "Done"
done

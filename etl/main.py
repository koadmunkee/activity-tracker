import csv
import io
import json
import functions_framework
from google.cloud import bigquery, secretmanager, storage
from google.oauth2.credentials import Credentials
from googleapiclient.discovery import build
import google.auth
from datetime import timedelta
from typing import Any, Dict, List

SECRET_NAME = "googlehealth-oauth-credentials"
CLIENT_ID = "245829505646-0cqt84djp87ekhn0uebr0s6opdf6sofu.apps.googleusercontent.com"
DATASET_ID = "activity_tracker_dataset"
TABLE_ID = "blood_glucose_formatted"
BUCKET_NAME = "georgeruiz-activity-tracker"

def upload_to_gcs_csv(data: List[Dict[str, Any]], file_name) -> None:
    if not data:
        return

    csv_buffer = io.StringIO()
    fieldnames = ["physicalTime", "bloodGlucoseMilligramsPerDeciliter"]
    writer = csv.DictWriter(csv_buffer, fieldnames=fieldnames)

    for item in data:
        blood_glucose_info = item.get("bloodGlucose", {})

        # Extract nested values safely using dict.get()
        glucose_val = blood_glucose_info.get(
            "bloodGlucoseMilligramsPerDeciliter"
        )
        physical_time = blood_glucose_info.get("sampleTime", {}).get(
            "physicalTime"
        )

        if glucose_val is not None and physical_time:
            writer.writerow(
                {
                    "physicalTime": physical_time,
                    "bloodGlucoseMilligramsPerDeciliter": glucose_val,
                }
            )

    storage_client = storage.Client()
    bucket = storage_client.bucket(BUCKET_NAME)
    blob = bucket.blob(file_name)

    blob.upload_from_string(csv_buffer.getvalue(), content_type="text/csv")

def get_oauth_credentials() -> Credentials:
    sm_client = secretmanager.SecretManagerServiceClient()
    _, project_id = google.auth.default()
    name = f"projects/{project_id}/secrets/{SECRET_NAME}/versions/latest"

    response = sm_client.access_secret_version(request={"name": name})
    secret_payload = json.loads(response.payload.data.decode("UTF-8"))

    return Credentials(
        token=None,
        refresh_token=secret_payload["refresh_token"],
        client_secret=secret_payload["client_secret"],
        client_id=CLIENT_ID,
        token_uri="https://oauth2.googleapis.com/token",
        scopes=["https://www.googleapis.com/auth/googlehealth.health_metrics_and_measurements.readonly"]
    )

@functions_framework.http
def sync_blood_glucose(request):
    bq_client = bigquery.Client()
    query = f"SELECT MAX(time) AS most_recent FROM `{DATASET_ID}.{TABLE_ID}`"
    results = list(bq_client.query(query).result())
    most_recent = results[0].most_recent if results and results[0].most_recent else None

    try:
        credentials = get_oauth_credentials()
        health_service = build("health", "v4", credentials=credentials)
    except Exception as e:
        return {"status": "error", "message": f"Authentication failed: {str(e)}"}, 500

    try:
        # TODO(georgeruiz): handle most_recent = None
        data_points = []
        page_token = None
        earliest_sample_time = most_recent + timedelta(seconds=3)

        while True:
            response = health_service.users().dataTypes().dataPoints().list(
                parent="users/me/dataTypes/blood-glucose",
                filter=f'blood_glucose.sample_time.physical_time >= "{earliest_sample_time.isoformat()}"',
                pageToken = page_token
            ).execute()
            
            data_points.extend(response.get("dataPoints", []))

            page_token = response.get("nextPageToken")
            if not page_token:
                break

        upload_to_gcs_csv(data_points, f"{earliest_sample_time.isoformat()}.csv")
        
        return {
            "status": "success",
            "count": len(data_points)
        }, 200

    except Exception as e:
        return {"status": "error", "message": str(e)}, 500

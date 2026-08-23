import functions_framework
from google.cloud import bigquery
from googleapiclient.discovery import build
import google.auth

DATASET_ID = "activity_tracker_dataset"
TABLE_ID = "blood_glucose_formatted"

@functions_framework.http
def sync_blood_glucose(request):
    bq_client = bigquery.Client()
    query = f"SELECT MAX(time) AS most_recent FROM `{DATASET_ID}.{TABLE_ID}`"
    results = list(bq_client.query(query).result())
    most_recent = results[0].most_recent if results and results[0].most_recent else 0

    return f'Most recent data timestamp: {most_recent}'

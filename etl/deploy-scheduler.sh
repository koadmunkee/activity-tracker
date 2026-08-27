#!/bin/sh

set -eou pipefail

export PROJECT_ID=georgeruiz-experimental
export SERVICE_ACCOUNT="activity-tracker-etl-invoker@${PROJECT_ID}.iam.gserviceaccount.com"
export FUNCTION_NAME="sync_blood_glucose"
export REGION="us-west2"

gcloud functions add-invoker-policy-binding ${FUNCTION_NAME} \
  --region=${REGION} \
  --member="serviceAccount:${SERVICE_ACCOUNT}"

# Replace 'update' with 'create' for initial deployment
gcloud scheduler jobs update http activity-tracker-etl-trigger \
  --location="${REGION}" \
  --schedule="0 12 * * *" \
  --time-zone="UTC" \
  --uri="https://${REGION}-${PROJECT_ID}.cloudfunctions.net/${FUNCTION_NAME}" \
  --http-method=POST \
  --oidc-service-account-email="${SERVICE_ACCOUNT}" \
  --oidc-token-audience="https://${REGION}-${PROJECT_ID}.cloudfunctions.net/${FUNCTION_NAME}"

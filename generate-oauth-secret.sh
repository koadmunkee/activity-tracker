#!/bin/bash

set -euo pipefail

# Configuration
SCOPE="https://www.googleapis.com/auth/googlehealth.health_metrics_and_measurements.readonly"
REDIRECT_URI="urn:ietf:wg:oauth:2.0:oob"
TOKEN_ENDPOINT="https://oauth2.googleapis.com/token"
OUTPUT_FILE="googlehealth-oauth-credentials"
CLIENT_ID="245829505646-0cqt84djp87ekhn0uebr0s6opdf6sofu.apps.googleusercontent.com"

echo "=== Google Health Metrics OAuth 2.0 Setup ==="
echo ""

# 1. Prompt for User Credentials
read -rsp "Enter Client Secret: " CLIENT_SECRET
echo -e "\n"

# 2. Build and display Authorization URL
AUTH_URL="https://accounts.google.com/o/oauth2/v2/auth?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=${SCOPE}&access_type=offline&prompt=consent"

echo "Please visit the following URL in your browser to authorize access:"
echo "------------------------------------------------------------------"
echo "$AUTH_URL"
echo "------------------------------------------------------------------"
echo ""

# 3. Prompt for Authorization Code
read -rp "Enter the authorization code from the browser: " AUTH_CODE
echo ""

echo "Exchanging authorization code for tokens..."

# 4. Exchange Auth Code for Access and Refresh Tokens
RESPONSE=$(curl -s -X POST "$TOKEN_ENDPOINT" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "code=${AUTH_CODE}" \
  -d "grant_type=authorization_code" \
  -d "redirect_uri=${REDIRECT_URI}")

# Check if curl failed or returned an error from Google
if echo "$RESPONSE" | grep -q '"error"'; then
  echo "Error retrieving tokens:"
  echo "$RESPONSE" | jq .
  exit 1
fi

REFRESH_TOKEN=$(echo "$RESPONSE" | jq -r '.refresh_token // empty')

if [ -z "$REFRESH_TOKEN" ]; then
  echo "Error: No refresh token returned. Ensure you included 'access_type=offline' or re-consent access."
  echo "Response: $RESPONSE"
  exit 1
fi

# 5. Output JSON File containing client_secret and refresh_token
jq -n \
  --arg client_secret "$CLIENT_SECRET" \
  --arg refresh_token "$REFRESH_TOKEN" \
  '{
    client_secret: $client_secret,
    refresh_token: $refresh_token,
  }' > "$OUTPUT_FILE"

echo "Success! Credentials saved to $OUTPUT_FILE"

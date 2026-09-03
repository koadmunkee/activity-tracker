import os
import requests
from flask import redirect
from google.cloud import secretmanager
import functions_framework
import google.auth

CLIENT_ID = "245829505646-3as2mtft1uu7j4kr0h2f1gkp2vchslui.apps.googleusercontent.com"
CLIENT_SECRET_NAME_LATEST = f"projects/245829505646/secrets/oauth-client-secret/versions/latest"
REFRESH_TOKEN_SECRET_NAME = f"projects/245829505646/secrets/googlehealth-refresh-token"
SCOPE="https://www.googleapis.com/auth/googlehealth.health_metrics_and_measurements.readonly"

@functions_framework.http
def handle_oauth(request):
    # TODO(georgeruiz): Dynamically build redirect URI based on deployed URL
    #redirect_uri = request.base_url.split('/callback')[0] + "/callback"
    redirect_uri = "https://us-west2-georgeruiz-experimental.cloudfunctions.net/refresh_auth/callback"
    
    # Route 1: Handle the OAuth Callback
    if request.path == "/callback":
        code = request.args.get("code")
        if not code:
           return "Error: No authorization code provided in the URL.", 400
            
        # Exchange the authorization code for an access and refresh token
        token_url = "https://oauth2.googleapis.com/token"
        sm_client = secretmanager.SecretManagerServiceClient()
        client_secret = sm_client.access_secret_version(request={"name": CLIENT_SECRET_NAME_LATEST})
        data = {
            "client_id": CLIENT_ID,
            "client_secret": client_secret.payload.data.decode("UTF-8"),
            "code": code,
            "grant_type": "authorization_code",
            "redirect_uri": redirect_uri
        }
        
        resp = requests.post(token_url, data=data)
        if resp.status_code != 200:
            return f"Token exchange failed: {resp.text}", 400
            
        token_data = resp.json()
        refresh_token = token_data.get("refresh_token")
        
        if not refresh_token:
            return "No refresh token returned. (Note: Google only sends a refresh token on the *first* consent. Go to your Google Account permissions, revoke the app, and try again.)", 400
            
        # Update the secret in GCP Secret Manager
        try:
            payload = refresh_token.encode("UTF-8")
            sm_client.add_secret_version(parent=REFRESH_TOKEN_SECRET_NAME, payload={"data": payload})
            return f"Success! A new version of the secret '{REFRESH_TOKEN_SECRET_NAME}' was securely saved.", 200
        except Exception as e:
            return f"Error updating Secret Manager: {str(e)}", 500

    # Route 2: Initiate OAuth Flow (Base URL)
    else:
        # access_type=offline and prompt=consent guarantee a refresh_token is issued
        auth_url = (
            "https://accounts.google.com/o/oauth2/v2/auth?"
            f"client_id={CLIENT_ID}&"
            f"redirect_uri={redirect_uri}&"
            "response_type=code&"
            f"scope={SCOPE}&"
            "access_type=offline&"
            "prompt=consent"
        )
        return redirect(auth_url)

#!/bin/bash

gcloud functions deploy refresh_auth \
    --gen2 \
    --runtime=python311 \
    --region=us-west2 \
    --trigger-http \
    --entry-point=handle_oauth

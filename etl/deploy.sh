#!/bin/bash

gcloud functions deploy sync_blood_glucose \
    --gen2 \
    --runtime=python311 \
    --region=us-west2 \
    --trigger-http \
    --entry-point=sync_blood_glucose \
    --allow-unauthenticated

#!/bin/bash

gcloud functions deploy python-hello-world \
    --gen2 \
    --runtime=python311 \
    --trigger-http \
    --entry-point=hello_world \
    --allow-unauthenticated

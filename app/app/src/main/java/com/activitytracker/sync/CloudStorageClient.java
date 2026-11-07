package com.activitytracker.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * REST client for uploading files to Google Cloud Storage bucket
 * "georgeruiz-activity-tracker-meal-v4" authenticated via OAuth 2.0 (FR 1.3).
 */
public class CloudStorageClient {

    public static final String BUCKET_NAME = "georgeruiz-activity-tracker-meal-v4";
    public static final String GCS_UPLOAD_URL_PREFIX = "https://storage.googleapis.com/upload/storage/v1/b/"
            + BUCKET_NAME + "/o?uploadType=media&name=";

    public interface HttpTransport {
        HttpResponse executeUpload(String url, String bearerToken, byte[] data, String contentType) throws IOException;
    }

    public static class HttpResponse {
        public final int statusCode;
        public final String responseBody;

        public HttpResponse(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }

    public static class DefaultHttpTransport implements HttpTransport {
        @Override
        public HttpResponse executeUpload(String urlStr, String bearerToken, byte[] data, String contentType) throws IOException {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data);
                os.flush();
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            String body = "";
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    body = sb.toString();
                }
            }
            conn.disconnect();
            return new HttpResponse(code, body);
        }
    }

    private HttpTransport transport;

    public CloudStorageClient() {
        this.transport = new DefaultHttpTransport();
    }

    public CloudStorageClient(HttpTransport transport) {
        this.transport = transport != null ? transport : new DefaultHttpTransport();
    }

    public void setTransportForTesting(HttpTransport transport) {
        this.transport = transport != null ? transport : new DefaultHttpTransport();
    }

    /**
     * Uploads TSV meal data to the target Cloud Storage bucket.
     *
     * @param bearerToken OAuth 2.0 Bearer access token
     * @param objectName  Unique name of the destination object in the bucket (e.g. meals_20261028_120000.tsv)
     * @param tsvContent  Full TSV file content (13 columns, one meal per row)
     * @return true if upload succeeded (HTTP 2xx)
     * @throws IOException on network or HTTP error
     */
    public boolean uploadTsv(String bearerToken, String objectName, String tsvContent) throws IOException {
        if (bearerToken == null || bearerToken.trim().isEmpty()) {
            throw new IOException("Missing OAuth 2.0 Bearer access token");
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new IOException("Missing Cloud Storage object name");
        }
        if (tsvContent == null) {
            tsvContent = "";
        }

        String encodedName = URLEncoder.encode(objectName.trim(), "UTF-8");
        String url = GCS_UPLOAD_URL_PREFIX + encodedName;
        byte[] bytes = tsvContent.getBytes(StandardCharsets.UTF_8);

        HttpResponse response = transport.executeUpload(url, bearerToken.trim(), bytes, "text/tab-separated-values; charset=utf-8");
        if (!response.isSuccess()) {
            throw new IOException("Google Cloud Storage upload failed (HTTP " + response.statusCode + "): " + response.responseBody);
        }
        return true;
    }
}

package com.activitytracker.sync;

import android.os.Handler;
import android.os.Looper;

import com.activitytracker.model.FoodItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles one-way synchronization of the local food library with a master food library
 * from a Google Sheets tab (FR 1.2).
 */
public class FoodLibrarySynchronizer {

    public static final String DEFAULT_MASTER_URL =
            "https://docs.google.com/spreadsheets/d/1YshkLkE1drK7Fe7L6btxHcviXBf6GyTao1kWB2vBicM/edit?gid=242278362#gid=242278362";

    private final FoodSyncStore store;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public static class SyncResult {
        public final int addedCount;
        public final int updatedCount;
        public final int unchangedCount;
        public final int totalProcessed;

        public SyncResult(int addedCount, int updatedCount, int unchangedCount, int totalProcessed) {
            this.addedCount = addedCount;
            this.updatedCount = updatedCount;
            this.unchangedCount = unchangedCount;
            this.totalProcessed = totalProcessed;
        }

        @Override
        public String toString() {
            return String.format("SyncResult: %d added, %d updated, %d unchanged (Total: %d)",
                    addedCount, updatedCount, unchangedCount, totalProcessed);
        }
    }

    public interface SyncCallback {
        void onProgress(String message);
        void onSuccess(SyncResult result);
        void onError(String errorMessage);
    }

    public FoodLibrarySynchronizer(FoodSyncStore store) {
        this.store = store;
        this.executor = Executors.newSingleThreadExecutor();
        Looper looper = null;
        try {
            looper = Looper.getMainLooper();
        } catch (Throwable ignored) {}
        this.mainHandler = (looper != null) ? new Handler(looper) : null;
    }

    /**
     * Converts a Google Sheets URL (edit, view, or export) into a direct CSV export URL.
     */
    public static String convertToCsvExportUrl(String inputUrl) {
        if (inputUrl == null || inputUrl.trim().isEmpty()) {
            return "";
        }
        String url = inputUrl.trim();
        if (url.contains("/export?") || url.endsWith("/export")) {
            return url;
        }

        Pattern sheetPattern = Pattern.compile("https?://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)");
        Matcher sheetMatcher = sheetPattern.matcher(url);
        if (sheetMatcher.find()) {
            String docId = sheetMatcher.group(1);
            String gid = "0";
            Pattern gidPattern = Pattern.compile("[?#&]gid=([0-9]+)");
            Matcher gidMatcher = gidPattern.matcher(url);
            if (gidMatcher.find()) {
                gid = gidMatcher.group(1);
            }
            return "https://docs.google.com/spreadsheets/d/" + docId + "/export?format=csv&gid=" + gid;
        }

        return url;
    }

    /**
     * Parses master food library CSV content according to RFC 4180.
     * Column mappings (0-indexed):
     * Col B (1): food name
     * Col C (2): reference weight
     * Col D (3): calories
     * Col E (4): total carbohydrates
     * Col F (5): saturated fat
     * Col G (6): fat
     * Col H (7): protein
     * Col I (8): fiber
     * Col J (9): cholesterol
     * Col K (10): digestable carbohydrates
     * Col L (11): omega 3 fats
     * Col M (12): omega 6 fats
     * Col P (15): version number
     * Col Q (16): TRUE if archived, otherwise FALSE
     */
    public static List<FoodItem> parseMasterFoodCsv(Reader reader) throws IOException {
        List<FoodItem> foods = new ArrayList<>();
        List<List<String>> rows = parseCsvRows(reader);

        // Tab has 1 header row: skip index 0
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() < 2) {
                continue;
            }

            String name = getColumnValue(row, 1);
            if (name == null || name.trim().isEmpty()) {
                continue; // Skip empty food names
            }

            double refWeight = parseDouble(getColumnValue(row, 2), 100.0);
            if (refWeight <= 0) refWeight = 100.0;

            double calories = parseDouble(getColumnValue(row, 3), 0.0);
            double totalCarbs = parseDouble(getColumnValue(row, 4), 0.0);
            double satFat = parseDouble(getColumnValue(row, 5), 0.0);
            double fat = parseDouble(getColumnValue(row, 6), 0.0);
            double protein = parseDouble(getColumnValue(row, 7), 0.0);
            double fiber = parseDouble(getColumnValue(row, 8), 0.0);
            double cholesterol = parseDouble(getColumnValue(row, 9), 0.0);
            double digCarbs = parseDouble(getColumnValue(row, 10), Math.max(0.0, totalCarbs - fiber));
            double omega3 = parseDouble(getColumnValue(row, 11), 0.0);
            double omega6 = parseDouble(getColumnValue(row, 12), 0.0);

            int version = parseInt(getColumnValue(row, 15), 1);
            boolean isArchived = parseBoolean(getColumnValue(row, 16));

            FoodItem item = new FoodItem(
                    0,
                    name.trim(),
                    refWeight,
                    calories,
                    digCarbs,
                    satFat,
                    fat,
                    protein,
                    fiber,
                    cholesterol,
                    totalCarbs,
                    omega3,
                    omega6,
                    isArchived,
                    version
            );
            foods.add(item);
        }

        return foods;
    }

    public static List<FoodItem> parseMasterFoodCsvString(String csvContent) throws IOException {
        return parseMasterFoodCsv(new StringReader(csvContent));
    }

    /**
     * Performs one-way synchronization:
     * - Matches food by name (case-insensitive via store)
     * - If not found: insert into local database
     * - If found with lower version: update reference weight, nutrients, version, and archived status
     * - If found with equal or higher version: leave unchanged
     */
    public SyncResult sync(List<FoodItem> masterFoods) {
        int added = 0;
        int updated = 0;
        int unchanged = 0;

        for (FoodItem masterFood : masterFoods) {
            String name = masterFood.getName();
            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            FoodItem existing = store.getFoodByName(name);
            if (existing == null) {
                store.insertFood(masterFood);
                added++;
            } else {
                if (masterFood.getVersion() > existing.getVersion()) {
                    existing.setReferenceWeight(masterFood.getReferenceWeight());
                    existing.setCalories(masterFood.getCalories());
                    existing.setDigestableCarbs(masterFood.getDigestableCarbs());
                    existing.setSaturatedFat(masterFood.getSaturatedFat());
                    existing.setFat(masterFood.getFat());
                    existing.setProtein(masterFood.getProtein());
                    existing.setFiber(masterFood.getFiber());
                    existing.setCholesterol(masterFood.getCholesterol());
                    existing.setCarbohydrates(masterFood.getCarbohydrates());
                    existing.setOmega3(masterFood.getOmega3());
                    existing.setOmega6(masterFood.getOmega6());
                    existing.setVersion(masterFood.getVersion());
                    existing.setArchived(masterFood.isArchived());
                    store.updateFood(existing);
                    updated++;
                } else {
                    unchanged++;
                }
            }
        }

        return new SyncResult(added, updated, unchanged, added + updated + unchanged);
    }

    /**
     * Synchronously downloads CSV from the specified URL and executes sync.
     */
    public SyncResult syncFromUrl(String rawUrl) throws IOException {
        String csvUrl = convertToCsvExportUrl(rawUrl);
        String csvContent = downloadCsv(csvUrl);
        List<FoodItem> masterFoods = parseMasterFoodCsvString(csvContent);
        return sync(masterFoods);
    }

    /**
     * Asynchronously downloads CSV from URL and performs synchronization.
     */
    public void syncFromUrlAsync(final String rawUrl, final SyncCallback callback) {
        executor.execute(() -> {
            try {
                postProgress(callback, "Connecting to master food library…");
                String csvUrl = convertToCsvExportUrl(rawUrl);
                postProgress(callback, "Downloading master food catalog…");
                String csvContent = downloadCsv(csvUrl);

                postProgress(callback, "Parsing food data…");
                List<FoodItem> masterFoods = parseMasterFoodCsvString(csvContent);

                postProgress(callback, "Synchronizing " + masterFoods.size() + " foods…");
                SyncResult result = sync(masterFoods);

                postSuccess(callback, result);
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Unknown sync error");
            }
        });
    }

    private String downloadCsv(String urlString) throws IOException {
        int redirects = 0;
        String currentUrl = urlString;

        while (redirects < 5) {
            URL url = java.net.URI.create(currentUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", "ActivityTracker-Android");

            int responseCode = conn.getResponseCode();

            // Detect redirection to Google login
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER
                    || responseCode == 307) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (location == null) {
                    throw new IOException("Server returned redirect with no Location header.");
                }
                if (location.contains("accounts.google.com") || location.contains("ServiceLogin")) {
                    throw new IOException("The master Google Sheet is private or requires sign-in. " +
                            "Please check sharing permissions (set to 'Anyone with the link can view').");
                }
                currentUrl = location;
                redirects++;
                continue;
            }

            if (responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                conn.disconnect();
                throw new IOException("Access denied (HTTP " + responseCode + "). The Google Sheet may be private.");
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                conn.disconnect();
                throw new IOException("Server returned HTTP " + responseCode);
            }

            String contentType = conn.getContentType();
            if (contentType != null && (contentType.contains("text/html") || contentType.contains("application/xhtml"))) {
                conn.disconnect();
                throw new IOException("The master Google Sheet is private or requires sign-in. Received HTML login page instead of CSV.");
            }

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int read;
            while ((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
            reader.close();
            conn.disconnect();

            String content = sb.toString();
            String trimmedPrefix = content.length() > 200 ? content.substring(0, 200).trim().toLowerCase() : content.trim().toLowerCase();
            if (trimmedPrefix.startsWith("<!doctype html") || trimmedPrefix.startsWith("<html")) {
                throw new IOException("The master Google Sheet is private or requires sign-in. Received HTML instead of CSV data.");
            }

            return content;
        }

        throw new IOException("Too many redirects attempting to download master sheet.");
    }

    /**
     * RFC 4180 compliant CSV parser.
     */
    public static List<List<String>> parseCsvRows(Reader reader) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);

        List<String> currentRow = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        int ch;

        while ((ch = br.read()) != -1) {
            char c = (char) ch;

            if (inQuotes) {
                if (c == '"') {
                    // Peek next character for escaped quote ("")
                    br.mark(1);
                    int next = br.read();
                    if (next == '"') {
                        currentField.append('"');
                    } else {
                        inQuotes = false;
                        if (next != -1) {
                            br.reset();
                        }
                    }
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    currentRow.add(currentField.toString().trim());
                    currentField.setLength(0);
                } else if (c == '\r') {
                    // Carriage return: check if next is '\n'
                    br.mark(1);
                    int next = br.read();
                    if (next != '\n' && next != -1) {
                        br.reset();
                    }
                    currentRow.add(currentField.toString().trim());
                    currentField.setLength(0);
                    if (!isRowEmpty(currentRow)) {
                        rows.add(currentRow);
                    }
                    currentRow = new ArrayList<>();
                } else if (c == '\n') {
                    currentRow.add(currentField.toString().trim());
                    currentField.setLength(0);
                    if (!isRowEmpty(currentRow)) {
                        rows.add(currentRow);
                    }
                    currentRow = new ArrayList<>();
                } else {
                    currentField.append(c);
                }
            }
        }

        // Add trailing field and row if any
        if (currentField.length() > 0 || !currentRow.isEmpty()) {
            currentRow.add(currentField.toString().trim());
            if (!isRowEmpty(currentRow)) {
                rows.add(currentRow);
            }
        }

        return rows;
    }

    private static boolean isRowEmpty(List<String> row) {
        if (row == null || row.isEmpty()) return true;
        for (String col : row) {
            if (!col.trim().isEmpty()) return false;
        }
        return true;
    }

    private static String getColumnValue(List<String> row, int index) {
        if (row != null && index >= 0 && index < row.size()) {
            return row.get(index);
        }
        return "";
    }

    private static double parseDouble(String val, double defaultValue) {
        if (val == null) return defaultValue;
        String cleaned = val.replace(",", "").trim();
        if (cleaned.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int parseInt(String val, int defaultValue) {
        if (val == null) return defaultValue;
        String cleaned = val.replace(",", "").trim();
        if (cleaned.isEmpty()) return defaultValue;
        try {
            // Handle decimal versions like "2.0"
            if (cleaned.contains(".")) {
                return (int) Double.parseDouble(cleaned);
            }
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean parseBoolean(String val) {
        if (val == null) return false;
        return "TRUE".equalsIgnoreCase(val.trim());
    }

    private void postProgress(SyncCallback callback, String msg) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onProgress(msg));
        } else {
            callback.onProgress(msg);
        }
    }

    private void postSuccess(SyncCallback callback, SyncResult result) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        } else {
            callback.onSuccess(result);
        }
    }

    private void postError(SyncCallback callback, String error) {
        if (callback == null) return;
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onError(error));
        } else {
            callback.onError(error);
        }
    }
}

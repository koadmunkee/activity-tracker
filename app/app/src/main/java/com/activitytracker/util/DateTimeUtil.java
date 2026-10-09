package com.activitytracker.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Date and time formatting helpers for meals and history.
 */
public class DateTimeUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "EEE, MMM d, yyyy";
    private static final String TIME_FORMAT = "h:mm a";

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return sdf.format(new Date());
    }

    public static String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.US);
        return sdf.format(new Date());
    }

    public static String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            Date date = srcFormat.parse(dateStr);
            if (date != null) {
                SimpleDateFormat destFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.US);
                return destFormat.format(date);
            }
        } catch (ParseException ignored) {
        }
        return dateStr;
    }

    public static Calendar parseDate(String dateStr) {
        Calendar cal = Calendar.getInstance();
        if (dateStr == null || dateStr.trim().isEmpty()) return cal;
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            Date date = srcFormat.parse(dateStr);
            if (date != null) {
                cal.setTime(date);
            }
        } catch (ParseException ignored) {
        }
        return cal;
    }

    public static String formatDate(int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return sdf.format(cal.getTime());
    }

    public static String formatTime(int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.US);
        return sdf.format(cal.getTime());
    }
}

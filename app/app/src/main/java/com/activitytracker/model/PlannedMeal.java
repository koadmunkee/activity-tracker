package com.activitytracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a planned or committed meal of the day.
 */
public class PlannedMeal implements Serializable {
    private long id;
    private String date; // YYYY-MM-DD, set when committed
    private String mealName; // Custom name defined in config, e.g. "Breakfast"
    private int mealOrder; // Order in the day (0, 1, 2, ...)
    private String startTime; // "HH:mm" or "hh:mm a"
    private double insulinDose; // Insulin amount in units
    private String insulinTime; // "HH:mm" or "hh:mm a"
    private boolean isCommitted;
    private boolean isAdHoc; // Special ad hoc meal (FR 2.9)
    private List<MealItem> items = new ArrayList<>();

    public PlannedMeal() {
        this.startTime = "";
        this.insulinTime = "";
        this.insulinDose = 0.0;
        this.isCommitted = false;
        this.isAdHoc = false;
        this.items = new ArrayList<>();
    }

    public PlannedMeal(long id, String date, String mealName, int mealOrder,
                       String startTime, double insulinDose, String insulinTime,
                       boolean isCommitted) {
        this(id, date, mealName, mealOrder, startTime, insulinDose, insulinTime, isCommitted, false);
    }

    public PlannedMeal(long id, String date, String mealName, int mealOrder,
                       String startTime, double insulinDose, String insulinTime,
                       boolean isCommitted, boolean isAdHoc) {
        this.id = id;
        this.date = date;
        this.mealName = mealName != null ? mealName : "";
        this.mealOrder = mealOrder;
        this.startTime = startTime != null ? startTime : "";
        this.insulinDose = insulinDose;
        this.insulinTime = insulinTime != null ? insulinTime : "";
        this.isCommitted = isCommitted;
        this.isAdHoc = isAdHoc;
        this.items = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }

    public int getMealOrder() { return mealOrder; }
    public void setMealOrder(int mealOrder) { this.mealOrder = mealOrder; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime != null ? startTime : ""; }

    public double getInsulinDose() { return insulinDose; }
    public void setInsulinDose(double insulinDose) { this.insulinDose = Math.max(0, insulinDose); }

    public String getInsulinTime() { return insulinTime; }
    public void setInsulinTime(String insulinTime) { this.insulinTime = insulinTime != null ? insulinTime : ""; }

    public boolean isCommitted() { return isCommitted; }
    public void setCommitted(boolean committed) { isCommitted = committed; }

    public boolean isAdHoc() { return isAdHoc; }
    public void setAdHoc(boolean adHoc) { isAdHoc = adHoc; }

    public List<MealItem> getItems() { return items; }
    public void setItems(List<MealItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(MealItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
        }
    }

    /**
     * Aggregates primary and secondary nutrients for this meal.
     */
    public NutrientSummary getNutrientSummary() {
        NutrientSummary total = NutrientSummary.empty();
        for (MealItem item : items) {
            total = total.plus(item.computeNutrients());
        }
        return total;
    }

    /**
     * Checks if this meal contains invalid food rows (FR 2.8).
     * If there are food rows, none can have unrecognized food or undefined weight.
     */
    public boolean hasInvalidItems() {
        for (MealItem item : items) {
            // Unrecognized food or undefined weight (<= 0)
            if (!item.isValid()) {
                return true;
            }
        }
        return false;
    }
}

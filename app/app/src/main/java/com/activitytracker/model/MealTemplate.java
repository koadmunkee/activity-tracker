package com.activitytracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reusable meal template consisting of food & weight pairs and insulin dosage.
 */
public class MealTemplate implements Serializable {
    private long id;
    private String templateName; // Must be unique (FR 3.1)
    private double insulinDose;
    private List<TemplateItem> items = new ArrayList<>();

    public MealTemplate() {
        this.templateName = "";
        this.insulinDose = 0.0;
        this.items = new ArrayList<>();
    }

    public MealTemplate(long id, String templateName, double insulinDose) {
        this.id = id;
        this.templateName = templateName != null ? templateName.trim() : "";
        this.insulinDose = insulinDose;
        this.items = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) {
        this.templateName = templateName != null ? templateName.trim() : "";
    }

    public double getInsulinDose() { return insulinDose; }
    public void setInsulinDose(double insulinDose) {
        this.insulinDose = Math.max(0, insulinDose);
    }

    public List<TemplateItem> getItems() { return items; }
    public void setItems(List<TemplateItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(TemplateItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
        }
    }

    public NutrientSummary getNutrientSummary() {
        NutrientSummary total = NutrientSummary.empty();
        for (TemplateItem item : items) {
            total = total.plus(item.computeNutrients());
        }
        return total;
    }

    /**
     * Checks if this template contains invalid items (FR 3.2).
     */
    public boolean hasInvalidItems() {
        if (items.isEmpty()) return true;
        for (TemplateItem item : items) {
            if (!item.isValid()) {
                return true;
            }
        }
        return false;
    }
}

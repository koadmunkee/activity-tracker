package com.activitytracker.sync;

import com.activitytracker.model.PlannedMeal;
import java.util.List;

/**
 * Persistence abstraction for meal cloud sync (FR 1.3).
 * Enables mocking in unit tests.
 */
public interface CloudSyncStore {
    List<PlannedMeal> getUnuploadedCommittedMeals();
    void markMealsAsUploaded(List<Long> mealIds);
    boolean isCloudSyncEnabled();
    void setCloudSyncEnabled(boolean enabled);
    String getCloudAccountName();
    void setCloudAccountName(String accountName);
    long getLastSyncTimestamp();
    void setLastSyncTimestamp(long timestamp);
    String getLastSyncStatus();
    void setLastSyncStatus(String status);
}

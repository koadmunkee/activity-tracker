package com.activitytracker.sync;

import com.activitytracker.model.FoodItem;

/**
 * Interface defining the persistence operations required for food library synchronization.
 * Implemented by FoodRepository for SQLite, and can be mocked for unit testing.
 */
public interface FoodSyncStore {
    FoodItem getFoodByName(String name);
    long insertFood(FoodItem food);
    boolean updateFood(FoodItem food);
}

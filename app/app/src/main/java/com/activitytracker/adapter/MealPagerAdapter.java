package com.activitytracker.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.activitytracker.ui.PlanningFragment;

import java.util.List;

public class MealPagerAdapter extends FragmentStateAdapter {

    private List<String> mealNames;

    public MealPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> mealNames) {
        super(fragmentActivity);
        this.mealNames = mealNames;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PlanningFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return mealNames.size();
    }
}

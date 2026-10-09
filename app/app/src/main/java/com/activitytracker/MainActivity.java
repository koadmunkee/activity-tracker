package com.activitytracker;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.activitytracker.model.MealTemplate;
import com.activitytracker.ui.config.ConfigFragment;
import com.activitytracker.ui.foods.FoodsFragment;
import com.activitytracker.ui.history.HistoryFragment;
import com.activitytracker.ui.planning.PlanningFragment;
import com.activitytracker.ui.templates.TemplatesFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main Activity hosting the application views, bottom navigation,
 * EdgeToEdge system bars handling, and inter-tab transitions.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;
    private PlanningFragment planningFragment;
    private HistoryFragment historyFragment;
    private TemplatesFragment templatesFragment;
    private FoodsFragment foodsFragment;
    private ConfigFragment configFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Edge to Edge
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);

        // System Bars / Window Insets handling (EdgeToEdge & adjustResize support)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Apply insets padding to prevent overlapping with status and navigation bars or IME
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            bottomNav.setPadding(0, 0, 0, bottomPadding);

            return WindowInsetsCompat.CONSUMED;
        });

        // Initialize fragments
        planningFragment = new PlanningFragment();
        historyFragment = new HistoryFragment();
        templatesFragment = new TemplatesFragment();
        foodsFragment = new FoodsFragment();
        configFragment = new ConfigFragment();

        setupBottomNavigation();

        // Default screen: Meal Planning View (as specified in User Flows)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_planning);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment;
            String title;

            if (itemId == R.id.nav_planning) {
                selectedFragment = planningFragment;
                title = getString(R.string.title_planning);
            } else if (itemId == R.id.nav_history) {
                selectedFragment = historyFragment;
                title = getString(R.string.title_history);
            } else if (itemId == R.id.nav_templates) {
                selectedFragment = templatesFragment;
                title = getString(R.string.title_templates);
            } else if (itemId == R.id.nav_foods) {
                selectedFragment = foodsFragment;
                title = getString(R.string.title_foods);
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = configFragment;
                title = getString(R.string.title_config);
            } else {
                return false;
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, selectedFragment);
            ft.commit();

            return true;
        });
    }

    /**
     * Navigates to Planning tab (e.g. after adjusting a committed day).
     */
    public void switchToPlanningTab() {
        bottomNav.setSelectedItemId(R.id.nav_planning);
    }

    /**
     * Applies a template and opens the Planning tab.
     */
    public void applyTemplateAndOpenPlanning(MealTemplate template) {
        bottomNav.setSelectedItemId(R.id.nav_planning);
        if (planningFragment != null && planningFragment.isAdded()) {
            planningFragment.applyTemplateToActiveMeal(template);
        }
    }
}

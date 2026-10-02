package com.activitytracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.activitytracker.R;
import com.activitytracker.adapter.MealPagerAdapter;
import com.activitytracker.database.MockDatabase;
import com.activitytracker.model.Meal;
import com.activitytracker.model.NutrientAggregation;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView textDayPrimary, textDaySecondary;
    private MealPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        textDayPrimary = findViewById(R.id.textDayPrimaryNutrients);
        textDaySecondary = findViewById(R.id.textDaySecondaryNutrients);

        Button btnCommitDay = findViewById(R.id.btnCommitDay);
        btnCommitDay.setOnClickListener(v -> showCommitDialog());

        setupViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViewPager();
        updateDayAggregationUI();
    }

    private void setupViewPager() {
        List<String> mealNames = MockDatabase.getInstance().getConfiguredMealNames();
        adapter = new MealPagerAdapter(this, mealNames);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(mealNames.get(position))
        ).attach();
    }

    public void updateDayAggregationUI() {
        NutrientAggregation dayTotal = new NutrientAggregation();
        for (Meal meal : MockDatabase.getInstance().getActiveUncommittedMeals()) {
            dayTotal.add(meal.getAggregatedNutrients());
        }

        textDayPrimary.setText(String.format(Locale.US,
                "Cal: %.0f kcal | Dig.Carbs: %.1fg | Sat.Fat: %.1fg | Fat: %.1fg | Pro: %.1fg",
                dayTotal.calories, dayTotal.digestibleCarbs, dayTotal.saturatedFat, dayTotal.totalFat, dayTotal.protein));

        textDaySecondary.setText(String.format(Locale.US,
                "Fiber: %.1fg | Chol: %.0fmg | Total Carbs: %.1fg | Omega-3: %.2fg | Omega-6: %.2fg",
                dayTotal.fiber, dayTotal.cholesterolMg, dayTotal.totalCarbs, dayTotal.omega3g, dayTotal.omega6g));
    }

    private void showCommitDialog() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        final EditText inputDate = new EditText(this);
        inputDate.setText(today);

        new AlertDialog.Builder(this)
                .setTitle("Commit Day's Meals")
                .setMessage("Associate date with current meals:")
                .setView(inputDate)
                .setPositiveButton("Commit", (dialog, which) -> {
                    String dateStr = inputDate.getText().toString();
                    MockDatabase.getInstance().commitAllActiveMeals(dateStr);
                    Toast.makeText(this, "Meals committed successfully!", Toast.LENGTH_SHORT).show();
                    setupViewPager();
                    updateDayAggregationUI();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_configuration) {
            startActivity(new Intent(this, ConfigurationActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

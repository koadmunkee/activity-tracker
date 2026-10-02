package com.activitytracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.adapter.FoodPairAdapter;
import com.activitytracker.database.MockDatabase;
import com.activitytracker.model.FoodWeightPair;
import com.activitytracker.model.MealTemplate;
import com.activitytracker.model.NutrientAggregation;

import java.util.List;
import java.util.Locale;

public class TemplateEditorActivity extends AppCompatActivity implements FoodPairAdapter.OnDataChangeListener {

    private EditText editTemplateName;
    private TextView textPrimary, textSecondary;
    private MealTemplate template;
    private FoodPairAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_template_editor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.template_editor_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTemplateName = findViewById(R.id.editTemplateName);
        textPrimary = findViewById(R.id.textTemplatePrimaryNutrients);
        textSecondary = findViewById(R.id.textTemplateSecondaryNutrients);

        String templateId = getIntent().getStringExtra("TEMPLATE_ID");
        if (templateId != null) {
            for (MealTemplate t : MockDatabase.getInstance().getTemplates()) {
                if (t.getId().equals(templateId)) {
                    template = t;
                    break;
                }
            }
        }

        if (template == null) {
            template = new MealTemplate("tpl_" + System.currentTimeMillis(), "");
        } else {
            editTemplateName.setText(template.getTemplateName());
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerTemplatePairs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodPairAdapter(template.getItems(), MockDatabase.getInstance().getFoodList(), this);
        recyclerView.setAdapter(adapter);

        Button btnAddRow = findViewById(R.id.btnAddTemplateFoodRow);
        btnAddRow.setOnClickListener(v -> {
            template.getItems().add(new FoodWeightPair(null, 0));
            adapter.notifyItemInserted(template.getItems().size() - 1);
            onDataChanged();
        });

        Button btnSave = findViewById(R.id.btnSaveTemplate);
        btnSave.setOnClickListener(v -> saveTemplate());

        updateNutrientUI();
    }

    private void saveTemplate() {
        String name = editTemplateName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a template name.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (MealTemplate t : MockDatabase.getInstance().getTemplates()) {
            if (t.getTemplateName().equalsIgnoreCase(name) && !t.getId().equals(template.getId())) {
                Toast.makeText(this, "A template with this name already exists.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        template.setTemplateName(name);
        if (!MockDatabase.getInstance().getTemplates().contains(template)) {
            MockDatabase.getInstance().getTemplates().add(template);
        }

        Toast.makeText(this, "Template saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onDataChanged() {
        updateNutrientUI();
    }

    private void updateNutrientUI() {
        NutrientAggregation agg = template.getAggregatedNutrients();
        textPrimary.setText(String.format(Locale.US,
                "Cal: %.0f kcal | Dig.Carbs: %.1fg | Sat.Fat: %.1fg | Fat: %.1fg | Pro: %.1fg",
                agg.calories, agg.digestibleCarbs, agg.saturatedFat, agg.totalFat, agg.protein));

        textSecondary.setText(String.format(Locale.US,
                "Fiber: %.1fg | Chol: %.0fmg | Total Carbs: %.1fg | Omega-3: %.2fg | Omega-6: %.2fg",
                agg.fiber, agg.cholesterolMg, agg.totalCarbs, agg.omega3g, agg.omega6g));
    }
}

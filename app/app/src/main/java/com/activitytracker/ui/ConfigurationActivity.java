package com.activitytracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.adapter.TemplateAdapter;
import com.activitytracker.database.MockDatabase;
import com.activitytracker.model.MealTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigurationActivity extends AppCompatActivity implements TemplateAdapter.TemplateActionListener {

    private EditText editMealNames;
    private TemplateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.config_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editMealNames = findViewById(R.id.editMealNamesConfig);
        List<String> currentConfig = MockDatabase.getInstance().getConfiguredMealNames();
        editMealNames.setText(android.text.TextUtils.join(", ", currentConfig));

        Button btnSaveConfig = findViewById(R.id.btnSaveConfig);
        btnSaveConfig.setOnClickListener(v -> saveConfiguration());

        Button btnCreateTemplate = findViewById(R.id.btnCreateTemplate);
        btnCreateTemplate.setOnClickListener(v -> {
            Intent intent = new Intent(this, TemplateEditorActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerTemplates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TemplateAdapter(MockDatabase.getInstance().getTemplates(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void saveConfiguration() {
        String raw = editMealNames.getText().toString();
        String[] parts = raw.split(",");
        List<String> newNames = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                newNames.add(trimmed);
            }
        }
        if (newNames.isEmpty()) {
            Toast.makeText(this, "Please enter at least one meal name.", Toast.LENGTH_SHORT).show();
            return;
        }
        MockDatabase.getInstance().setConfiguredMealNames(newNames);
        MockDatabase.getInstance().reloadUncommittedMeals();
        Toast.makeText(this, "Configuration Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onEdit(MealTemplate template) {
        Intent intent = new Intent(this, TemplateEditorActivity.class);
        intent.putExtra("TEMPLATE_ID", template.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(MealTemplate template) {
        MockDatabase.getInstance().getTemplates().remove(template);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Template deleted", Toast.LENGTH_SHORT).show();
    }
}

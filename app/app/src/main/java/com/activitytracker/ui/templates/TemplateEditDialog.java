package com.activitytracker.ui.templates;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.db.FoodRepository;
import com.activitytracker.db.TemplateRepository;
import com.activitytracker.model.FoodItem;
import com.activitytracker.model.MealTemplate;
import com.activitytracker.model.NutrientSummary;
import com.activitytracker.model.TemplateItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dialog for creating and editing meal templates (FR 3.1, 3.2).
 */
public class TemplateEditDialog extends Dialog implements TemplateFoodRowAdapter.OnTemplateItemChangeListener {

    public interface OnTemplateSavedListener {
        void onTemplateSaved();
    }

    private final MealTemplate template;
    private final boolean isEditMode;
    private final OnTemplateSavedListener saveListener;

    private final TemplateRepository templateRepository;
    private final FoodRepository foodRepository;
    private List<FoodItem> availableFoods;

    private TextInputEditText etTemplateName;
    private TextInputEditText etTemplateInsulin;
    private TextView textPrimaryAgg;
    private TextView textSecondaryAgg;
    private TextView textError;
    private RecyclerView recyclerFoods;
    private TemplateFoodRowAdapter adapter;

    public TemplateEditDialog(@NonNull Context context, MealTemplate template, OnTemplateSavedListener saveListener) {
        super(context);
        this.templateRepository = new TemplateRepository(context);
        this.foodRepository = new FoodRepository(context);
        this.saveListener = saveListener;
        this.isEditMode = (template != null);

        if (template != null) {
            // Copy template for editing
            this.template = new MealTemplate(template.getId(), template.getTemplateName(), template.getInsulinDose());
            List<TemplateItem> copyItems = new ArrayList<>();
            for (TemplateItem item : template.getItems()) {
                TemplateItem ci = new TemplateItem(item.getId(), item.getTemplateId(), item.getFoodId(), item.getFoodName(), item.getWeight());
                ci.setFoodItem(item.getFoodItem());
                copyItems.add(ci);
            }
            this.template.setItems(copyItems);
        } else {
            this.template = new MealTemplate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_template);
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        availableFoods = foodRepository.getUnarchivedFoods();

        TextView textTitle = findViewById(R.id.text_dialog_template_title);
        textTitle.setText(isEditMode ? R.string.edit_template : R.string.create_template);

        etTemplateName = findViewById(R.id.et_template_name);
        etTemplateInsulin = findViewById(R.id.et_template_insulin);
        textPrimaryAgg = findViewById(R.id.text_tpl_dialog_primary);
        textSecondaryAgg = findViewById(R.id.text_tpl_dialog_secondary);
        textError = findViewById(R.id.text_tpl_error);

        MaterialButton btnAddRow = findViewById(R.id.btn_tpl_add_food_row);
        MaterialButton btnCancel = findViewById(R.id.btn_tpl_cancel);
        MaterialButton btnSave = findViewById(R.id.btn_tpl_save);

        recyclerFoods = findViewById(R.id.recycler_tpl_foods);
        recyclerFoods.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TemplateFoodRowAdapter(getContext(), template.getItems(), availableFoods, foodRepository, this);
        recyclerFoods.setAdapter(adapter);

        etTemplateName.setText(template.getTemplateName());
        if (template.getInsulinDose() > 0) {
            etTemplateInsulin.setText(String.format(Locale.US, "%.1f", template.getInsulinDose()));
        } else {
            etTemplateInsulin.setText("");
        }

        btnAddRow.setOnClickListener(v -> {
            TemplateItem newItem = new TemplateItem();
            template.addItem(newItem);
            adapter.notifyItemInserted(template.getItems().size() - 1);
            updateAggregations();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> handleSave());

        updateAggregations();
    }

    private void updateAggregations() {
        NutrientSummary s = template.getNutrientSummary();
        textPrimaryAgg.setText(String.format(Locale.US,
                "%.0f kcal | C: %.1fg | P: %.1fg | F: %.1fg (Sat: %.1fg)",
                s.getCalories(), s.getDigestableCarbs(), s.getProtein(), s.getFat(), s.getSaturatedFat()));

        textSecondaryAgg.setText(String.format(Locale.US,
                "Fiber: %s • Chol: %s • Carbs: %s • Ω3: %s • Ω6: %s",
                s.getFormattedFiber(), s.getFormattedCholesterol(), s.getFormattedCarbohydrates(),
                s.getFormattedOmega3(), s.getFormattedOmega6()));
    }

    @Override
    public void onTemplateItemChanged() {
        updateAggregations();
    }

    @Override
    public void onTemplateItemDeleted(int position) {
        if (position >= 0 && position < template.getItems().size()) {
            template.removeItem(position);
            adapter.notifyItemRemoved(position);
            updateAggregations();
        }
    }

    private void handleSave() {
        String name = etTemplateName.getText() != null ? etTemplateName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            showError("Please specify a template name.");
            return;
        }

        // FR 3.1: Unique template name
        if (!templateRepository.isTemplateNameUnique(name, template.getId())) {
            showError("A template with this name already exists. Template name must be unique.");
            return;
        }

        String insulinText = etTemplateInsulin.getText() != null ? etTemplateInsulin.getText().toString().trim() : "";
        double insulin = 0.0;
        if (!insulinText.isEmpty()) {
            try {
                insulin = Double.parseDouble(insulinText);
            } catch (NumberFormatException ignored) {}
        }
        template.setTemplateName(name);
        template.setInsulinDose(insulin);

        // FR 3.2: "I should not be allowed to submit a meal template that has unrecognized food or undefined weight."
        if (template.getItems().isEmpty()) {
            showError("Template must have at least one food & weight pair.");
            return;
        }

        StringBuilder valErrors = new StringBuilder();
        for (int i = 0; i < template.getItems().size(); i++) {
            TemplateItem item = template.getItems().get(i);
            if (item.getFoodItem() == null || item.getFoodItem().getId() <= 0) {
                valErrors.append("• Row ").append(i + 1).append(" has unrecognized food (\"")
                        .append(item.getFoodName()).append("\").\n");
            }
            if (item.getWeight() <= 0) {
                valErrors.append("• Row ").append(i + 1).append(" has undefined or zero weight.\n");
            }
        }

        if (valErrors.length() > 0) {
            showError("Cannot submit template:\n" + valErrors.toString());
            return;
        }

        templateRepository.saveOrUpdateTemplate(template);
        if (saveListener != null) {
            saveListener.onTemplateSaved();
        }
        dismiss();
    }

    private void showError(String msg) {
        textError.setVisibility(View.VISIBLE);
        textError.setText(msg);
    }
}

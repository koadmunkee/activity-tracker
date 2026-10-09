package com.activitytracker.ui.templates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.model.MealTemplate;
import com.activitytracker.model.NutrientSummary;
import com.activitytracker.model.TemplateItem;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying meal templates in the Templates tab.
 */
public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

    public interface OnTemplateActionListener {
        void onEditTemplate(MealTemplate template);
        void onDeleteTemplate(MealTemplate template);
        void onApplyTemplate(MealTemplate template);
    }

    private final Context context;
    private final List<MealTemplate> templates;
    private final OnTemplateActionListener listener;

    public TemplateAdapter(Context context, List<MealTemplate> templates, OnTemplateActionListener listener) {
        this.context = context;
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        holder.bind(templates.get(position));
    }

    @Override
    public int getItemCount() {
        return templates != null ? templates.size() : 0;
    }

    class TemplateViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textInsulin;
        private final TextView textPrimary;
        private final TextView textSecondary;
        private final TextView textItems;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;
        private final MaterialButton btnApply;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_template_name);
            textInsulin = itemView.findViewById(R.id.text_template_insulin);
            textPrimary = itemView.findViewById(R.id.text_template_primary_nutrients);
            textSecondary = itemView.findViewById(R.id.text_template_secondary_nutrients);
            textItems = itemView.findViewById(R.id.text_template_items_summary);
            btnEdit = itemView.findViewById(R.id.btn_edit_template);
            btnDelete = itemView.findViewById(R.id.btn_delete_template);
            btnApply = itemView.findViewById(R.id.btn_apply_template);
        }

        public void bind(MealTemplate template) {
            textName.setText(template.getTemplateName());
            textInsulin.setText(String.format(Locale.US, "Insulin: %.1f units", template.getInsulinDose()));

            NutrientSummary s = template.getNutrientSummary();
            textPrimary.setText(String.format(Locale.US,
                    "%.0f kcal | C: %.1fg | P: %.1fg | F: %.1fg (Sat: %.1fg)",
                    s.getCalories(), s.getDigestableCarbs(), s.getProtein(), s.getFat(), s.getSaturatedFat()));

            textSecondary.setText(String.format(Locale.US,
                    "Fiber: %s • Chol: %s • Carbs: %s • Ω3: %s • Ω6: %s",
                    s.getFormattedFiber(), s.getFormattedCholesterol(), s.getFormattedCarbohydrates(),
                    s.getFormattedOmega3(), s.getFormattedOmega6()));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < template.getItems().size(); i++) {
                TemplateItem item = template.getItems().get(i);
                if (i > 0) sb.append(", ");
                sb.append(item.getFoodName()).append(" (")
                        .append(String.format(Locale.US, "%.0fg", item.getWeight())).append(")");
            }
            textItems.setText(sb.length() > 0 ? sb.toString() : "No food items.");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditTemplate(template);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteTemplate(template);
            });

            btnApply.setOnClickListener(v -> {
                if (listener != null) listener.onApplyTemplate(template);
            });
        }
    }
}

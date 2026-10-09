package com.activitytracker.ui.templates;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.db.FoodRepository;
import com.activitytracker.model.FoodItem;
import com.activitytracker.model.NutrientSummary;
import com.activitytracker.model.TemplateItem;
import com.activitytracker.ui.planning.FoodAutocompleteAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for food & weight pairs inside Template creation/edit dialog (FR 3.1).
 */
public class TemplateFoodRowAdapter extends RecyclerView.Adapter<TemplateFoodRowAdapter.ViewHolder> {

    public interface OnTemplateItemChangeListener {
        void onTemplateItemChanged();
        void onTemplateItemDeleted(int position);
    }

    private final Context context;
    private final List<TemplateItem> items;
    private final List<FoodItem> availableFoods;
    private final FoodRepository foodRepository;
    private final OnTemplateItemChangeListener changeListener;

    public TemplateFoodRowAdapter(Context context, List<TemplateItem> items, List<FoodItem> availableFoods,
                                  FoodRepository foodRepository, OnTemplateItemChangeListener changeListener) {
        this.context = context;
        this.items = items;
        this.availableFoods = availableFoods;
        this.foodRepository = foodRepository;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template_food_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final AutoCompleteTextView actvFoodName;
        private final TextInputEditText etWeight;
        private final ImageButton btnDelete;
        private final TextView textNutrients;
        private TextWatcher nameWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            actvFoodName = itemView.findViewById(R.id.actv_tpl_food_name);
            etWeight = itemView.findViewById(R.id.et_tpl_food_weight);
            btnDelete = itemView.findViewById(R.id.btn_tpl_delete_row);
            textNutrients = itemView.findViewById(R.id.text_tpl_row_nutrients);
        }

        public void bind(TemplateItem item) {
            FoodAutocompleteAdapter autocompleteAdapter = new FoodAutocompleteAdapter(context, availableFoods);
            actvFoodName.setAdapter(autocompleteAdapter);

            if (nameWatcher != null) {
                actvFoodName.removeTextChangedListener(nameWatcher);
            }

            actvFoodName.setText(item.getFoodName());

            nameWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    item.setFoodName(s != null ? s.toString().trim() : "");
                }
            };
            actvFoodName.addTextChangedListener(nameWatcher);

            actvFoodName.setOnItemClickListener((parent, view, pos, id) -> {
                FoodItem selectedFood = (FoodItem) parent.getItemAtPosition(pos);
                if (selectedFood != null) {
                    item.setFoodItem(selectedFood);
                    updateNutrients(item);
                    if (changeListener != null) changeListener.onTemplateItemChanged();
                }
            });

            actvFoodName.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String input = actvFoodName.getText().toString().trim();
                    if (!input.isEmpty()) {
                        FoodItem matched = foodRepository.getFoodByName(input);
                        item.setFoodItem(matched);
                        if (matched == null) item.setFoodName(input);
                    } else {
                        item.setFoodItem(null);
                        item.setFoodName("");
                    }
                    updateNutrients(item);
                    if (changeListener != null) changeListener.onTemplateItemChanged();
                }
            });

            if (item.getWeight() > 0) {
                if (item.getWeight() == Math.round(item.getWeight())) {
                    etWeight.setText(String.valueOf((long) item.getWeight()));
                } else {
                    etWeight.setText(String.format(Locale.US, "%.1f", item.getWeight()));
                }
            } else {
                etWeight.setText("");
            }

            etWeight.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    applyWeight(item);
                }
            });

            etWeight.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    applyWeight(item);
                    etWeight.clearFocus();
                    return true;
                }
                return false;
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && changeListener != null) {
                    changeListener.onTemplateItemDeleted(pos);
                }
            });

            updateNutrients(item);
        }

        private void applyWeight(TemplateItem item) {
            String text = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
            double w = 0.0;
            if (!text.isEmpty()) {
                try {
                    w = Double.parseDouble(text);
                } catch (NumberFormatException ignored) {}
            }
            if (Double.compare(item.getWeight(), w) != 0) {
                item.setWeight(w);
                updateNutrients(item);
                if (changeListener != null) changeListener.onTemplateItemChanged();
            }
        }

        private void updateNutrients(TemplateItem item) {
            NutrientSummary s = item.computeNutrients();
            textNutrients.setText(String.format(Locale.US,
                    "%.0f kcal | C: %.1fg | P: %.1fg | F: %.1fg | Fib: %.1fg | Chol: %.0fmg",
                    s.getCalories(), s.getDigestableCarbs(), s.getProtein(), s.getFat(), s.getFiber(), s.getCholesterol()));
        }
    }
}

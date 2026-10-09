package com.activitytracker.ui.planning;

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
import com.activitytracker.model.MealItem;
import com.activitytracker.model.NutrientSummary;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for planning view food & weight rows.
 * Implements requirement: weight adjustments do not update nutrient rollups
 * until focus is lost or enter is pressed.
 */
public class FoodRowAdapter extends RecyclerView.Adapter<FoodRowAdapter.FoodRowViewHolder> {

    public interface OnMealItemChangeListener {
        void onMealItemChanged();
        void onMealItemDeleted(int position);
    }

    private final Context context;
    private final List<MealItem> items;
    private final List<FoodItem> availableFoods;
    private final FoodRepository foodRepository;
    private final OnMealItemChangeListener changeListener;

    public FoodRowAdapter(Context context, List<MealItem> items, List<FoodItem> availableFoods,
                          FoodRepository foodRepository, OnMealItemChangeListener changeListener) {
        this.context = context;
        this.items = items;
        this.availableFoods = availableFoods;
        this.foodRepository = foodRepository;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public FoodRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_row, parent, false);
        return new FoodRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodRowViewHolder holder, int position) {
        MealItem item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class FoodRowViewHolder extends RecyclerView.ViewHolder {

        private final AutoCompleteTextView actvFoodName;
        private final TextInputEditText etFoodWeight;
        private final ImageButton btnDelete;

        private final TextView textCalories;
        private final TextView textDigCarbs;
        private final TextView textSatFat;
        private final TextView textFat;
        private final TextView textProtein;
        private final TextView textSecondary;

        private TextWatcher foodNameWatcher;

        public FoodRowViewHolder(@NonNull View itemView) {
            super(itemView);
            actvFoodName = itemView.findViewById(R.id.actv_food_name);
            etFoodWeight = itemView.findViewById(R.id.et_food_weight);
            btnDelete = itemView.findViewById(R.id.btn_delete_food_row);

            textCalories = itemView.findViewById(R.id.text_row_calories);
            textDigCarbs = itemView.findViewById(R.id.text_row_dig_carbs);
            textSatFat = itemView.findViewById(R.id.text_row_sat_fat);
            textFat = itemView.findViewById(R.id.text_row_fat);
            textProtein = itemView.findViewById(R.id.text_row_protein);
            textSecondary = itemView.findViewById(R.id.text_row_secondary_nutrients);
        }

        public void bind(MealItem item, int position) {
            // Food autocomplete setup
            FoodAutocompleteAdapter autocompleteAdapter = new FoodAutocompleteAdapter(context, availableFoods);
            actvFoodName.setAdapter(autocompleteAdapter);

            if (foodNameWatcher != null) {
                actvFoodName.removeTextChangedListener(foodNameWatcher);
            }

            actvFoodName.setText(item.getFoodName());

            foodNameWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    item.setFoodName(s != null ? s.toString().trim() : "");
                }
            };
            actvFoodName.addTextChangedListener(foodNameWatcher);

            actvFoodName.setOnItemClickListener((parent, view, pos, id) -> {
                FoodItem selectedFood = (FoodItem) parent.getItemAtPosition(pos);
                if (selectedFood != null) {
                    item.setFoodItem(selectedFood);
                    updateNutrientViews(item);
                    if (changeListener != null) {
                        changeListener.onMealItemChanged();
                    }
                }
            });

            actvFoodName.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String inputName = actvFoodName.getText().toString().trim();
                    if (!inputName.isEmpty()) {
                        FoodItem matched = foodRepository.getFoodByName(inputName);
                        item.setFoodItem(matched);
                        if (matched == null) {
                            item.setFoodName(inputName);
                        }
                    } else {
                        item.setFoodItem(null);
                        item.setFoodName("");
                    }
                    updateNutrientViews(item);
                    if (changeListener != null) {
                        changeListener.onMealItemChanged();
                    }
                }
            });

            // Weight input setup
            if (item.getWeight() > 0) {
                if (item.getWeight() == Math.round(item.getWeight())) {
                    etFoodWeight.setText(String.valueOf((long) item.getWeight()));
                } else {
                    etFoodWeight.setText(String.format(Locale.US, "%.1f", item.getWeight()));
                }
            } else {
                etFoodWeight.setText("");
            }

            // FR & User Flow: Adjusting meal weight will not trigger a change in nutrient
            // roll up until the field loses focus or enter is pressed.
            etFoodWeight.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    applyWeightChange(item);
                }
            });

            etFoodWeight.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    applyWeightChange(item);
                    etFoodWeight.clearFocus();
                    return true;
                }
                return false;
            });

            // Delete action
            btnDelete.setOnClickListener(v -> {
                int currentPos = getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION && changeListener != null) {
                    changeListener.onMealItemDeleted(currentPos);
                }
            });

            updateNutrientViews(item);
        }

        private void applyWeightChange(MealItem item) {
            String text = etFoodWeight.getText() != null ? etFoodWeight.getText().toString().trim() : "";
            double weight = 0.0;
            if (!text.isEmpty()) {
                try {
                    weight = Double.parseDouble(text);
                } catch (NumberFormatException ignored) {}
            }
            if (Double.compare(item.getWeight(), weight) != 0) {
                item.setWeight(weight);
                updateNutrientViews(item);
                if (changeListener != null) {
                    changeListener.onMealItemChanged();
                }
            }
        }

        private void updateNutrientViews(MealItem item) {
            NutrientSummary nutrients = item.computeNutrients();
            textCalories.setText(nutrients.getFormattedCalories());
            textDigCarbs.setText("C: " + nutrients.getFormattedDigestableCarbs());
            textSatFat.setText("Sat: " + nutrients.getFormattedSaturatedFat());
            textFat.setText("F: " + nutrients.getFormattedFat());
            textProtein.setText("P: " + nutrients.getFormattedProtein());

            String secondary = String.format(Locale.US,
                    "Fiber: %s • Chol: %s • Carbs: %s • Ω3: %s • Ω6: %s",
                    nutrients.getFormattedFiber(),
                    nutrients.getFormattedCholesterol(),
                    nutrients.getFormattedCarbohydrates(),
                    nutrients.getFormattedOmega3(),
                    nutrients.getFormattedOmega6());
            textSecondary.setText(secondary);
        }
    }
}

package com.activitytracker.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.db.DatabaseHelper;
import com.activitytracker.models.FoodItem;
import com.activitytracker.models.FoodWeightPair;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

public class FoodRowAdapter extends RecyclerView.Adapter<FoodRowAdapter.ViewHolder> {

    public interface OnRowChangeListener {
        void onRowChanged();
        void onRowRemoved(int position);
    }

    private Context context;
    private List<FoodWeightPair> items;
    private OnRowChangeListener listener;
    private List<FoodItem> foodList;

    public FoodRowAdapter(Context context, List<FoodWeightPair> items, OnRowChangeListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.foodList = DatabaseHelper.getInstance().getFoodDatabase();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodWeightPair pair = items.get(position);

        ArrayAdapter<FoodItem> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, foodList);
        holder.autoCompleteFood.setAdapter(adapter);

        if (pair.getFoodItem() != null) {
            holder.autoCompleteFood.setText(pair.getFoodItem().getName(), false);
        } else {
            holder.autoCompleteFood.setText("", false);
        }

        if (pair.getWeightGrams() > 0) {
            holder.editWeight.setText(String.format(Locale.US, "%.1f", pair.getWeightGrams()));
        } else {
            holder.editWeight.setText("");
        }

        updateNutrientText(holder, pair);

        // Autocomplete Listener
        holder.autoCompleteFood.setOnItemClickListener((parent, view, pos, id) -> {
            FoodItem selected = (FoodItem) parent.getItemAtPosition(pos);
            pair.setFoodItem(selected);
            updateNutrientText(holder, pair);
            if (listener != null) listener.onRowChanged();
        });

        // Focus Loss requirement for Weight Input updates
        holder.editWeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                commitWeightChange(holder, pair);
            }
        });

        // Keyboard "Done/Enter" action trigger
        holder.editWeight.setOnEditorActionListener((v, actionId, event) -> {
            commitWeightChange(holder, pair);
            holder.editWeight.clearFocus();
            return false;
        });

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onRowRemoved(currentPos);
            }
        });
    }

    private void commitWeightChange(ViewHolder holder, FoodWeightPair pair) {
        String val = holder.editWeight.getText().toString().trim();
        try {
            double weight = val.isEmpty() ? 0.0 : Double.parseDouble(val);
            pair.setWeightGrams(weight);
        } catch (NumberFormatException e) {
            pair.setWeightGrams(0.0);
        }
        updateNutrientText(holder, pair);
        if (listener != null) listener.onRowChanged();
    }

    private void updateNutrientText(ViewHolder holder, FoodWeightPair pair) {
        holder.txtNutrients.setText(String.format(Locale.US,
                "Cal: %.0f | Dig.Carbs: %.1fg | Sat Fat: %.1fg | Fat: %.1fg | Prot: %.1fg",
                pair.getCalories(), pair.getDigestibleCarbs(), pair.getSaturatedFat(), pair.getTotalFat(), pair.getProtein()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AutoCompleteTextView autoCompleteFood;
        TextInputEditText editWeight;
        ImageButton btnRemove;
        TextView txtNutrients;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            autoCompleteFood = itemView.findViewById(R.id.auto_complete_food);
            editWeight = itemView.findViewById(R.id.edit_text_weight);
            btnRemove = itemView.findViewById(R.id.btn_remove_row);
            txtNutrients = itemView.findViewById(R.id.txt_food_nutrients);
        }
    }
}

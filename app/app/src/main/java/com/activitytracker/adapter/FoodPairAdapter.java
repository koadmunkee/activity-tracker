package com.activitytracker.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.model.FoodItem;
import com.activitytracker.model.FoodWeightPair;
import com.activitytracker.model.NutrientAggregation;

import java.util.List;
import java.util.Locale;

public class FoodPairAdapter extends RecyclerView.Adapter<FoodPairAdapter.ViewHolder> {

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    private List<FoodWeightPair> items;
    private List<FoodItem> availableFoods;
    private OnDataChangeListener listener;

    public FoodPairAdapter(List<FoodWeightPair> items, List<FoodItem> availableFoods, OnDataChangeListener listener) {
        this.items = items;
        this.availableFoods = availableFoods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_pair, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodWeightPair pair = items.get(position);

        ArrayAdapter<FoodItem> adapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_dropdown_item_1line,
                availableFoods
        );
        holder.autoCompleteFood.setAdapter(adapter);

        if (pair.getFoodItem() != null) {
            holder.autoCompleteFood.setText(pair.getFoodItem().getName(), false);
        } else {
            holder.autoCompleteFood.setText("");
        }

        if (pair.getWeightGrams() > 0) {
            holder.editWeight.setText(String.format(Locale.US, "%.1f", pair.getWeightGrams()));
        } else {
            holder.editWeight.setText("");
        }

        updateRowNutrientText(holder, pair);

        holder.autoCompleteFood.setOnItemClickListener((parent, view, pos, id) -> {
            FoodItem selected = (FoodItem) parent.getItemAtPosition(pos);
            pair.setFoodItem(selected);
            updateRowNutrientText(holder, pair);
            if (listener != null) listener.onDataChanged();
        });

        // Focus and Enter action handling for delay rollout per technical requirement
        holder.editWeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                commitWeightChange(holder, pair);
            }
        });

        holder.editWeight.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitWeightChange(holder, pair);
                holder.editWeight.clearFocus();
                return true;
            }
            return false;
        });

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, items.size());
                if (listener != null) listener.onDataChanged();
            }
        });
    }

    private void commitWeightChange(ViewHolder holder, FoodWeightPair pair) {
        String valStr = holder.editWeight.getText().toString();
        try {
            double w = Double.parseDouble(valStr);
            pair.setWeightGrams(w);
        } catch (NumberFormatException e) {
            pair.setWeightGrams(0);
        }
        updateRowNutrientText(holder, pair);
        if (listener != null) listener.onDataChanged();
    }

    private void updateRowNutrientText(ViewHolder holder, FoodWeightPair pair) {
        NutrientAggregation agg = pair.getNutrients();
        holder.textNutrients.setText(String.format(Locale.US,
                "%.0f kcal | %.1fg Dig.Carbs | %.1fg Fat | %.1fg Pro",
                agg.calories, agg.digestibleCarbs, agg.totalFat, agg.protein));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AutoCompleteTextView autoCompleteFood;
        EditText editWeight;
        TextView textNutrients;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            autoCompleteFood = itemView.findViewById(R.id.autoCompleteFood);
            editWeight = itemView.findViewById(R.id.editWeight);
            textNutrients = itemView.findViewById(R.id.textPairNutrients);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

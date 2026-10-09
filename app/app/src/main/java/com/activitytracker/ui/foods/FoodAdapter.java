package com.activitytracker.ui.foods;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.model.FoodItem;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying active and archived food items (FR 5.1, 5.2, 5.3).
 */
public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnFoodActionListener {
        void onEditFood(FoodItem food);
        void onArchiveFood(FoodItem food);
        void onUnarchiveFood(FoodItem food);
    }

    private final Context context;
    private final List<FoodItem> foods;
    private final OnFoodActionListener listener;

    public FoodAdapter(Context context, List<FoodItem> foods, OnFoodActionListener listener) {
        this.context = context;
        this.foods = foods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        holder.bind(foods.get(position));
    }

    @Override
    public int getItemCount() {
        return foods != null ? foods.size() : 0;
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textRefWeight;
        private final TextView textCalories;
        private final TextView textDigCarbs;
        private final TextView textSatFat;
        private final TextView textFat;
        private final TextView textProtein;
        private final TextView textSecondary;
        private final ImageButton btnEdit;
        private final ImageButton btnArchive;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_item_food_name);
            textRefWeight = itemView.findViewById(R.id.text_item_food_ref_weight);
            textCalories = itemView.findViewById(R.id.text_food_calories);
            textDigCarbs = itemView.findViewById(R.id.text_food_dig_carbs);
            textSatFat = itemView.findViewById(R.id.text_food_sat_fat);
            textFat = itemView.findViewById(R.id.text_food_fat);
            textProtein = itemView.findViewById(R.id.text_food_protein);
            textSecondary = itemView.findViewById(R.id.text_food_secondary);
            btnEdit = itemView.findViewById(R.id.btn_edit_food);
            btnArchive = itemView.findViewById(R.id.btn_archive_food);
        }

        public void bind(FoodItem food) {
            textName.setText(food.getName());
            textRefWeight.setText(String.format(Locale.US, "per %.0fg", food.getReferenceWeight()));

            textCalories.setText(String.format(Locale.US, "%.0f kcal", food.getCalories()));
            textDigCarbs.setText(String.format(Locale.US, "C: %.1fg", food.getDigestableCarbs()));
            textSatFat.setText(String.format(Locale.US, "Sat: %.1fg", food.getSaturatedFat()));
            textFat.setText(String.format(Locale.US, "F: %.1fg", food.getFat()));
            textProtein.setText(String.format(Locale.US, "P: %.1fg", food.getProtein()));

            String secondary = String.format(Locale.US,
                    "Fiber: %.1fg • Chol: %.0fmg • Carbs: %.1fg • Ω3: %.2fg • Ω6: %.2fg",
                    food.getFiber(),
                    food.getCholesterol(),
                    food.getCarbohydrates(),
                    food.getOmega3(),
                    food.getOmega6());
            textSecondary.setText(secondary);

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditFood(food);
            });

            if (food.isArchived()) {
                btnArchive.setImageResource(R.drawable.ic_unarchive);
                btnArchive.setContentDescription(context.getString(R.string.unarchive_food));
                btnArchive.setOnClickListener(v -> {
                    if (listener != null) listener.onUnarchiveFood(food);
                });
            } else {
                btnArchive.setImageResource(R.drawable.ic_archive);
                btnArchive.setContentDescription(context.getString(R.string.archive_food));
                btnArchive.setOnClickListener(v -> {
                    if (listener != null) listener.onArchiveFood(food);
                });
            }
        }
    }
}

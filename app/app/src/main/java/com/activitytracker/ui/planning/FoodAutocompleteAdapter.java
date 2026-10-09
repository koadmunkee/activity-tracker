package com.activitytracker.ui.planning;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.activitytracker.R;
import com.activitytracker.model.FoodItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Custom autocomplete adapter for food items ensuring high contrast in light/dark themes
 * and responsive real-time name filtering.
 */
public class FoodAutocompleteAdapter extends ArrayAdapter<FoodItem> {

    private final List<FoodItem> originalList;
    private List<FoodItem> filteredList;
    private final LayoutInflater inflater;

    public FoodAutocompleteAdapter(@NonNull Context context, @NonNull List<FoodItem> foods) {
        super(context, 0, new ArrayList<>(foods));
        this.originalList = new ArrayList<>(foods);
        this.filteredList = new ArrayList<>(foods);
        this.inflater = LayoutInflater.from(context);
    }

    public void updateData(List<FoodItem> newFoods) {
        originalList.clear();
        if (newFoods != null) {
            originalList.addAll(newFoods);
        }
        filteredList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    @Nullable
    @Override
    public FoodItem getItem(int position) {
        if (filteredList != null && position >= 0 && position < filteredList.size()) {
            return filteredList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_dropdown_food, parent, false);
        }

        FoodItem item = getItem(position);
        TextView textName = convertView.findViewById(R.id.text_food_name);
        TextView textPreview = convertView.findViewById(R.id.text_food_nutrients_preview);

        if (item != null) {
            textName.setText(item.getName());
            String preview = String.format(Locale.US,
                    "per %.0fg: %.0f kcal | P: %.1fg | C: %.1fg | F: %.1fg",
                    item.getReferenceWeight(),
                    item.getCalories(),
                    item.getProtein(),
                    item.getDigestableCarbs(),
                    item.getFat());
            textPreview.setText(preview);
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<FoodItem> suggestions = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    suggestions.addAll(originalList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase(Locale.US).trim();
                    for (FoodItem item : originalList) {
                        if (item.getName().toLowerCase(Locale.US).contains(filterPattern)) {
                            suggestions.add(item);
                        }
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<FoodItem>) results.values;
                if (filteredList == null) {
                    filteredList = new ArrayList<>();
                }
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof FoodItem) {
                    return ((FoodItem) resultValue).getName();
                }
                return super.convertResultToString(resultValue);
            }
        };
    }
}

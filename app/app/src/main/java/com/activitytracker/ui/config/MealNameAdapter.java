package com.activitytracker.ui.config;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for meal custom name fields in Configuration View (FR 1.1).
 */
public class MealNameAdapter extends RecyclerView.Adapter<MealNameAdapter.ViewHolder> {

    private final Context context;
    private final List<String> mealNames;

    public MealNameAdapter(Context context, List<String> mealNames) {
        this.context = context;
        this.mealNames = mealNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_config_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mealNames != null ? mealNames.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textIndex;
        private final TextInputEditText etCustomName;
        private TextWatcher watcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textIndex = itemView.findViewById(R.id.text_meal_index);
            etCustomName = itemView.findViewById(R.id.et_custom_meal_name);
        }

        public void bind(int position) {
            textIndex.setText(String.format(Locale.US, "Meal %d", position + 1));

            if (watcher != null) {
                etCustomName.removeTextChangedListener(watcher);
            }

            etCustomName.setText(mealNames.get(position));

            watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < mealNames.size()) {
                        mealNames.set(pos, s != null ? s.toString() : "");
                    }
                }
            };
            etCustomName.addTextChangedListener(watcher);
        }
    }
}

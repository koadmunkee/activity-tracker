package com.activitytracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.models.MealTemplate;

import java.util.List;
import java.util.Locale;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    public interface OnTemplateClickListener {
        void onEditClick(MealTemplate template);
        void onDeleteClick(MealTemplate template);
    }

    private Context context;
    private List<MealTemplate> templates;
    private OnTemplateClickListener listener;

    public TemplateAdapter(Context context, List<MealTemplate> templates, OnTemplateClickListener listener) {
        this.context = context;
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealTemplate t = templates.get(position);
        holder.txtTitle.setText(t.getTemplateName());
        holder.txtInfo.setText(String.format(Locale.US, "Items: %d | Insulin Default: %.1f U",
                t.getItems().size(), t.getInsulinDosage()));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(t);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtInfo;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txt_template_title);
            txtInfo = itemView.findViewById(R.id.txt_template_info);
            btnEdit = itemView.findViewById(R.id.btn_edit_template);
            btnDelete = itemView.findViewById(R.id.btn_delete_template);
        }
    }
}

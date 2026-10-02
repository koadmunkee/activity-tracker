package com.activitytracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.R;
import com.activitytracker.model.MealTemplate;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    public interface TemplateActionListener {
        void onEdit(MealTemplate template);
        void onDelete(MealTemplate template);
    }

    private List<MealTemplate> templates;
    private TemplateActionListener listener;

    public TemplateAdapter(List<MealTemplate> templates, TemplateActionListener listener) {
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealTemplate t = templates.get(position);
        holder.textName.setText(t.getTemplateName());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(t));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(t));
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTemplateName);
            btnEdit = itemView.findViewById(R.id.btnEditTemplate);
            btnDelete = itemView.findViewById(R.id.btnDeleteTemplate);
        }
    }
}

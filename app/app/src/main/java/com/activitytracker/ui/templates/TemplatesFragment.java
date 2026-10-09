package com.activitytracker.ui.templates;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activitytracker.MainActivity;
import com.activitytracker.R;
import com.activitytracker.db.TemplateRepository;
import com.activitytracker.model.MealTemplate;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Meal Templates View - allows creating, editing, deleting, and using meal templates (FR 3.1, 3.2).
 */
public class TemplatesFragment extends Fragment implements TemplateAdapter.OnTemplateActionListener {

    private RecyclerView recyclerTemplates;
    private LinearLayout layoutEmpty;
    private ExtendedFloatingActionButton fabCreate;

    private TemplateRepository templateRepository;
    private final List<MealTemplate> templatesList = new ArrayList<>();
    private TemplateAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_templates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();
        templateRepository = new TemplateRepository(context);

        recyclerTemplates = view.findViewById(R.id.recycler_templates);
        layoutEmpty = view.findViewById(R.id.layout_empty_templates);
        fabCreate = view.findViewById(R.id.fab_create_template);

        recyclerTemplates.setLayoutManager(new LinearLayoutManager(context));
        adapter = new TemplateAdapter(context, templatesList, this);
        recyclerTemplates.setAdapter(adapter);

        fabCreate.setOnClickListener(v -> showEditDialog(null));

        loadTemplates();
    }

    public void loadTemplates() {
        templatesList.clear();
        templatesList.addAll(templateRepository.getAllTemplates());

        if (templatesList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerTemplates.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerTemplates.setVisibility(View.VISIBLE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showEditDialog(MealTemplate template) {
        TemplateEditDialog dialog = new TemplateEditDialog(requireContext(), template, () -> {
            loadTemplates();
            Toast.makeText(requireContext(), R.string.template_saved, Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    @Override
    public void onEditTemplate(MealTemplate template) {
        showEditDialog(template);
    }

    @Override
    public void onDeleteTemplate(MealTemplate template) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_template)
                .setMessage(getString(R.string.delete_template_confirm, template.getTemplateName()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    templateRepository.deleteTemplate(template.getId());
                    loadTemplates();
                    Toast.makeText(requireContext(), R.string.template_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onApplyTemplate(MealTemplate template) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).applyTemplateAndOpenPlanning(template);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTemplates();
    }
}

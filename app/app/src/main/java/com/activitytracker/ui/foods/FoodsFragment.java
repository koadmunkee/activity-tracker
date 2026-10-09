package com.activitytracker.ui.foods;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.activitytracker.R;
import com.activitytracker.db.FoodRepository;
import com.activitytracker.model.FoodItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Food Library View - displays active and archived foods, supports editing, creating,
 * and archiving foods with confirmation dialogue (FR 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7).
 */
public class FoodsFragment extends Fragment implements FoodAdapter.OnFoodActionListener {

    private RecyclerView recyclerFoods;
    private LinearLayout layoutEmpty;
    private ChipGroup chipGroupFilter;
    private Chip chipActive;
    private Chip chipArchived;
    private TextInputEditText etSearch;
    private ExtendedFloatingActionButton fabAddFood;

    private FoodRepository foodRepository;
    private final List<FoodItem> currentDisplayList = new ArrayList<>();
    private List<FoodItem> fullSectionList = new ArrayList<>();
    private FoodAdapter adapter;
    private boolean showingArchived = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_foods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();
        foodRepository = new FoodRepository(context);

        recyclerFoods = view.findViewById(R.id.recycler_foods);
        layoutEmpty = view.findViewById(R.id.layout_empty_foods);
        chipGroupFilter = view.findViewById(R.id.chip_group_food_filter);
        chipActive = view.findViewById(R.id.chip_active_foods);
        chipArchived = view.findViewById(R.id.chip_archived_foods);
        etSearch = view.findViewById(R.id.et_search_food);
        fabAddFood = view.findViewById(R.id.fab_add_food);

        recyclerFoods.setLayoutManager(new LinearLayoutManager(context));
        adapter = new FoodAdapter(context, currentDisplayList, this);
        recyclerFoods.setAdapter(adapter);

        chipActive.setOnClickListener(v -> {
            showingArchived = false;
            loadFoods();
        });

        chipArchived.setOnClickListener(v -> {
            showingArchived = true;
            loadFoods();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterList(s != null ? s.toString().trim() : "");
            }
        });

        fabAddFood.setOnClickListener(v -> showFoodEditDialog(null));

        loadFoods();
    }

    public void loadFoods() {
        if (showingArchived) {
            fullSectionList = foodRepository.getArchivedFoods();
        } else {
            fullSectionList = foodRepository.getUnarchivedFoods();
        }
        String currentQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        filterList(currentQuery);
    }

    private void filterList(String query) {
        currentDisplayList.clear();
        if (query.isEmpty()) {
            currentDisplayList.addAll(fullSectionList);
        } else {
            String lower = query.toLowerCase(Locale.US);
            for (FoodItem item : fullSectionList) {
                if (item.getName().toLowerCase(Locale.US).contains(lower)) {
                    currentDisplayList.add(item);
                }
            }
        }

        if (currentDisplayList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerFoods.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerFoods.setVisibility(View.VISIBLE);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showFoodEditDialog(FoodItem foodToEdit) {
        FoodEditDialog dialog = new FoodEditDialog(requireContext(), foodToEdit, savedFood -> {
            loadFoods();
            Toast.makeText(requireContext(), "Saved: " + savedFood.getName(), Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    @Override
    public void onEditFood(FoodItem food) {
        showFoodEditDialog(food);
    }

    /**
     * FR 5.2: Archive a food.
     * Brings up confirmation dialogue allowing the user to cancel or confirm archival.
     * Archiving hides it from the food list and prevents it from appearing in autocomplete selector.
     */
    @Override
    public void onArchiveFood(FoodItem food) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.archive_confirm_title)
                .setMessage(getString(R.string.archive_confirm_msg, food.getName()))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    foodRepository.setArchived(food.getId(), true);
                    loadFoods();
                    Toast.makeText(requireContext(),
                            String.format(getString(R.string.food_archived), food.getName()),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /**
     * FR 5.3: Unarchive a food when viewing archived foods.
     */
    @Override
    public void onUnarchiveFood(FoodItem food) {
        foodRepository.setArchived(food.getId(), false);
        loadFoods();
        Toast.makeText(requireContext(),
                String.format(getString(R.string.food_unarchived), food.getName()),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFoods();
    }
}

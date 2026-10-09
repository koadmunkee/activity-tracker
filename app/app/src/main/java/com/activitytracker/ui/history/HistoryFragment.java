package com.activitytracker.ui.history;

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
import com.activitytracker.db.MealRepository;
import com.activitytracker.model.CommittedDay;
import com.activitytracker.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Meal History View - displays committed past meals and enables adjusting past days (FR 4.1, 4.2).
 */
public class HistoryFragment extends Fragment implements HistoryDayAdapter.OnAdjustDayClickListener {

    private RecyclerView recyclerHistory;
    private LinearLayout layoutEmpty;

    private MealRepository mealRepository;
    private final List<CommittedDay> committedDays = new ArrayList<>();
    private HistoryDayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();
        mealRepository = new MealRepository(context);

        recyclerHistory = view.findViewById(R.id.recycler_history);
        layoutEmpty = view.findViewById(R.id.layout_empty_history);

        recyclerHistory.setLayoutManager(new LinearLayoutManager(context));
        adapter = new HistoryDayAdapter(context, committedDays, this);
        recyclerHistory.setAdapter(adapter);

        loadHistory();
    }

    public void loadHistory() {
        committedDays.clear();
        committedDays.addAll(mealRepository.getCommittedDays());

        if (committedDays.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerHistory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerHistory.setVisibility(View.VISIBLE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * FR 4.2: Adjust a committed day.
     * Clears all food/weight pairs from planning view,
     * makes selected day's meals uncommitted, and opens them in the planning view.
     */
    @Override
    public void onAdjustDay(String date) {
        String displayDate = DateTimeUtil.formatDisplayDate(date);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.adjust_day_confirm_title)
                .setMessage(getString(R.string.adjust_day_confirm_msg, displayDate))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    mealRepository.uncommitDayMeals(date);
                    Toast.makeText(requireContext(),
                            String.format(getString(R.string.adjust_day_success), displayDate),
                            Toast.LENGTH_LONG).show();

                    // Navigate to Planning View
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).switchToPlanningTab();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }
}

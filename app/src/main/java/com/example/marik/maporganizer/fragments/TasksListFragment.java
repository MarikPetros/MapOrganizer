package com.example.marik.maporganizer.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.activity.MainActivity;
import com.example.marik.maporganizer.adapters.TaskAdapter;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.viewModel.TaskViewModel;

import java.util.Objects;
import java.util.UUID;

import static android.support.v7.widget.RecyclerView.*;


public class TasksListFragment extends android.support.v4.app.Fragment {

    private TaskAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TaskViewModel mViewModel;
    private FloatingActionButton mAddTask;

    public TasksListFragment() {
    }

    TaskAdapter.OnItemsListClicked mListClickedListener = new TaskAdapter.OnItemsListClicked() {
        @Override
        public void onClickItem(TaskItem item) {
            BottomSheetDialogFragment bottomSheetDialogFragment = TaskFragment.newInstance(item, 0);
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
        }


        @Override
        public void onRemove(UUID id) {
            mAdapter.mItems.remove(id);
            mViewModel.deleteItem(id);

            //refreshing Geofences
            if (!mViewModel.getAllTaskItems().isEmpty()) {
                ((MainActivity) Objects.requireNonNull(getActivity())).addGeofences();
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
    }

    private void init(View view) {

        mRecyclerView = view.findViewById(R.id.recyclerview);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL);
        manager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new TaskAdapter(getActivity());
        getTaskItemsFromViewModel();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setListClickedListener(mListClickedListener);
        mAddTask = view.findViewById(R.id.floatingActionButton);

        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                ((StaggeredGridLayoutManager)recyclerView.getLayoutManager()).invalidateSpanAssignments();
            }
        });

        mAddTask.setOnClickListener(v -> {
            BottomSheetDialogFragment bottomSheetDialogFragment = new TaskFragment();
            bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

        });
    }


    public void getTaskItemsFromViewModel() {
        mViewModel = ViewModelProviders.of((Objects.requireNonNull(getActivity()))).get(TaskViewModel.class);
        mViewModel.getItems().observe(this, taskItems -> {
            mAdapter.setList(taskItems);
            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}

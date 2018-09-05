package com.example.marik.maporganizer.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static android.app.Notification.VISIBILITY_PUBLIC;


public class FragmentTasksList extends android.support.v4.app.Fragment {

    private TaskAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TaskViewModel mViewModel;

    public FragmentTasksList() {
    }

    TaskAdapter.OnItemsListClicked mListClickedListener = new TaskAdapter.OnItemsListClicked() {
        @Override
        public void onClickItem(TaskItem item) {
//            item = mViewModel.getItem(item.getId());
            BottomSheetDialogFragment bottomSheetDialogFragment =FragmentTaskCreation.newInstance(item);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks_list, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
    }
    private void init(View view) {

        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        mAdapter = new TaskAdapter(getActivity());
        getTaskItemsFromViewModel();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setListClickedListener(mListClickedListener);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void getTaskItemsFromViewModel() {
        mViewModel = ViewModelProviders.of((Objects.requireNonNull(getActivity()))).get(TaskViewModel.class);
        mViewModel.getItems().observe(this, new Observer<List<TaskItem>>() {
            @Override
            public void onChanged(@Nullable List<TaskItem> taskItems) {
                mAdapter.setList(taskItems);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void editTask(TaskItem taskItem) {
        BottomSheetDialogFragment bottomSheetDialogFragment = FragmentTaskCreation.newInstance(taskItem);
        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

//        FragmentTaskCreation.OnTaskFragmentInteraction taskFragmentInteractionListener = new FragmentTaskCreation.OnTaskFragmentInteraction() {
//            @Override
//            public void onAddTask(TaskItem item) {
//                mViewModel.insertItem(item);
//            }
//
//            @Override
//            public void onEditTask(TaskItem item) {
//                mViewModel.update(item);
//            }
//        };
    }

}

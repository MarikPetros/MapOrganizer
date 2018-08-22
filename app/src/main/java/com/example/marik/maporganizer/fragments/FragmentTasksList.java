package com.example.marik.maporganizer.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.adapters.TaskAdapter;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.viewModel.TaskViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

//import com.example.marik.maporganizer.viewModel.TaskViewModel;


public class FragmentTasksList extends android.support.v4.app.Fragment {

    private TaskAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TaskViewModel mViewModel;

//    private OnFragmentInteractionListener mListener;

    public FragmentTasksList() {
    }


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
        mAdapter = new TaskAdapter();
        mRecyclerView.setAdapter(mAdapter);
        getTodoItemsFromViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void getTodoItemsFromViewModel() {
        mViewModel = ViewModelProviders.of((Objects.requireNonNull(getActivity()))).get(TaskViewModel.class);
        mViewModel.getItems().observe(this, new Observer<List<TaskItem>>() {
            @Override
            public void onChanged(@Nullable List<TaskItem> taskItems) {
                mAdapter.setList(taskItems);
                mAdapter.notifyDataSetChanged();
            }
        });
    }


//    public interface OnFragmentInteractionListener {
//
//        void onEditItem(UUID id);
//    }
}

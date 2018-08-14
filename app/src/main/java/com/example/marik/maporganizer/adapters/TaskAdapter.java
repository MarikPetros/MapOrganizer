package com.example.marik.maporganizer.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.viewHolder.TaskHolder;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

    List<TaskItem> mItems;

    public TaskAdapter() {
        mItems = new ArrayList<>();
    }

    public void setList(List<TaskItem> list){
        mItems = list;
    }


    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);

        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {

        TaskItem taskItem = mItems.get(position);

        holder.getTitle().setText(taskItem.getTitle());
        holder.getDescription().setText(taskItem.getDescription());
        holder.getDate().setText(taskItem.getDate().toString());
        holder.getChoosedAddress().setText(taskItem.getAddress().toString());

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addItem(TaskItem item) {
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);

    }
}



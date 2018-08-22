package com.example.marik.maporganizer.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.db.TaskRepository;
import com.example.marik.maporganizer.viewHolder.TaskHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

   public List<TaskItem> mItems;
    Context mContext;
    OnItemsListClicked mListClickedListener;

    public TaskAdapter(Context context)
    {
        mContext=context;
        mItems = new ArrayList<>();
    }

    public void setList(List<TaskItem> list){

        mItems.clear();
        mItems.addAll(list);
    }


    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);

        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaskHolder holder, final int position) {
        holder.bindHolder(mItems.get(position));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.getDeleteBtn().setVisibility(View.VISIBLE);
                return false;
            }
        });
        holder.getDeleteBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                builder.setTitle("DELETE").setIcon(R.drawable.ic_delete).
                setMessage("Do you want delete task?").setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position=holder.getAdapterPosition();
                        TaskItem taskItem=mItems.get(position);
                        mListClickedListener.onRemove(taskItem.getId());
                        notifyDataSetChanged();
                      //  removeItem(holder.getAdapterPosition());
                        dialog.dismiss();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        holder.getDeleteBtn().setVisibility(View.GONE);
                    }
                }).create().show();

            }
        });
        holder.getDeleteBtn().setVisibility(View.GONE);

    }

    public void setListClickedListener(OnItemsListClicked listClickedListener) {
        mListClickedListener = listClickedListener;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

public TaskItem getTaskAtPosition(int position){
        return mItems.get(position);
}

    public void addItem(TaskItem item) {
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }


    public interface OnItemsListClicked{
        void onClickItem(TaskItem item, int id);
        void onRemove(UUID id);

    }
}



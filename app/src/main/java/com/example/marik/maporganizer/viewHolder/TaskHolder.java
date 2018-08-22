package com.example.marik.maporganizer.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.adapters.TaskAdapter;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.db.TaskRepository;

import java.util.UUID;


public class TaskHolder extends RecyclerView.ViewHolder {

    private TextView mDescription;
    private TextView mDate;
    private TextView mTitle;
    private TextView mChoosedAddress;
    private ImageView mDeleteBtn;


    public TaskHolder(View itemView) {

        super(itemView);
        mTitle = itemView.findViewById(R.id.title_view);
        mDescription = itemView.findViewById(R.id.description_view);
        mDate = itemView.findViewById(R.id.date_view);
        mChoosedAddress = itemView.findViewById(R.id.location_view);
        mDeleteBtn=itemView.findViewById(R.id.delete_btn);
    }

    public void bindHolder(TaskItem pTaskItem) {

        mTitle.setText(pTaskItem.getTitle());
        mDescription.setText(pTaskItem.getDescription());
        mDate.setText(pTaskItem.getDate().toString());
        mChoosedAddress.setText(pTaskItem.getAddress().getAddressLine(0));

    }

    public ImageView getDeleteBtn() {
        return mDeleteBtn;
    }
}

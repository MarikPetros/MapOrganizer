package com.example.marik.maporganizer.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;


public class TaskHolder extends RecyclerView.ViewHolder {


    private TextView mDescription;
    private TextView mDate;
    private TextView mTitle;
    private TextView mChoosedAddress;


    public TaskHolder(View itemView) {

        super(itemView);
        mTitle = itemView.findViewById(R.id.title_view);
        mDescription = itemView.findViewById(R.id.description_view);
        mDate = itemView.findViewById(R.id.date_view);
        mChoosedAddress = itemView.findViewById(R.id.location_view);

    }

    public TextView getDescription() {
        return mDescription;
    }

    public void setDescription(TextView description) {
        mDescription = description;
    }

    public TextView getDate() {
        return mDate;
    }

    public void setDate(TextView date) {
        mDate = date;
    }

    public TextView getTitle() {
        return mTitle;
    }


    public TextView getChoosedAddress() {
        return mChoosedAddress;
    }

    public void setChoosedAddress(TextView choosedAddress) {
        mChoosedAddress = choosedAddress;
    }

    public void setTitle(TextView title) {
        mTitle = title;
    }


    public void bindHolder(TaskItem pTaskItem) {
        mTitle.setText(pTaskItem.getTitle());
        mDescription.setText(pTaskItem.getDescription());
        mDate.setText(pTaskItem.getDate().toString());
        mChoosedAddress.setText(pTaskItem.getAddress().toString());
    }
}

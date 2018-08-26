package com.example.marik.maporganizer.adapters.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.fragments.MapsFragment;
import com.example.marik.maporganizer.utils.DateUtil;
import com.google.android.gms.maps.model.LatLng;


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
    //    mDate.setText(DateUtil.formatDateToLongStyle(pTaskItem.getDate()));
        mChoosedAddress.setText(pTaskItem.getChoosedAddress());

    }

    public ImageView getDeleteBtn() {
        return mDeleteBtn;
    }
}


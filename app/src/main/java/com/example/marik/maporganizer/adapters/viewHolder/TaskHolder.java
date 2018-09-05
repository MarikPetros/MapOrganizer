package com.example.marik.maporganizer.adapters.viewHolder;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.fragments.MapsFragment;
import com.example.marik.maporganizer.utils.DateUtil;
import com.google.android.gms.maps.model.LatLng;


public class TaskHolder extends RecyclerView.ViewHolder {
private ImageView mPhoto;
    private TextView mDescription;
    private TextView mDate;
    private TextView mTitle;
    private TextView mChoosedAddress;
    private CheckBox mDeleteCheckBox;


    public TaskHolder(View itemView) {

        super(itemView);
        mPhoto=itemView.findViewById(R.id.list_photo);
        mTitle = itemView.findViewById(R.id.title_view);
        mDescription = itemView.findViewById(R.id.description_view);
        mDate = itemView.findViewById(R.id.date_view);
        mChoosedAddress = itemView.findViewById(R.id.location_view);
        mDeleteCheckBox=itemView.findViewById(R.id.delete_checkbox);
    }

    public void bindHolder(TaskItem pTaskItem) {

        if(pTaskItem.getImageUri()==null){
        mPhoto.setVisibility(View.GONE);}
        else{
            mPhoto.setVisibility(View.VISIBLE);
          mPhoto.setImageBitmap(BitmapFactory.decodeFile(pTaskItem.getImageUri()));
        }
        mTitle.setText(pTaskItem.getTitle());
        if (pTaskItem.getDescription().isEmpty()) {
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setVisibility(View.VISIBLE);
        mDescription.setText(pTaskItem.getDescription());}

        mDate.setText(DateUtil.formatDateToLongStyle(pTaskItem.getDate()));
        if(pTaskItem.getChoosedAddress()==null){
            mChoosedAddress.setVisibility(View.GONE);
        }
        else
            mChoosedAddress.setVisibility(View.VISIBLE);
        mChoosedAddress.setText(pTaskItem.getChoosedAddress());
    }


    public CheckBox getDeleteCheckBox() {
        return mDeleteCheckBox;
    }
}


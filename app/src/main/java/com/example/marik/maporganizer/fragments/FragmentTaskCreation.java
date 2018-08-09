package com.example.marik.maporganizer.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;


import com.example.marik.maporganizer.ImagePicker;
import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.item.TaskItem;
import com.example.marik.maporganizer.viewModel.TaskViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class FragmentTaskCreation extends Fragment {
    private static final String ARG_TASK_ITEM = "arg.taskitem";
    private static final int PICK_IMAGE_ID = 1;
    private static final String MODE_CREATION = "CREATE";
    private static final String MODE_EDIT = "EDIT";
    private static final String[] spinner = {"15 minutes", "30 minutes", "45 minutes", "1 hour",
            "2 hours", "3 hours", "10 hours", "1 day"};

    TaskViewModel mViewModel;

    private TaskItem mTaskItem;
    private TextView mChoosedAddress;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDate;
    private ImageView mPhoto;
    private CheckBox mReminderCheckBox, mNotifybyPlaceCheckBox;
    private Spinner mRemindTime;

    private Calendar mSelectedDate = Calendar.getInstance();

    private String mMode;


    public FragmentTaskCreation() {
    }


    public static FragmentTaskCreation newInstance(TaskItem taskItem) {
        FragmentTaskCreation fragment = new FragmentTaskCreation();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK_ITEM, taskItem);
        fragment.setArguments(args);
        return fragment;
    }


    DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mSelectedDate.set(Calendar.YEAR, year);
            mSelectedDate.set(Calendar.MONTH, monthOfYear);
            mSelectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            openTimePicker();
        }
    };

    TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mSelectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mSelectedDate.set(Calendar.MINUTE, minute);

            updateDateLabel();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments() != null) {
                mTaskItem = getArguments().getParcelable(ARG_TASK_ITEM);
                if (mTaskItem == null) {
                    mMode = MODE_CREATION;
                } else {
                    mMode = MODE_EDIT;
                }
            }
        }
//        mPhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent chooseImageIntent = ImagePicker.getPickImageIntent(getActivity());
//                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
//            }
//        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_creation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init(view);

        updateDateLabel();

        if (mTaskItem != null) {
            fillData(mTaskItem);
        }
    }


    private void fillData(TaskItem taskItem) {
    }


    private void init(View root) {

        mChoosedAddress = root.findViewById(R.id.location_text);
        mTitle = root.findViewById(R.id.title_text);
        mDescription = root.findViewById(R.id.description_text);
        mPhoto = root.findViewById(R.id.photo);
        mDate = root.findViewById(R.id.date);
        mReminderCheckBox = root.findViewById(R.id.reminder_checkbox);
        mNotifybyPlaceCheckBox = root.findViewById(R.id.notify_by_place_checkbox);

        mRemindTime = root.findViewById(R.id.reminder_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRemindTime.setAdapter(adapter);


        mReminderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRemindTime.setVisibility(View.VISIBLE);
                } else {
                    mRemindTime.setVisibility(View.GONE);
                }
            }
        });

        mNotifybyPlaceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mChoosedAddress.setVisibility(View.VISIBLE);
                } else {
                    mChoosedAddress.setVisibility(View.GONE);
                }

            }
        });
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


    private void openDatePicker() {
        new DatePickerDialog(getActivity(), mOnDateSetListener, mSelectedDate.get(Calendar.YEAR),
                mSelectedDate.get(Calendar.MONTH),
                mSelectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openTimePicker() {
        new TimePickerDialog(getActivity(), mOnTimeSetListener, mSelectedDate.get(Calendar.HOUR_OF_DAY),
                mSelectedDate.get(Calendar.MINUTE), true).show();
    }

    private void updateDateLabel() {

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        mDate.setText(dateFormat.format(mSelectedDate.getTime()));
    }

    public TaskItem getTaskItem() {
        String title = mTitle.getText().toString();
        String description = mDescription.getText().toString();
        String date = mDate.toString();
        return mTaskItem;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(getActivity(), resultCode, data);
                mPhoto.setImageBitmap(bitmap);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


}

package com.example.marik.maporganizer.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.viewModel.TaskViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class FragmentTaskCreation extends Fragment {

    private static final String ARG_TASK_ITEM = "arg.taskitem";
    private static final int PICK_IMAGE_ID = 1;
    private static final String MODE_CREATION = "CREATE";
    private static final String MODE_EDIT = "EDIT";
    private static final String remind15 = "15 minutes";
    private static final String remind30 = "30 minutes";
    private static final String remind45 = "45 minutes";
    private static final String remind1 = "1 hour";
    private static final String remind2 = "2 hours";
    private static final String remind3 = "3 hours";
    private static final String remind10 = "10 hours";
    private static final String remindDay = "1 day";
    private static final String[] spinner = {remind15, remind30, remind45, remind1,
            remind2, remind3, remind10, remindDay};

    TaskViewModel mViewModel;
    //   Address mAddress;

    private TextView mChoosedAddress;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDate;
    private ImageView mPhoto;
    private CheckBox mReminderCheckBox, mNotifybyPlaceCheckBox;
    private Spinner mRemindSpinner;
    private String mImageUri;
    private Calendar mSelectedDate = Calendar.getInstance();

    private String mMode;
    private long mRemindTime;
    private int mAlertRadius=100;
    private TaskItem mTaskItem;


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


    @Override
    public void onStop() {
        super.onStop();
        mViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
       // mViewModel.setItem(getTaskItem());

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

        mRemindSpinner = root.findViewById(R.id.reminder_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRemindSpinner.setAdapter(adapter);

        mReminderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRemindSpinner.setVisibility(View.VISIBLE);

                } else {
                    mRemindSpinner.setVisibility(View.GONE);
                }
            }
        });
        mRemindSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                switch (item) {
                    case remind15:
                        mRemindTime = 15;
                        break;
                    case remind30:
                        mRemindTime = 30;
                        break;
                    case remind45:
                        mRemindTime = 45;
                        break;
                    case remind1:
                        mRemindTime = 60;
                        break;
                    case remind2:
                        mRemindTime = 120;
                        break;
                    case remind3:
                        mRemindTime = 180;
                        break;
                    case remind10:
                        mRemindTime = 600;
                        break;
                    case remindDay:
                        mRemindTime = 1440;
                        break;
                    default:
                        mRemindTime = 15;
                        break;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    private TaskItem createTaskItem() {

        mTaskItem.setChoosedAddress(mChoosedAddress.getText().toString());
        mTaskItem.setTitle(mTitle.getText().toString());
        mTaskItem.setDescription(mDescription.getText().toString());
        // TODO   iMAGE URI SET
        mTaskItem.setReminder(mReminderCheckBox.isChecked());
        mTaskItem.setRemindtime((Long) mRemindSpinner.getSelectedItem());
        mTaskItem.setNotifyByPlace(mNotifybyPlaceCheckBox.isChecked());
        if(mNotifybyPlaceCheckBox.isChecked()){
            mTaskItem.setAlertRadius(mAlertRadius);

        } else
            mTaskItem.setAlertRadius(0);
        return mTaskItem;
    }

    private void editTaskItem(TaskItem item){

        mChoosedAddress.setText(item.getChoosedAddress());
        mTitle.setText(item.getTitle());
        mDescription.setText(item.getDescription());
        //TODO IMAGE URI SET
        mReminderCheckBox.setChecked(item.isReminder());
      // TODO ReMIND TIME  and AlertRadius SET




    }

    }




package com.example.marik.maporganizer.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.ContentFrameLayout;
import android.util.Log;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.marik.maporganizer.imagePicker.ImagePicker;
import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.utils.DateUtil;
import com.example.marik.maporganizer.utils.KeyboardUtil;
import com.example.marik.maporganizer.viewModel.TaskViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FragmentTaskCreation extends BottomSheetDialogFragment {

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }


        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_task_creation, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }


    private static final String ARG_TASK_ITEM = "arg.taskitem";
    private static final String ARG_LAT = "arg.lat";
    private static final String ARG_LNG = "arg.lng";

    private static final int PICK_IMAGE_ID = 1;
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
    Context mContext;
    List<Address> addresses = null;
    Address resultAddress;
    String addressLine;
    Bitmap bitmap;
    TaskViewModel mViewModel;

    private ContentFrameLayout mFrameLayout;
    private AutoCompleteTextView mChoosedAddress;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDate;
    private ImageView mPhoto;
    private CheckBox mReminderCheckBox, mNotifybyPlaceCheckBox, mAttachPhotoCheckBox;
    private Spinner mRemindSpinner;
    private String mImageUri;
    private Calendar mSelectedDate = Calendar.getInstance();
    private long mRemindTime;
    private int mAlertRadius = 100;
    private TaskItem mTaskItem;
//    OnTaskFragmentInteraction mListener;

    private boolean isNewCreated = true;

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

    public FragmentTaskCreation() {
    }

    public static FragmentTaskCreation newInstance(LatLng pLatLng) {
        FragmentTaskCreation fragment = new FragmentTaskCreation();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, pLatLng.latitude);
        args.putDouble(ARG_LNG, pLatLng.longitude);
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentTaskCreation newInstance(TaskItem taskItem) {
        FragmentTaskCreation fragment = new FragmentTaskCreation();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK_ITEM, taskItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_TASK_ITEM)) {
                mTaskItem = args.getParcelable(ARG_TASK_ITEM);
//                if (mTaskItem != null && mTaskItem.getDate() != null) {
//                    mSelectedDate.setTime(mTaskItem.getDate());
//                }
                isNewCreated = false;
            } else {
                mTaskItem = new TaskItem();
                mTaskItem.setDate(new Date());
                mTaskItem.setLatitude(args.getDouble(ARG_LAT));
                mTaskItem.setLongitude(args.getDouble(ARG_LNG));
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_creation, container, false);
        if (mTaskItem == null) {
            // init task item
            mTaskItem = new TaskItem();
            mTaskItem.setDate(new Date());
            // ev ayln
        }
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init(view);
        filldata();
        getAddressFromLatitLong(mTaskItem.getLatitude(), mTaskItem.getLongitude(), new GetAddressAsyncTask.OnResultListener() {
            @Override
            public void onResult(String pAddress) {
                mTaskItem.setChoosedAddress(pAddress);
                mChoosedAddress.setText(mTaskItem.getChoosedAddress());
            }
        });

        mViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (isNewCreated) {
            mViewModel.insertItem(updateTaskItemValues());
        } else {
            if (!isEmptyTask()) {
                mViewModel.update(updateTaskItemValues());
            }
        }
    }

    public void onPickImage(View view) {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(getActivity());
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    private void init(final View root) {

        mChoosedAddress = root.findViewById(R.id.addressLine);
        mTitle = root.findViewById(R.id.title_text);
        mDescription = root.findViewById(R.id.description_text);
        mDate = root.findViewById(R.id.date);
        mAttachPhotoCheckBox = root.findViewById(R.id.attach_photo_checkbox);
        mPhoto = root.findViewById(R.id.photo);
        mReminderCheckBox = root.findViewById(R.id.reminder_checkbox);
        mNotifybyPlaceCheckBox = root.findViewById(R.id.notify_by_place_checkbox);

        mRemindSpinner = root.findViewById(R.id.reminder_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRemindSpinner.setAdapter(adapter);
        mFrameLayout = root.findViewById(R.id.frame_in_creator);
        setListeners();
    }

    private void setListeners() {
        mAttachPhotoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPhoto.setVisibility(View.VISIBLE);
                    if (mTaskItem.getImageUri() == null)
                        onPickImage(getView());

                } else {
                    mPhoto.setVisibility(View.GONE);
                }
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
                    FragmentTransaction fragmentTransaction = FragmentTaskCreation.this.getChildFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame_in_creator, new TempMapFragment());
                    fragmentTransaction.commit();
                }
                if (!isChecked) {
                    mFrameLayout.setVisibility(View.GONE);

                }

            }
        });

        mChoosedAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = FragmentTaskCreation.this.getChildFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_in_creator, new TempMapFragment());
                fragmentTransaction.commit();
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

    private void filldata() {
        mSelectedDate.setTime(mTaskItem.getDate());
        updateDateLabel();

        mTitle.setText(mTaskItem.getTitle());
        mDescription.setText(mTaskItem.getDescription());
        mAttachPhotoCheckBox.setChecked(mTaskItem.isAttached());
        // TODO  mPhoto.setImageBitmap(mTaskItem.getImageUri());
        mReminderCheckBox.setChecked(mTaskItem.isReminder());
        if (mReminderCheckBox.isChecked())
            //   switch(mRemindSpinner.)
            //   mRemindSpinner.setSelection();
            //TODO  //     switch (mRemindSpinner.getItemIdAtPosition()){
            //    case :
            mNotifybyPlaceCheckBox.setChecked(mTaskItem.isNotifyByPlace());
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

    boolean isExist = true;

    public TaskItem updateTaskItemValues() {
        if (isEmptyTask()) {
            isExist = false;
            mTaskItem = new TaskItem();
        }
        Log.v("fragmenti lat/lng", "" + mTaskItem.getLatitude() + ", " + mTaskItem.getLongitude() + "");
//        mTaskItem.setChoosedAddress(getAddressFromLatitLong(mTaskItem.getLatitude(), mTaskItem.getLongitude()));
        Log.d("address", "" + mTaskItem.getChoosedAddress() + "");
        mTaskItem.setTitle(mTitle.getText().toString());
        mTaskItem.setDescription(mDescription.getText().toString());
        mTaskItem.setDate(mSelectedDate.getTime());
        if (mAttachPhotoCheckBox.isChecked()) {
            mTaskItem.setAttached(mAttachPhotoCheckBox.isChecked());
            // TODO mTaskItem.setImageUri(mImageUri);
        }

        if (mReminderCheckBox.isChecked()) {
            if (mReminderCheckBox.isChecked()) {
                mTaskItem.setReminder(mReminderCheckBox.isChecked());

                //  mTaskItem.setRemindtime((Long) mRemindSpinner.getSelectedItem());
                // TODO mTaskItem.setRemindtime((Long) mRemindSpinner.getSelectedItem());
            }
            mTaskItem.setNotifyByPlace(mNotifybyPlaceCheckBox.isChecked());
            if (mNotifybyPlaceCheckBox.isChecked()) {
                mTaskItem.setAlertRadius(mAlertRadius);
            } else
                mTaskItem.setAlertRadius(0);
        }
        return mTaskItem;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_ID:
              //  bitmap = ImagePicker.getImageFromResult(getActivity(), resultCode, data);

             //   mPhoto.setImageBitmap(bitmap);
             //   mImageUri = getImageUri(getActivity(), bitmap).toString();

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private boolean isEmptyTask() {
        if ((mTitle.getText().toString().isEmpty()) && (mDescription.getText().toString().isEmpty()))
            return true;
        else return false;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        KeyboardUtil.hideKeyboard(getActivity());

    }

    public void getAddressFromLatitLong(double latitude, double longitude, GetAddressAsyncTask.OnResultListener pOnResultListener) {
        new GetAddressAsyncTask(getActivity(), pOnResultListener).execute(latitude, longitude);
    }


    public static class GetAddressAsyncTask extends AsyncTask<Double, Void, String> {

        private Geocoder mGeocoder;
        private OnResultListener mOnResultListener;

        GetAddressAsyncTask(Context context, OnResultListener pCallback) {
            mGeocoder = new Geocoder(context, Locale.getDefault());
            mOnResultListener = pCallback;
        }

        @Override
        protected String doInBackground(Double... params) {
            return getAddress(params[0], params[1]);
        }

        private String getAddress(double latitude, double longitude) {
            String street = "aaa";
            String area = "bbb";

            StringBuilder result = new StringBuilder();
            try {
                // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                //}
                List<Address> addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);

                    result.append(address.getAddressLine(0));
//                    result.append(address.getLocality());
                    //  result.append(address.getAdminArea());


                }
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            mOnResultListener.onResult(s);
        }

        interface OnResultListener {
            void onResult(String pAddress);
        }

    }

//    public void setFragmentInteraction(OnTaskFragmentInteraction fragmentInteraction) {
//        mListener = fragmentInteraction;
//    }
//
//    public interface OnTaskFragmentInteraction {
//    void onAddTask(TaskItem item);
//    void onEditTask(TaskItem item);


}


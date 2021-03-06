package com.example.marik.maporganizer.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ContentFrameLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.activity.MainActivity;
import com.example.marik.maporganizer.activity.TempMapActivity;
import com.example.marik.maporganizer.activity.ar_activities.CameraViewActivity;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.imagePicker.ImagePicker;
import com.example.marik.maporganizer.imagePicker.WriteBitmapToFileTask;
import com.example.marik.maporganizer.utils.KeyboardUtil;
import com.example.marik.maporganizer.viewModel.TaskViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.ALARM_SERVICE;
import static com.example.marik.maporganizer.activity.TempMapActivity.LATLONG_KEY;
import static com.example.marik.maporganizer.activity.TempMapActivity.RADIUS_KEY;
import static com.example.marik.maporganizer.imagePicker.Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;

//import com.example.marik.maporganizer.activity.ar_activities.CameraViewActivity;


public class TaskFragment extends BottomSheetDialogFragment implements WriteBitmapToFileTask.OnResultListener {


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


    public static final String ARG_TASK_ITEM = "arg.taskitem";
    public static final int ALERT_RADIUS = 2;
    public static final String OPEN_FLAG = "FLAG_FROM_NOTIFICATIONS";
    public static final String DIR_ARGS = "direction latlng";
    public static final String LAT_LANG_FOR_AR = "LAT_LANG_FOR_AR";
    public static final String ARG_LAT = "arg.lat";
    public static final String ARG_LNG = "arg.lng";
    private static final int PICK_IMAGE_ID = 1;
    public static final int REQUEST_CODE = 2;
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
    private static final int REQUEST_CAMERA = 11;
    private static final int SELECT_FILE = 10;
    private ImageView ivImage;
    private String userChoosenTask;

    private ContentFrameLayout mFrameLayout;
    private Button mDirection;
    private TextView mChoosedAddress;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDate;
    private TextView showLocation;
    private ImageView mPhoto, mAddPhoto, mDeletePhoto, arButton, mDialogImage;
    private CheckBox mReminderCheckBox, mNotifybyPlaceCheckBox, mAttachPhotoCheckBox;
    private Spinner mRemindSpinner;
    private String mImageUri;
    private Calendar mSelectedDate = Calendar.getInstance();
    private long mRemindTime = 15;
    private int mAlertRadius = 100;
    private TaskItem mTaskItem;
    private OnDirectionListener mOndirectionListener;
    private boolean reminderIsChecked;
    ArrayAdapter<String> mSpinnerAdapter;
    private int mFlag;
    Animation zoomAnimation;

    private boolean isNewCreated = true;
    private AlarmManager alarmManager;

    //for timed notification
    public final static String ACTION_NOTIFY_NOTIFY_AT_TIME = "com.example.marik.maporganizer.ACTION_NOTIFY_AT_TIME";
    public final static String ITEM_EXTRA = "com.example.marik.maporganizer.NOTIFYING_TASK_ITEM";
    public final static String TIME_NOTIFIER = "com.example.marik.maporganizer.TIME_NOTIFIER";
    public final static String TASK_DATE = "com.example.marik.maporganizer.TASK_DATE";
    public final static String ALERT_TIME = "com.example.marik.maporganizer.ALERT_TIME";
    public final static String ITEM_ADDRESS = "com.example.marik.maporganizer.NOTIFYING_TASK_ADDRESS";
    public final static String ADDRESS = "address";
    public final static String TAG_DIRECTION = "send latlng to map for roure";


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


    public TaskFragment() {
    }

    public static TaskFragment newInstance(LatLng pLatLng) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, pLatLng.latitude);
        args.putDouble(ARG_LNG, pLatLng.longitude);
        fragment.setArguments(args);
        return fragment;
    }

    public static TaskFragment newInstance(TaskItem taskItem, int flag) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK_ITEM, taskItem);
        args.putInt(OPEN_FLAG, flag);
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
                isNewCreated = false;
            } else {
                isNewCreated = true;
                mTaskItem = new TaskItem();
                mTaskItem.setDate(new Date());
                mTaskItem.setLatitude(args.getDouble(ARG_LAT));
                mTaskItem.setLongitude(args.getDouble(ARG_LNG));
            }

            if (args.containsKey(OPEN_FLAG)) {
                mFlag = args.getInt(OPEN_FLAG);
            }
        }

        alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_creation, container, false);
        if (mTaskItem == null) {
            // init task item

            mTaskItem = new TaskItem();
            mTaskItem.setDate(new Date());

        }
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init(view);

        mChoosedAddress.setVisibility(View.GONE);
        mDirection.setVisibility(View.GONE);
        arButton.setVisibility(View.GONE);
        updateDateLabel();


        getAddressFromLatitLong(mTaskItem.getLatitude(), mTaskItem.getLongitude(), new GetAddressAsyncTask.OnResultListener() {
            @Override
            public void onResult(String pAddress) {
                mTaskItem.setChoosedAddress(pAddress);
                mChoosedAddress.setText(mTaskItem.getChoosedAddress());
            }
        });

        filldata();

        mViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(TaskViewModel.class);
        mViewModel.getItems().observe(this, new Observer<List<TaskItem>>() {
            @Override
            public void onChanged(@Nullable List<TaskItem> taskItems) {
                if (taskItems != null) {
                }
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        if (isEmptyTask()) {
            mViewModel.deleteItem(updateTaskItemValues().getId());
        }
        if (isNewCreated) {
            if (!isEmptyTask()) {
                mViewModel.insertItem(updateTaskItemValues());
            }

        } else {
            if (!isEmptyTask()) {
                mViewModel.update(updateTaskItemValues());
            }
            // bitmap.recycle();
        }

        //adding Geofences
        if (!mViewModel.getAllTaskItems().isEmpty()) {
            ((MainActivity) Objects.requireNonNull(getActivity())).addGeofences();
        }

        //for timed notifications
        if (reminderIsChecked) {
            setAlarmManager();
        }
    }


    private void init(final View root) {

        mDirection = root.findViewById(R.id.directions_button);
        arButton = root.findViewById(R.id.augmented_reality);
        mChoosedAddress = root.findViewById(R.id.addressLine);
        mTitle = root.findViewById(R.id.title_text);
        mDescription = root.findViewById(R.id.description_text);
        mDate = root.findViewById(R.id.date);
        mAttachPhotoCheckBox = root.findViewById(R.id.attach_photo_checkbox);
        mPhoto = root.findViewById(R.id.photo);
        mDeletePhoto = root.findViewById(R.id.delete_image);
        mAddPhoto = root.findViewById(R.id.add_image);
        mReminderCheckBox = root.findViewById(R.id.reminder_checkbox);
        mNotifybyPlaceCheckBox = root.findViewById(R.id.notify_by_place_checkbox);
        showLocation = root.findViewById(R.id.show_location);
        showLocation.setVisibility(View.GONE);
        zoomAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.zoom);
        mRemindSpinner = root.findViewById(R.id.reminder_spinner);
        mSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinner);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRemindSpinner.setAdapter(mSpinnerAdapter);
        setListeners();
    }

    private void setListeners() {


        mDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra(TAG_DIRECTION, ITEM_EXTRA);
                intent.putExtra(ARG_LAT, mTaskItem.getLatitude());
                intent.putExtra(ARG_LNG, mTaskItem.getLongitude());
                startActivity(intent);
                dismiss();
            }
        });

        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CameraViewActivity.class);
                intent.putExtra(LAT_LANG_FOR_AR, new double[]{mTaskItem.getLatitude(), mTaskItem.getLongitude()});
                intent.putExtra(ADDRESS, mTaskItem.getChoosedAddress());
                startActivity(intent);
            }
        });

        mAttachPhotoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPhoto.setVisibility(View.VISIBLE);
                    if (mTaskItem.getImageUri() == null)
                        mAddPhoto.setVisibility(View.VISIBLE);
                    mDeletePhoto.setVisibility(View.VISIBLE);

                } else {
                    mPhoto.setVisibility(View.GONE);
                    mAddPhoto.setVisibility(View.GONE);
                    mDeletePhoto.setVisibility(View.GONE);
                }
            }
        });


        mPhoto.setOnClickListener(v -> mPhoto.startAnimation(zoomAnimation));


        mAddPhoto.setOnClickListener(v -> {
            checkPermissions(getContext());
            onPickImage();
        });

        mDeletePhoto.setOnClickListener(v -> {
            mPhoto.setImageBitmap(null);
            mImageUri = null;
        });

        mReminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mRemindSpinner.setVisibility(View.VISIBLE);
                reminderIsChecked = true;
            } else {
                mRemindSpinner.setVisibility(View.GONE);
                reminderIsChecked = false;
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
                mRemindTime = 15;
            }
        });

        mNotifybyPlaceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mTaskItem.setNotifyByPlace(true);
                mTaskItem.setAlertRadius(mAlertRadius);
                showLocation.setVisibility(View.VISIBLE);
            } else {
                mTaskItem.setNotifyByPlace(false);
                showLocation.setVisibility(View.GONE);
            }
        });

        showLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TempMapActivity.class);
            intent.putExtra(ARG_TASK_ITEM, mTaskItem);
            startActivityForResult(intent, ALERT_RADIUS);
        });

        mDate.setOnClickListener(v -> openDatePicker());

        mPhoto.setOnClickListener(v -> {

        });
    }


    private void openDatePicker() {
        new DatePickerDialog(Objects.requireNonNull(getActivity()), mOnDateSetListener, mSelectedDate.get(Calendar.YEAR),
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
        mTaskItem.setTitle(mTitle.getText().toString());
        mTaskItem.setDescription(mDescription.getText().toString());
        mTaskItem.setDate(mSelectedDate.getTime());
        if (mAttachPhotoCheckBox.isChecked()) {
            mTaskItem.setAttached(mAttachPhotoCheckBox.isChecked());
        }

        mTaskItem.setReminder(mReminderCheckBox.isChecked());
        if (mReminderCheckBox.isChecked()) {
            mTaskItem.setRemindtime(mRemindTime);
        }

        mTaskItem.setNotifyByPlace(mNotifybyPlaceCheckBox.isChecked());
        if (mNotifybyPlaceCheckBox.isChecked()) {
            mTaskItem.setNotifyByPlace(true);
        } else {
            mTaskItem.setNotifyByPlace(false);
        }

        return mTaskItem;
    }


    private void filldata() {
        if (mTaskItem.getLatitude() == 0 || mTaskItem.getLongitude() == 0) {
            mChoosedAddress.setVisibility(View.GONE);
            mDirection.setVisibility(View.GONE);
            arButton.setVisibility(View.GONE);
        } else {
            mChoosedAddress.setVisibility(View.VISIBLE);
            mDirection.setVisibility(View.VISIBLE);
            arButton.setVisibility(View.VISIBLE);
        }
        mSelectedDate.setTime(mTaskItem.getDate());
        updateDateLabel();
        mTitle.setText(mTaskItem.getTitle());
        mDescription.setText(mTaskItem.getDescription());
        mAttachPhotoCheckBox.setChecked(mTaskItem.isAttached());
        if (mTaskItem.isAttached()) {
            if (mTaskItem.getImageUri() != null) {
                mPhoto.setImageBitmap(BitmapFactory.decodeFile(mTaskItem.getImageUri()));
                mAddPhoto.setVisibility(View.VISIBLE);
                mDeletePhoto.setVisibility(View.VISIBLE);
            }
        } else {
            mPhoto.setVisibility(View.GONE);
            mAddPhoto.setVisibility(View.GONE);
            mDeletePhoto.setVisibility(View.GONE);
        }
        if (mFlag == 1) {
            mReminderCheckBox.setChecked(false);
        } else {
            mReminderCheckBox.setChecked(mTaskItem.isReminder());
        }
        if (mReminderCheckBox.isChecked()) {
            mRemindTime = mTaskItem.getRemindtime();
            String mRemindString = Long.toString(mRemindTime).trim();
            //selectSpinnerValue(mRemindSpinner, Long.toString(mRemindTime));
            switch (mRemindString) {
                case "15":
                    mRemindSpinner.setSelection(0);
                    break;
                case "30":
                    mRemindSpinner.setSelection(1);
                    break;
                case "45":
                    mRemindSpinner.setSelection(2);
                    break;
                case "60":
                    mRemindSpinner.setSelection(3);
                    break;
                case "120":
                    mRemindSpinner.setSelection(4);
                    break;
                case "180":
                    mRemindSpinner.setSelection(5);
                    break;
                case "600":
                    mRemindSpinner.setSelection(6);
                    break;
                case "1440":
                    mRemindSpinner.setSelection(7);
                    break;
            }
        }
        if (mFlag == 2)

        {
            mNotifybyPlaceCheckBox.setChecked(false);
            mTaskItem.setNotifyByPlace(false);
        } else

        {
            mNotifybyPlaceCheckBox.setChecked(mTaskItem.isNotifyByPlace());
        }
    }


    public void onPickImage() {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(getContext());
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_ID:
                bitmap = ImagePicker.getImageFromResult(getActivity(), resultCode, data);
                if (getContext() != null) {
                    writeBitmapToFile(bitmap, getActivity().getFilesDir() +
                            File.separator + mTaskItem.getId(), this);
                }
                break;
            case ALERT_RADIUS:
                if (getContext() != null && data != null) {
                    mAlertRadius = data.getIntExtra(RADIUS_KEY, 0);
                    double[] latAndLng = data.getDoubleArrayExtra(LATLONG_KEY);
                    mTaskItem.setAlertRadius(mAlertRadius);
                    mTaskItem.setLatitude(latAndLng[0]);
                    mTaskItem.setLongitude(latAndLng[1]);
                    mChoosedAddress.setVisibility(View.VISIBLE);
                    mDirection.setVisibility(View.VISIBLE);
                    arButton.setVisibility(View.VISIBLE);
                    getAddressFromLatitLong(latAndLng[0], latAndLng[1], new GetAddressAsyncTask.OnResultListener() {
                        @Override
                        public void onResult(String pAddress) {
                            mTaskItem.setChoosedAddress(pAddress);
                            mChoosedAddress.setText(mTaskItem.getChoosedAddress());
                        }
                    });


                } else {
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermissions(getContext());
    }

    private void setFragment(Fragment fragment) {
        assert getFragmentManager() != null;
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


    private boolean isEmptyTask() {
        return (mTitle.getText().toString().isEmpty()) && (mDescription.getText().toString().isEmpty());
    }

    public void checkPermissions(Context context) {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(context, permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, permissions[2]) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions((Activity) context, permissions, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        KeyboardUtil.hideKeyboard(getActivity());
    }

    public void writeBitmapToFile(Bitmap bitmap, String path, WriteBitmapToFileTask.OnResultListener onResultListener) {
        new WriteBitmapToFileTask(bitmap, path, onResultListener).execute();
    }

    public void getAddressFromLatitLong(double latitude, double longitude, GetAddressAsyncTask.OnResultListener pOnResultListener) {
        new GetAddressAsyncTask(getActivity(), pOnResultListener).execute(latitude, longitude);
    }

    @Override
    public void onBitmapSavedResult(String pPath, Bitmap pBitmap) {
        if (pPath == null) {
            // can't save
        } else {
            mTaskItem.setImageUri(pPath);
            mPhoto.setImageBitmap(pBitmap);
        }
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
                List<Address> addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);

                    result.append(address.getAddressLine(0));
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

    public interface OnDirectionListener {
        void showDirection(LatLng pLatLng);

    }


    /*
   ----------------------------------- Setting AlarmManager for notification by time -----------------------------------------------------------------------------
   */

    private void setAlarmManager() {
        long alertTime = 0;
        if (mTaskItem != null) {
            alertTime = mTaskItem.getDate().getTime() - (mRemindTime * 60 * 1000);
        }
        long taskDate = mTaskItem.getDate().getTime();
        int notificationId = (int) Math.round(((mTaskItem.getLatitude() + mTaskItem.getLongitude()) * 100000) % 100);
        double[] latLng = new double[]{mTaskItem.getLatitude(), mTaskItem.getLongitude()};
        Intent notifyIntent = new Intent(ACTION_NOTIFY_NOTIFY_AT_TIME);
        notifyIntent.putExtra(ITEM_EXTRA, latLng);
        notifyIntent.putExtra(TASK_DATE, taskDate);
        notifyIntent.putExtra(ITEM_ADDRESS, mTaskItem.getChoosedAddress());
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                (mContext, notificationId, notifyIntent, PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alertTime, notifyPendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alertTime, notifyPendingIntent);
        }
    }
}


package com.example.marik.maporganizer.fragments;

import android.annotation.SuppressLint;
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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ContentFrameLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.imagePicker.Utility;
import com.example.marik.maporganizer.imagePicker.WriteBitmapToFileTask;
import com.example.marik.maporganizer.utils.KeyboardUtil;
import com.example.marik.maporganizer.viewModel.TaskViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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


public class FragmentTaskCreation extends BottomSheetDialogFragment implements WriteBitmapToFileTask.OnResultListener {


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
    private ImageView mPhoto, mAddPhoto, mDeletePhoto;
    private CheckBox mReminderCheckBox, mNotifybyPlaceCheckBox, mAttachPhotoCheckBox;
    private Spinner mRemindSpinner;
    private String mImageUri;
    private Calendar mSelectedDate = Calendar.getInstance();
    private long mRemindTime = 15 * 60 * 1000;
    private int mAlertRadius = 100;
    private TaskItem mTaskItem;
    private OnDirectionListener mOndirectionListener;
    private boolean reminderIsChecked;
//    OnTaskFragmentInteraction mListener;

    private boolean isNewCreated = true;
    private AlarmManager alarmManager;

    //for timed notification
    public final static String ACTION_NOTIFY_NOTIFY_AT_TIME = "com.example.marik.maporganizer.ACTION_NOTIFY_AT_TIME";
    public final static String ITEM_EXTRA = "com.example.marik.maporganizer.NOTIFYING_TASK_ITEM";
    public final static String TIME_NOTIFIER = "com.example.marik.maporganizer.TIME_NOTIFIER";
    public final static String TASK_DATE = "com.example.marik.maporganizer.TASK_DATE";
    public final static String ITEM_ADDRESS = "com.example.marik.maporganizer.NOTIFYING_TASK_ADDRESS";

    public void setmOndirectionListener(OnDirectionListener mOndirectionListener) {
        this.mOndirectionListener = mOndirectionListener;
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
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_TASK_ITEM)) {
                mTaskItem = args.getParcelable(ARG_TASK_ITEM);
//                if (mTaskItem != null && mTaskItem.getDate() != null) {
//                    mSelectedDate.setTime(mTaskItem.getDate());
//                }
                isNewCreated = false;
            } else {
                isNewCreated = true;
                mTaskItem = new TaskItem();
                mTaskItem.setDate(new Date());
                mTaskItem.setLatitude(args.getDouble(ARG_LAT));
                mTaskItem.setLongitude(args.getDouble(ARG_LNG));
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
            // ev ayln
        }
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init(view);

        updateDateLabel();

        filldata();

        getAddressFromLatitLong(mTaskItem.getLatitude(), mTaskItem.getLongitude(), new GetAddressAsyncTask.OnResultListener() {
            @Override
            public void onResult(String pAddress) {
                mTaskItem.setChoosedAddress(pAddress);
                mChoosedAddress.setText(mTaskItem.getChoosedAddress());
            }
        });

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
        if (isNewCreated) {
            mViewModel.insertItem(updateTaskItemValues());
            if (isEmptyTask()) {
                mViewModel.deleteItem(updateTaskItemValues().getId());
            }
        } else {
            if (!isEmptyTask()) {
                mViewModel.update(updateTaskItemValues());
            }
            //   bitmap.recycle();
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

//    public void onPickImage(View view) {
//        Intent chooseImageIntent = ImagePicker.getPickImageIntent(getContext());
//        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
//    }

    private void init(final View root) {

        mDirection = root.findViewById(R.id.directions_button);
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

        mRemindSpinner = root.findViewById(R.id.reminder_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRemindSpinner.setAdapter(adapter);
        setListeners();
    }

    private void setListeners() {


        mDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(mTaskItem.getLatitude(),mTaskItem.getLongitude());
                mOndirectionListener.showDirection(latLng);
            }
        });

        mAttachPhotoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPhoto.setVisibility(View.VISIBLE);
                    if (mTaskItem.getImageUri() == null)
                        // onPickImage(getView());
                        mAddPhoto.setVisibility(View.VISIBLE);
                    mDeletePhoto.setVisibility(View.VISIBLE);

                } else {
                    mPhoto.setVisibility(View.GONE);
                    mAddPhoto.setVisibility(View.GONE);
                    mDeletePhoto.setVisibility(View.GONE);
                }
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
//                if (mTaskItem.getImageUri() == null)
//                    onPickImage(getView());

            }
        });


        mDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    bitmap.recycle();
                mTaskItem.setImageUri(null);
                mTaskItem.setAttached(false);
            }
        });

        mReminderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRemindSpinner.setVisibility(View.VISIBLE);
                    reminderIsChecked = true;
                } else {
                    mRemindSpinner.setVisibility(View.GONE);
                    reminderIsChecked = false;
                }
            }
        });


        mRemindSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                switch (item) {
                    case remind15:
                        mRemindTime = 15 * 60 * 1000;
                        break;
                    case remind30:
                        mRemindTime = 30 * 60 * 1000;
                        break;
                    case remind45:
                        mRemindTime = 45 * 60 * 1000;
                        break;
                    case remind1:
                        mRemindTime = 60 * 60 * 1000;
                        break;
                    case remind2:
                        mRemindTime = 120 * 60 * 1000;
                        break;
                    case remind3:
                        mRemindTime = 180 * 60 * 1000;
                        break;
                    case remind10:
                        mRemindTime = 600 * 60 * 1000;
                        break;
                    case remindDay:
                        mRemindTime = 1440 * 60 * 1000;
                        break;
                    default:
                        mRemindTime = 15 * 60 * 1000;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mRemindTime = 15 * 60 * 1000;
            }
        });

        mNotifybyPlaceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTaskItem.setAlertRadius(mAlertRadius);
                    showLocation.setVisibility(View.VISIBLE);
                }
            }
        });

        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TempMapActivity.class);
                intent.putExtra(ARG_TASK_ITEM, mTaskItem);
                startActivityForResult(intent, ALERT_RADIUS);
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
        if (mTaskItem.isAttached()) {
            mPhoto.setImageBitmap(BitmapFactory.decodeFile(mTaskItem.getImageUri()));
        }
        mReminderCheckBox.setChecked(mTaskItem.isReminder());
        if (mReminderCheckBox.isChecked()) {
            //   switch(mRemindSpinner.)
            //   mRemindSpinner.setSelection();
            //TODO  //     switch (mRemindSpinner.getItemIdAtPosition()){
        } //    case :
        mNotifybyPlaceCheckBox.setChecked(mTaskItem.isNotifyByPlace());
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
            mTaskItem.setReminder(mReminderCheckBox.isChecked());

            //  mTaskItem.setRemindtime((Long) mRemindSpinner.getSelectedItem());
            // TODO mTaskItem.setRemindtime((Long) mRemindSpinner.getSelectedItem());
        }

        mTaskItem.setNotifyByPlace(mNotifybyPlaceCheckBox.isChecked());

        if (mNotifybyPlaceCheckBox.isChecked()) {
            //   mTaskItem.setAlertRadius(mAlertRadius);
        } /*else {
            mTaskItem.setAlertRadius(0);
        }*/

        return mTaskItem;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_FILE:
                onSelectFromGalleryResult(data);

                break;
            case REQUEST_CAMERA:
                onCaptureImageResult(data);
                writeBitmapToFile(bitmap, getContext().getFilesDir() + File.separator + mTaskItem.getId(), this);
                break;

//            case PICK_IMAGE_ID:
//                bitmap = ImagePicker.getImageFromResult(getContext(), resultCode, data);
//                if (getContext() != null) {
//                }
//                break;
            case ALERT_RADIUS:
                if (getContext() != null && data != null) {
                    mAlertRadius = data.getIntExtra(RADIUS_KEY, 0);
                    double[] latAndLng = data.getDoubleArrayExtra(LATLONG_KEY);
                    mTaskItem.setAlertRadius(mAlertRadius);
                    mTaskItem.setLatitude(latAndLng[0]);
                    mTaskItem.setLongitude(latAndLng[1]);
                    mViewModel.update(mTaskItem);
                } else {
                    Log.e("radius", "Radiusy chekav");
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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

    //TODO delegation for directions
    public interface OnDirectionListener {
        void showDirection(LatLng pLatLng);
    }

//    public void setFragmentInteraction(OnTaskFragmentInteraction fragmentInteraction) {
//        mListener = fragmentInteraction;
//    }
//
//    public interface OnTaskFragmentInteraction {
//    void onAddTask(TaskItem item);
//    void onEditTask(TaskItem item);


    /*
   ----------------------------------- Setting AlarmManager for notification by time -----------------------------------------------------------------------------
   */

    private void setAlarmManager() {
        long alertTime = 0;
        if (mTaskItem != null) {
            alertTime = mTaskItem.getDate().getTime() - mRemindTime;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getContext());

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == SELECT_FILE)
//                onSelectFromGalleryResult(data);
//            else if (requestCode == REQUEST_CAMERA)
//                onCaptureImageResult(data);
//        }
//    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPhoto.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mPhoto.setImageBitmap(bm);
    }


}


package com.example.marik.maporganizer.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.fragments.TaskFragment;
import com.example.marik.maporganizer.fragments.TasksListFragment;
import com.example.marik.maporganizer.fragments.MapsFragment;
import com.example.marik.maporganizer.service.GeofencerService;
import com.example.marik.maporganizer.utils.GeofenceMaker;
import com.example.marik.maporganizer.viewModel.TaskViewModel;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.ArCoreApk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.marik.maporganizer.appwidget.TaskAppWidgetProvider.ITEM_INDEX;
import static com.example.marik.maporganizer.fragments.TaskFragment.ARG_LAT;
import static com.example.marik.maporganizer.fragments.TaskFragment.ARG_LNG;
import static com.example.marik.maporganizer.fragments.TaskFragment.TIME_NOTIFIER;
import static com.example.marik.maporganizer.service.GeofencerService.TRIGGERING_LOCATIONS;

//import com.google.ar.core.ArCoreApk;

public class MainActivity extends AppCompatActivity {

    public final static int PERMISSION_CODE = 26;
    public final static int REMIND_NOTIF_CODE = 1;
    public final static int GEOFENCE_NOTIF_CODE = 2;

    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private GeofenceMaker mGeofenceMaker = GeofenceMaker.getGeofenceMakerInstance();
    private MapsFragment mMapsFragment;
    private TasksListFragment mTaskListFragment;
    private BottomNavigationView mBottomNavigationView;
    private TaskViewModel model;
    private ImageView mArButton;
    double dirLat;
    double dirLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getIntent().getExtras() != null) {
            Bundle args = getIntent().getExtras();
            dirLat = args.getDouble(ARG_LAT);
            dirLng = args.getDouble(ARG_LNG);
            Log.v("main", dirLat + ", " + dirLng);

        }
        init();
    }


    public void init() {

        mBottomNavigationView = findViewById(R.id.nav_view_bar);
        mArButton = findViewById(R.id.augmented_reality);

        mGeofencingClient = LocationServices.getGeofencingClient(this);
        createGeofencesList();

        mTaskListFragment = new TasksListFragment();
        mMapsFragment = new MapsFragment();
        setTabs();

        setFragment(mMapsFragment);

        if (getIntent().hasExtra(ITEM_INDEX)) {
            launchInfoFragment();
        }

        if (getIntent().hasExtra(TIME_NOTIFIER)) {
            launchCreationFragmentfromNotification();
        }

        if (getIntent().hasExtra(TRIGGERING_LOCATIONS)) {
            launchCreationFragmentByGeofence();
        }

        // Enable AR related functionality on ARCore supported devices only.
//        maybeEnableArButton();


    }

    private void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override/**/
                public void run() {
                    maybeEnableArButton();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            mArButton.setVisibility(View.VISIBLE);
            mArButton.setEnabled(true);
            // indicator on the button.
        } else {
            mArButton.setVisibility(View.INVISIBLE);
            mArButton.setEnabled(false);
        }
    }

    private void createGeofencesList() {
        model = ViewModelProviders.of(this).get(TaskViewModel.class);
        model.getItems().observe(this, new Observer<List<TaskItem>>() {  /// hnaravor a sa tarvi creati mej aveli verev
            @Override
            public void onChanged(@Nullable List<TaskItem> taskItems) {
                if (taskItems != null) {
                    mGeofenceMaker.crateGeofenceList(selectGeofencingTasks(taskItems));
                } else mGeofenceMaker.crateGeofenceList(new ArrayList<>());
            }
        });
    }

    private void launchInfoFragment() {
        int indexInWidget = getIntent().getIntExtra(ITEM_INDEX, 0);
        if (indexInWidget >= 0) {
            launchCreationFragment(indexInWidget);
            indexInWidget = -1;
        }
    }


    private void launchCreationFragment(int index) {
        TaskItem item = model.getAllTaskItems().get(index);
        BottomSheetDialogFragment bottomSheetDialogFragment = TaskFragment.newInstance(item, 0);
        //show it
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }


    private void launchCreationFragmentByGeofence() {
        ArrayList<Location> locations = getIntent().getParcelableArrayListExtra(TRIGGERING_LOCATIONS);
        if (locations != null && locations.size() > 0) {
            for (Location location : locations) {
                Log.e("triggering_location","triggered from " +location.getLatitude() + " " + location.getLongitude());
                TaskItem item = model.getItemByLocation(location.getLatitude(), location.getLongitude());

                BottomSheetDialogFragment bottomSheetDialogFragment = TaskFragment.newInstance(item, 2);
                //show it
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        }
    }

    private void launchCreationFragmentfromNotification() {
        double[] latlng = getIntent().getDoubleArrayExtra(TIME_NOTIFIER);

        if (latlng != null) {
            TaskItem item = model.getItemByLocation(latlng[0], latlng[1]);
            BottomSheetDialogFragment bottomSheetDialogFragment = TaskFragment.newInstance(item, 1);
            //show it
            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }
    }


    private void setFragment(Fragment fragment) {
        assert getFragmentManager() != null;
        if (dirLat != 0 && dirLng != 0) {
            Bundle args = new Bundle();
            args.putString(TaskFragment.TAG_DIRECTION, "lat/lng");
            args.putDouble(ARG_LAT, dirLat);
            args.putDouble(ARG_LNG, dirLng);
            fragment.setArguments(args);
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


    public void setTabs() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mapNav:
                                setFragment(mMapsFragment);
                                return true;

                            case R.id.listNav:
                                setFragment(mTaskListFragment);
                                return true;

                            default:
                                return false;
                        }
                    }
                });
    }

    // ---------------------------------------------------------------------------------------------
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(this),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_CODE);
            }
        }
    }


    /**
     * -----------------------  Geofencing ----------------------------------------------------------------------------------------------------------------------------------------------
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofencerService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    public void addGeofences() {
        checkLocationPermission();
        mGeofencingClient.addGeofences(mGeofenceMaker.getGeofencingRequestOfList(), getGeofencePendingIntent())
                .addOnSuccessListener(Objects.requireNonNull(this), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Toast.makeText(getApplicationContext(), "Geofence successfuly added", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Geofencees failed", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private List<TaskItem> selectGeofencingTasks(List<TaskItem> taskItems) {
        List<TaskItem> items = new ArrayList<>();
        for (TaskItem item : taskItems) {
            if (item.isNotifyByPlace()) {
                items.add(item);
            }
        }
        return items;
    }

}
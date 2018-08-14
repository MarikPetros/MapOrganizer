package com.example.marik.maporganizer.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.adapters.SectionPagerAdapter;
import com.example.marik.maporganizer.fragments.FragmentTaskCreation;
import com.example.marik.maporganizer.fragments.FragmentTasksList;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.fragments.MapsFragment;
import com.example.marik.maporganizer.service.GeofencerService;
import com.example.marik.maporganizer.utils.GeofenceMaker;
import com.example.marik.maporganizer.viewModel.TaskViewModel;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapsActivity extends AppCompatActivity implements MapsFragment.OnFragmentInteractionListener {
    public final static int PERMISSION_CODE = 26;

     ViewPager viewPager;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private GeofenceMaker mGeofenceMaker = GeofenceMaker.getGeofenceMakerInstance() ;

    private FragmentActivity mFragmentActivity;
    private FragmentTaskCreation fragmentTaskCreation;
    private BottomNavigationView mBottomNavigationView;
    private FragmentTasksList mFragmentTasksList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        TaskViewModel model = ViewModelProviders.of(this).get(TaskViewModel.class);
        model.getItems().observe(this, new Observer<List<TaskItem>>() {
            @Override
            public void onChanged(@Nullable List<TaskItem> taskItems) {
                mGeofenceMaker.crateGeofenceList(selectGeofencingTasks(taskItems));
            }
        });


        mFragmentTasksList = new FragmentTasksList();
        fragmentTaskCreation = new FragmentTaskCreation();
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view_bar);

        setTabs();

    }


    //-----------------------------Sections aka tabs -------------------------------------------


    public void setFragment(Fragment fragment){
        assert getFragmentManager() != null;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map, fragment);
        fragmentTransaction.commit();

        }


        public void setTabs(){
                 mBottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.mapNav:
                                    //TODO stay on Map activity
                                    setFragment(fragmentTaskCreation);

                                    return true;

                                case R.id.listNav:
                                    setFragment(fragmentTaskCreation);
                                    return  true;
                            }

                            return true;
                        }
                    });

        }

    private void setupViewPager(ViewPager viewPager) {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapsFragment());

        viewPager.setAdapter(adapter);
    }


   // ---------------------------------------------------------------------------------------------
    private void checkLocationPermission() {
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
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
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
        Intent intent = new Intent(this,GeofencerService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    private void addGeofences() {
        checkLocationPermission();
        mGeofencingClient.addGeofences(mGeofenceMaker.getGeofencingRequestOfList(),getGeofencePendingIntent())
                .addOnSuccessListener(Objects.requireNonNull(this),new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener(this,new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        e.printStackTrace();
                    }
                });
    }

    // es der piti kargavorvi
    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    public class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                    addGeofences();
            }
        }
    }


    private List<TaskItem> selectGeofencingTasks(List<TaskItem> taskItems){
        List<TaskItem> items = new ArrayList<>();
        for (TaskItem item : taskItems){
            if (item.isNotifyByPlace()){
                items.add(item);
            }
        }
        return items;
    }
}
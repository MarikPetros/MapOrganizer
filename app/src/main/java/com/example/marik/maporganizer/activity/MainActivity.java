package com.example.marik.maporganizer.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.fragments.FragmentTasksList;
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

public class MainActivity extends AppCompatActivity implements MapsFragment.OnFragmentInteractionListener{
    public final static int PERMISSION_CODE = 26;

    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private GeofenceMaker mGeofenceMaker = GeofenceMaker.getGeofenceMakerInstance();
    private MapsFragment mMapsFragment;
    private FragmentTasksList mTaskListFragment;
    private BottomNavigationView mBottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      init();


    }

    public void init(){
        mBottomNavigationView = findViewById(R.id.nav_view_bar);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        TaskViewModel model = ViewModelProviders.of(this).get(TaskViewModel.class);
        model.getItems().observe(this, new Observer<List<TaskItem>>() {
            @Override
            public void onChanged(@Nullable List<TaskItem> taskItems) {
                if (taskItems != null) {
                    mGeofenceMaker.crateGeofenceList(selectGeofencingTasks(taskItems));
                }else mGeofenceMaker.crateGeofenceList(new ArrayList<TaskItem>());
            }
        });
       //----------------Geofencing test-----------------------------------
     //   mGeofenceMaker.crateTestGeofenceList();
//        addGeofences();  //            ?????????????????
//-------------------------------------------------------------
        mTaskListFragment = new FragmentTasksList();
        mMapsFragment = new MapsFragment();


        setTabs();

        setFragment(mMapsFragment, true);

    }

    private void setFragment(Fragment fragment, boolean addToBackStack) {
        assert getFragmentManager() != null;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment, "map");
        fragmentTransaction.addToBackStack("map");//fragment container
        fragmentTransaction.commit();
    }

    private void setFragment(Fragment fragment) {
        assert getFragmentManager() != null;
        if (fragment instanceof MapsFragment) {
            getSupportFragmentManager().popBackStack();
        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container,fragment);
            fragmentTransaction.addToBackStack("other fragment");
            fragmentTransaction.commit();
        }

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
        Intent intent = new Intent(this,GeofencerService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    private void addGeofences() {
        checkLocationPermission();
        mGeofencingClient.addGeofences(mGeofenceMaker.getGeofencingRequestOfList(), getGeofencePendingIntent())
                .addOnSuccessListener(Objects.requireNonNull(this), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Toast.makeText(getApplicationContext(),"Geofence successfuly added", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Geofencees failed", Toast.LENGTH_LONG).show();
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

//-------------------------------------------------------------------

    private void doFragmentTransaction(Fragment fragment,boolean addToBackStack) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }

        fragmentTransaction.commit();
    }

    // es der piti kargavorvi
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}

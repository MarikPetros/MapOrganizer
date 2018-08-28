package com.example.marik.maporganizer.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.marik.maporganizer.activity.MainActivity;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.service.GeofencerService;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.marik.maporganizer.activity.MainActivity.PERMISSION_CODE;

public class GeofenceManager {
    private static GeofenceManager SINSTANSE;
    private Context mContext;
    private PendingIntent mGeofencePendingIntent;
    private GeofenceMaker mGeofenceMaker = GeofenceMaker.getGeofenceMakerInstance();

    private GeofenceManager(Context mContext) {
        this.mContext = mContext;
    }

    public static GeofenceManager getInstance(Context context) {
        if (SINSTANSE == null) {
            SINSTANSE = new GeofenceManager(context);
        }

        return SINSTANSE;
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofencerService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(mContext)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions((MainActivity)mContext,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions((MainActivity)mContext,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_CODE);
            }

    }

    public void addGeofences(List<TaskItem> taskItems) {
        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(mContext);

        checkLocationPermission();
        makeGeofencingRequest(taskItems);
        mGeofencingClient.addGeofences(mGeofenceMaker.getGeofencingRequestOfList(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext,"Geofence successfuly added", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext,"Geofencees failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    //getting items for geofencing
    private List<TaskItem> selectGeofencingTasks(List<TaskItem> taskItems) {
        List<TaskItem> items = new ArrayList<>();
        for (TaskItem item : taskItems) {
            if (item.isNotifyByPlace()) {
                items.add(item);
            }
        }
        return items;
    }

    //Making geofencing request
    private void makeGeofencingRequest(List<TaskItem> taskItems) {
        if (taskItems != null) {
            mGeofenceMaker.crateGeofenceList(selectGeofencingTasks(taskItems));
        } else mGeofenceMaker.crateGeofenceList(new ArrayList<TaskItem>());
    }
}



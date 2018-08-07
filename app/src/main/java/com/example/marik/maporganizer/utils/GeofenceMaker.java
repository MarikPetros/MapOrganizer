package com.example.marik.maporganizer.utils;

import android.provider.SyncStateContract;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

public class GeofenceMaker {
    private List<Geofence> mGeofenceList = new ArrayList<>();

    public void crateGeofences(TaskItem taskItem) // needed location and radius
    {

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(taskItem.getAddress.toString())

                .setCircularRegion(
                        taskItem.getAddress.getLatitude(),
                        taskItem.getAddress.getLongitude(),
                        taskItem.getAlertRadius()
                )
                .setExpirationDuration(NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER )
                .build());
    }
}

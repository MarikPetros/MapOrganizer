package com.example.marik.maporganizer.utils;

import com.example.marik.maporganizer.db.TaskItem;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofencingRequest.INITIAL_TRIGGER_ENTER;

public class GeofenceMaker {
    private static GeofenceMaker SINSTANCE;

    private GeofenceMaker() {
    }

    public static GeofenceMaker getGeofenceMakerInstance() {
        if (SINSTANCE == null) {
            SINSTANCE = new GeofenceMaker();
        }
        return SINSTANCE;
    }

    private List<Geofence> mGeofenceList = new ArrayList<>();

    private Geofence crateGeofence(TaskItem taskItem)
    {
        Geofence geofence = new Geofence() {
            @Override
            public String getRequestId() {
                return " ";
            }
        };
        if (taskItem !=null) {
            geofence = new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(taskItem.getId().toString())
                    .setCircularRegion(
                            taskItem.getLatitude(),
                            taskItem.getLongitude(),
                            taskItem.getAlertRadius()
                    )
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER )
                    .build();
        }
        return geofence;
    }


    public List<Geofence> crateGeofenceList(List<TaskItem> taskItems) {
        for (TaskItem task : taskItems) {
            mGeofenceList.add(crateGeofence(task));
        }
        return mGeofenceList;
    }


    public GeofencingRequest getGeofenceRequest(TaskItem taskItem) {
        GeofencingRequest request;
        request = new GeofencingRequest.Builder().
                addGeofence(crateGeofence(taskItem)).
                setInitialTrigger(INITIAL_TRIGGER_ENTER).
                build();

        return request;
    }

    public GeofencingRequest getGeofencingRequestOfList() {
        GeofencingRequest request = new GeofencingRequest.Builder().addGeofence(crateGeofence(new TaskItem())).build();
        if (!mGeofenceList.isEmpty()) {
            request = new GeofencingRequest.Builder().
                    addGeofences(mGeofenceList).
                    setInitialTrigger(INITIAL_TRIGGER_ENTER).
                    build();
        }

        return request;
    }
}

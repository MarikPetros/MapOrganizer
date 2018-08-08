package com.example.marik.maporganizer.utils;

import com.example.marik.maporganizer.item.TaskItem;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofencingRequest.INITIAL_TRIGGER_DWELL;

public class GeofenceMaker {
    private static final int NOTIFICATION_RESPONCIVENESS_VALUE = (1000 * 60 * 60 * 2);
    private List<Geofence> mGeofenceList = new ArrayList<>();

    public Geofence crateGeofence(TaskItem taskItem) // needed location and radius
    {
        Geofence geofence;
        geofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(taskItem.getAddress().toString())
               // .setNotificationResponsiveness(NOTIFICATION_RESPONCIVENESS_VALUE)
                .setCircularRegion(
                        taskItem.getAddress().getLatitude(),
                        taskItem.getAddress().getLongitude(),
                        taskItem.getRadius()
                )
                .setExpirationDuration(NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL )
                .setLoiteringDelay(1000 * 60 * 60 * 3)
                .build();
        return geofence;
    }


    public List<Geofence> crateGeofenceList(List<TaskItem> taskItems)
    {
        for (TaskItem task : taskItems) {
            mGeofenceList.add(crateGeofence(task));
        }

        return mGeofenceList;
    }

    public  GeofencingRequest getGeofenceRequest(TaskItem taskItem){
        GeofencingRequest request;
        request = new  GeofencingRequest.Builder().
                addGeofence(crateGeofence(taskItem)).
                setInitialTrigger(INITIAL_TRIGGER_DWELL).
                build();

        return request;
    }

    public  GeofencingRequest getGeofencingRequestOfList(){
        GeofencingRequest request;
        request = new  GeofencingRequest.Builder().
                addGeofences(mGeofenceList).
                setInitialTrigger(INITIAL_TRIGGER_DWELL).
                build();

        return request;
    }
}

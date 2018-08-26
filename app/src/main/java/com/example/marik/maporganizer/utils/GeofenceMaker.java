package com.example.marik.maporganizer.utils;

import com.example.marik.maporganizer.db.TaskItem;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofencingRequest.INITIAL_TRIGGER_DWELL;
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
                    // .setNotificationResponsiveness(NOTIFICATION_RESPONCIVENESS_VALUE)
                    .setCircularRegion(
                            taskItem.getAddress().getLatitude(),
                            taskItem.getAddress().getLongitude(),
                            taskItem.getAlertRadius()
                    )
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER )
                   // .setLoiteringDelay(1000 * 60)
                    .build();
        }
        return geofence;
    }

   /* private Geofence crateGeofence(TaskItem taskItem) {
        Geofence geofence = new Geofence() {
            @Override
            public String getRequestId() {
                return null;
            }
        };
        if (taskItem != null) {
            Geofence.Builder builder = new Geofence.Builder();
            builder.setRequestId(taskItem.getId().toString());
            builder.setCircularRegion(
                    taskItem.getAddress().getLatitude(),
                    taskItem.getAddress().getLongitude(),
                    taskItem.getAlertRadius()
            );
            builder.setExpirationDuration(NEVER_EXPIRE);
            builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
            builder.build();
        }
        return geofence;
    }*/


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
        GeofencingRequest request;
        request = new GeofencingRequest.Builder().
                addGeofences(mGeofenceList).
                setInitialTrigger(INITIAL_TRIGGER_ENTER).
                build();

        return request;
    }

    //--------------------------------------------For test--------------------------------------------------
    private Geofence crateTestGeofence(String name, double v, double v1, int v2) // needed location and radius
    {
        Geofence geofence = new Geofence() {
            @Override
            public String getRequestId() {
                return null;
            }
        };

        geofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(name)
                // .setNotificationResponsiveness(NOTIFICATION_RESPONCIVENESS_VALUE)
                .setCircularRegion(
                        v, // taskItem.getAddress().getLatitude(),
                        v1,//taskItem.getAddress().getLongitude(),
                        v2// taskItem.getRadius()
                )
                .setExpirationDuration(NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                //       .setLoiteringDelay(1000 * 60)
                .build();

        return geofence;
    }

    public List<Geofence> crateTestGeofenceList() {

        mGeofenceList.add(crateTestGeofence("1001", 40.48957148408654, 44.76345129311084, 100));
        mGeofenceList.add(crateTestGeofence("2002", 40.489595197742666, 44.766380935907364, 100));
        mGeofenceList.add(crateTestGeofence("3003", 40.490793873895186, 44.76555313915014, 100));

        return mGeofenceList;
    }
}

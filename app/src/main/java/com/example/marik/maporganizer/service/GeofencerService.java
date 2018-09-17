package com.example.marik.maporganizer.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.activity.MainActivity;
import com.example.marik.maporganizer.db.Converters;
import com.example.marik.maporganizer.db.TaskRepository;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static android.app.Notification.VISIBILITY_PUBLIC;
import static android.content.ContentValues.TAG;

public class GeofencerService extends IntentService {
    int SUMMARY_ID = 0;
    String GROUP_KEY_GEOFENCE_ALERT = "com.example.marik.maporganizer.GEOFENCE_ALERT";
    public static final String TRIGGERING_LOCATIONS = "com.example.marik.maporganizer.TRIGGERING_LOCATION";
    public static final String GEOFENCE_CHANNEL_ID = "geofence";

    private NotificationCompat.Builder mBuilder;
    private String explanation;
    private String dismissalId;
    private String notificationTag;
    private Location location;
    private TaskRepository taskRepository;

    public GeofencerService() {
        super(GeofencerService.class.getSimpleName());
        taskRepository = TaskRepository.getRepository(getApplication());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // Get event location
        location = geofencingEvent.getTriggeringLocation();

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
            Toast.makeText(getApplicationContext(), geofenceTransitionDetails, Toast.LENGTH_LONG).show();

        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
            Toast.makeText(getApplicationContext(), R.string.geofence_transition_invalid_type, Toast.LENGTH_LONG).show();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendNotification(String geofenceTransitionDetails) {
        createNotification(geofenceTransitionDetails);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationManager.notify(SUMMARY_ID, mSummaryBuilder.build());
        int notificationId = 222;
        notificationManager.notify(notificationTag, notificationId, mBuilder.build());
    }

    private String getGeofenceTransitionDetails(GeofencerService geofencerService, int geofenceTransition, List<Geofence> triggeringGeofences) {
        StringBuilder stringBuilder = new StringBuilder();

        //For test
        //      String notifText = "datark a";

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            stringBuilder.append(getString(R.string.alert_text));
            List<String> tags = new ArrayList<>();
            List<String> addresses = new ArrayList<>();
            //           int i=0;
            for (Geofence g : triggeringGeofences) {
                //           notifText = g.getRequestId(); /// This is for test
                notificationTag = g.getRequestId();
                String itemAddress = null;
                try {
                    itemAddress = taskRepository.getById(UUID.fromString(g.getRequestId())).getChoosedAddress();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tags.add(notificationTag);
                addresses.add(itemAddress);
            }
            // making content for bigText
            explanation = getString(R.string.explanation) + " " + addresses.get(0);

            // setting notification's tag with first tasks UUID
            notificationTag = tags.get(0);

            // setting notification's dismissalId with first tasks UUID
            dismissalId = notificationTag; // ids.get(0).toString();

            //complete geofenceTransitionDetails text
            stringBuilder.append(addresses.get(0));
//            stringBuilder.append(notifText);
        }
        return stringBuilder.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createNotification(String geofenceTransitionDetails) {
        // Create an explicit intent for  MaosActivity
        Intent intent = new Intent(this, MainActivity.class);
        ArrayList<Location> locations = new ArrayList<>();
        locations.add(location);
        intent.putParcelableArrayListExtra(TRIGGERING_LOCATIONS, locations);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Greate wearableExtender
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        wearableExtender.setDismissalId(dismissalId);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
                                                                                                       //String CHANNEL_ID = "Geofence";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(GEOFENCE_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.GREEN);
            channel.shouldShowLights();
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(VISIBILITY_PUBLIC);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // using NotificationCompat
        mBuilder = new NotificationCompat.Builder(this,GEOFENCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_nearby)
                .setContentTitle(getString(R.string.nearby))
                .setContentText(geofenceTransitionDetails)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(explanation))
                .setVisibility(VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(GROUP_KEY_GEOFENCE_ALERT)
                .extend(wearableExtender)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);


        NotificationCompat.Builder mSummaryBuilder = new NotificationCompat.Builder(this, GEOFENCE_CHANNEL_ID)
                .setContentTitle(getString(R.string.nearTasks))
                //set content text to support devices running API level < 24
                .setContentText("New geofence messages")
                .setSmallIcon(R.drawable.ic_notif_nearby)
                //build summary info into InboxStyle template
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(getString(R.string.some_messages)))
                //specify which group this notification belongs to
                .setGroup(GROUP_KEY_GEOFENCE_ALERT)
                .extend(wearableExtender)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                //set this notification as the summary for the group
                .setGroupSummary(true);
    }

    public Location getLocation() {
        return location;
    }
}
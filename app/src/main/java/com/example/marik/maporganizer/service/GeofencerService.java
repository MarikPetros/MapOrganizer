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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.activity.MapsActivity;
import com.example.marik.maporganizer.db.TaskRepository;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Notification.VISIBILITY_PUBLIC;
import static android.content.ContentValues.TAG;

public class GeofencerService extends IntentService {
    int SUMMARY_ID = 0;
    String GROUP_KEY_GEOFENCE_ALERT = "com.example.marik.maporganizer.GEOFENCE_ALERT";

    private NotificationCompat.Builder mBuilder;
    private NotificationCompat.Builder mSummaryBuilder;
    private String explanation;
    int notificationId;
    private Location location;
    private TaskRepository taskRepository;

    public GeofencerService() {
        super(GeofencerService.class.getSimpleName());
        taskRepository = TaskRepository.getRepository(getApplication());
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get event location
        location = geofencingEvent.getTriggeringLocation();

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

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
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
        }
    }

    private void sendNotification(String geofenceTransitionDetails) {
        createNotification(geofenceTransitionDetails);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, mBuilder.build());
        notificationManager.notify(SUMMARY_ID, mSummaryBuilder.build());
    }

    private String getGeofenceTransitionDetails(GeofencerService geofencerService, int geofenceTransition, List<Geofence> triggeringGeofences) {
        StringBuilder stringBuilder = new StringBuilder();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            stringBuilder.append(getString(R.string.alert_text));
            List<Integer> ids = new ArrayList<>();
            List<String> addresses = new ArrayList<>();
            for (Geofence g : triggeringGeofences) {
                notificationId = Integer.parseInt(g.getRequestId());
                Address itemAddress = taskRepository.getById(UUID.fromString(g.getRequestId())).getAddress();
                ids.add(notificationId);
                addresses.add(itemAddress.toString() + "/n");
            }
            // making content for bigText
            explanation = getString(R.string.explanation) + addresses.toString();

            // setting notification's id with first tasks UUID
            notificationId = ids.get(0);

            //complete geofenceTransitionDetails text
            stringBuilder.append(addresses.get(0));
        }
        return stringBuilder.toString();
    }

    private void createNotification(String geofenceTransitionDetails) {
        // Create an explicit intent for  MaosActivity
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Greate wearableExtender
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        String CHANNEL_ID = "Geofence";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
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
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
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
                .setAutoCancel(true);



        mSummaryBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
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
                //set this notification as the summary for the group
                .setGroupSummary(true);
    }

    public Location getLocation() {
        return location;
    }
}
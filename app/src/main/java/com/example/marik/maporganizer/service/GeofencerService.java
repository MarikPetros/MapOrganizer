package com.example.marik.maporganizer.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.activity.MapsActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.Random;

import static android.app.Notification.VISIBILITY_PUBLIC;
import static android.content.ContentValues.TAG;
import static java.lang.Math.random;

public class GeofencerService extends IntentService {
    private String CHANNEL_ID = "Geofence";
    private NotificationCompat.Builder mBuilder;
    private String explanation;

    public GeofencerService() {
        super(GeofencerService.class.getSimpleName());
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

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
        Random random = new Random();
        createNotification(geofenceTransitionDetails);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = random.nextInt(100);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private String getGeofenceTransitionDetails(GeofencerService geofencerService, int geofenceTransition, List<Geofence> triggeringGeofences) {
        StringBuilder stringBuilder = new StringBuilder();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            stringBuilder.append(getString(R.string.alert_text));
        }
        // TODO get location and append to stringBuilder
        for (Geofence g : triggeringGeofences){
           stringBuilder.append(g.getRequestId());
           explanation = getString(R.string.explanation) + g.getRequestId();
        }
        return stringBuilder.toString();
    }

    private void createNotification(String geofenceTransitionDetails) {

        // Create an explicit intent for  MaosActivity
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
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
                .setContentTitle("NearBy")
                .setContentText(geofenceTransitionDetails)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))// what must todo
                .setVisibility(VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setTimeoutAfter(1000 * 60 * 60)
                .setAutoCancel(true);

    }

}
package com.example.marik.maporganizer.service;

import android.content.Context;

import com.example.marik.maporganizer.R;
import com.google.android.gms.location.GeofenceStatusCodes;

class GeofenceErrorMessages {
    public static String getErrorString(Context context, int statusCodes){
        String errorstring = context.getString(R.string.error_NotSpecified);
        switch (statusCodes){
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                errorstring = context.getString(R.string.error_code_NotAvailable);
                break;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                errorstring = context.getString(R.string.error_code_TooManyGeafences);
                break;
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                errorstring = context.getString(R.string.error_code_TooManyPendingintents);
                break;
        }
        return errorstring;
    }
}

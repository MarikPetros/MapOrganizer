package com.example.marik.maporganizer.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.marik.maporganizer.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Objects;

import static com.example.marik.maporganizer.activity.MainActivity.PERMISSION_CODE;
import static java.security.AccessController.getContext;

public class MyCurrentLocation {
    private Context mContext;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;

    public MyCurrentLocation(Context mContext) {
        this.mContext = mContext;
    }

    public Location getCurrentLocation() {
        checkLocationPermission();
        Task<Location> task = null;
        if (mContext != null) {
            task = mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener((Activity) Objects.requireNonNull(mContext), new OnSuccessListener<Location>() {
                        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mCurrentLocation = location;
                                moveCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                                        15f, "my location");
                            }
                        }
                    });
        }

        if (task != null/**/) {
            mCurrentLocation = task.getResult();
        }
        return mCurrentLocation;
    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull((Activity) mContext),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder((Activity) mContext)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions((Activity) mContext,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions((Activity) mContext,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_CODE);
            }
        }
    }

    //@Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull((Activity) mContext),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(mContext, "permission denied", Toast.LENGTH_LONG).show();
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
         /*
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = getCurrentLocation();
                if (mCurrentLocationMarker != null) {
                    mCurrentLocationMarker.remove();
                }

                //Place current location marker
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng)
                            .title("Current Position")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.kid_icon));

                    // mCurrentLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));


                }
            }*/
        }
    };


    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (title.equals("my location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng);
            //  mMarker = mMap.addMarker(options);
        }
        //   hideKeyboard(getActivity());
    }
}

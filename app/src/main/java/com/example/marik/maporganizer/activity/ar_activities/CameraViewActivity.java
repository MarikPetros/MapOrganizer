package com.example.marik.maporganizer.activity.ar_activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.fragments.TaskFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CameraViewActivity extends Activity implements
        SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener {

    public Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean isCameraviewOn = false;
    private AugmentedPOI mPoi;

    private double mAzimuthReal = 0;
    private double mAzimuthTeoretical = 0;
    private static double AZIMUTH_ACCURACY = 5;
    private double mMyLatitude = 0;
    private double mMyLongitude = 0;
    private double[] mLatLng;
    private MyCurrentAzimuth myCurrentAzimuth;
    private MyCurrentLocation myCurrentLocation;
    String address;

    TextView descriptionTextView;
    ImageView pointerIcon, leftIcon, rightIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission();
        setContentView(R.layout.activity_camera_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        address = getIntent().getStringExtra(TaskFragment.ADDRESS);
        mLatLng = getIntent().getDoubleArrayExtra(TaskFragment.LAT_LANG_FOR_AR);
        setupListeners();
        setupLayout();
        setAugmentedRealityPoint(address, mLatLng[0], mLatLng[1]);
    }

    private void setAugmentedRealityPoint(String address, double latitude, double longitude) {
        mPoi = new AugmentedPOI(address, latitude, longitude);
    }

    public double calculateTeoreticalAzimuth() {
        double dX = mPoi.getPoiLatitude() - mMyLatitude;
        double dY = mPoi.getPoiLongitude() - mMyLongitude;

        double phiAngle;
        double tanPhi;
        double azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quater
            return azimuth = phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            return azimuth = 180 - phiAngle;
        } else if (dX < 0 && dY < 0) { // III
            return azimuth = 180 + phiAngle;
        } else if (dX > 0 && dY < 0) { // IV
            return azimuth = 360 - phiAngle;
        }

        return phiAngle;
    }

    private List<Double> calculateAzimuthAccuracy(double azimuth) {
        double minAngle = azimuth - AZIMUTH_ACCURACY;
        double maxAngle = azimuth + AZIMUTH_ACCURACY;
        List<Double> minMax = new ArrayList<Double>();

        if (minAngle < 0)
            minAngle += 360;

        if (maxAngle >= 360)
            maxAngle -= 360;

        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);

        return minMax;
    }

    private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        if (minAngle > maxAngle) {
            return isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth);
        } else {
            return azimuth > minAngle && azimuth < maxAngle;
        }
    }


    private void updateDescription() {
        descriptionTextView.setText(address);
    }

    @Override
    public void onLocationChanged(Location location) {
        mMyLatitude = location.getLatitude();
        mMyLongitude = location.getLongitude();
        mAzimuthTeoretical = calculateTeoreticalAzimuth();
        updateDescription();
    }

    @Override
    public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {
        mAzimuthReal = azimuthChangedTo;
        mAzimuthTeoretical = calculateTeoreticalAzimuth();

        pointerIcon = findViewById(R.id.icon);
        leftIcon = findViewById(R.id.left_arraw);
        rightIcon = findViewById(R.id.arrow_right);

        double minAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);

        double maxAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);
        double minDelta = minAngle - 90;
        double maxDelta = maxAngle + 90;
        if (isBetween(minAngle-10, maxAngle+10, mAzimuthReal)) {
            pointerIcon.setVisibility(View.VISIBLE);
            leftIcon.setVisibility(View.INVISIBLE);
            rightIcon.setVisibility(View.INVISIBLE);
        } else if (isBetween(minDelta, minAngle-20, mAzimuthReal)) {
            pointerIcon.setVisibility(View.INVISIBLE);
            leftIcon.setVisibility(View.INVISIBLE);
            rightIcon.setVisibility(View.VISIBLE);

        } else if (isBetween(maxAngle+20, maxDelta, mAzimuthReal)) {
            pointerIcon.setVisibility(View.INVISIBLE);
            leftIcon.setVisibility(View.VISIBLE);
            rightIcon.setVisibility(View.INVISIBLE);
        } else {
            pointerIcon.setVisibility(View.INVISIBLE);
            leftIcon.setVisibility(View.INVISIBLE);
            rightIcon.setVisibility(View.INVISIBLE);
        }
        updateDescription();
    }

    @Override
    protected void onStop() {
        myCurrentAzimuth.stop();
        myCurrentLocation.stop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myCurrentAzimuth.start();
        myCurrentLocation.start();
    }

    private void setupListeners() {
        myCurrentLocation = new MyCurrentLocation(this,this);
        myCurrentLocation.buildGoogleApiClient(this);
        myCurrentLocation.start();

        myCurrentAzimuth = new MyCurrentAzimuth(this, this);
        myCurrentAzimuth.start();
    }

    private void setupLayout() {
        descriptionTextView = findViewById(R.id.cameraTextView);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = findViewById(R.id.cameraview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (isCameraviewOn) {
            mCamera.stopPreview();
            isCameraviewOn = false;
        }

        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                isCameraviewOn = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            //ask for authorisation
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        else {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        isCameraviewOn = false;
    }
}

//package com.example.marik.maporganizer.activity.ar_activities;
//
//import android.app.Activity;
//import android.content.Context;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//
//import com.example.marik.maporganizer.R;
//
//public class SensorActivity extends Activity implements SensorEventListener {
//
//    private SensorManager mSensorManager;
//    private final float[] mAccelerometerReading = new float[3];
//    private final float[] mMagnetometerReading = new float[3];
//
//    private final float[] mRotationMatrix = new float[9];
//    private final float[] mOrientationAngles = new float[3];
//    public float deviceAzimuth;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sensor);
//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//    }
//
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        // Do something here if sensor accuracy changes.
//        // You must implement this callback in your code.
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // Get updates from the accelerometer and magnetometer at a constant rate.
//        // To make batch operations more efficient and reduce power consumption,
//        // provide support for delaying updates to the application.
//        //
//        // In this example, the sensor reporting delay is small enough such that
//        // the application receives an update before the system checks the sensor
//        // readings again.
//        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        if (accelerometer != null) {
//            mSensorManager.registerListener(this, accelerometer,
//                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
//        }
//        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        if (magneticField != null) {
//            mSensorManager.registerListener(this, magneticField,
//                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // Don't receive any more updates from either sensor.
//        mSensorManager.unregisterListener(this);
//    }
//
//    // Get readings from accelerometer and magnetometer. To simplify calculations,
//    // consider storing these readings as unit vectors.
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            System.arraycopy(event.values, 0, mAccelerometerReading,
//                    0, mAccelerometerReading.length);
//        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            System.arraycopy(event.values, 0, mMagnetometerReading,
//                    0, mMagnetometerReading.length);
//        }
//    }
//
//    // Compute the three orientation angles based on the most recent readings from
//    // the device's accelerometer and magnetometer.
//    public void updateOrientationAngles() {
//        // Update rotation matrix, which is needed to update orientation angles.
//        SensorManager.getRotationMatrix(mRotationMatrix, null,
//                mAccelerometerReading, mMagnetometerReading);
//
//        // "mRotationMatrix" now has up-to-date information.
//
//        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
//
//        // "mOrientationAngles" now has up-to-date information.
//    }
//
//    public float getDeviceAzimut() {
//        deviceAzimuth = mOrientationAngles[0];
//        return deviceAzimuth;
//    }
//}
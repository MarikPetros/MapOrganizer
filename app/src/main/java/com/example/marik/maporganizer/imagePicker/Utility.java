package com.example.marik.maporganizer.imagePicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.marik.maporganizer.activity.MainActivity;

public class Utility {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public static void checkPermissions(Context context) {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(context, permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, permissions[2]) == PackageManager.PERMISSION_GRANTED) {
         // pickImage(context);

        } else{
            ActivityCompat.requestPermissions((Activity) context, permissions, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);}

    }


//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    public static boolean checkPermission(final Context context)
//    {
//        int currentAPIVersion = Build.VERSION.SDK_INT;
//        if(currentAPIVersion>= Build.VERSION_CODES.M)
//        {
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA)) {
//                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
//                    alertBuilder.setCancelable(true);
//                    alertBuilder.setTitle("Permission necessary");
//                    alertBuilder.setMessage("External storage permission is necessary");
//                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//                        public void onClick(DialogInterface dialog, int which) {
//                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}
//                            , MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//                        }
//                    });
//                    AlertDialog alert = alertBuilder.create();
//                    alert.show();
//
//                } else {
//                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//                }
//                return false;
//            } else {
//                return true;
//            }
//        } else {
//            return true;
//        }
//    }
//

}

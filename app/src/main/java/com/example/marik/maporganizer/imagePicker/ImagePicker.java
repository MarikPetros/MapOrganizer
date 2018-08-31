package com.example.marik.maporganizer.imagePicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.example.marik.maporganizer.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.graphics.TypefaceCompatUtil.getTempFile;


public class ImagePicker {

    public static Uri selectedImageUri;

    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
    private static final String TAG = "ImagePicker";
    private static final String TEMP_IMAGE_NAME = "tempImage";
    private static final int TAKE_PHOTO_ID = 1;
    private static final int GET_FROM_FILE = 2;

    public static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;


    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;
        //boolean check = Utility.checkPermission(context);
        List<Intent> intentList = new ArrayList<>();
        // if (check) {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
pickIntent.putExtra("pick intent", GET_FROM_FILE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        takePhotoIntent.putExtra("TAKE_PHOTO_ID", TAKE_PHOTO_ID);
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);
        // }
        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            Log.v("package name+ ", "" + resolveInfo.activityInfo.packageName + "");
            targetedIntent.setPackage(packageName);

            list.add(targetedIntent);
            Log.d(TAG, "Intent: " + intent.getAction() + " package: " + packageName);
        }
        return list;
    }

    @SuppressLint("RestrictedApi")
    public static Uri getUriFromResult(Context context, int resultCode,int requestCode,
                                            Intent imageReturnedIntent) {
        Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        Bitmap bm = null;

      File imageFile = getTempFile(context);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO_ID) {
                boolean isCamera = (imageReturnedIntent == null ||
                        imageReturnedIntent.getData() == null ||
                        imageReturnedIntent.getData().toString().contains(imageFile.toString()));

                if (isCamera) {
                    /** CAMERA **/
                    selectedImageUri = Uri.fromFile(imageFile);
                } else {            /** ALBUM **/
                    selectedImageUri = imageReturnedIntent.getData();
                }
                Log.d(TAG, "selectedImage: " + selectedImageUri);

//                bm = getImageResized(context, selectedImageUri);
//                int rotation = getRotation(context, selectedImageUri, isCamera);
//                bm = rotate(bm, rotation);
            }}
        //    return bm;
        return selectedImageUri;

        }

    public static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }
}
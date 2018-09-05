package com.example.marik.maporganizer.imagePicker;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class WriteBitmapToFileTask extends AsyncTask<Void, Void, String> {

    private Bitmap mBitmap;
    private String mPath;
    private WeakReference<OnResultListener> mListenerWeakReference;

    public WriteBitmapToFileTask(Bitmap pBitmap, String pPath, OnResultListener pOnResultListener)  {
        mBitmap = pBitmap;
        mPath = pPath;
        mListenerWeakReference = new WeakReference<>(pOnResultListener);
    }

    @Override
    protected String doInBackground(Void... pVoids) {
        try {
            String filePath = mPath;

            if (mBitmap != null) {
                FileOutputStream stream = new FileOutputStream(filePath);

                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                //Cleanup
                stream.close();
            }

            return filePath;
        } catch (Exception pE) {
            pE.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String pPath) {
        OnResultListener onResultListener = mListenerWeakReference.get();
        if (onResultListener != null)
            onResultListener.onBitmapSavedResult(pPath, mBitmap);
    }

    public interface OnResultListener {
        void onBitmapSavedResult(String pPath, Bitmap pBitmap);
    }
}
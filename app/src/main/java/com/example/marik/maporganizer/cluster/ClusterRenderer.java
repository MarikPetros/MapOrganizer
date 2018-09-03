package com.example.marik.maporganizer.cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class ClusterRenderer extends DefaultClusterRenderer<TaskItem> {
    private final IconGenerator mClusterIconGenerator;
    Context mContext;

    public ClusterRenderer(Context context,GoogleMap map,ClusterManager<TaskItem> clusterManager) {
        super(context,map,clusterManager);
        mContext = context;
        mClusterIconGenerator = new IconGenerator(mContext.getApplicationContext());
    }

    @Override
    protected void onBeforeClusterItemRendered(TaskItem clusters,MarkerOptions markerOptions) {
        // Customize the marker here
        markerOptions
                .position(clusters.getPosition())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_pin));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<TaskItem> cluster,MarkerOptions markerOptions) {
        // Customize the cluster here
        mClusterIconGenerator.setBackground(
                ContextCompat.getDrawable(mContext,R.drawable.cluster_background_circle));

     //mClusterIconGenerator.setTextAppearance(R.style.AppTheme_WhiteTextAppearance); // causes some problems on lower versions

        final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

    }
}

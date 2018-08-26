package com.example.marik.maporganizer.cluster;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.Map;

public class ClusterRenderer extends DefaultClusterRenderer<Clusters>{


    public ClusterRenderer(Context context,GoogleMap map,ClusterManager<Clusters> clusterManager) {
        super(context,map,clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(Clusters clusters, MarkerOptions markerOptions) {
        // Customize the marker here
        markerOptions
                .position(clusters.getPosition())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<Clusters> cluster, MarkerOptions markerOptions) {
        // Customize the cluster here
        markerOptions
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
    }


    Map<Cluster<Clusters>, Marker> clusterMarkerMap = new HashMap<>();
    @Override
    protected void onClusterRendered(Cluster<Clusters> cluster,Marker marker) {
        super.onClusterRendered(cluster,marker);
        clusterMarkerMap.put(cluster, marker);
    }
}
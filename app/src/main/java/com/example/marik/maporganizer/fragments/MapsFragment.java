package com.example.marik.maporganizer.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.adapters.PlaceAutocompleteAdapter;
import com.example.marik.maporganizer.adapters.SectionPagerAdapter;
import com.example.marik.maporganizer.cluster.Clusters;
import com.example.marik.maporganizer.models.PlaceInfo;
import com.example.marik.maporganizer.service.GeofencerService;
import com.example.marik.maporganizer.utils.GeofenceMaker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapsFragment extends Fragment  implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private final static int PERMISSION_CODE = 26;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-169),new LatLng(70,137));
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private Marker mCurrentLocationMarker;
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private ClusterManager<Clusters> mClusterManager;
    private FrameLayout mFrameLayout;
    private BottomNavigationView mBottomNavigationView;
    private FragmentTasksList mFragmentTasksList;
    private FragmentActivity mFragmentActivity;
    private FragmentTaskCreation fragmentTaskCreation;
    private Boolean mLocationPermissionsGranted = false;

    private Marker mMarker;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private GeofenceMaker mGeofenceMaker = new GeofenceMaker();
    private SectionPagerAdapter mSectionPagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mLayout;
     SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;



    public class Bootreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {
            addGeofences();
        }
    }

    private OnFragmentInteractionListener mListener;

    public MapsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MapsFragment newInstance(String param1,String param2) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater  inflater,ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map,container,false);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(supportMapFragment==null){
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            supportMapFragment = SupportMapFragment.newInstance();
            transaction.replace(R.id.map, supportMapFragment).commit();
        }
        supportMapFragment.getMapAsync(this);

        return rootView;
    }



    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        initOnViewCreated(view);
    }

    public void initOnViewCreated(View root) {
       mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        /*mGeofencingClient = LocationServices.getGeofencingClient(this);
         addGeofences();*/
        mSearchText = (AutoCompleteTextView) root.findViewById(R.id.input_search);

        mGps = (ImageView) root.findViewById(R.id.ic_gps);
        checkLocationPermission();


        // Tabs navigation
        mBottomNavigationView = (BottomNavigationView) root.findViewById(R.id.nav_view_bar);

        mFrameLayout = (FrameLayout) root.findViewById(R.id.fragment_container);

        Button mButton = root.findViewById(R.id.add);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         //    mapFragment.getView().setVisibility(View.INVISIBLE);
                /*mLayout = v.findViewById(R.id.frame_for_map);
                mLayout.setVisibility(View.INVISIBLE);
                FragmentTaskCreation mTaskCreation = FragmentTaskCreation.newInstance(null);*/
               // android.support.v4.app.FragmentManager mFragmentManager = getSupportFragmentManager();
               // android.support.v4.app.FragmentTransaction mTransaction = mFragmentManager.beginTransaction();
//                mTransaction.add(R.id.fragment_container,mTaskCreation,"Creation");
//                mTransaction.addToBackStack("creation");
//                mTransaction.commit();
            }

        });
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(), "Map is Ready", Toast.LENGTH_SHORT).show();

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); // ten second interval
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setSmallestDisplacement(20);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        onMapClick();
        initSearch();
    }


    private void onMapClick() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                // Clears the previously touched position
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(markerOptions);

                //TODO open a new fragment which adds new event
                //TODO change marker positition on long click
            }

        });

        setUpClusterer();
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = getCurrentLocation();
                if (mCurrentLocationMarker != null) {
                    mCurrentLocationMarker.remove();
                }

                //Place current location marker
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mCurrentLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

                }
            }
        }
    };


    public Location getCurrentLocation() {
        checkLocationPermission();
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(Objects.requireNonNull(getActivity()),new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mCurrentLocation = location;
                            moveCamera(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()),
                                    DEFAULT_ZOOM,"my location");
                        }
                    }
                });
        return mCurrentLocation;
    }



    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(),"permission denied",Toast.LENGTH_LONG).show();
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    //-------------------------------------------------------------------------------------

    private void initSearch() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
               .enableAutoManage(getActivity(),this)
                .build();

        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(),mGoogleApiClient,LAT_LNG_BOUNDS,null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView,int actionId,KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    // dont forget to executee method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCurrentLocation();
            }
        });

        hideKeyboard(((Activity) getActivity()));
    }

    private void geoLocate() {
        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString,1);
        } catch (IOException e) {
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }
    }


    private void moveCamera(LatLng latLng,float zoom,String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        if (!title.equals("my location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMarker = mMap.addMarker(options); //mMap
        }
        hideKeyboard( getActivity());
    }

    private void hideKeyboard(Activity  activity) {
        try{
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocusedView = activity.getCurrentFocus();
            if (currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }   try{
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocusedView = activity.getCurrentFocus();
            if (currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    //-----------------------------Sections aka tabs -------------------------------------------

    private void setupViewPager(ViewPager mViewPager) {
        //SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        // adapter.addFragment(new FragmentTasksList());
        //ToDO check what i should do in this case to add the maps activity
        //  mViewPager.setAdapter(adapter);
    }


    /*
   -----------------------------google places API autocomplete suggestions------------
   */
    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
            hideKeyboard(((Activity) getActivity()));

            final AutocompletePrediction mAutocompletePrediction = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = mAutocompletePrediction.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId);
            placeResult.setResultCallback(mPLacesUbdDetailsCallback);
        }
    };


    private ResultCallback<PlaceBuffer> mPLacesUbdDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();//to prevent the memory leak
                return;
            }

            final Place place = places.get(0);

            //later we will create a popup window on the marker click
            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setId(place.getId());
                mPlace.setLatLng(place.getLatLng());
                mPlace.setWebsiteUri(place.getWebsiteUri());
            } catch (NullPointerException e) {
            }

            moveCamera(new LatLng(Objects.requireNonNull(place.getViewport()).getCenter().latitude,
                    place.getViewport().getCenter().longitude),DEFAULT_ZOOM,mPlace.getName());

            places.release();


        }
    };


    //-----------------------Marker clustering-------------
    //
    private void setUpClusterer() {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.177200,-44.503490),14));

        mClusterManager = new ClusterManager<Clusters>(getContext(),mMap);

        // point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        addItems();
    }



    //just to be sure that it works! (will delete it later)
    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 40.177200;
        double lng = 44.503490;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            Clusters offsetItem = new Clusters(lat,lng);
            mClusterManager.addItem(offsetItem);
        }
    }


    /**
     * -----------------------  Geofencing ----------------------------------------------------------------------------------------------------------------------------------------------
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getContext(),GeofencerService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(getContext(),0,intent,PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    private void addGeofences() {
        checkLocationPermission();
        mGeofencingClient.addGeofences(mGeofenceMaker.getGeofencingRequestOfList(),getGeofencePendingIntent())
                .addOnSuccessListener(getActivity(),new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener(getActivity(),new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        e.printStackTrace();
                    }
                });
    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

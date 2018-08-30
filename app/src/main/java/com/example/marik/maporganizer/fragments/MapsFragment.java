package com.example.marik.maporganizer.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.cluster.Clusters;
import com.example.marik.maporganizer.cluster.ClusterRenderer;
import com.example.marik.maporganizer.cluster.DataParser;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.models.PlaceInfo;
import com.example.marik.maporganizer.viewModel.TaskViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import static android.content.Context.LOCATION_SERVICE;


public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private final static int PERMISSION_CODE = 26;
    private static final float DEFAULT_ZOOM = 15f;
    private final static String GEOFENCING_LOCATIONS = "Geofence triggering locations";
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-169), new LatLng(44,137));

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
    private Marker mMarker;
    private SupportMapFragment supportMapFragment;
    private OnFragmentInteractionListener mListener;
    private TaskViewModel mViewModel;
    ArrayList<LatLng> MarkerPoints;

    double mLatitude=0;
    double mLongitude=0;

    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance(ArrayList<Location> locations) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(GEOFENCING_LOCATIONS, locations); //get from this for a destination point
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient
                .Builder(Objects.requireNonNull(getContext()))
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(Objects.requireNonNull(getActivity()),this)
                .build();

        }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of((Objects.requireNonNull(getActivity()))).get(TaskViewModel.class);
        mViewModel.getItems();

    }

    private void setMarkerState(List<TaskItem> taskItems) {
        for (TaskItem item : taskItems) {
            LatLng latLng = new LatLng(item.getLatitude(),item.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    //.title(latLng.toString())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_pin)));
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map,container,false);

        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment == null) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            supportMapFragment = SupportMapFragment.newInstance();
            transaction.replace(R.id.map,supportMapFragment).commit();
        }
        supportMapFragment.getMapAsync(this);


        return rootView;
    }



    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        initOnViewCreated(view);
    }

    @Override
    public void onPause() {
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();

        super.onPause();
    }

    public void initOnViewCreated(View root) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));

        mSearchText = root.findViewById(R.id.input_search);
        mGps = root.findViewById(R.id.ic_gps);
        checkLocationPermission();

        MarkerPoints = new ArrayList<>();
    }

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

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(),"Map is Ready",Toast.LENGTH_SHORT).show();

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

    @Override
    public void onResume() {
        super.onResume();
//added, needs to be tested
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        mViewModel.getItems().observe(this, new Observer<List<TaskItem>>() {
            @Override
            public void onChanged(@Nullable List<TaskItem> taskItems) {
                setMarkerState(taskItems);
            }
        });
    }

    private void onMapClick() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        //  to retrieve provider
        Criteria criteria = new Criteria();

        // the best provider
        assert locationManager != null;
        String provider = locationManager.getBestProvider(criteria,true);

        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(provider);


      //  Location location = getCurrentLocation();
        if (location != null) {
            onLocationChanged(location);
        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
            @Override
            public void onMapClick(LatLng latLng) {

                if (MarkerPoints.size() > 1) {
                    MarkerPoints.clear();
                    mMap.clear();

                    LatLng startPoint = new LatLng(mLatitude, mLongitude);
                    drawMarker(startPoint);
                }
                   drawMarker(latLng);

                if (MarkerPoints.size() == 2) {
                    LatLng origin = MarkerPoints.get(0);
                    LatLng dest = MarkerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getUrl(origin,dest);
                    FetchUrl FetchUrl = new FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                }

            }
        });


        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        setUpClusterer();
    }

    private  void drawMarker (LatLng latLng){

        MarkerPoints.add(latLng);
        MarkerOptions options = new MarkerOptions();
        // setting the position of the marker
        options.position(latLng);

        if (MarkerPoints.size() == 1) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.kid_icon));
        } else if (MarkerPoints.size() == 2) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }

       mMap.addMarker(options);
        }


    @Override
    public boolean onMarkerClick(Marker marker) {

        //TODO fix
        double positionLat = marker.getPosition().latitude;
        double positionLng = marker.getPosition().longitude;
        TaskItem item = mViewModel.getItemByLocation(positionLat,positionLng);
        //Initializing a bottom sheet
        BottomSheetDialogFragment bottomSheetDialogFragment = FragmentTaskCreation.newInstance(item);

        //show it
        bottomSheetDialogFragment.show(getChildFragmentManager(),bottomSheetDialogFragment.getTag());

        return false;
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(latLng.toString()));
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_pin)));

        TaskItem taskItem = new TaskItem();
        taskItem.setLatitude(latLng.latitude);
        taskItem.setLongitude(latLng.longitude);
        Log.v("map's lat/lng","" + latLng.latitude + ", " + latLng.longitude + "");
        mViewModel.insertItem(taskItem);

        //Initializing a bottom sheet
        BottomSheetDialogFragment bottomSheetDialogFragment = FragmentTaskCreation.newInstance(latLng);

        //show it
        bottomSheetDialogFragment.show(getChildFragmentManager(),bottomSheetDialogFragment.getTag());

    }


    //-----------------------------------------------------------


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
                    markerOptions.position(latLng)
                            .title("Current Position")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.kid_icon));

                   // mCurrentLocationMarker = mMap.addMarker(markerOptions);

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
                    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
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
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
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


    //--------------------------------Searching stuff-----------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void initSearch() {

        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getActivity(),mGoogleApiClient,LAT_LNG_BOUNDS,null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);


        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView,int actionId,KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

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

        hideKeyboard((getActivity()));
    }


    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
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
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void moveCamera(LatLng latLng, float zoom,String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        if (title.equals("my location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng);
            //  mMarker = mMap.addMarker(options);
        }
        hideKeyboard(getActivity());
    }


    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocusedView = activity.getCurrentFocus();
            if (currentFocusedView != null) {
                assert inputManager != null;
                inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(),"Connection Failed",Toast.LENGTH_SHORT).show();

    }


    /*
   -----------------------------google places API autocomplete suggestions------------
   */
    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        @Override
        public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
            hideKeyboard((getActivity()));

            final AutocompletePrediction mAutocompletePrediction = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = Objects.requireNonNull(mAutocompletePrediction).getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId);
            placeResult.setResultCallback(mPLacesUbdDetailsCallback);
        }
    };


    private ResultCallback<PlaceBuffer> mPLacesUbdDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
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
                mPlace.setAddress(Objects.requireNonNull(place.getAddress()).toString());
                mPlace.setPhoneNumber(Objects.requireNonNull(place.getPhoneNumber()).toString());
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
        // Position the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.177200,-44.503490),14));

        mClusterManager = new ClusterManager<>(Objects.requireNonNull(getContext()),mMap);
        mClusterManager.setRenderer(new ClusterRenderer(getContext(),mMap,mClusterManager));
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Clusters>() {
            @Override
            public boolean onClusterClick(Cluster<Clusters> cluster) {
                return false;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Clusters>() {
            @Override
            public boolean onClusterClick(Cluster<Clusters> cluster) {
                return false;
            }
        });

        // point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);


        mClusterManager.cluster();
        // addItems();
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
            Clusters offsetItem = new Clusters(lat, lng);
            mClusterManager.addItem(offsetItem);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    //-----------------------Route drawing-------------------------------


    @Override
    public void onLocationChanged(Location location) {
        if(MarkerPoints.size() < 2){

            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            LatLng point = new LatLng(mLatitude, mLongitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            drawMarker(point);
        }
    }

    @Override
    public void onStatusChanged(String provider,int status,Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private String getUrl(LatLng origin,LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }



    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);


            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
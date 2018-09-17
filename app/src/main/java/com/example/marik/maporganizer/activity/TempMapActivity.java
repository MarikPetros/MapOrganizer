package com.example.marik.maporganizer.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.marik.maporganizer.R;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.fragments.PlaceAutocompleteAdapter;
import com.example.marik.maporganizer.models.PlaceInfo;
import com.example.marik.maporganizer.viewModel.TaskViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.example.marik.maporganizer.fragments.FragmentTaskCreation.ARG_TASK_ITEM;


public class TempMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -169), new LatLng(70, 137));
    public static final String RADIUS_KEY = "com.maporganizer.RADIUS_KEY";
    public static final String LATLONG_KEY = "com.maporganizer.tempmap.LATLONG_KEY";

    private GoogleMap mMap;
    private Circle circle;
    private EditText desiredRadius;
    private int radius;
    private AutoCompleteTextView mAutoCompleteTextView;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;
    private PlaceInfo mPlace;
    private double latitude;
    private double longitude;
    private TaskItem taskitem;
    Intent intent = new Intent();


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_map);

        taskitem = getIntent().getParcelableExtra(ARG_TASK_ITEM);
        latitude = taskitem.getLatitude();
        longitude = taskitem.getLongitude();
        Log.v("lat/long", "" + latitude + " ," + longitude + " ");
        if (taskitem.getAlertRadius() != 0) {
            radius = taskitem.getAlertRadius();
        }else{
            radius = 100;
        }

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.tMap);

        mapFragment.getMapAsync(TempMapActivity.this);

        mGoogleApiClient = new GoogleApiClient
                .Builder(Objects.requireNonNull(this))
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        init();
    }


    public void init() {

        mAutoCompleteTextView = findViewById(R.id.radius_input_search);
        ImageView mSearch = findViewById(R.id.radius_search_icon);
        desiredRadius = findViewById(R.id.editTextRadius);
        ImageView saveBtn = findViewById(R.id.radius_save_img);
        ImageView saveSettingsBtn = findViewById(R.id.radius_settings_save_img);

        // Make circle1 radius from content of EditText
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radius = Integer.parseInt(com.example.marik.maporganizer.activity.TempMapActivity.this.desiredRadius.getText().toString());
                if (radius >= 100 && radius <= 5000) {
                    circle.setRadius(radius);
                } else {
                    circle.setRadius(100);
                }
            }
        });

        intent.putExtra(RADIUS_KEY, radius).putExtra(LATLONG_KEY, new double[] {latitude,longitude});

        saveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Showing current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        //  googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);


        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng midLatLng = mMap.getCameraPosition().target;
                getAddress(midLatLng.latitude, midLatLng.longitude);
                Log.v("---------", midLatLng.latitude + " " + midLatLng.longitude);
            }
        });

        addMarkerToChoosedAddress(new LatLng(latitude, longitude));
        moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM, "location");
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
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3));

                // Clears the previously touched position
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(markerOptions); // es pakac er

                //    moveCamera(latLng, DEFAULT_ZOOM, markerOptions.getTitle()); //Pordzenq
                drawCircle(latLng);
                intent.putExtra(RADIUS_KEY, radius).putExtra(LATLONG_KEY,new double[] {latLng.latitude, latLng.longitude});
            }
        });
    }


    private void addMarkerToChoosedAddress(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3));
        // Clears the previously touched position
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(markerOptions);
        drawCircle(latLng);
        intent.putExtra(RADIUS_KEY, radius).putExtra(LATLONG_KEY, new double[] {latLng.latitude, latLng.longitude});

    }

    public String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {

            System.out.println("get address");
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                System.out.println("size====" + addresses.size());
                Address address = addresses.get(0);

                for (int i = 0; i <= addresses.get(0).getMaxAddressLineIndex(); i++) {
                    if (i == addresses.get(0).getMaxAddressLineIndex()) {
                        result.append(addresses.get(0).getAddressLine(i));
                    } else {
                        result.append(addresses.get(0).getAddressLine(i) + ",");
                    }
                }
                System.out.println("ad==" + address);
                System.out.println("result---" + result.toString());

                //    mAutoCompleteTextView.setText(result.toString()); //  AutoCompleteTextView for setting  string address
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }


    private void drawCircle(LatLng point) {

        // Instantiating CircleOptions to draw a circle1 around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle1
        circleOptions.center(point);

        // Radius of the circle1
        circleOptions.radius(radius);

        // Border color of the circle1
        circleOptions.strokeColor(Color.GREEN);

        // Fill color of the circle1
        circleOptions.fillColor(0x300097A7);

        // Border width of the circle1
        circleOptions.strokeWidth(2);

        // Adding the circle1 to the GoogleMap
        circle = mMap.addCircle(circleOptions);

    }

//-------------------------Autocomplete--------------

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void initSearch() {

        mAutoCompleteTextView.setOnItemClickListener(mAutoCompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null);
        mAutoCompleteTextView.setAdapter(mPlaceAutocompleteAdapter);

        mAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocate();
                }
                return false;
            }
        });

        hideKeyboard();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void geoLocate() {
        String searchString = mAutoCompleteTextView.getText().toString();

        Geocoder geocoder = new Geocoder(this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            latitude = address.getLatitude();
            longitude = address.getLongitude();
            LatLng choosedLocation = new LatLng(latitude, longitude);
            moveCamera(choosedLocation, DEFAULT_ZOOM, address.getAddressLine(0));
            drawCircle(choosedLocation);
            intent.putExtra(RADIUS_KEY, radius).putExtra(LATLONG_KEY, new double[] {latitude, longitude});

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("my location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
     //       mMarker = mMap.addMarker(options);
        }
        hideKeyboard();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();

            final AutocompletePrediction mAutocompletePrediction = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = Objects.requireNonNull(mAutocompletePrediction).getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
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

            latitude = Objects.requireNonNull(place.getViewport()).getCenter().latitude;
            longitude = place.getViewport().getCenter().longitude;

            /*moveCamera(new LatLng(Objects.requireNonNull(place.getViewport()).getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());*/

            LatLng choosedLocation = new LatLng(latitude, longitude);
            moveCamera(choosedLocation, DEFAULT_ZOOM, mPlace.getName());
            drawCircle(choosedLocation);
            intent.putExtra(RADIUS_KEY, radius).putExtra(LATLONG_KEY, new double[] {latitude, longitude});

            places.release();
        }
    };


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }
}


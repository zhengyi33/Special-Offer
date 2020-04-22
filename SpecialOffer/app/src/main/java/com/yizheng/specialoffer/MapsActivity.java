package com.yizheng.specialoffer;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView addressText;
    private CheckBox geofenceCheckbox;
    private CheckBox addressCheckbox;
    private Geocoder geocoder;

    private boolean zooming;
    private float oldZoom;

    private LocationManager locationManager;

    private LocationListener locationListener;

    private Polyline llHistoryPolyline;
    private ArrayList<LatLng> latLonHistory = new ArrayList<>();

    private Marker carMarker;

    private List<PatternItem> pattern = Collections.<PatternItem>singletonList(new Dot());

    private ArrayList<Circle> circles = new ArrayList<>();

    private static final int LOCATION_REQUEST = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        new Thread(new Runnable() {
            @Override
            public void run() {
                FenceManager.getInstance(MapsActivity.this);
            }
        }).start();

        addressText = findViewById(R.id.textView3);
        geofenceCheckbox = findViewById(R.id.checkBox);
        addressCheckbox = findViewById(R.id.checkBox2);
        geocoder = new Geocoder(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    GoogleMap getmMap() {
        return mMap;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
        zooming = true;

        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        setupLocationListener();
        setupZoomListener();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }


    //@SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission() && locationManager != null && locationListener != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
    }

    private void setupZoomListener() {
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener(){

            @Override
            public void onCameraIdle() {
                if (zooming){
                    zooming = false;
                    oldZoom = mMap.getCameraPosition().zoom;
                }
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (mMap.getCameraPosition().zoom != oldZoom){
                    zooming = true;
                }
            }
        });
    }

    //@SuppressLint("MissingPermission")
    private void setupLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocListener(this);

        if (checkPermission() && locationManager != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);

    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return false;
        }
        return true;
    }

    public void showAddressClicked(View v){
        CheckBox cb = (CheckBox) v;

        if(cb.isChecked()){
            addressText.setVisibility(View.VISIBLE);
        } else {
            addressText.setVisibility(View.INVISIBLE);
        }
    }

    public void showGeofenceClicked(View v){
        CheckBox cb = (CheckBox) v;

        if (cb.isChecked()){
            FenceManager.getInstance(this).drawFences(this);//////////
        } else {
            eraseFences();
        }
    }

    private void eraseFences() {
        for (Circle c : circles){
            c.remove();
        }
        circles.clear();
    }

//    private static void drawFences() {
//        for (String id : FenceManager.getFences().keySet()){
//            drawFence(FenceManager.getFences().get(id));
//        }
//    }

    void drawFence(FenceData fd) {
        int line = Color.parseColor(fd.getFenceColor());
        int fill = ColorUtils.setAlphaComponent(line, 85);

        LatLng latLng = new LatLng(fd.getLat(), fd.getLon());
        Circle c = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(fd.getRadius())
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill));

        circles.add(c);
    }

    public void updateLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng);

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addresses.get(0);
            addressText.setText(address.getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
            addressText.setText("");
        }

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            zooming = true;
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(8);
            llHistoryPolyline.setColor(Color.BLUE);


            float r = getRadius();
            if (r > 0) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.car);
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                options.rotation(location.getBearing());

                if (carMarker != null) {
                    carMarker.remove();
                }

                carMarker = mMap.addMarker(options);
            }
        }

        if (!zooming)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        return 15f * z - 145f;
    }
}

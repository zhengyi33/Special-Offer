package com.yizheng.specialoffer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FenceManager {

    private static final String TAG = "FenceManager";
    private Activity activity;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private static FenceManager instance;
    private Geocoder geocoder;
    private static HashMap<String, FenceData> fences = new HashMap<>();

    public static FenceManager getInstance(Activity activity){

        if (instance == null){
            instance = new FenceManager(activity);
        }
        return instance;
    }

    static FenceData getFenceData(String id){
        return fences.get(id);
    }

//    static HashMap<String, FenceData> getFences() {
//        return fences;
//    }

    private FenceManager(Activity a){
        this.activity = a;
        geofencingClient = LocationServices.getGeofencingClient(activity);

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: removeGeofences");
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: removeGeofences");
                        Toast.makeText(activity, "Trouble removing existing fences: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        geocoder = new Geocoder(activity);

        new FenceDataDownloader(activity, this).execute();
    }

    public void receiveResult(String result) {
        //ArrayList<FenceData> fs = new ArrayList<>();
        try {
            JSONObject jObj = new JSONObject(result);
            JSONArray jArr = jObj.getJSONArray("fences");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject fObj = jArr.getJSONObject(i);
                String id = fObj.getString("id");
                String address = fObj.getString("address");
                String website = fObj.getString("website");
                float rad = (float) fObj.getDouble("radius");
                int type = fObj.getInt("type");
                String message = fObj.getString("message");
                String code = fObj.getString("code");
                String color = fObj.getString("fenceColor");
                String logo = fObj.getString("logo");

                LatLng ll = getLatLong(address);

                if (ll != null) {
                    FenceData fd = new FenceData(id, address, website, rad, type, ll.latitude, ll.longitude, message, code, color, logo);
                    fences.put(fd.getId(), fd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        addFences();
    }

    static private LatLng getLatLong(String address) {
        try {
            List<Address> addressList = instance.getGeocoder().getFromLocationName(address, 1);
            Address a = addressList.get(0);
            return new LatLng(a.getLatitude(), a.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


//    static void drawFences() {
//        for (String id : fences.keySet()){
//            drawFence(fences.get(id));
//        }
//    }
//
//    private static void drawFence(FenceData fd) {
//        int line = Color.parseColor(fd.getFenceColor());
//        int fill = ColorUtils.setAlphaComponent(line, 85);
//
//        LatLng latLng = new LatLng(fd.getLat(), fd.getLon());
//
//        Circle c = mapsActivity.getMap().addCircle(new CircleOptions()
//                .center(latLng)
//                .radius(fd.getRadius())
//                .strokePattern(pattern)
//                .strokeColor(line)
//                .fillColor(fill));
//
//        circles.add(c);
//    }

    Geocoder getGeocoder() {
        return geocoder;
    }

    void addFences() {
//        fences.clear();
//        for (FenceData fd : fs){
//            fences.put(fd.getId(), fd);
//        }

        for (FenceData fd : fences.values()){
            //FenceData fd = fences.get(id);
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(fd.getId())
                    .setCircularRegion(
                            fd.getLat(),
                            fd.getLon(),
                            fd.getRadius())
                    .setTransitionTypes(fd.getType())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE) //Fence expires after N millis  -or- Geofence.NEVER_EXPIRE
                    .build();
            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .addGeofence(geofence)
                    .build();

            geofencePendingIntent = getGeofencePendingIntent();

            geofencingClient
                    .addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: addGeofences");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "onFailure: addGeofences");

                            //Toast.makeText(mapsActivity, "Trouble adding new fence: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });
        }
        //drawFences();
    }

    public void drawFences(MapsActivity mapsActivity){
        for (FenceData fd : fences.values()){
            mapsActivity.drawFence(fd);
        }
    }

//    private void drawFences() {
//        for (String id : fences.keySet()){
//            FenceData fd = fences.get(id);
//            int line = Color.parseColor(fd.getFenceColor());
//            int fill = ColorUtils.setAlphaComponent(line, 85);
//
//            LatLng latLng = new LatLng(fd.getLat(), fd.getLon());
//            Circle c = mMap.addCircle(new CircleOptions()
//                    .center(latLng)
//                    .radius(fd.getRadius())
//                    .strokePattern(pattern)
//                    .strokeColor(line)
//                    .fillColor(fill));
//
//            circles.add(c);
//        }
//    }

    private  PendingIntent getGeofencePendingIntent() {

        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(activity, GeofenceBroadcastReceiver.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
}

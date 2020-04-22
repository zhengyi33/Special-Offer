package com.yizheng.specialoffer;

import android.content.IntentSender;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static com.yizheng.specialoffer.SplashActivity.ACCURACY_REQUEST;


public class LocationAccuracyAsyncTask extends AsyncTask<String, Void, Void> {

    SplashActivity splashActivity;

    public LocationAccuracyAsyncTask(SplashActivity splashActivity) {
        this.splashActivity = splashActivity;
    }

    @Override
    protected Void doInBackground(String... strings) {
        checkLocationAccuracy();
        return null;
    }

    private void checkLocationAccuracy(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(splashActivity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(splashActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //call back to splash
                splashActivity.startFenceMgr();
            }
        });

        task.addOnFailureListener(splashActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(splashActivity, ACCURACY_REQUEST);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }


}

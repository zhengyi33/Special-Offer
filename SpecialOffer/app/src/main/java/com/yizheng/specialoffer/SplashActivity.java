package com.yizheng.specialoffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 111;
    static final int ACCURACY_REQUEST = 222;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (checkPermission()){
            setUp();
        }
    }

    private void setUp() {
        new LocationAccuracyAsyncTask(this).execute();
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return false;
        }
        return true;
    }

    void startFenceMgr(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                FenceManager.getInstance(SplashActivity.this);
//            }
//        }).start();
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
        progressBar.setVisibility(View.INVISIBLE);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
            startFenceMgr();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("High-Accuracy Location Services Required");
            builder.setMessage("High-Accuracy Location Services Required");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //setupLocationListener();
                        setUp();
                    } else {
                        //Toast.makeText(this, "Location Permission not Granted", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) { }

                                });
                        builder.setMessage("App cannot be run without this permission.");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        }
    }
}

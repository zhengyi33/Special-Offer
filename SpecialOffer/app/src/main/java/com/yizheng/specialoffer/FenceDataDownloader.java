package com.yizheng.specialoffer;

import android.app.Activity;
import android.location.Address;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FenceDataDownloader extends AsyncTask<String, Void, String> {

    private Activity activity;
    private static final String FENCE_URL = "http://www.christopherhield.com/data/fences.json";

    FenceManager fenceManager;

    public FenceDataDownloader(Activity activity, FenceManager fenceManager) {
        this.activity = activity;
        this.fenceManager = fenceManager;
    }

    @Override
    protected void onPostExecute(String result) {

        if (result == null)
            return;

        fenceManager.receiveResult(result);


    }

    private LatLng getLatLong(String address) {
        try {
            List<Address> addressList = FenceManager.getInstance(activity).getGeocoder().getFromLocationName(address, 1);
            Address a = addressList.get(0);
            return new LatLng(a.getLatitude(), a.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(FENCE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



}

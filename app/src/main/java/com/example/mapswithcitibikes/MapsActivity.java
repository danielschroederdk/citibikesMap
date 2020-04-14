package com.example.mapswithcitibikes;

import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new MarkerTask().execute();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }

    private class MarkerTask extends AsyncTask<Void, Void, String> {

        private static final String MAP_URL = "https://feeds.citibikenyc.com/stations/stations.json";
        private static final String LOG_TAG = "MarkerTask_Debug";

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection httpURLConnection = null;


            final StringBuilder json = new StringBuilder();

            try {
                URL url = new URL(MAP_URL);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStreamReader inboundStream = new InputStreamReader(httpURLConnection.getInputStream());

                int input;
                char[] buff = new char[1024];
                while ((input = inboundStream.read(buff)) != -1){
                    json.append(buff,0,input);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Connection failed.",e);
            }
            finally {
                if(httpURLConnection != null)
                    httpURLConnection.disconnect();
            }
            return json.toString();


        }

        @Override
        protected void onPostExecute(String jsonData) {
            try {
                JSONObject jsonObj = new JSONObject(jsonData);

                JSONArray jsonArray = jsonObj.getJSONArray("stationBeanList");

                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    LatLng latLng = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));

                    if(i == 0){
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(10).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }

                    mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title(jsonObject.getString("stationName")).snippet("Available bikes: " + Integer.toString(jsonObject.getInt("availableBikes"))).position(latLng));

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Can't process JSON data",e);        }
        }
    }

}

package com.example.rpino.geopost;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

public class PostActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /* GESTIONE POSIZIONE
    ================================ */
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    PendingResult<LocationSettingsResult> locationSettingsResultPendingResult;
    Location location;
    public double currentLatitude;
    public double currentLongitude;
    GoogleMap map;

    SharedPreferences sharedPreferences;
    String myPreferences;

    RequestQueue requesteQueue;
    JSONObject obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);

        requesteQueue = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("MainActivity", "SupportMapFragment");

        /* GESTIONE POSIZIONE
        ================================ */
        // build the googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                // new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                // adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();
        // Create the LocationRequest object
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(60 * 1000) // 60 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 seconds, in milliseconds

        /* BOTTOM NAVIGATION BAR
        ========================================================================================= */

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2).setChecked(true);

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_map:
                        Intent intent = new Intent(PostActivity.this, MapActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_list:
                        Intent intent2 = new Intent(PostActivity.this, ListActivity.class);
                        startActivity(intent2);
                        return true;

                    case R.id.navigation_post:
                        break;

                    case R.id.navigation_add:
                        Intent intent4 = new Intent(PostActivity.this, AddActivity.class);
                        startActivity(intent4);
                        return true;

                    case R.id.navigation_profile:
                        Intent intent5 = new Intent(PostActivity.this, ProfileActivity.class);
                        startActivity(intent5);
                        return true;

                }
                return false;
            }
        };

        // textView = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /* ========================================================================================= */

    } // method onCreate

    /* GESTIONE POSIZIONE
    ========================================================================================= */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("MainActivity", "onLocationChanged");

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Log.d("onLocationChanged", "currentLatitude: "+currentLatitude);
        Log.d("onLocationChanged", "currentLongitude: "+currentLongitude);

        aggiornaPosizione();
    }

    @Override
    public void onMapReady(GoogleMap maps) {
        Log.d("MainActivity", "onMapReady");
        map=maps;
        aggiornaPosizione();
    }

    public void aggiornaPosizione() {
        map.clear();
        Log.d("MainActivity", "aggiornaPosizione()");
        map.addMarker(new MarkerOptions()
                .position(new LatLng(currentLatitude, currentLongitude))
                .title("Mia Posizione"));
        LatLng myposition = new LatLng(currentLatitude,currentLongitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myposition, 15));
    }

    /*
    public void aggiornaPosizione(View view) {
        map.clear();
        Log.d("MainActivity", "aggiornaPosizione(View view)");

        map.addMarker(new MarkerOptions()
                .position(new LatLng(currentLatitude, currentLongitude))
                .title("Mia Posizione"));
        LatLng myposition = new LatLng(currentLatitude,currentLongitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myposition, 15));
    }
    */

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    // client needs to connected when the app starts
    @Override
    public void onStart() {
        Log.d("MainActivity", "onStart");
        super.onStart();
        googleApiClient.connect();
    }

    // client needs to disconnected when the app stops
    @Override
    public void onStop() {
        Log.d("MainActivity", "onStop");
        super.onStop();
        googleApiClient.disconnect();
    }

    /* ========================================================================================= */

    public void inviaMessaggio(View view) {

        TextView messaggioText = findViewById(R.id.messaggio);
        Log.d("MainActivity", "messaggio: "+ messaggioText.getText().toString());
        if (messaggioText.getText().toString().isEmpty()){
            Log.d("MainActivity", "if messaggio");
            Toast.makeText(this, "Devi inserire un messaggio", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("inviaMessaggio","lat: "+Double.toString(currentLatitude));
        Log.d("inviaMessaggio","lon: "+Double.toString(currentLongitude));

        String messaggioString = messaggioText.getText().toString().trim();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("message",messaggioString);
        editor.putString("lat",Double.toString(currentLatitude));
        editor.putString("lon",Double.toString(currentLongitude));
        editor.commit();

        /* VOLLEY
        ================================= */

        String session_id = sharedPreferences.getString("session_id", null);

        String url = "https://ewserver.di.unimi.it/mobicomp/geopost/status_update?session_id="+
                session_id+
                "&message="+messaggioString+
                "&lat="+currentLatitude+
                "&lon="+currentLongitude;

        Log.d("MainActivity","url: "+url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.d("MainActivity","onResponse");
                if(response.isEmpty())
                    Toast.makeText(PostActivity.this, "Messaggio inviato!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(PostActivity.this, "Errore strano...", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", error.toString());
                Toast.makeText(PostActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
            }
        });

        // Adds the JSON object request "objectRequest" to the request queue
        requesteQueue.add(stringRequest);


        /* ================================= */

    }

} // class PostActivity
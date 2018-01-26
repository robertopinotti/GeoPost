package com.example.rpino.geopost;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static double currentLatitude;
    private static double currentLongitude;
    TextView textView;
    GoogleMap map;
    RequestQueue requesteQueue;
    JSONObject obj;
    Intent intent;

    SharedPreferences sharedPreferences;
    String myPreferences;

    /* LOCATION PERMISSION REQUEST
    ================================ */
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    PendingResult<LocationSettingsResult> locationSettingsResultPendingResult;
    static final Integer LOCATION = 0x1;
    static final Integer GPS_SETTINGS = 0x7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);

        intent = intent = new Intent(this, LoginActivity.class);

        requesteQueue = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* LOCATION PERMISSION REQUEST
        ================================ */
        // ask for permission
        askForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION);
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
        MenuItem menuItem = menu.getItem(0).setChecked(true);

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_map:
                        break;

                    case R.id.navigation_list:
                        Intent intent2 = new Intent(MapActivity.this, ListActivity.class);
                        startActivity(intent2);
                        return true;

                    case R.id.navigation_post:
                        Intent intent3 = new Intent(MapActivity.this, PostActivity.class);
                        startActivity(intent3);
                        return true;

                    case R.id.navigation_add:
                        Intent intent4 = new Intent(MapActivity.this, AddActivity.class);
                        startActivity(intent4);
                        return true;

                    case R.id.navigation_profile:
                        Intent intent5 = new Intent(MapActivity.this, ProfileActivity.class);
                        startActivity(intent5);
                        return true;

                }
                return false;
            }
        };

        textView = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    } // method onCreate

    /* GESTIONE DELLA MAPPA
    ========================================================================================= */

    @Override
    public void onMapReady(GoogleMap maps) {

        Log.d("MainActivity", "onMapReady");

        map=maps;

        String session_id = sharedPreferences.getString("session_id", null);
        Log.d("onCreate", "session_id: "+session_id);

        String url = "https://ewserver.di.unimi.it/mobicomp/geopost/followed?session_id="+session_id;

        /* VOLLEY
        ================================= */

        JsonObjectRequest objectRequest = new JsonObjectRequest(url, obj,
                new Response.Listener<JSONObject>(){

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            double latmin=0, lonmin=0, latmax=0, lonmax=0;
                            int x=0;

                            JSONArray arr = response.getJSONArray("followed");

                            Log.d("arr ", arr.toString());

                            for (int i = 0; i < arr.length(); i++) {

                                JSONObject jsonObject = arr.getJSONObject(i);

                                String username = jsonObject.getString("username");
                                String msg = jsonObject.getString("msg");
                                String lat = jsonObject.getString("lat");
                                String lon = jsonObject.getString("lon");

                                Log.d("stampa ", username+msg+lat+lon);

                                if (lat.equals("null")) { // tolgo quegli utenti che non hanno mai postato
                                    Log.d("onResponse", "null");
                                    x++;
                                } else {

                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)))
                                            .title(username)
                                            .snippet(msg));

                                    x++; // se c'è un utente, aumento il valore di x



                                } // else

                            } // for



                            // centro
                            LatLng myposition = new LatLng(currentLatitude,currentLongitude);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myposition, 15));
                            map.animateCamera(CameraUpdateFactory.zoomTo(8), 1000, null);

                            if (x==0) {
                                Log.d("onMapReady()", "x==0");
                                Toast.makeText(MapActivity.this, "Non segui nessuno!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } // onReponse()

                }, // Response.Listener<JSONObject>

                // The final parameter overrides the method onErrorResponse()
                //and passes VolleyError as a parameter
                new Response.ErrorListener() {
                    @Override
                    // Handles errors that occur due to Volley
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error");
                        Toast.makeText(MapActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Adds the JSON object request "objectRequest" to the request queue
        requesteQueue.add(objectRequest);


        /* ================================= */

    } // method onMapReady

    /* LOCATION PERMISSION REQUEST
    ========================================================================================= */

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MapActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MapActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MapActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            // Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    // To handle the results of a permission request, the onRequestPermissionsResult method is called
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            askForGPS();

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder   builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Autorizzazione negata")
                    .setMessage("L'applicazione ha bisogno di usare la tua posizione per funzionare." +
                            " Premi OK per dare l'autorizzazione.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            askForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
    }

    // This function prompts the user to enable GPS if it’s not enabled
    private void askForGPS() {

        // create a LocationSettingsRequest.Builder and add all of the LocationRequests that the app will be using:
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        locationSettingsResultPendingResult = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        // Prompt the User to Change Location Settings
        locationSettingsResultPendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MapActivity.this, GPS_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Log.d("MainActivity", "startLocationUpdates()");
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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (LocationListener) this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("MainActivity", "onLocationChanged");

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Log.d("onLocationChanged", "currentLatitude: "+currentLatitude);
        Log.d("onLocationChanged", "currentLongitude: "+currentLongitude);

    }

    public static double getCurrentLatitude(){
        return currentLatitude;
    }
    public static double getCurrentLongitude(){
        return currentLongitude;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

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

    /* ================================================================================================ */

} // class MapActivity
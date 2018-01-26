package com.example.rpino.geopost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView textView1, textView2, textView3;
    RequestQueue requesteQueue;
    Intent intent;
    GoogleMap map;
    String session_id;
    JSONObject obj;
    Double lat=0.0,lon=0.0;
    String title, snippet;

    SharedPreferences sharedPreferences;
    String myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);

        requesteQueue = Volley.newRequestQueue(this);

        textView1 = (TextView) findViewById(R.id.textView1);
        String username = sharedPreferences.getString("username", null);
        if (username != null)
            textView1.setText("Hi, "+username + "!");
        session_id = sharedPreferences.getString("session_id", null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* BOTTOM NAVIGATION BAR
        ========================================================================================= */

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(4).setChecked(true);

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_map:
                        Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_list:
                        Intent intent2 = new Intent(ProfileActivity.this, ListActivity.class);
                        startActivity(intent2);
                        return true;

                    case R.id.navigation_post:
                        Intent intent3 = new Intent(ProfileActivity.this, PostActivity.class);
                        startActivity(intent3);
                        return true;

                    case R.id.navigation_add:
                        Intent intent4 = new Intent(ProfileActivity.this, AddActivity.class);
                        startActivity(intent4);
                        return true;

                    case R.id.navigation_profile:
                        break;

                }
                return false;
            }
        };

        textView3 = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /*volley*/

    } // method onCreate

    /* GESTIONE MAPPA
    ========================================================================================= */

    @Override
    public void onMapReady(GoogleMap maps) {
        Log.d("MainActivity", "onMapReady");

        map=maps;

        /* VOLLEY
        ========================================================================================= */

        // recupero il session_id salvato in locale
        session_id = sharedPreferences.getString("session_id", null);
        Log.d("onCreate", "session_id: "+session_id);

        // creo l'url per la mia chiamata volley
        String url = "https://ewserver.di.unimi.it/mobicomp/geopost/profile?session_id="+session_id;
        Log.d("onCreate", "url: "+url);

        JsonObjectRequest objectRequest = new JsonObjectRequest(url, obj,
                new Response.Listener<JSONObject>(){

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if ( response.getString("lat").equals("null") ) {
                                Log.d("onResponse", "lat null");
                                Toast.makeText(ProfileActivity.this, "Non hai ancora postato alcun messaggio!", Toast.LENGTH_SHORT).show();

                            } else {
                                Log.d("onResponse", "response: "+Double.parseDouble(response.getString("lat")));
                                lat = Double.parseDouble(response.getString("lat"));

                                Log.d("onResponse", "response: "+Double.parseDouble(response.getString("lon")));
                                lon = Double.parseDouble(response.getString("lon"));

                                Log.d("onResponse", "response: "+Double.parseDouble(response.getString("lon")));
                                title = response.getString("username");

                                Log.d("onResponse", "response: "+Double.parseDouble(response.getString("lon")));
                                snippet = response.getString("msg");

                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat, lon))
                                        .title(title)
                                        .snippet(snippet))
                                        .showInfoWindow();

                                LatLng myposition = new LatLng(lat,lon);
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myposition, 15));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } // onReponse()

                }, // Response.Listener<JSONObject>

                // se la chiamata Volley è andata male
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error");
                        Toast.makeText(ProfileActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Adds the JSON object request "objectRequest" to the request queue
        requesteQueue.add(objectRequest);

        /* ========================================================================================= */

        Log.d("onMapReady", "lat: "+lat);
        Log.d("onMapReady", "lon: "+lon);


    } // method onMapReady

    /* LOGOUT
    ========================================================================================= */

    public void logoutButton(View view) {

        Log.d("logoutButton", "premiuto");

        intent = new Intent(this, LoginActivity.class);

        String url="";
        session_id = sharedPreferences.getString("session_id", null);
        if (session_id!=null)
            url = "https://ewserver.di.unimi.it/mobicomp/geopost/logout?session_id="+session_id;

        Log.d("logoutButton", "url: "+url);

        // Creating the JsonObjectRequest class called objectRequest, passing required parameters:
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.d("onResponse", "response: "+response);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("session_id",null);
                editor.commit();

                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", "That didn't work!");
                Toast.makeText(ProfileActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue.
        requesteQueue.add(stringRequest);
    }

} // class ProfileActivity

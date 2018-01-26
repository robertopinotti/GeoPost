package com.example.rpino.geopost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText usernameText=null, passwordText=null;
    String usernameString="", passwordString="";
    RequestQueue requestQueue;
    Intent intent;

    SharedPreferences sharedPreferences;
    String myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);
        String session_id = sharedPreferences.getString("session_id", null);
        Log.d("onCreate", "session_id: "+session_id);

        if (session_id!=null) {
            intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }

        /*
        String session_id = MySingleton.getInstance().getSession_id();
        Log.d("onCreate", "session_id: "+session_id);
        if (session_id!=""){
            intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }
        */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // creo la richiesta
        requestQueue = Volley.newRequestQueue(this);

    } // method onCreate

    @Override
    protected void onStart() {
        super.onStart();

        String session_id = sharedPreferences.getString("session_id", null);
        Log.d("onStart", "session_id: "+session_id);

        if (session_id!=null) {
            startActivity(intent);
        }

    }

    /* LOGIN
    ========================================================================================= */

    public void checkLogin(View view) {

        intent = new Intent(this, MapActivity.class);

        String url = "https://ewserver.di.unimi.it/mobicomp/geopost/login";

        // username
        usernameText = findViewById(R.id.username);
        if (usernameText.getText().toString().isEmpty()){
            Log.d("MainActivity", "if username");
            Toast.makeText(this, "Devi inserire un username", Toast.LENGTH_SHORT).show();
            return;
        }
        usernameString = usernameText.getText().toString().trim();

        // password
        passwordText = findViewById(R.id.password);
        if (passwordText.getText().toString().isEmpty()){
            Log.d("MainActivity", "if password");
            Toast.makeText(this, "Devi inserire una password", Toast.LENGTH_SHORT).show();
            return;
        }
        passwordString= passwordText.getText().toString().trim();

        final StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("onResponse", "response: "+response);
                if (!response.isEmpty()){

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("session_id",response);
                    editor.putString("username",usernameString);
                    editor.commit();

                    startActivity(intent);
                }
                else {
                    Toast.makeText(LoginActivity.this, "Username o Password errati!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", "That didn't work!");
                Toast.makeText(LoginActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("username", usernameString);
                MyData.put("password", passwordString);
                return MyData;
            }
        };

        requestQueue.add(MyStringRequest);

    }

    /* ========================================================================================= */

} // class LoginActivity
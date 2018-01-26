package com.example.rpino.geopost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddActivity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    RequestQueue requesteQueue;
    JSONObject obj;
    SharedPreferences sharedPreferences;
    String myPreferences;
    String session_id, username;

    /* GESTIONE ADAPTER
    ================================ */
    ListView listView;
    ArrayList<Person> arrayOfWebData = new ArrayList<Person>();
    static ArrayList<String> resultRow;
    class Person {
        public String username;
    }
    MyAdapter myAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        listView = (ListView)findViewById(R.id.listView);
        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);
        requesteQueue = Volley.newRequestQueue(this);
        session_id = sharedPreferences.getString("session_id", null);
        username = sharedPreferences.getString("username", null);

        Log.d("onCreate",session_id+" "+username);

        /* BOTTOM NAVIGATION BAR
        ========================================================================================= */

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3).setChecked(true);

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_map:
                        Intent intent = new Intent(AddActivity.this, MapActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_list:
                        Intent intent2 = new Intent(AddActivity.this, ListActivity.class);
                        startActivity(intent2);
                        return true;

                    case R.id.navigation_post:
                        Intent intent3 = new Intent(AddActivity.this, PostActivity.class);
                        startActivity(intent3);
                        return true;

                    case R.id.navigation_add:
                        break;

                    case R.id.navigation_profile:
                        Intent intent5 = new Intent(AddActivity.this, ProfileActivity.class);
                        startActivity(intent5);
                        return true;

                }
                return false;
            }
        };

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /* AUTOCOMPLETE
        ========================================================================================= */

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                arrayOfWebData.clear(); // per pulire la listview

                Log.d("onTextChanged", autoCompleteTextView.getText().toString());

                /* VOLLEY
                ================================= */

                String url = "https://ewserver.di.unimi.it/mobicomp/geopost/users?session_id="+
                        session_id+"&usernamestart="+autoCompleteTextView.getText().toString()+"&limit=5";

                JsonObjectRequest objectRequest = new JsonObjectRequest(url, obj,
                        new Response.Listener<JSONObject>(){

                            // Takes the response from the JSON request
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    JSONArray arr = response.getJSONArray("usernames");

                                    Log.d("onResponse ", "arr: " + arr.toString());

                                    for (int i = 0; i < arr.length(); i++) {

                                        Log.d("onResponse", arr.get(i).toString());

                                        if(!username.equals(arr.get(i).toString())) {
                                            Log.d("if","if");
                                            Person resultRow = new Person();
                                            resultRow.username = arr.get(i).toString();
                                            arrayOfWebData.add(resultRow);
                                        }// if

                                    } // for

                                    myAdapter = new MyAdapter();
                                    listView.setAdapter(myAdapter);


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
                                Toast.makeText(AddActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

                // Adds the JSON object request "objectRequest" to the request queue
                requesteQueue.add(objectRequest);

                /* ================================= */

            }

            @Override
            public void afterTextChanged(Editable s) {}

        });

    } // method OnCreate

    /* ADAPTER
    ========================================================================================= */

    class MyAdapter extends ArrayAdapter<Person> {

        public MyAdapter() {
            super(AddActivity.this, android.R.layout.simple_list_item_1, arrayOfWebData);
        }

        public View getView(int position, View convertView, ViewGroup parent){

            ViewHolder holder;

            if (convertView==null) {
                LayoutInflater inflater=getLayoutInflater();
                convertView=inflater.inflate(R.layout.list_element_add, null);

                //here is something new.  we are using a class called a view holder
                holder=new ViewHolder(convertView);
                //we are using that class to cache the result of the findViewById function
                //which we then store in a tag on the view
                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }
            holder.populateFrom(arrayOfWebData.get(position));

            return(convertView);

        }

        class ViewHolder {
            public TextView username;

            ViewHolder(View row) {
                username=(TextView)row.findViewById(R.id.username);
            }
            //notice we had to change our populate from to take an arguement of type person
            void populateFrom(Person person) {

                username.setText(person.username);

            }
        }

    } // class MyAdapter

    public void seguiPersona(View view) {
        Log.d("seguiPersona", "button premuto");

        int position = listView.getPositionForView((View) view.getParent());
        Log.d("seguiPersona", "position: " + position);
        final String username = arrayOfWebData.get(position).username.toString();
        Log.d("seguiPersona", "position: " + username);

        /* VOLLEY
        ================================= */

        String url = "https://ewserver.di.unimi.it/mobicomp/geopost/follow?session_id="+
                session_id+"&username="+username;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equals("OK"))
                            Toast.makeText(AddActivity.this, "Ora segui "+username+"!", Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                String[] serverResponse = parseNetworkError(error).toString().split("com.android.volley.VolleyError: ");
                String serverResponseSplit = serverResponse[1];
                Log.e("Volley", "serverResponseSplit: " + serverResponseSplit);

                if (serverResponseSplit.equals("CANNOT FOLLOW YOURSELF"))
                    Toast.makeText(AddActivity.this, "Non puoi seguire te stesso!", Toast.LENGTH_SHORT).show();
                else if (serverResponseSplit.equals("ALREADY FOLLOWING USER"))
                    Toast.makeText(AddActivity.this, "Segui già questo utente!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(AddActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
            }

            protected VolleyError parseNetworkError(VolleyError volleyError){
                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }
                return volleyError;
            }

        });

        requesteQueue.add(stringRequest);

        /*================================= */

    } // method seguiPersona

} // class AddActivity
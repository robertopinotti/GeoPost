package com.example.rpino.geopost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    /* GESTIONE SHARED PREFERENCES
    ================================ */
    SharedPreferences sharedPreferences;
    String myPreferences;

    /* GESTIONE VOLLEY
    ================================ */
    RequestQueue requesteQueue;
    JSONObject obj;

    /* GESTIONE ADAPTER
    ================================ */
    ListView listView;
    ArrayList<Person> arrayOfWebData = new ArrayList<Person>();
    static ArrayList<String> resultRow;
    class Person {
        public String username;
        public String message;
        public String distance;
    }
    MyAdapter myAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = (ListView)findViewById(R.id.listView);
        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);
        requesteQueue = Volley.newRequestQueue(this);

        /* BOTTOM NAVIGATION BAR
        ========================================================================================= */

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1).setChecked(true);

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_map:
                        Intent intent = new Intent(ListActivity.this, MapActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_list:
                        break;

                    case R.id.navigation_post:
                        Intent intent3 = new Intent(ListActivity.this, PostActivity.class);
                        startActivity(intent3);
                        return true;

                    case R.id.navigation_add:
                        Intent intent4 = new Intent(ListActivity.this, AddActivity.class);
                        startActivity(intent4);
                        return true;

                    case R.id.navigation_profile:
                        Intent intent5 = new Intent(ListActivity.this, ProfileActivity.class);
                        startActivity(intent5);
                        return true;

                }
                return false;
            }
        };

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /* VOLLEY
        ========================================================================================= */

        // recupero il session_id salvato in locale
        String session_id = sharedPreferences.getString("session_id", null);
        Log.d("onCreate", "session_id: "+session_id);

        // creo l'url per la mia chiamata volley
        String url = "https://ewserver.di.unimi.it/mobicomp/geopost/followed?session_id="+session_id;
        Log.d("onCreate", "url: "+url);

        JsonObjectRequest objectRequest = new JsonObjectRequest(url, obj,
                new Response.Listener<JSONObject>(){

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            int x = 0;

                            // recupero l'array dal server
                            JSONArray jsonArray = response.getJSONArray("followed");
                            Log.d("onResponse", "jsonArray: "+jsonArray);

                            // creo un nuovo array, che andrò successivamente a popolare con gli elementi ordinati
                            JSONArray jsonArraySort = new JSONArray();
                            Log.d("onResponse", "jsonArraySort: "+jsonArraySort);

                            // creo una lista di oggetti json
                            List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
                            Log.d("onResponse", "jsonObjectList: "+jsonObjectList);

                            // ciclo for dove metto nella lista di oggetti json solo quegli utenti che hanno postato
                            for (int i = 0; i < jsonArray.length(); i++) {
                                if ( jsonArray.getJSONObject(i).getString("lat").equals("null") ) {
                                    Log.d("onResponse", "utente con coordinate nulle");
                                    x++;
                                } else {
                                    jsonObjectList.add(jsonArray.getJSONObject(i));
                                    x++;
                                }
                            } // for
                            Log.d("onResponse", "jsonObjectList: "+jsonObjectList);
                            if(x==0){
                                Log.d("onResponse()","x==0");
                                Toast.makeText(ListActivity.this, "Non segui nessuno!", Toast.LENGTH_SHORT).show();
                            }

                            // ordino gli elementi di jsonObjectList
                            Collections.sort( jsonObjectList, new Comparator<JSONObject>() {

                                @Override
                                public int compare(JSONObject a, JSONObject b) {

                                    Log.d("=", "==========================================");

                                    Double latA=0.0, lonA=0.0, latB=0.0, lonB=0.0;

                                    try {
                                        // recuper lat e lon dei due utenti che devo confrontare
                                        latA = Double.parseDouble(a.getString("lat"));
                                        lonA = Double.parseDouble(a.getString("lon"));
                                        Log.d("compare", a.getString("username")+": "+ latA +" "+ lonA);
                                        latB = Double.parseDouble(b.getString("lat"));
                                        lonB = Double.parseDouble(b.getString("lon"));
                                        Log.d("compare", b.getString("username")+": "+ latB +" "+ lonB);
                                    }
                                    catch (JSONException e) {
                                        //do something
                                    }

                                    // creo le location degli utenti che devo confrontare e una location con la mia posizione
                                    Location loc1 = new Location("");
                                    loc1.setLatitude(latA);
                                    loc1.setLongitude(lonA);
                                    Location loc2 = new Location("");
                                    loc2.setLatitude(latB);
                                    loc2.setLongitude(lonB);
                                    Location loc3 = new Location("");
                                    loc3.setLatitude(MapActivity.getCurrentLatitude());
                                    loc3.setLongitude(MapActivity.getCurrentLongitude());
                                    try {
                                        Log.d("compare", "loc "+a.getString("username")+": "+ loc1);
                                        Log.d("compare", "loc "+b.getString("username")+": "+ loc2);
                                        Log.d("compare", "loc utente loggato: " + loc3);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        // ritorno il risultato del confronto tra il primo utente e la mia posizione attuale con il secondo utente e la mia posizione attuale
                                        Log.d("compare", "distanza di : "+a.getString("username")+": " + Integer.toString((int) loc1.distanceTo(loc3)/1000) );
                                        Log.d("compare", "distanza di : "+b.getString("username")+": " + Integer.toString((int) loc2.distanceTo(loc3)/1000) );
                                        Log.d("compare", "return: " + Integer.toString( Math.round(loc1.distanceTo(loc3)) - Math.round(loc2.distanceTo(loc3)) )); // from int - to integer - to string
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    return Math.round(loc1.distanceTo(loc3)) - Math.round(loc2.distanceTo(loc3)); // from float to int

                                }
                            }); // Collections.sort

                            Log.d("=", "==========================================");

                            // metto gli elementi della lista (ora ordinata) di oggetti json nel jsonArraySort
                            for (int i = 0; i < jsonObjectList.size(); i++) {
                                jsonArraySort.put(jsonObjectList.get(i));
                            }
                            Log.d("onResponse", "jsonArraySort: "+jsonArraySort);

                            // popolo l'oggetto person, nel quale ho i campi username, messagge e distance
                            for (int i = 0; i < jsonArraySort.length(); i++) {

                                Log.d("=", "==========================================");

                                Person resultRow = new Person();

                                // recupero il singolo oggetto
                                JSONObject jsonObject = jsonArraySort.getJSONObject(i);
                                Log.d("onResponse", "jsonObject: "+jsonObject);

                                resultRow.username = jsonObject.getString("username");
                                resultRow.message = jsonObject.getString("msg");

                                // creo due location: una con la posizione dell'utente, una con la mia posizione attuale
                                Location loc1 = new Location("");
                                loc1.setLatitude(Double.valueOf(jsonObject.getString("lat")));
                                loc1.setLongitude(Double.valueOf(jsonObject.getString("lon")));
                                Location loc2 = new Location("");
                                loc2.setLatitude(MapActivity.getCurrentLatitude());
                                loc2.setLongitude(MapActivity.getCurrentLongitude());
                                Log.d("onResponse", "mia posizione: "+MapActivity.getCurrentLatitude()+" "+MapActivity.getCurrentLongitude());
                                // salvo la distanza tra la pos dell'utente e la mia pos attuale nel campo distance dell'oggetto person
                                if ( (int) loc1.distanceTo(loc2)/1000 < 10)
                                    resultRow.distance = String.valueOf( Math.round( (loc1.distanceTo(loc2)/1000)*10.0 )/10.0 );
                                else
                                    resultRow.distance = Integer.toString((int) loc1.distanceTo(loc2)/1000); // viene salvata in km, senza cifre significative
                                Log.d("onResponse", "distance: "+resultRow.distance);

                                // aggiungo il mio aggetto nell'array arrayOfWebData
                                arrayOfWebData.add(resultRow);

                            } // for

                            Log.d("=", "==========================================");

                            // creo un nuovo adapter e lo inserisco nella listView
                            myAdapter = new MyAdapter();
                            listView.setAdapter(myAdapter);

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
                        Toast.makeText(ListActivity.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Adds the JSON object request "objectRequest" to the request queue
        requesteQueue.add(objectRequest);

        /* TAP ON LISTVIEW
        ========================================================================================= */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("onItemClick", "position: "+position+" id: "+id);
                String nome_utente = myAdapter.getItem(position).username;
                Log.d("onItemClick","nome_utente:"+ nome_utente );

                Intent intent = new Intent(ListActivity.this, Friend.class);
                intent.putExtra("nome_utente", nome_utente);
                startActivity(intent);
            }
        });

    } // method onCreate

    public Object getItem(int position) {
        return arrayOfWebData.get(position);
    }

    /* ADAPTER
    ========================================================================================= */

    class MyAdapter extends ArrayAdapter<Person> {

        public MyAdapter() {
            super(ListActivity.this, android.R.layout.simple_list_item_1, arrayOfWebData);
        }

        public View getView(int position, View convertView, ViewGroup parent){

            Log.d("MyAdapter", "getView");

            ViewHolder holder;

            if (convertView==null) {
                LayoutInflater inflater=getLayoutInflater();
                convertView=inflater.inflate(R.layout.list_element, null);

                //here is something new.  we are using a class called a view holder
                holder=new ViewHolder(convertView);
                //we are using that class to cache the result of the findViewById function
                //which we then store in a tag on the view
                convertView.setTag(holder);
            } else {
                holder=(ViewHolder)convertView.getTag();
            }

            holder.populateFrom(arrayOfWebData.get(position));

            return(convertView);

        }

        class ViewHolder {

            public TextView username;
            public TextView message;
            public TextView distance;

            ViewHolder(View row) {
                username=(TextView)row.findViewById(R.id.username);
                message=(TextView)row.findViewById(R.id.messagge);
                distance=(TextView)row.findViewById(R.id.distance);
            }

            //notice we had to change our populate from to take an argument of type person
            void populateFrom(Person person) {

                Log.d("MyAdapter", "populateFrom");

                username.setText(person.username);
                message.setText(person.message);
                distance.setText(person.distance + " km");

                Log.d("MyAdapter", "username:"+person.username);

            }
        }

    } // class MyAdapter

} // class ListActivity
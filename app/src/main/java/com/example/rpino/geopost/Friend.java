package com.example.rpino.geopost;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class Friend extends AppCompatActivity {

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
        public String value;
        public String timestamp;
    }
    MyAdapter myAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        listView = (ListView)findViewById(R.id.listView);
        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);
        requesteQueue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        String nome_utente = intent.getExtras().getString("nome_utente");

        TextView textView = findViewById(R.id.nome_utente);
        textView.setText(nome_utente);
        Log.d("test", "nome_utente: "+nome_utente);

        /* VOLLEY
        ================================= */

        String session_id = sharedPreferences.getString("session_id", null);
        Log.d("test", "session_id: "+session_id);

        String url = "https://ewserver.di.unimi.it/mobicomp/geopost/history?session_id="+session_id+"&username="+nome_utente;

        JsonObjectRequest objectRequest = new JsonObjectRequest(url, obj,
                new Response.Listener<JSONObject>(){

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONArray jsonArray = response.getJSONArray("history");
                            Log.d("esameMC", "Risposta: "+jsonArray);
                            Log.d("esameMC", "Numero di post: "+jsonArray.length());

                            // ========== PUNTO 3 =================== //

                            // creo una lista di oggetti json
                            List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();

                            // ciclo for dove metto nella lista gli oggetti json
                            for (int i = 0; i < jsonArray.length(); i++)
                                jsonObjectList.add(jsonArray.getJSONObject(i));

                            // popolo l'oggetto person, nel quale ho i campi username, messagge e distance
                            for (int i = 0; i < jsonArray.length(); i++) {

                                Person resultRow = new Person();

                                // recupero il singolo oggetto
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                resultRow.value = jsonObject.getString("value");
                                resultRow.timestamp = jsonObject.getString("timestamp");

                                // aggiungo il mio aggetto nell'array arrayOfWebData
                                arrayOfWebData.add(resultRow);

                            } // for

                            // creo un nuovo adapter e lo inserisco nella listView
                            myAdapter = new MyAdapter();
                            listView.setAdapter(myAdapter);

                            // ========== PUNTO 3 =================== //

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
                        Toast.makeText(Friend.this, "Errore di connessione!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Adds the JSON object request "objectRequest" to the request queue
        requesteQueue.add(objectRequest);


        /* ================================= */

    } // method onCreate


    /* ADAPTER
    ========================================================================================= */

    class MyAdapter extends ArrayAdapter<Person> {

        public MyAdapter() {
            super(Friend.this, android.R.layout.simple_list_item_1, arrayOfWebData);
        }

        public View getView(int position, View convertView, ViewGroup parent){

            ViewHolder holder;

            if (convertView==null) {
                LayoutInflater inflater=getLayoutInflater();
                convertView=inflater.inflate(R.layout.list_friend, null);

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

            public TextView value;
            public TextView timestamp;

            ViewHolder(View row) {
                value=(TextView)row.findViewById(R.id.value);
                timestamp=(TextView)row.findViewById(R.id.timestamp);
            }

            //notice we had to change our populate from to take an argument of type person
            void populateFrom(Person person) {

                value.setText(person.value);
                timestamp.setText(person.timestamp);

            }
        }

    } // class MyAdapter

}

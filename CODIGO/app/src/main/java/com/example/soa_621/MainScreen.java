package com.example.soa_621;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainScreen<Interval> extends AppCompatActivity implements SensorEventListener{
    private static final String API_URL_EVENT = "http://so-unlam.net.ar/api/api/event";
    private TextView cantidadDeCaidas;
    private TextView ultimaCaida;
    private TextView ultimaSiesta;
    private TextView cantidadDeSiestas;
    public  SharedPreferences sharedPreferences;
    int caidas=0;
    int cantidadDeSiestasNum=0;
    private SensorManager mSensorManager;
    Date currentTime;
    Date ultimaCaidaDate;
    Date ultimaVezDeApagado;
    Interval interval;
    public IntentFilter filtro;
    Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public ListView listView ;
    ArrayList<String> values;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sharedPreferences = getSharedPreferences("USER",MODE_PRIVATE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        activarSensores();
        cantidadDeCaidas = (TextView) findViewById(R.id.cantidadDeCaidas);
        ultimaCaida = (TextView) findViewById(R.id.ultimaCaida);
        ultimaVezDeApagado = Calendar.getInstance().getTime();
        ultimaCaidaDate = Calendar.getInstance().getTime();
        cantidadDeSiestas = (TextView) findViewById(R.id.cantidadDeSiestas);
        ultimaSiesta = (TextView) findViewById(R.id.ultimaSiesta);
        listView = (ListView) findViewById(R.id.lista);
        this.values = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, values);
        listView.setAdapter(adapter);
        loadData();
        listView.invalidateViews();
        this.cantidadDeCaidas.setText(String.valueOf(caidas));
        this.cantidadDeSiestas.setText(String.valueOf(this.cantidadDeSiestasNum));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                if (datosJsonString.equals("Error")) {
                    Toast.makeText(context.getApplicationContext(), "Error al registrar evento en base de datos", Toast.LENGTH_LONG).show();
                }else {
                    JSONObject JSONData = new JSONObject(datosJsonString);
                    Log.i("[DEBUG] Main", "Datos recibidos:" + datosJsonString);
                    Log.i("[DEBUG] Main", "Tengo: " + JSONData.get("state"));
                    if (JSONData.get("state").equals("success")) {
                        JSONObject events = JSONData.getJSONObject("event");
                        Toast.makeText(context.getApplicationContext(), events.get("description").toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }

        private void onNetworkChange(NetworkInfo networkInfo) {
            if (networkInfo != null && networkInfo.isConnected() ) {
                Log.d("MenuActivity", "CONNECTED");
            }else{
                Log.d("MenuActivity", "DISCONNECTED");
                values.add(formatter.format(Calendar.getInstance().getTime())+" - Conexion a internet perdida.");
                listView.invalidateViews();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        registrarReciever();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registrarReciever();
    }

    @Override
    protected void onDestroy()
    {
        desactivarSensores();
        super.onDestroy();
    }

    private void activarSensores()
    {
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void desactivarSensores()
    {
        mSensorManager.unregisterListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
    }
    private void registrarReciever() {
        filtro=new IntentFilter("intent.action.MainScreen");
        filtro.addCategory("intent.category.LAUNCHER");
        registerReceiver(receiver, filtro);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        String txt = "";

        synchronized (this)
        {
            switch(event.sensor.getType())
            {

                case Sensor.TYPE_ACCELEROMETER :
                    if ((event.values[0] > 12) || (event.values[1] > 12) || (event.values[2] > 12))
                    {
                        long tiempoViejoCaidas = ultimaCaidaDate.getTime();
                        long tiempoActualCaidas = Calendar.getInstance().getTime().getTime();
                        if ((tiempoActualCaidas-tiempoViejoCaidas)/1000 > 2) { //por las dudas no voy a saturar el servidor
                            this.caidas++;
                            this.cantidadDeCaidas.setText(String.valueOf(caidas));
                            String nowTime = formatter.format(Calendar.getInstance().getTime());
                            this.ultimaCaida.setText(nowTime);
                            this.values.add(nowTime+" - Caida.");
                            listView.invalidateViews();
                            registrarEvento("caida");
                        }
                        ultimaCaidaDate=Calendar.getInstance().getTime();
                    }
                    break;

                case Sensor.TYPE_LIGHT :
                        if (event.values[0] == 0){
                            long tiempoViejo = ultimaVezDeApagado.getTime();
                            long tiempoActual = Calendar.getInstance().getTime().getTime();
                            if ((tiempoActual-tiempoViejo)/1000 > 10){
                                    String nowTime = formatter.format(Calendar.getInstance().getTime());
                                    this.ultimaSiesta.setText(nowTime);
                                    this.values.add(nowTime+" - Siesta.");
                                    listView.invalidateViews();
                                    this.cantidadDeSiestasNum++;
                                    this.cantidadDeSiestas.setText(String.valueOf(this.cantidadDeSiestasNum));
                                    registrarEvento("siesta");
                                }
                            ultimaVezDeApagado= Calendar.getInstance().getTime();
                        }

                    break;

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registrarEvento(String type){
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", ActivityLogin.TOKEN);
            obj.put("env", "DEV");
            obj.put("type_events", "Evento");
            obj.put("state", "ACTIVO");
            if (type.equals("caida")){
                obj.put("description", "Caida del dispositivo registrada");
            }
            else if (type.equals("siesta")){
                obj.put("description", "Siesta registrada");
            }
            Intent i = new Intent(MainScreen.this, HTTPPost.class);
            i.putExtra("uri", API_URL_EVENT);
            i.putExtra("datosJson", obj.toString());
            i.putExtra("type","event");
            startService(i);
            saveData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        registrarReciever();
    }

    private String listToString(List<String> list) {
        StringBuilder csvList = new StringBuilder();
        for(String s : list){
            csvList.append(s);
            csvList.append(",");
        }
        return csvList.toString();
    }

    private List<String> stringToList(String csvList) {
        String[] items = csvList.split(",");
        List<String> list = new ArrayList<String>();
        for(int i=0; i < items.length; i++){
            list.add(items[i]);
        }
        return list;
    }

    private void saveData() {
        SharedPreferences sharedPreferencesObject = getSharedPreferences("USER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesObject.edit();
        Log.i("[DEBUG] Shared Pref", "Datos a guardar:" + listToString(this.values));
        editor.putString("Event", listToString(this.values));
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferencesObject = getSharedPreferences("USER", MODE_PRIVATE);
        String events = sharedPreferencesObject.getString("Event", "");
        Log.i("[DEBUG] Shared Pref", "Datos recibidos:" + events);
        if (events != "") {
            ArrayList<String> listaDeEventos = (ArrayList<String>) stringToList(events);
            for (String eventString:listaDeEventos) {
                if(eventString.contains("Caida")){
                    this.caidas++;
                }
                else if(eventString.contains("Siesta")){
                    this.cantidadDeSiestasNum++;
                }
                this.values.add(eventString);
            }
        }
    }

}

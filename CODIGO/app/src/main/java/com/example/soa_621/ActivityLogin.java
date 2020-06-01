package com.example.soa_621;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
//I/[DEBUG] Main: Datos recibidos:{"state":"success","env":"DEV","token":"$2y$10$AqjV2vARRjqrUtUEPJ4M9O\/046ZXtqQwIreVBcveATUO7M\/yGGS\/O"}
//I/[DEBUG] HTTP Post: Datos a enviar{"env":"DEV","name":"Jason","lastname":"Linares","dni":39785415,"email":"jason@json.net","password":"12345678","commission":3,"group":621}uri: http://so-unlam.net.ar/api/api/register
public class ActivityLogin extends AppCompatActivity {
    private static final String API_URL_LOGIN = "http://so-unlam.net.ar/api/api/login";
    public static String TOKEN = "";
    public IntentFilter filtro;
    private Button btnLogin ;
    private EditText txtEmail;
    private EditText txtPassword;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                Log.i("[DEBUG] Login", "Tengo: " + datosJsonString);
                if (datosJsonString.equals("Error")) {
                    Toast.makeText(context.getApplicationContext(), "Usuario o contraseña incorrecta", Toast.LENGTH_LONG).show();
                }else{
                JSONObject JSONData = new JSONObject(datosJsonString);
                Log.i("[DEBUG] Main", "Datos recibidos:" + datosJsonString);
                Log.i("[DEBUG] Main", "Tengo: " + JSONData.get("state"));
                if (JSONData.get("state").equals("success")) {
                    Toast.makeText(context.getApplicationContext(), "Login correcto!:", Toast.LENGTH_LONG).show();
                    TOKEN= (String) JSONData.get("token");
                    startActivity(new Intent(ActivityLogin.this, MainScreen.class));
                } else {
                    Toast.makeText(context.getApplicationContext(), "Error:" + JSONData.get("msg"), Toast.LENGTH_LONG).show();
                }
            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState1) {

        super.onCreate(savedInstanceState1);
        setContentView(R.layout.activity_login);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPass);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                if (validarCampos()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("env", "DEV");
                    obj.put("email", txtEmail.getText().toString());
                    obj.put("name", "nombrehardcodeado");
                    obj.put("lastname", "tambienhardcodeado");
                    obj.put("dni", 12345678);
                    obj.put("commission", 03);
                    obj.put("group", 621);
                    obj.put("password", txtPassword.getText().toString());
                    Intent i = new Intent(ActivityLogin.this, HTTPPost.class);
                    i.putExtra("uri", API_URL_LOGIN);
                    i.putExtra("datosJson", obj.toString());
                    i.putExtra("type","login");
                    startService(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                }
            }
        });
        registrarReciever();
    }

    private void registrarReciever() {
        filtro=new IntentFilter("intent.action.ActivityLogin");
        filtro.addCategory("intent.category.LAUNCHER");
        registerReceiver(receiver, filtro);
    }

    public void accederAMain(View view) {
        startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
    }

    public boolean validarCampos() {
        boolean isvalid = true;
        String email = txtEmail.getText().toString();
        String secret = txtPassword.getText().toString();

        if(email.isEmpty() || !email.contains("@")){
            txtEmail.setError("Email invalido");
            isvalid = false;
        }
        if(secret.isEmpty() || secret.length()<8){
            txtPassword.setError("Password invalida");
            isvalid = false;
        }
        return isvalid;
    }
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
        super.onDestroy();
    }
}

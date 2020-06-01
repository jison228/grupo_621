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

public class ActivityMain extends AppCompatActivity {
    private static final String API_URL_REGISTER = "http://so-unlam.net.ar/api/api/register";
    private EditText txtNombre;
    private EditText txtApellido;
    private EditText txtDni;
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtGrupo;
    private EditText txtComision;
    private Button btnRegistrar;
    public IntentFilter filtro;
    
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                if (datosJsonString.equals("Error")) {
                    Toast.makeText(context.getApplicationContext(), "El usuario ya existe!", Toast.LENGTH_LONG).show();
                }else {
                    JSONObject JSONData = new JSONObject(datosJsonString);
                    Log.i("[DEBUG] Main", "Datos recibidos:" + datosJsonString);
                    Log.i("[DEBUG] Main", "Tengo: " + JSONData.get("state"));
                    if (JSONData.get("state").equals("success")) {
                        Toast.makeText(context.getApplicationContext(), "Registro correcto!:", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ActivityMain.this, ActivityLogin.class));
                    } else {
                        Toast.makeText(context.getApplicationContext(), "Error:" + JSONData.get("msg"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    public boolean validarCampos() {

        boolean isvalid = true;
        String email = txtEmail.getText().toString();
        String secret = txtPassword.getText().toString();
        String comision = txtComision.getText().toString();
        String grupo = txtGrupo.getText().toString();
        String nombre = txtNombre.getText().toString();
        String apellido = txtApellido.getText().toString();
        String dni = txtDni.getText().toString();

        if(nombre.isEmpty()){
            txtNombre.setError("Nombre invalido");
            isvalid = false;
        }
        if(apellido.isEmpty()){
            txtApellido.setError("Apellido invalido");
            isvalid = false;
        }

        if(dni.isEmpty() || dni.length()>8){
            txtDni.setError("DNI invalido");
            isvalid = false;
        }

        if(email.isEmpty() || !email.contains("@")){
            txtEmail.setError("Email invalido");
            isvalid = false;
        }
        if(secret.isEmpty() || secret.length()<8){
            txtPassword.setError("Password invalida");
            isvalid = false;
        }

        if(comision.isEmpty()){
            txtComision.setError("Comision invalida");
            isvalid = false;
        }

        if(grupo.isEmpty()){
            txtGrupo.setError("Grupo invalido");
            isvalid = false;
        }
        return isvalid;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        txtNombre = (EditText) findViewById(R.id.txtNombre);
        txtApellido = (EditText) findViewById(R.id.txtApellido);
        txtDni = (EditText) findViewById(R.id.numDni);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPass);
        txtGrupo = (EditText) findViewById(R.id.numGrupo);
        txtComision = (EditText) findViewById(R.id.numComision);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override


            public void onClick(View v) {

                if (validarCampos()) {

                    JSONObject obj = new JSONObject();

                    try {
                        obj.put("env", "DEV");
                        obj.put("name", txtNombre.getText().toString());
                        obj.put("lastname", txtApellido.getText().toString());
                        obj.put("dni", Integer.parseInt(txtDni.getText().toString()));
                        obj.put("email", txtEmail.getText().toString());
                        obj.put("password", txtPassword.getText().toString());
                        obj.put("commission", Integer.parseInt(txtComision.getText().toString()));
                        obj.put("group", Integer.parseInt(txtGrupo.getText().toString()));
                        Intent i = new Intent(ActivityMain.this, HTTPPost.class);
                        i.putExtra("uri", API_URL_REGISTER);
                        i.putExtra("datosJson", obj.toString());
                        i.putExtra("type","register");
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
        filtro=new IntentFilter("intent.action.ActivityMain");
        filtro.addCategory("intent.category.LAUNCHER");
        registerReceiver(receiver, filtro);
    }


    public void accederALogin(View view) {
            startActivity(new Intent(ActivityMain.this, ActivityLogin.class));
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

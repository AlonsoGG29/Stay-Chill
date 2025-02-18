package com.aka.staychill;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CrearEvento extends AppCompatActivity {

    private EditText inputNombre, inputLocalizacion, inputDescripcion, inputFecha, inputHora;
    private final OkHttpClient client = new OkHttpClient();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        sessionManager = new SessionManager(this);
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        inputNombre = findViewById(R.id.inputNombre);
        inputLocalizacion = findViewById(R.id.inputLocalizacion);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputFecha = findViewById(R.id.inputFecha);
        inputHora = findViewById(R.id.inputHora);
        Button btnCrearEvento = findViewById(R.id.btnCrearEvento);

        btnCrearEvento.setOnClickListener(v -> {
            if (validarCampos()) {
                crearEventoEnSupabase();
            }
        });
    }

    private boolean validarCampos() {
        if (inputNombre.getText().toString().trim().isEmpty()) {
            mostrarError("El nombre del evento es obligatorio");
            return false;
        }
        if (inputFecha.getText().toString().trim().isEmpty()) {
            mostrarError("La fecha es obligatoria");
            return false;
        }
        if (inputHora.getText().toString().trim().isEmpty()) {
            mostrarError("La hora es obligatoria");
            return false;
        }
        return true;
    }

    private void crearEventoEnSupabase() {
        new Thread(() -> {
            try {
                JsonObject eventoJson = new JsonObject();
                eventoJson.addProperty("nombre_evento", inputNombre.getText().toString().trim());
                eventoJson.addProperty("localizacion", inputLocalizacion.getText().toString().trim());
                eventoJson.addProperty("descripcion", inputDescripcion.getText().toString().trim());
                eventoJson.addProperty("fecha_evento", parsearFecha(inputFecha.getText().toString()));
                eventoJson.addProperty("hora_evento", inputHora.getText().toString() + "+02:00"); // Ajusta la zona horaria
                eventoJson.addProperty("creador_evento", sessionManager.getUserId());

                RequestBody body = RequestBody.create(
                        eventoJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/eventos")
                        .post(body)
                        .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                        .addHeader("Authorization", "Bearer " + sessionManager.getUserToken())
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> mostrarError("Error de conexión: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        if (response.isSuccessful()) {
                            runOnUiThread(() -> {
                                Toast.makeText(CrearEvento.this, "Evento creado con éxito", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> mostrarError("Error del servidor: " + response.code()));
                        }
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> mostrarError("Error: " + e.getMessage()));
            }
        }).start();
    }

    private String parsearFecha(String fechaInput) throws Exception {
        // Asume que el formato de entrada es dd/MM/yyyy (puedes ajustarlo)
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = inputFormat.parse(fechaInput);
        return dateFormat.format(date);
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }
}
package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Login extends AppCompatActivity {

    private Button btnEntrar;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        client = SupabaseConfig.getClient(); // Obtener cliente OkHttp

        btnEntrar = findViewById(R.id.btn_login);
        configurarBotonEntrar();
    }

    private void configurarBotonEntrar() {
        btnEntrar.setOnClickListener(view -> {
            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String contrasenia = ((EditText) findViewById(R.id.password)).getText().toString();

            if (email.isEmpty() || contrasenia.isEmpty()) {
                Toast.makeText(Login.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                iniciarSesion(email, contrasenia);
            }
        });
    }

    private void iniciarSesion(String email, String contrasenia) {
        String url = SupabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password";
        String apiKey = SupabaseConfig.getSupabaseKey();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", contrasenia);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Login.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        if (jsonObject.has("access_token")) {
                            String accessToken = jsonObject.getString("access_token");
                            getSharedPreferences("app_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("access_token", accessToken)
                                    .apply();

                            obtenerDetallesUsuario(accessToken);
                        } else {
                            runOnUiThread(() -> Toast.makeText(Login.this, "Email o contraseña errónea", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Login.this, "Email o contraseña errónea", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void obtenerDetallesUsuario(String accessToken) {
        String url = SupabaseConfig.getSupabaseUrl() + "/auth/v1/user";
        String apiKey = SupabaseConfig.getSupabaseKey();

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Login.this, "Error al obtener detalles del usuario", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        String userId = jsonObject.getString("id");
                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("user_id", userId)
                                .apply();

                        runOnUiThread(() -> {
                            Toast.makeText(Login.this, "Iniciada la sesión correctamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, Main_bn.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Login.this, "Error al obtener detalles del usuario", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}

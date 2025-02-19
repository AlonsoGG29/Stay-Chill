package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    private SessionManager sessionManager;
    private OkHttpClient client;
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        client = SupabaseConfig.getClient();

        if (sessionManager.isLoggedIn()) {
            validarTokenEnServidor();
        }

        inicializarVistas();
    }

    private void inicializarVistas() {
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvRegistro = findViewById(R.id.register_text);

        btnLogin.setOnClickListener(v -> validarYLogin());
        estilizarTextoRegistro(tvRegistro);
    }

    private void validarYLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (!validarCampos(email, password)) return;

        RequestBody body = RequestBody.create(
                crearJsonCredenciales(email, password),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password")
                .post(body)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    procesarRespuestaExitosa(response);
                } else {
                    String errorMessage = obtenerMensajeError(response);
                    mostrarError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión: " + e.getMessage());
            }
        });
    }

    private String crearJsonCredenciales(String email, String password) {
        try {
            return new JSONObject()
                    .put("email", email)
                    .put("password", password)
                    .toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    private void procesarRespuestaExitosa(Response response) throws IOException {
        try {
            JSONObject json = new JSONObject(response.body().string());
            String accessToken = json.getString("access_token");
            String refreshToken = json.getString("refresh_token");
            UUID userId = UUID.fromString(json.getJSONObject("user").getString("id"));

            sessionManager.saveSession(accessToken, refreshToken, userId);
            verificarExistenciaPerfil(userId);

        } catch (JSONException | IllegalArgumentException e) {
            mostrarError("Error procesando respuesta del servidor");
        }
    }

    private String obtenerMensajeError(Response response) throws IOException {
        try {
            JSONObject errorJson = new JSONObject(response.body().string());
            return errorJson.optString("message", "Error desconocido");
        } catch (JSONException e) {
            return "Error " + response.code();
        }
    }

    private void validarTokenEnServidor() {
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/auth/v1/user")
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    redirigirAMain();
                } else {
                    sessionManager.logout();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> mostrarError("Error de conexión"));
            }
        });
    }

    private boolean validarCampos(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarError("Formato de email inválido");
            return false;
        }
        return true;
    }

    private void estilizarTextoRegistro(TextView textView) {
        SpannableString spannable = new SpannableString("¿No tienes cuenta? Regístrate");
        int inicio = spannable.toString().indexOf("Regístrate");

        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(Login.this, Signup.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(getColor(android.R.color.white));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        spannable.setSpan(clickSpan, inicio, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), inicio, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void redirigirAMain() {
        runOnUiThread(() -> {
            mostrarExito("Inicio de sesión exitoso");
            startActivity(new Intent(this, Main_bn.class));
            finish();
        });
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }

    private void mostrarExito(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }

    private void verificarExistenciaPerfil(UUID userId) {
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful() && responseBody.length() > 2) {
                    redirigirAMain();
                } else {
                    runOnUiThread(() -> {
                        mostrarError("Perfil incompleto. Complete su perfil primero");
                        sessionManager.logout();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión");
            }
        });
    }
}
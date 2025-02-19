package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Signup extends AppCompatActivity {

    private SessionManager sessionManager;
    private EditText emailField, passwordField, confirmPasswordField, nameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            redirigirAMain();
            return;
        }

        inicializarVistas();
    }

    private void inicializarVistas() {
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.password_repetir);
        nameField = findViewById(R.id.nombre);
        Button btnSignup = findViewById(R.id.btn_signup);
        TextView tvLogin = findViewById(R.id.inicio_text);

        btnSignup.setOnClickListener(v -> procesarRegistro());
        estilizarTextoLogin(tvLogin);
    }

    private void procesarRegistro() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String name = nameField.getText().toString().trim();

        if (!validarCampos(email, password, confirmPassword, name)) return;

        RequestBody body = RequestBody.create(
                crearJsonRegistro(email, password, name),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/auth/v1/signup")
                .post(body)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    procesarRegistroExitoso(response);
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

    private void procesarRegistroExitoso(Response response) throws IOException {
        try {
            JSONObject json = new JSONObject(response.body().string());
            String accessToken = json.getString("access_token");
            String refreshToken = json.getString("refresh_token");
            UUID userId = UUID.fromString(json.getJSONObject("user").getString("id"));

            sessionManager.saveSession(accessToken, refreshToken, userId);
            crearUsuarioEnBD(nameField.getText().toString().trim());

        } catch (JSONException | IllegalArgumentException e) {
            mostrarError("Error procesando respuesta del servidor");
        }
    }

    private void crearUsuarioEnBD(String nombre) {
        try {
            JSONObject json = new JSONObject();
            json.put("nombre", nombre);
            json.put("foren_uid", sessionManager.getUserId().toString());

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios")
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .addHeader("Prefer", "return=representation")
                    .build();

            SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        mostrarExito("Registro exitoso!");
                        redirigirAMain();
                    } else {
                        eliminarUsuarioAuth();
                        mostrarError("Error creando perfil");
                        sessionManager.logout();
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    eliminarUsuarioAuth();
                    mostrarError("Error de conexión");
                    sessionManager.logout();
                }
            });

        } catch (JSONException e) {
            mostrarError("Error construyendo datos del usuario");
        }
    }

    private String obtenerMensajeError(Response response) throws IOException {
        try {
            JSONObject errorJson = new JSONObject(response.body().string());
            return errorJson.optString("message", "Error en el registro");
        } catch (JSONException e) {
            return "Error " + response.code();
        }
    }

    private boolean validarCampos(String email, String password, String confirmPassword, String name) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarError("Formato de email inválido");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            mostrarError("Las contraseñas no coinciden");
            return false;
        }

        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        return true;
    }

    private String crearJsonRegistro(String email, String password, String name) {
        try {
            return new JSONObject()
                    .put("email", email)
                    .put("password", password)
                    .put("options", new JSONObject()
                            .put("data", new JSONObject()
                                    .put("display_name", name)
                            )
                    ).toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    private void estilizarTextoLogin(TextView textView) {
        SpannableString spannable = new SpannableString("¿Ya tienes cuenta? Inicia sesión");
        int inicio = spannable.toString().indexOf("Inicia sesión");

        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(Signup.this, Login.class));
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, Main_bn.class));
            finish();
        }, 1000); // Retardo para ver el Toast
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }

    private void mostrarExito(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }

    private void eliminarUsuarioAuth() {
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/auth/v1/admin/users/" + sessionManager.getUserId())
                .delete()
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey())
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {}

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}
        });
    }
}
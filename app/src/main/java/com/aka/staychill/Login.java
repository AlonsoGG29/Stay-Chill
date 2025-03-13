package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
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

    private static final String TAG = "LoginActivity";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String SUPABASE_AUTH_URL = SupabaseConfig.getSupabaseUrl() + "/auth/v1/";

    private SessionManager sessionManager;
    private OkHttpClient client;
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarComponentes();
        verificarSesionExistente();
    }

    private void inicializarComponentes() {
        sessionManager = new SessionManager(this);
        client = SupabaseConfig.getClient();
        inicializarVistas();
    }

    private void verificarSesionExistente() {
        if (sessionManager.isLoggedIn()) {
            validarTokenEnServidor();
        }
    }

    private void inicializarVistas() {
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);

        configurarBotonLogin();
        configurarTextoRegistro();
    }

    private void configurarBotonLogin() {
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> validarYLogin());
    }

    private void configurarTextoRegistro() {
        TextView tvRegistro = findViewById(R.id.register_text);
        estilizarTextoRegistro(tvRegistro);
    }

    private void validarYLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (!validarCampos(email, password)) return;

        realizarPeticionLogin(email, password);
    }

    private void realizarPeticionLogin(String email, String password) {
        RequestBody body = RequestBody.create(
                crearJsonCredenciales(email, password),
                JSON
        );

        Request request = new Request.Builder()
                .url(SUPABASE_AUTH_URL + "token?grant_type=password")
                .post(body)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                procesarRespuestaLogin(response);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                manejarErrorConexion(e);
            }
        });
    }

    private void procesarRespuestaLogin(Response response) throws IOException {
        assert response.body() != null;
        String responseBody = response.body().string();
        if (response.isSuccessful()) {
            procesarRespuestaExitosa(responseBody);
        } else {
            manejarErrorServidor(responseBody);
        }
    }

    private void procesarRespuestaExitosa(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            String accessToken = json.getString("access_token");
            String refreshToken = json.getString("refresh_token");
            UUID userId = UUID.fromString(json.getJSONObject("user").getString("id"));

            sessionManager.saveSession(accessToken, refreshToken, userId);
            redirigirAMain();

        } catch (JSONException | IllegalArgumentException e) {
            Log.e(TAG, "Error procesando respuesta: " + e.getMessage());
            mostrarMensaje("Error procesando respuesta del servidor");
        }
    }

    private void manejarErrorServidor(String responseBody) {
        String errorMessage = obtenerMensajeError(responseBody, emailField.getText().toString());
        Log.e(TAG, "Error en login: " + errorMessage);
        mostrarMensaje(errorMessage);
    }

    private void manejarErrorConexion(IOException e) {
        Log.e(TAG, "Error de conexión: " + e.getMessage());
        mostrarMensaje("Error de conexión: " + e.getMessage());
    }

    private String crearJsonCredenciales(String email, String password) {
        try {
            return new JSONObject()
                    .put("email", email)
                    .put("password", password)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException("Error creando JSON de credenciales");
        }
    }

    private String obtenerMensajeError(String responseBody, String email) {
        try {
            JSONObject errorJson = new JSONObject(responseBody);
            String mensajeUsuario = "Error de autenticación";

            if (errorJson.has("error_code")) {
                String errorCode = errorJson.optString("error_code");
                String errorMsg = errorJson.optString("msg");

                switch (errorCode.toLowerCase()) {
                    case "user_not_found":
                        mensajeUsuario = "Usuario no registrado";
                        break;
                    case "invalid_credentials":
                        mensajeUsuario = validarFormatoEmail(email)
                                ? "Correo o contraseña incorrectos"
                                : "Formato de email inválido";
                        break;
                    case "email_not_confirmed":
                        mensajeUsuario = "Confirma tu email antes de iniciar sesión";
                        break;
                    case "too_many_requests":
                        mensajeUsuario = "Demasiados intentos. Espera 5 minutos";
                        break;
                    default:
                        mensajeUsuario = "Error: " + errorMsg;
                }
            }
            return mensajeUsuario;

        } catch (JSONException e) {
            return "Error inesperado. Intenta nuevamente";
        }
    }

    private boolean validarFormatoEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void validarTokenEnServidor() {
        Request request = new Request.Builder()
                .url(SUPABASE_AUTH_URL + "user")
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    mostrarMensaje("Tu sesión ha caducado");
                    sessionManager.logout();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarMensaje("Error de conexión");
            }
        });
    }

    private boolean validarCampos(String email, String password) {
        if (email.isEmpty()) {
            mostrarMensaje("Ingresa tu correo electrónico");
            return false;
        }
        if (password.isEmpty()) {
            mostrarMensaje("Ingresa tu contraseña");
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
        textView.setText(spannable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void redirigirAMain() {
        runOnUiThread(() -> {
            mostrarMensaje("Inicio de sesión exitoso");
            startActivity(new Intent(this, Main_bn.class));
            finish();
        });
    }

    private void mostrarMensaje(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }

}
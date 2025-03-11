package com.aka.staychill; //Clean Fernando

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
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Signup extends AppCompatActivity {

    // Constantes
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final String AUTH_URL = SupabaseConfig.getSupabaseUrl() + "/auth/v1/";
    private static final String REST_URL = SupabaseConfig.getSupabaseUrl() + "/rest/v1/";

    // Variables
    private EditText emailField, passwordField, confirmPasswordField, nameField;
    private Button signup;
    private TextView login;
    private SessionManager sessionManager;

    // Ciclo de vida de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);
        verificarSesionExistente();
        inicializarComponentes();
    }

    // Configuración Inicial
    private void verificarSesionExistente() {
        if (sessionManager.isLoggedIn()) {
            redirigirAMain();
        }
    }

    private void inicializarComponentes() {
        inicializarVistas();
        configurarListeners();
        estilizarTextoLogin();
    }

    // Configuración de Vistas
    private void inicializarVistas() {
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.password_repetir);
        nameField = findViewById(R.id.nombre);
        signup = findViewById(R.id.btn_signup);
        login = findViewById(R.id.inicio_text);

        configurarBotonRegistro();
        estilizarTextoLogin();
    }

    private void configurarBotonRegistro() {
        Button btnSignup = findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(v -> procesarRegistro());
    }

    private void configurarListeners() {
        signup.setOnClickListener(v -> procesarRegistro());
        login.setOnClickListener(v ->
                startActivity(new Intent(Signup.this, Login.class)));
    }

    private void estilizarTextoLogin() {
        TextView tvLogin = findViewById(R.id.inicio_text);
        SpannableString spannable = new SpannableString("¿Ya tienes cuenta? Inicia sesión");
        int inicio = spannable.toString().indexOf("Inicia sesión");

        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
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
        tvLogin.setText(spannable);
        tvLogin.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // Lógica de Registro
    private void procesarRegistro() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String name = nameField.getText().toString().trim();

        if (!validarCampos(email, password, confirmPassword, name)) return;

        realizarRegistroAuth(email, password, name);
    }

    private void realizarRegistroAuth(String email, String password, String name) {
        RequestBody body = RequestBody.create(
                crearJsonRegistro(email, password, name),
                JSON
        );

        Request request = new Request.Builder()
                .url(AUTH_URL + "signup")
                .post(body)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    procesarRegistroExitoso(response);
                } else {
                    manejarErrorRegistro(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                manejarErrorConexion(e);
            }
        });
    }

    private void procesarRegistroExitoso(Response response) throws IOException {
        try {
            JSONObject json = new JSONObject(response.body().string());
            guardarSesionUsuario(json);
            crearPerfilUsuario();
        } catch (JSONException e) {
            manejarErrorProcesamiento();
        }
    }

    private void guardarSesionUsuario(JSONObject json) throws JSONException {
        String accessToken = json.getString("access_token");
        String refreshToken = json.getString("refresh_token");
        UUID userId = UUID.fromString(json.getJSONObject("user").getString("id"));
        sessionManager.saveSession(accessToken, refreshToken, userId);
    }

    // Creación de Perfil
    private void crearPerfilUsuario() {
        try {
            Request request = construirRequestPerfil();

            SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        manejarRegistroCompleto();
                    } else {
                        manejarErrorPerfil();
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    manejarErrorPerfil();
                }
            });

        } catch (JSONException e) {
            mostrarMensaje("Error construyendo datos del usuario");
        }
    }

    private Request construirRequestPerfil() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("nombre", nameField.getText().toString().trim());
        json.put("foren_uid", sessionManager.getUserId().toString());

        return new Request.Builder()
                .url(REST_URL + "usuarios")
                .post(RequestBody.create(json.toString(), JSON))
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Prefer", "return=representation")
                .build();
    }

    // Manejo de Errores
    private void manejarErrorRegistro(Response response) throws IOException {
        String errorMessage = obtenerMensajeError(response);
        mostrarMensaje(errorMessage);
    }

    private String obtenerMensajeError(Response response) throws IOException {
        try {
            JSONObject errorJson = new JSONObject(response.body().string());
            String errorCode = errorJson.optString("error_code", "general_error");
            String errorMsg = errorJson.optString("message", "Error en el registro");

            switch (errorCode.toLowerCase()) {
                case "user_already_exists":
                    return "El usuario ya está registrado";
                case "weak_password":
                    return "La contraseña es demasiado débil";
                case "email_not_validated":
                    return "Verifica tu correo electrónico";
                case "rate_limit_exceeded":
                    return "Demasiados intentos. Intenta más tarde";
                default:
                    return "Error: " + errorMsg;
            }
        } catch (JSONException e) {
            return "Error desconocido (" + response.code() + ")";
        }
    }

    private void manejarErrorProcesamiento() {
        mostrarMensaje("Error procesando respuesta del servidor");
        sessionManager.logout();
    }

    private void manejarErrorConexion(IOException e) {
        mostrarMensaje("Error de conexión: " + e.getMessage());
    }

    private void manejarErrorPerfil() {
        eliminarUsuarioAuth();
        mostrarMensaje("Error creando perfil de usuario");
        sessionManager.logout();
    }

    // Validaciones
    private boolean validarCampos(String email, String password, String confirmPassword, String name) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarMensaje("Formato de email inválido");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            mostrarMensaje("Las contraseñas no coinciden");
            return false;
        }

        if (password.length() < 6) {
            mostrarMensaje("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        return true;
    }

    // Helpers
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

    private void eliminarUsuarioAuth() {
        Request request = new Request.Builder()
                .url(AUTH_URL + "admin/users/" + sessionManager.getUserId())
                .delete()
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey()) // Usar service role key
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    Log.e("Signup", "Error eliminando usuario auth: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Signup", "Error de conexión al eliminar usuario", e);
            }
        });
    }

    private void manejarRegistroCompleto() {
        mostrarMensaje("¡Registro exitoso!");
        redirigirAMain();
    }

    private void redirigirAMain() {
        runOnUiThread(() -> {
            startActivity(new Intent(this, Main_bn.class));
            finish();
        });
    }

    private void mostrarMensaje(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }
}
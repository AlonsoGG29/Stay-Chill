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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class Signup extends AppCompatActivity {

    private Button btnEntrarSignUp;
    private TextView inicio;
    private EditText emailField, passwordField, passwordRepetirField, nombreField;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        client = SupabaseConfig.getClient();

        btnEntrarSignUp = findViewById(R.id.btn_signup);
        inicio = findViewById(R.id.inicio_text);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        passwordRepetirField = findViewById(R.id.password_repetir);
        nombreField = findViewById(R.id.nombre);

        configurarBotonEntrar();
        aplicarInsetsVentana();
        estilizarYHacerClicable();
    }

    private void configurarBotonEntrar() {
        btnEntrarSignUp.setOnClickListener(view -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            String passwordRepetir = passwordRepetirField.getText().toString();
            String nombre = nombreField.getText().toString();

            if (email.isEmpty() || password.isEmpty() || passwordRepetir.isEmpty() || nombre.isEmpty()) {
                showToast("Por favor, rellena todos los campos");
            } else if (!password.equals(passwordRepetir)) {
                showToast("Las contraseñas no coinciden");
            } else if (password.length() < 6) {
                showToast("La contraseña debe tener al menos 6 caracteres");
            } else {
                registrarUsuario(email, password, nombre);
            }
        });
    }

    private void aplicarInsetsVentana() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fr_main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });
    }

    private void estilizarYHacerClicable() {
        SpannableString spannableString = new SpannableString("¿Ya tienes cuenta? Inicia sesión");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(Signup.this, Login.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setFakeBoldText(true);
                ds.setColor(ContextCompat.getColor(Signup.this, android.R.color.white));
            }
        };

        spannableString.setSpan(clickableSpan, 17, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 17, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        inicio.setText(spannableString);
        inicio.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void registrarUsuario(String email, String password, String nombre) {
        String url = SupabaseConfig.getSupabaseUrl() + "/auth/v1/signup";
        String apiKey = SupabaseConfig.getSupabaseKey();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("display_name", nombre);
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
                runOnUiThread(() -> showToast("Error al registrar usuario"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String userId = jsonObject.getJSONObject("user").getString("id");

                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("user_id", userId)
                                .apply();

                        crearEntradaUsuarios(userId, nombre);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        try {
                            showToast("Error al registrar usuario: " + response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        });
    }

    private void crearEntradaUsuarios(String userId, String nombre) {
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios";
        String apiKey = SupabaseConfig.getSupabaseKey();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("foren_uid", userId);
            jsonBody.put("nombre", nombre);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> showToast("Error al crear entrada en la tabla usuarios"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        showToast("Usuario registrado con éxito");
                        startActivity(new Intent(Signup.this, Main_bn.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        try {
                            showToast("Error al crear entrada en la tabla usuarios: " + response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(Signup.this, message, Toast.LENGTH_SHORT).show());
    }
}
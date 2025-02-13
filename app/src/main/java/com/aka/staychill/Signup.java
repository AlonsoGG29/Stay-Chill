package com.aka.staychill;

import android.content.Intent;
import android.graphics.Typeface;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;

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

        client = SupabaseConfig.getClient(); // Obtener cliente OkHttp

        btnEntrarSignUp = findViewById(R.id.btn_signup);
        inicio = findViewById(R.id.inicio_text);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        passwordRepetirField = findViewById(R.id.password_repetir);
        nombreField = findViewById(R.id.nombre);

        // Configurar el botón "Entrar"
        configurarBotonEntrar();

        // Aplicar insets de ventana
        aplicarInsetsVentana();

        // Estilizar y hacer clicable "Inicia sesión"
        estilizarYHacerClicable();
    }

    private void configurarBotonEntrar() {
        btnEntrarSignUp.setOnClickListener(view -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            String passwordRepetir = passwordRepetirField.getText().toString();
            String nombre = nombreField.getText().toString();

            if (email.isEmpty() || password.isEmpty() || passwordRepetir.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(Signup.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(passwordRepetir)) {
                Toast.makeText(Signup.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(Signup.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            } else {
                crearCuenta(email, password, nombre);
            }
        });
    }

    private void aplicarInsetsVentana() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fr_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void estilizarYHacerClicable() {
        String texto = "¿Ya tienes cuenta? Inicia sesión";
        SpannableString spannableString = new SpannableString(texto);

        // Hacer que "Inicia sesión" sea clicable y en negrita
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navegar a la actividad Login
                Intent intent = new Intent(Signup.this, Login.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setFakeBoldText(true); // Aplicar negrita al texto
                ds.setColor(ContextCompat.getColor(Signup.this, android.R.color.white)); // Cambiar el color a blanco
            }
        }, texto.indexOf("Inicia sesión"), texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplicar negrita a "Inicia sesión"
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), texto.indexOf("Inicia sesión"), texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        inicio.setText(spannableString);
        inicio.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void crearCuenta(String email, String password, String nombre) {
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios"; // 'usuarios' es el nombre de tu tabla
        String apiKey = SupabaseConfig.getSupabaseKey();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("contrasenia", password);
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
                runOnUiThread(() -> Toast.makeText(Signup.this, "Error al crear la cuenta: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(responseBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String errorMessage = "Fallo en el registro";
                if (!response.isSuccessful()) {
                    if (jsonResponse != null) {
                        if (jsonResponse.has("error_description")) {
                            errorMessage = jsonResponse.optString("error_description");
                        } else if (jsonResponse.has("message")) {
                            errorMessage = jsonResponse.optString("message");
                        }

                        // Añadir comprobaciones específicas
                        if (errorMessage.contains("invalid email")) {
                            errorMessage = "El formato del correo electrónico no es válido.";
                        } else if (errorMessage.contains("weak password")) {
                            errorMessage = "La contraseña es demasiado débil.";
                        } else if (errorMessage.contains("already registered")) {
                            errorMessage = "El correo electrónico ya está registrado.";
                        }
                    }
                    String finalErrorMessage = "Fallo en el registro: " + errorMessage;
                    runOnUiThread(() -> Toast.makeText(Signup.this, finalErrorMessage, Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(Signup.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Signup.this, Main_bn.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }
}

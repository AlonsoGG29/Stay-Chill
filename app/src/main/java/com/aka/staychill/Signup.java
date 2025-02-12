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

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Signup extends AppCompatActivity {

    private Button btnEntrarSignUp;
    private TextView inicio;
    private EditText emailField, passwordField, passwordRepetirField;
    private IniciarSupabase supabaseConfig;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        supabaseConfig = new IniciarSupabase(); // Inicializar SupabaseConfig
        client = IniciarSupabase.getClient(); // Obtener cliente OkHttp

        btnEntrarSignUp = findViewById(R.id.btn_signup);
        inicio = findViewById(R.id.inicio_text);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        passwordRepetirField = findViewById(R.id.password_repetir);

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

            if (email.isEmpty() || password.isEmpty() || passwordRepetir.isEmpty()) {
                Toast.makeText(Signup.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(passwordRepetir)) {
                Toast.makeText(Signup.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(Signup.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            } else {
                crearCuenta(email, password);
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

    private void crearCuenta(String email, String password) {
        String signupUrl = IniciarSupabase.getSupabaseUrl() + "/auth/v1/signup";
        String apiKey = IniciarSupabase.getSupabaseKey();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(signupUrl)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Signup.this, "Error de conexión", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(Signup.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Signup.this, Main_bn.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(Signup.this, "Fallo en el registro: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}

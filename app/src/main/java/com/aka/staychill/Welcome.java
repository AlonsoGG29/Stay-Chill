package com.aka.staychill;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Welcome extends AppCompatActivity {

    private Button btnEntrar, btnIniciarSesion;
    private TextView registrarse;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        client = SupabaseConfig.getClient(); // Obtener cliente OkHttp

        // Verificar si el usuario ya está autenticado
        verificarAutenticacion();

        // Configurar la vista y listeners
        btnEntrar = findViewById(R.id.entrar_welcome);
        btnIniciarSesion = findViewById(R.id.sesion_welcome);
        registrarse = findViewById(R.id.registrarse_welcome);

        // Configurar listeners de botones
        configurarListenersBotones();

        // Aplicar insets de ventana
        aplicarInsetsVentana();

        // Estilizar y hacer clicable "Regístrate"
        estilizarYHacerClicable();
    }

    private void verificarAutenticacion() {
        String sessionUrl = SupabaseConfig.getSupabaseUrl() + "/auth/v1/user";
        String apiKey = SupabaseConfig.getSupabaseKey();

        Request request = new Request.Builder()
                .url(sessionUrl)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.has("id")) {
                            // Usuario autenticado, redirigir a Main_bn
                            runOnUiThread(() -> {
                                startActivity(new Intent(Welcome.this, Main_bn.class));
                                finish();
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void configurarListenersBotones() {
        // Navegar a la actividad Main_bn
        btnEntrar.setOnClickListener(view -> startActivity(new Intent(Welcome.this, Main_bn.class)));

        // Navegar a la actividad Login
        btnIniciarSesion.setOnClickListener(view -> startActivity(new Intent(Welcome.this, Login.class)));
    }

    private void aplicarInsetsVentana() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fr_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void estilizarYHacerClicable() {
        String texto = getString(R.string.no_tiene_cuenta);

        // Encontrar "Regístrate" en el texto
        int inicio = texto.indexOf("Regístrate");
        int fin = inicio + "Regístrate".length();

        SpannableString spannableString = new SpannableString(texto);

        // Aplicar estilos
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Hacer "Regístrate" clicable
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(Welcome.this, Signup.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.WHITE);
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
            }
        }, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        registrarse.setText(spannableString);
        registrarse.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

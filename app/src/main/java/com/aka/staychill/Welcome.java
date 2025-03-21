package com.aka.staychill;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Welcome extends AppCompatActivity {

    private static final String SUPABASE_AUTH_URL = SupabaseConfig.getSupabaseUrl() + "/auth/v1/";

    private Button btnEntrar, btnIniciarSesion;
    private TextView registrarse;
    private SessionManager sessionManager;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        inicializarComponentes();
        verificarSesionExistente();

        inicializarVistas();
        configurarListeners();
        estilizarTextoRegistro();
    }

    private void inicializarComponentes() {
        sessionManager = new SessionManager(this);
        client = SupabaseConfig.getClient();
    }

    private void verificarSesionExistente() {
        if (sessionManager.isLoggedIn()) {
            validarTokenEnServidor();
        }
    }

    private void inicializarVistas() {
        btnEntrar = findViewById(R.id.entrar_welcome);
        btnIniciarSesion = findViewById(R.id.sesion_welcome);
        registrarse = findViewById(R.id.registrarse_welcome);
    }

    private void configurarListeners() {
        btnEntrar.setOnClickListener(v -> redirigirAMain());

        btnIniciarSesion.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));
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
                    sessionManager.logout();
                } else {
                    redirigirAMain();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(Welcome.this, "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void estilizarTextoRegistro() {
        String texto = getString(R.string.no_tiene_cuenta);
        SpannableString spannable = new SpannableString(texto);
        int inicio = texto.indexOf("Regístrate");
        int fin = inicio + "Regístrate".length();

        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(Welcome.this, Signup.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(getColor(android.R.color.white));
            }
        };

        spannable.setSpan(clickSpan, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new UnderlineSpan(), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        registrarse.setText(spannable);
        registrarse.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void redirigirAMain() {
        startActivity(new Intent(this, Main_bn.class));
        finish();
    }

}
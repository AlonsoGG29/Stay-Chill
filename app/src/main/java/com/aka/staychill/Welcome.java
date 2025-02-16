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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    private Button btnEntrar, btnIniciarSesion;
    private TextView registrarse;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            redirigirAMain();
            return;
        }

        inicializarVistas();
        configurarListeners();
        estilizarTextoRegistro();
    }

    private void inicializarVistas() {
        btnEntrar = findViewById(R.id.entrar_welcome);
        btnIniciarSesion = findViewById(R.id.sesion_welcome);
        registrarse = findViewById(R.id.registrarse_welcome);
    }

    private void configurarListeners() {
        btnEntrar.setOnClickListener(v -> redirigirAMain());
        btnIniciarSesion.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
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
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

public class Login extends AppCompatActivity {

    Button btnEntrar;
    TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnEntrar = findViewById(R.id.btn_login);
        register = findViewById(R.id.register_text);

        // Función del botón de Entrar
        btnEntrar.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Main_bn.class);
            startActivity(intent);
        });

        // Estilizar y hacer clicable "Regístrate"

        String text = "¿No tienes cuenta? Regístrate";

        // Encuentra la parte "Regístrate"
        int start = text.indexOf("Regístrate");
        int end = start + "Regístrate".length();

        SpannableString spannableString = new SpannableString(text);

        // Aplicar cursiva, negrita y subrayado
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Hacer que "Regístrate" sea clicable
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navegar a la actividad Signup
                Intent intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // Subrayar
                ds.setColor(Color.WHITE);  // Color blanco (opcional)
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)); // Negrita y cursiva
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplica el texto formateado al TextView
        register.setText(spannableString);

        // Necesario para habilitar clics en el texto
        register.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

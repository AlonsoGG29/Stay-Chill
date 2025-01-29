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

public class Signup extends AppCompatActivity {

    Button btn_entrarSignUp;

    TextView inicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        btn_entrarSignUp = findViewById(R.id.btn_signup);
        inicio = findViewById(R.id.inicio_text);

        // Funcion de boton "Entrar"
        btn_entrarSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(Signup.this,  Main_bn.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Estilizar y hacer clicable "Inicia sesión"
        String text = "¿Ya tienes cuenta? Inicia sesión";

        // Encuentra la parte "Inicia sesión"
        int start = text.indexOf("Inicia sesión");
        int end = start + "Inicia sesión".length();

        SpannableString spannableString = new SpannableString(text);

        // Aplicar cursiva, negrita y subrayado
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Hacer que "Inicia sesión" sea clicable
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
                ds.setUnderlineText(true); // Subrayar
                ds.setColor(Color.WHITE);  // Color blanco (opcional)
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)); // Negrita y cursiva
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplica el texto formateado al TextView
        inicio.setText(spannableString);

        // Necesario para habilitar clics en el texto
        inicio.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

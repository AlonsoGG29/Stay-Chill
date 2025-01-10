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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class welcome extends AppCompatActivity {

    Button btn_enter, btn_login;
    TextView registrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        btn_enter = findViewById(R.id.entrar_welcome);
        btn_login = findViewById(R.id.sesion_welcome);
        registrarse = findViewById(R.id.registrarse_welcome);

        //Funcion de boton de entrar
        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(welcome.this, main.class);
                startActivity(intent);
            }
        });

        //Funcion de boton de iniciar sesion
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(welcome.this, login.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Estilizar y hacer clicable "Regístrate"
        TextView textView = findViewById(R.id.registrarse_welcome);
        String text = getString(R.string.no_tiene_cuenta); // Obtén el texto del recurso

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
            public void onClick(View widget) {
                // Navegar a la actividad signup
                Intent intent = new Intent(welcome.this, signup.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // Subrayar
                ds.setColor(Color.WHITE);  // Color blanco (opcional)
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)); // Negrita y cursiva
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplica el texto formateado al TextView
        textView.setText(spannableString);

        // Necesario para habilitar clics en el texto
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

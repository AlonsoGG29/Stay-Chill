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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private Button btnEntrar;
    private TextView register;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Inicializar Firebase
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        btnEntrar = findViewById(R.id.btn_login);
        register = findViewById(R.id.register_text);

        // Configurar botón de Entrar
        configurarBotonEntrar();

        // Estilizar y hacer clicable "Regístrate"
        estilizarYHacerClicable();
    }

    private void configurarBotonEntrar() {
        btnEntrar.setOnClickListener(view -> {
            String email = ((TextView) findViewById(R.id.email)).getText().toString();
            String contrasenia = ((TextView) findViewById(R.id.password)).getText().toString();

            if (email.isEmpty() || contrasenia.isEmpty()) {
                Toast.makeText(Login.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                iniciarSesion(email, contrasenia);
            }
        });
    }

    private void estilizarYHacerClicable() {
        String texto = "¿No tienes cuenta? Regístrate";
        SpannableString spannableString = new SpannableString(texto);

        // Hacer que "Regístrate" sea clicable y en negrita
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
                ds.setFakeBoldText(true); // Aplicar negrita al texto
                ds.setColor(ContextCompat.getColor(Login.this, android.R.color.white)); // Cambiar el color a blanco
            }
        }, texto.indexOf("Regístrate"), texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplicar negrita a "Regístrate"
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), texto.indexOf("Regístrate"), texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        register.setText(spannableString);
        register.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void iniciarSesion(String email, String contrasenia) {
        auth.signInWithEmailAndPassword(email, contrasenia)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Si se loguea con éxito
                        FirebaseUser usuario = auth.getCurrentUser();
                        if (usuario != null) {
                            Toast.makeText(this, "Iniciada la sesión correctamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, Main_bn.class);
                            startActivity(intent);
                            finish(); // Para cerrar la actividad Login
                        }
                    } else {
                        // Si falla
                        Toast.makeText(this, "Email o contraseña errónea", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

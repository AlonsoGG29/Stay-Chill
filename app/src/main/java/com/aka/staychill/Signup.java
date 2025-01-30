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

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Signup extends AppCompatActivity {

    private Button btn_entrarSignUp;
    private TextView inicio;
    private EditText emailField, passwordField, passwordRepetirField;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Inicializar Firebase
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        btn_entrarSignUp = findViewById(R.id.btn_signup);
        inicio = findViewById(R.id.inicio_text);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        passwordRepetirField = findViewById(R.id.password_repetir);

        // Función del botón "Entrar"
        btn_entrarSignUp.setOnClickListener(view -> {
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
                createAccount(email, password);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Estilizar y hacer clicable "Inicia sesión"
        String text = "¿Ya tienes cuenta? Inicia sesión";
        SpannableString spannableString = new SpannableString(text);

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
                ds.setColor(getResources().getColor(android.R.color.white)); // Cambiar el color a blanco
            }
        }, text.indexOf("Inicia sesión"), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplicar negrita a "Inicia sesión"
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("Inicia sesión"), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        inicio.setText(spannableString);
        inicio.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void createAccount(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso, navegar a la actividad principal
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(Signup.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Signup.this, Main_bn.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Si el registro falla, mostrar un mensaje al usuario
                        Toast.makeText(Signup.this, "Fallo en el registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

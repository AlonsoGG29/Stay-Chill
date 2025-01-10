package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class signup extends AppCompatActivity {

    Button btn_entrarSignUp;
    ImageView back_sign;
    TextView inicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        btn_entrarSignUp = findViewById(R.id.btn_signup);
        back_sign = findViewById(R.id.volver_sign);
        inicio = findViewById(R.id.inicio_text);

        // Funcion de boton "Entrar"
        btn_entrarSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signup.this, main.class);
                startActivity(intent);
            }
        });

        //Funcion de texto si tienes cuenta
        inicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signup.this, login.class);
                startActivity(intent);
            }
        });

        // Funcion de flecha
        back_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finaliza esta actividad para volver a la anterior
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
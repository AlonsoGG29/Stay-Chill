package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inicializar la animación Lottie
        inicializarAnimacionLottie();

        // Iniciar actividad después de la duración de la animación
        iniciarActividadPrincipal();
    }

    private void inicializarAnimacionLottie() {
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        lottieAnimationView.playAnimation();  // Arranca la animación
    }

    private void iniciarActividadPrincipal() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, Welcome.class));
            finish(); // Cierra la actividad Splash
        }, 4000); // Cambio de actividad después de 4 segundos
    }
}

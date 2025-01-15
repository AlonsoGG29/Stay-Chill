package com.aka.staychill;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inicializar la animación Lottie
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);

        // Aquí, puedes agregar un Listener o simplemente hacer que la animación se ejecute
        lottieAnimationView.playAnimation();  // Esto arranca la animación

        // Usamos un Handler para iniciar la actividad después de la duración de la animación
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Después de la animación (3 segundos o lo que definas), pasa a la actividad principal
                startActivity(new Intent(splash.this, welcome.class));
                finish(); // Para cerrar la actividad splash
            }
        }, 4000); // Esto indica que el cambio de actividad ocurrirá después de 3 segundos
    }
}

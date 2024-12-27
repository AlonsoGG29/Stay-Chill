package com.aka.staychill;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
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

        // Iniciar la actividad principal después de la animación
        lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Después de la animación, pasa a la actividad principal
                startActivity(new Intent(splash.this, welcome.class));
                finish(); // Para cerrar la actividad splash
            }
        });
    }
}

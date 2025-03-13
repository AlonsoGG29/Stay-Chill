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

        inicializarAnimacionLottie();

        iniciarActividadPrincipal();
    }

    private void inicializarAnimacionLottie() {
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        lottieAnimationView.playAnimation();
    }

    private void iniciarActividadPrincipal() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, Welcome.class));
            finish();
        }, 4000);
    }
}

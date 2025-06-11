package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Splash extends AppCompatActivity {

    private static final String SUPABASE_AUTH_URL = SupabaseConfig.getSupabaseUrl() + "/auth/v1/user";
    private static final long SPLASH_DURATION_MS = 4_000;

    private SessionManager sessionManager;
    private OkHttpClient client;

    private boolean animationDone = false;
    private boolean validationDone = false;
    private boolean tokenValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        client = SupabaseConfig.getClient();

        playLottieAnimation();
        startValidation();
        scheduleSplashEnd();
    }

    private void playLottieAnimation() {
        LottieAnimationView lottie = findViewById(R.id.lottieAnimationView);
        lottie.playAnimation();
    }

    private void scheduleSplashEnd() {
        new Handler().postDelayed(() -> {
            animationDone = true;
            maybeProceed();
        }, SPLASH_DURATION_MS);
    }

    //Comprueba si el usuario ha iniciado sesión anteriormente
    private void startValidation() {
        if (!sessionManager.isLoggedIn()) {
            validationDone = true;
            tokenValid = false;
            return;
        }

        Request req = new Request.Builder()
                .url(SUPABASE_AUTH_URL)
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Splash", "Token validation failed", e);
                validationDone = true;
                tokenValid = false;
                runOnUiThread(() -> maybeProceed());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                validationDone = true;
                tokenValid = response.isSuccessful();
                // si el token no era válido, limpia sesión
                if (!tokenValid) sessionManager.logout();
                runOnUiThread(() -> maybeProceed());
            }
        });
    }

    //Espera a que termine de verificar la sesión y que haya terminado la animación

    private void maybeProceed() {
        if (!animationDone || !validationDone) return;

        if (tokenValid) {
            startActivity(new Intent(this, Main_bn.class));
        } else {
            startActivity(new Intent(this, Welcome.class));
        }
        finish();
    }
}

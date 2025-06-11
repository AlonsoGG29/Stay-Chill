package com.aka.staychill;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.messaging.FirebaseMessaging;
import android.util.Log;



public class Conf_notificaciones extends AppCompatActivity {
    private static final String TAG = "Conf_notificaciones";
    private SwitchMaterial switchTodas;
    private SwitchMaterial switchMensajes;
    private SwitchMaterial switchEventos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_notificaciones);

        switchTodas    = findViewById(R.id.switchTodas);
        switchMensajes = findViewById(R.id.switchMensajes);
        switchEventos  = findViewById(R.id.switchEventos);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean globalEnabled = prefs.getBoolean("notif_global", true);
        boolean notifMensajes = prefs.getBoolean("notif_mensajes", true);
        boolean notifEventos  = prefs.getBoolean("notif_eventos", true);

        // Estados iniciales
        switchTodas.setChecked(globalEnabled);
        switchMensajes.setChecked(globalEnabled && notifMensajes);
        switchEventos.setChecked(globalEnabled && notifEventos);
        switchMensajes.setEnabled(globalEnabled);
        switchEventos.setEnabled(globalEnabled);

        // **Si estaba activado, nos suscribimos YA al arrancar**
        if (globalEnabled && notifMensajes) {
            FirebaseMessaging.getInstance()
                    .subscribeToTopic("mensajes")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful())
                            Log.e(TAG, "subscribeToTopic mensajes failed", task.getException());
                    });
        }
        if (globalEnabled && notifEventos) {
            FirebaseMessaging.getInstance()
                    .subscribeToTopic("eventos")
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful())
                            Log.e(TAG, "subscribeToTopic eventos failed", task.getException());
                    });
        }

        // Listener global
        switchTodas.setOnCheckedChangeListener((btn, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notif_global", isChecked);
            if (!isChecked) {
                // Desactivar hijos y topics
                editor.putBoolean("notif_mensajes", false);
                editor.putBoolean("notif_eventos",  false);
                FirebaseMessaging.getInstance().unsubscribeFromTopic("mensajes");
                FirebaseMessaging.getInstance().unsubscribeFromTopic("eventos");
            }
            editor.apply();

            switchMensajes.setEnabled(isChecked);
            switchEventos.setEnabled(isChecked);
            if (!isChecked) {
                switchMensajes.setChecked(false);
                switchEventos.setChecked(false);
            }
        });

        // Listener mensajes
        switchMensajes.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!switchTodas.isChecked()) return;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notif_mensajes", isChecked);
            editor.apply();

            if (isChecked) {
                FirebaseMessaging.getInstance()
                        .subscribeToTopic("mensajes")
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful())
                                Log.e(TAG, "subscribeToTopic mensajes failed", task.getException());
                        });
            } else {
                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic("mensajes")
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful())
                                Log.e(TAG, "unsubscribeFromTopic mensajes failed", task.getException());
                        });
            }
        });

        // Listener eventos
        switchEventos.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!switchTodas.isChecked()) return;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notif_eventos", isChecked);
            editor.apply();

            if (isChecked) {
                FirebaseMessaging.getInstance()
                        .subscribeToTopic("eventos")
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful())
                                Log.e(TAG, "subscribeToTopic eventos failed", task.getException());
                        });
            } else {
                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic("eventos")
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful())
                                Log.e(TAG, "unsubscribeFromTopic eventos failed", task.getException());
                        });
            }
        });
    }
}
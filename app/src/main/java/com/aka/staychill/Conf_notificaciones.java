package com.aka.staychill;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.messaging.FirebaseMessaging;

public class Conf_notificaciones extends AppCompatActivity {

    private SwitchMaterial switchTodas;
    private SwitchMaterial switchMensajes;
    private SwitchMaterial switchEventos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_notificaciones);

        // Suponiendo que has definido en el layout:
        // - Un switch global para todas las notificaciones con id "switchTodas"
        // - Un switch para notificaciones de mensajes con id "switchMensajes"
        // - Un switch para notificaciones de eventos con id "switchEventos"
        switchTodas = findViewById(R.id.switchTodas);
        switchMensajes = findViewById(R.id.switchMensajes);
        switchEventos = findViewById(R.id.switchEventos);

        // Recupérate las preferencias guardadas; si no existen, se usan valores por defecto (todas encendidas)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean globalEnabled = prefs.getBoolean("notif_global", true);
        boolean notifMensajes = prefs.getBoolean("notif_mensajes", true);
        boolean notifEventos = prefs.getBoolean("notif_eventos", true);

        // Configuramos los estados iniciales de los switches
        switchTodas.setChecked(globalEnabled);
        // Si el global está desactivado, forzamos que los hijos estén en false
        switchMensajes.setChecked(globalEnabled ? notifMensajes : false);
        switchEventos.setChecked(globalEnabled ? notifEventos : false);

        // Además, solo deben ser editables si la opción global está activa
        switchMensajes.setEnabled(globalEnabled);
        switchEventos.setEnabled(globalEnabled);

        // Listener para el switch global "todas"
        switchTodas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notif_global", isChecked);
            // Si se desactiva el switch global, forzamos que los secundarios se desactiven
            if (!isChecked) {
                editor.putBoolean("notif_mensajes", false);
                editor.putBoolean("notif_eventos", false);
            }
            editor.apply();

            // Habilitamos o deshabilitamos los switches secundarios
            switchMensajes.setEnabled(isChecked);
            switchEventos.setEnabled(isChecked);

            // Si se desactiva globalmente, marcamos visualmente los switches secundarios como apagados
            if (!isChecked) {
                switchMensajes.setChecked(false);
                switchEventos.setChecked(false);
            }
        });

        // Listener para el switch de notificaciones de mensajes
        switchMensajes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Solo se actualiza la preferencia si global está activo
            if (switchTodas.isChecked()) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("notif_mensajes", isChecked);
                editor.apply();
            }
        });

        // Listener para el switch de notificaciones de eventos
        switchEventos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchTodas.isChecked()) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("notif_eventos", isChecked);
                editor.apply();
            }
        });
    }
}

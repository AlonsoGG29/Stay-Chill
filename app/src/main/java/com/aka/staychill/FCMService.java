package com.aka.staychill;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.preference.PreferenceManager;

import com.aka.staychill.types.Notificacion;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private final Set<String> seenIds = new HashSet<>();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token recibido: " + token);
        new SessionManager(this).guardarFCMToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Mensaje recibido desde: " + remoteMessage.getFrom());
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "RemoteMessage data: " + data);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean global = prefs.getBoolean("notif_global", true);
        boolean mensajes = prefs.getBoolean("notif_mensajes", true);
        boolean eventos  = prefs.getBoolean("notif_eventos", true);

        if (!global) {
            Log.d(TAG, "Notificaciones globales desactivadas, ignorando.");
            return;
        }
        String tipo = data.get("tipo");
        if ("mensaje".equals(tipo) && !mensajes) {
            Log.d(TAG, "Notificaciones de mensajes desactivadas, ignorando.");
            return;
        }
        if ("evento".equals(tipo) && !eventos) {
            Log.d(TAG, "Notificaciones de eventos desactivadas, ignorando.");
            return;
        }

        String id = data.get("notification_id");
        if (id == null) {
            id = remoteMessage.getMessageId();
        }
        if (id != null && !seenIds.add(id)) {
            Log.d(TAG, "Duplicado detectado, ignorando ID=" + id);
            return;
        }

        if (data.containsKey("notificacion")) {
            try {
                Notificacion noti = new Gson().fromJson(data.get("notificacion"), Notificacion.class);
                mostrarNotificacion(noti);
            } catch (Exception e) {
                Log.e(TAG, "Error al deserializar 'notificacion': " + e.getMessage());
            }
        } else if (data.containsKey("mensaje") && data.containsKey("tipo")) {
            Notificacion noti = new Notificacion();
            noti.setMensaje(data.get("mensaje"));
            noti.setTipo(data.get("tipo"));
            Notificacion.Usuario usuario = new Notificacion.Usuario();
            usuario.setNombre("Notificación de " + data.get("tipo"));
            noti.setUsuarioEmisor(usuario);
            mostrarNotificacion(noti);
        } else {
            Log.d(TAG, "Payload sin info válida, no se muestra notificación.");
        }
    }

    private void mostrarNotificacion(Notificacion notificacion) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        crearCanalNotificaciones(manager);

        Intent intent = new Intent(this, Notificaciones.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = notificacion.getUsuarioEmisor() != null && notificacion.getUsuarioEmisor().getNombre() != null
                ? notificacion.getUsuarioEmisor().getNombre()
                : "Notificación";
        String content = notificacion.getMensaje() != null ? notificacion.getMensaje() : "";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "staychill_channel")
                .setSmallIcon(R.drawable.img_stay_chill)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
            Log.d(TAG, "Notificación enviada. ID interna: " + notificationId);
        } else {
            Log.e(TAG, "NotificationManager es null.");
        }
    }

    private void crearCanalNotificaciones(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "staychill_channel",
                    "Mensajes StayChill",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones de mensajes y eventos");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

package com.aka.staychill;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.aka.staychill.types.Notificacion;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token recibido: " + token);
        // Actualiza el token en el backend (por ejemplo, en Supabase) usando tu SessionManager.
        new SessionManager(this).guardarFCMToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Mensaje recibido desde: " + remoteMessage.getFrom());
        Log.d(TAG, "RemoteMessage data: " + remoteMessage.getData().toString());

        Map<String, String> data = remoteMessage.getData();

        // Opción 1: Se recibió la clave "notificacion" (payload agrupado)
        if (data.containsKey("notificacion")) {
            String jsonNotificacion = data.get("notificacion");
            Log.d(TAG, "Payload 'notificacion': " + jsonNotificacion);
            try {
                Notificacion notificacion = new Gson().fromJson(jsonNotificacion, Notificacion.class);
                if (notificacion != null) {
                    String emisor = (notificacion.getUsuarioEmisor() != null)
                            ? notificacion.getUsuarioEmisor().getNombre()
                            : "Usuario";
                    Log.d(TAG, "Deserialización OK. Usuario Emisor: "
                            + emisor + " | Mensaje: " + notificacion.getMensaje());
                    mostrarNotificacion(notificacion);
                } else {
                    Log.e(TAG, "Error: Notificacion es null tras la deserialización.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al deserializar la notificación: " + e.getMessage());
            }
        }
        // Opción 2: No existe "notificacion", se utilizan "mensaje" y "tipo"
        else if (data.containsKey("mensaje") && data.containsKey("tipo")) {
            Log.d(TAG, "Construyendo Notificacion a partir de 'mensaje' y 'tipo'");
            Notificacion notificacion = new Notificacion();
            notificacion.setMensaje(data.get("mensaje"));
            notificacion.setTipo(data.get("tipo"));
            // Creamos un objeto Usuario para asignar el tipo al título
            Notificacion.Usuario usuario = new Notificacion.Usuario();
            usuario.setNombre("Notificación de " + data.get("tipo"));
            notificacion.setUsuarioEmisor(usuario);
            mostrarNotificacion(notificacion);
        } else {
            Log.d(TAG, "El payload no tiene la información necesaria para construir una notificación.");
        }
    }

    private void mostrarNotificacion(Notificacion notificacion) {
        Log.d(TAG, "Mostrando notificación...");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        crearCanalNotificaciones(manager);

        // Se define un PendingIntent que abre la actividad Notificaciones
        Intent intent = new Intent(this, Notificaciones.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Definimos el título usando el nombre del usuario (en este ejemplo, incluye el tipo)
        String title = (notificacion.getUsuarioEmisor() != null && notificacion.getUsuarioEmisor().getNombre() != null)
                ? notificacion.getUsuarioEmisor().getNombre()
                : "Notificación";
        String content = (notificacion.getMensaje() != null) ? notificacion.getMensaje() : "";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "staychill_channel")
                .setSmallIcon(R.drawable.img_stay_chill)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            Log.d(TAG, "Notificación ID: " + notificationId);
            manager.notify(notificationId, builder.build());
            Log.d(TAG, "Notificación enviada.");
        } else {
            Log.e(TAG, "Error: NotificationManager es null.");
        }
    }

    private void crearCanalNotificaciones(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creando canal de notificaciones...");
            NotificationChannel channel = new NotificationChannel("staychill_channel", "Mensajes", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones de mensajes nuevos");
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notificaciones creado.");
            } else {
                Log.e(TAG, "NotificationManager es null al crear el canal.");
            }
        }
    }
}

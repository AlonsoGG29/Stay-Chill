package com.aka.staychill;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aka.staychill.adapters.NotificacionesAdapter;
import com.aka.staychill.types.Notificacion;
import com.google.gson.Gson;
import android.Manifest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Notificaciones extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WebSocket webSocket;
    private TextView sinNotificaciones;
    private NotificacionesAdapter adapter;
    private final OkHttpClient client = new OkHttpClient();
    private final List<Notificacion> notificaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        recyclerView = findViewById(R.id.recyclerNotificaciones);
        sinNotificaciones = findViewById(R.id.sinNotificaciones);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }

        configurarRecyclerView();
        cargarNotificaciones();
        iniciarWebSocket();
    }

    private void configurarRecyclerView() {
        adapter = new NotificacionesAdapter(this, notificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void cargarNotificaciones(){
        String userId = new SessionManager(this).getUserIdString();

        HttpUrl url = HttpUrl.parse(SupabaseConfig.getSupabaseUrl() + "/rest/v1/notificaciones")
                .newBuilder()
                .addQueryParameter("select", "*,usuarios!sender_id(nombre,profile_image_url)")
                .addQueryParameter("user_id", "eq." + userId)
                .addQueryParameter("order", "fecha_creacion.desc")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(new Headers.Builder()
                        .add("apikey", SupabaseConfig.getSupabaseKey())
                        .add("Authorization", "Bearer " + new SessionManager(this).getAccessToken())
                        .build())
                .build();

        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(Notificaciones.this,
                        "Error cargando notificaciones", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    List<Notificacion> notifs = parsearNotificaciones(response.body().string());
                    runOnUiThread(() -> actualizarUI(notifs));
                }
            }
        });
    }

    private List<Notificacion> parsearNotificaciones(String json) {
        return Arrays.asList(new Gson().fromJson(json, Notificacion[].class));
    }

    private void actualizarUI(List<Notificacion> notificaciones) {
        if (notificaciones.isEmpty()) {
            sinNotificaciones.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            sinNotificaciones.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.actualizarNotificaciones(notificaciones);
        }
    }

    // En tu actividad Notificaciones.java
    private void iniciarWebSocket() {
        Request request = new Request.Builder()
                .url("wss://" + SupabaseConfig.getSupabaseUrl() + "/realtime/v1/websocket")
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {

                String authMsg = String.format(
                        "{\"event\":\"phx_join\",\"payload\":{\"access_token\":\"%s\"},\"ref\":0}",
                        new SessionManager(Notificaciones.this).getAccessToken()
                );
                webSocket.send(authMsg);

                String subscribeMsg = "{\"event\":\"phx_join\",\"payload\":{\"config\":{\"postgres_changes\":[{\"event\":\"INSERT\",\"schema\":\"public\",\"table\":\"notificaciones\",\"filter\":\"user_id=eq." + new SessionManager(Notificaciones.this).getUserIdString() + "\"}]}},\"ref\":1,\"topic\":\"realtime:public:notificaciones\"}";
                webSocket.send(subscribeMsg);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Log.d("WebSocket", "Mensaje recibido: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    if (json.getString("event").equals("INSERT")) {
                        JSONObject record = json.getJSONObject("payload")
                                .getJSONObject("data")
                                .getJSONObject("record");

                        Notificacion nueva = new Gson().fromJson(record.toString(), Notificacion.class);
                        Log.d("Notificacion", "Nueva notificación: " + nueva.getMensaje());

                        runOnUiThread(() -> {
                            // 1. Actualizar UI
                            notificaciones.add(0, nueva);
                            adapter.notifyItemInserted(0);
                            recyclerView.smoothScrollToPosition(0);

                            // 2. Mostrar notificación solo si la app está en segundo plano
                            if (!isAppEnPrimerPlano()) {
                                mostrarNotificacionLocal(nueva);
                                Log.d("Notificacion", "Notificación mostrada");
                            } else {
                                Log.d("Notificacion", "App en primer plano - No mostrar");
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("WebSocket", "Error parsing JSON", e);
                }
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d("WebSocket", "Conexión cerrada: " + reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                Log.e("WebSocket", "Error de conexión", t);
            }
        });
    }

    // Método mejorado para detectar estado de la app
    private boolean isAppEnPrimerPlano() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (
                appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                        appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
        );
    }

    private void mostrarNotificacionLocal(Notificacion notificacion) {
        // Leer preferencias del usuario
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean habilitarMensajes = prefs.getBoolean("notif_mensajes", true);
        boolean habilitarEventos = prefs.getBoolean("notif_eventos", true);

        // Revisar el tipo y, si está deshabilitado, no mostrar la notificación
        String tipo = notificacion.getTipo(); // Se espera "mensaje" o "evento"
        if ("mensaje".equals(tipo) && !habilitarMensajes) {
            Log.d("Notificacion", "Notificación de mensaje deshabilitada por preferencia");
            return;
        }
        if ("evento".equals(tipo) && !habilitarEventos) {
            Log.d("Notificacion", "Notificación de evento deshabilitada por preferencia");
            return;
        }

        // Asignar canal y contenido según el tipo
        String channelId = "default_channel";
        String channelName = "Notificaciones";
        String contentTitle = "Notificación";
        String contentText = notificacion.getMensaje();

        if ("mensaje".equals(tipo)) {
            channelId = "mensajes_channel";
            channelName = "Mensajes";
            // Si el emisor está disponible, se muestra su nombre
            contentTitle = notificacion.getUsuarioEmisor() != null ?
                    notificacion.getUsuarioEmisor().getNombre() : "Nuevo mensaje";
        } else if ("evento".equals(tipo)) {
            channelId = "eventos_channel";
            channelName = "Eventos";
            contentTitle = "Nuevo evento";
            // Puedes modificar el cuerpo, por ejemplo:
            contentText = "Evento: " + notificacion.getMensaje();
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        // Crear canal en Android 8.0+ según el tipo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            // Descripción personalizada según el tipo
            String description = "Notificaciones " + ("mensaje".equals(tipo) ? "de mensajes nuevos" : "de eventos");
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.img_stay_chill)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Mostrar notificación (considerando permisos para Android 13+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cerrar WebSocket si es necesario
    }
}
package com.aka.staychill;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aka.staychill.adapters.NotificacionesAdapter;
import com.aka.staychill.types.Notificacion;
import com.google.gson.Gson;

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
    private static final String TAG       = "Notificaciones";
    private static final int    REQ_NOTIF = 1001;

    private RecyclerView       recyclerView;
    private TextView           sinNotificaciones;
    private NotificacionesAdapter adapter;
    private OkHttpClient       client;
    private List<Notificacion> notificaciones;
    private WebSocket          webSocket;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        // Init HTTP client & data list
        client         = new OkHttpClient();
        notificaciones = new ArrayList<>();

        recyclerView      = findViewById(R.id.recyclerNotificaciones);
        sinNotificaciones = findViewById(R.id.sinNotificaciones);
        btnBack = findViewById(R.id.btnBack);

        // Solicitar permiso en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                    REQ_NOTIF
            );
        }

        btnBack.setOnClickListener(v -> volverMenu());

        configurarRecyclerView();
        cargarNotificaciones();
        iniciarWebSocket();
    }

    private void configurarRecyclerView() {
        adapter = new NotificacionesAdapter(this, notificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void cargarNotificaciones() {
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
                        .build()
                )
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(Notificaciones.this,
                                "Error cargando notificaciones",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }

            @Override public void onResponse(@NonNull Call call,
                                             @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                List<Notificacion> list = parsearNotificaciones(response.body().string());
                runOnUiThread(() -> actualizarUI(list));
            }
        });
    }

    private List<Notificacion> parsearNotificaciones(String json) {
        Notificacion[] arr = new Gson()
                .fromJson(json, Notificacion[].class);
        return Arrays.asList(arr);
    }

    private void actualizarUI(List<Notificacion> list) {
        if (list.isEmpty()) {
            sinNotificaciones.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            sinNotificaciones.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.actualizarNotificaciones(list);
        }
    }

    private void iniciarWebSocket() {
        String url = "wss://" +
                SupabaseConfig.getSupabaseUrl() +
                "/realtime/v1/websocket?apikey=" +
                SupabaseConfig.getSupabaseKey();

        Request req = new Request.Builder().url(url).build();

        webSocket = client.newWebSocket(req, new WebSocketListener() {
            @Override public void onOpen(@NonNull WebSocket ws,
                                         @NonNull Response resp) {
                // 1) Auth con access_token
                String auth = String.format(
                        "{\"event\":\"access_token\",\"payload\":\"%s\",\"ref\":0}",
                        new SessionManager(Notificaciones.this).getAccessToken()
                );
                ws.send(auth);

                // 2) Subscribe INSERT notificaciones
                String sub = String.format(
                        "{\"event\":\"phx_join\",\"payload\":{\"config\":{\"postgres_changes\":[{\"event\":\"INSERT\",\"schema\":\"public\",\"table\":\"notificaciones\",\"filter\":\"user_id=eq.%s\"}]}},\"ref\":1,\"topic\":\"realtime:public:notificaciones\"}",
                        new SessionManager(Notificaciones.this).getUserIdString()
                );
                ws.send(sub);
            }

            @Override public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                try {
                    JSONObject root = new JSONObject(text);
                    if (root.has("payload")) {
                        JSONObject rec = root.getJSONObject("payload")
                                .getJSONObject("data")
                                .getJSONObject("record");

                        if (rec.has("push_enviado") && rec.getBoolean("push_enviado")) {
                            Log.d("WebSocket", "Notificación ya procesada por FCM, ignorando.");
                            return; // ❌ Ignorar registros que ya generaron un push
                        }

                        Notificacion nueva = new Gson().fromJson(rec.toString(), Notificacion.class);
                        runOnUiThread(() -> {
                            notificaciones.add(0, nueva);
                            adapter.notifyItemInserted(0);
                            recyclerView.smoothScrollToPosition(0);
                        });
                    }
                } catch (Exception e) {
                    Log.e("WebSocket", "Error procesando JSON", e);
                }
            }



            @Override public void onFailure(@NonNull WebSocket ws,
                                            @NonNull Throwable t,
                                            @Nullable Response r) {
                Log.e(TAG, "WS error", t);
            }

            @Override public void onClosed(@NonNull WebSocket ws,
                                           int code,
                                           @NonNull String reason) {
                Log.d(TAG, "WS cerrado: " + reason);
            }
        });
    }
    private void volverMenu() {
        runOnUiThread(() -> {
            Intent intent = new Intent(this, Main_bn.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            intent.putExtra("start_tab", 1);
            startActivity(intent);
            finish();
        });
    }
}
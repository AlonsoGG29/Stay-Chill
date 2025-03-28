package com.aka.staychill;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Chat extends AppCompatActivity {
    // === CONSTANTES ===
    private static final long POLLING_INTERVAL = 5000;
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");
    private static final String WEBSOCKET_AUTH_MSG = "{\"event\":\"access_token\",\"payload\":\"%s\",\"ref\":0}";
    private static final String WEBSOCKET_SUB_MSG = "{\"event\":\"phx_join\",\"payload\":{\"config\":{\"broadcast\":{\"self\":false},\"postgres_changes\":[{\"event\":\"INSERT\",\"schema\":\"public\",\"table\":\"mensajes\"}]}},\"ref\":1,\"topic\":\"realtime:public:messages\"}";

    // === VIEWS ===
    private RecyclerView recyclerMensajes;
    private EditText etMensaje;
    private ImageButton btnEnviar;
    private ImageView imgPerfil;
    private TextView tvNombre;

    // === COMPONENTES ===
    private MensajesAdapter adapter;
    private SessionManager sessionManager;
    private CargarImagenes cargadorImagenes;
    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;
    private Handler handler = new Handler();

    // === ESTADO ===
    private String conversacionId;
    private String contactoId;
    private Runnable pollingRunnable;

    // ########################################
    // ##          CICLO DE VIDA             ##
    // ########################################

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        inicializarComponentes();
        configurarUI();
        manejarFlujoInicial();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerPolling();
    }

    @Override
    protected void onResume() {
        super.onResume();
        iniciarPolling();
    }

    @Override
    protected void onDestroy() {
        cerrarWebSocket();
        detenerPolling();
        super.onDestroy();
    }

    // ########################################
    // ##       INICIALIZACIÓN BÁSICA       ##
    // ########################################

    private void inicializarComponentes() {
        sessionManager = new SessionManager(this);
        cargadorImagenes = CargarImagenes.getInstance(this);
        conversacionId = getIntent().getStringExtra("conversacion_id");
        contactoId = getIntent().getStringExtra("contacto_id");

        recyclerMensajes = findViewById(R.id.recyclerMensajes);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        imgPerfil = findViewById(R.id.imgPerfilChat);
        tvNombre = findViewById(R.id.tvNombreChat);
    }

    private void configurarUI() {
        adapter = new MensajesAdapter(new ArrayList<>(), sessionManager.getUserIdString());
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));
        recyclerMensajes.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> manejarEnvioMensaje());
    }

    // ########################################
    // ##        LÓGICA PRINCIPAL            ##
    // ########################################

    private void manejarFlujoInicial() {
        if (validarIdsIniciales()) return;

        cargarDatosContacto();

        if (conversacionId == null) {
            verificarConversacionExistente();
        } else {
            cargarMensajes();
        }

        iniciarConexionTiempoReal();
    }

    private boolean validarIdsIniciales() {
        if (conversacionId == null && contactoId == null) {
            mostrarErrorYSalir("IDs inválidos");
            return true;
        }
        return false;
    }

    // ########################################
    // ##      MANEJO DE CONVERSACIONES      ##
    // ########################################

    private void verificarConversacionExistente() {
        List<String> participantes = obtenerParticipantesOrdenados();
        Request request = new Request.Builder()
                .url(construirUrlVerificacionConversacion(participantes))
                .headers(obtenerHeadersAuth())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    procesarRespuestaConversacion(response.body().string());
                }
            }
        });
    }

    private void procesarRespuestaConversacion(String respuesta) {
        try {
            JSONArray jsonArray = new JSONArray(respuesta);
            if (jsonArray.length() > 0) {
                conversacionId = jsonArray.getJSONObject(0).getString("id");
                runOnUiThread(() -> {
                    cargarMensajes();
                    iniciarPolling();
                });
            }
        } catch (JSONException e) {
            mostrarError("Error procesando respuesta");
        }
    }

    // ########################################
    // ##        MANEJO DE MENSAJES          ##
    // ########################################

    private void manejarEnvioMensaje() {
        String contenido = etMensaje.getText().toString().trim();
        if (contenido.isEmpty()) return;

        if (conversacionId == null) {
            crearConversacionYEnviarMensaje(contenido);
        } else {
            enviarMensajeExistente(contenido);
        }

        etMensaje.setText("");
    }

    private void crearConversacionYEnviarMensaje(String contenido) {
        try {
            JSONObject body = new JSONObject();
            List<String> participantes = obtenerParticipantesOrdenados();
            body.put("participante1", participantes.get(0));
            body.put("participante2", participantes.get(1));
            body.put("ultimo_mensaje", contenido);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones")
                    .post(RequestBody.create(body.toString(), JSON_MEDIA_TYPE))
                    .headers(obtenerHeadersAuth())
                    .addHeader("Prefer", "return=representation")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mostrarError("Error creando conversación");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        procesarNuevaConversacion(response.body().string(), contenido);
                    }
                }
            });
        } catch (JSONException e) {
            mostrarError("Error creando mensaje");
        }
    }

    private void procesarNuevaConversacion(String respuesta, String contenido) {
        try {
            JSONArray jsonArray = new JSONArray(respuesta);
            if (jsonArray.length() > 0) {
                conversacionId = jsonArray.getJSONObject(0).getString("id");
                enviarMensajeExistente(contenido);
            }
        } catch (JSONException e) {
            mostrarError("Error procesando respuesta");
        }
    }

    private void enviarMensajeExistente(String contenido) {
        try {
            JSONObject mensajeBody = new JSONObject();
            mensajeBody.put("sender_id", sessionManager.getUserIdString());
            mensajeBody.put("contenido", contenido);
            mensajeBody.put("conversacion_id", conversacionId);

            Request mensajeRequest = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/mensajes")
                    .post(RequestBody.create(mensajeBody.toString(), JSON_MEDIA_TYPE))
                    .headers(obtenerHeadersAuth())
                    .build();

            JSONObject conversacionBody = new JSONObject();
            conversacionBody.put("ultimo_mensaje", contenido);
            conversacionBody.put("fecha", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));

            Request conversacionRequest = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones?id=eq." + conversacionId)
                    .patch(RequestBody.create(conversacionBody.toString(), JSON_MEDIA_TYPE))
                    .headers(obtenerHeadersAuth())
                    .build();

            client.newCall(mensajeRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mostrarError("Error enviando mensaje");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        client.newCall(conversacionRequest).enqueue(new Callback() {
                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                cargarMensajes();
                            }

                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.e("Chat", "Error actualizando conversación", e);
                            }
                        });
                    }
                }
            });

        } catch (JSONException e) {
            mostrarError("Error creando mensaje");
        }
    }

    // ########################################
    // ##        CARGA DE MENSAJES           ##
    // ########################################

    private void cargarMensajes() {
        if (conversacionId == null) return;

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SupabaseConfig.getSupabaseUrl() + "/rest/v1/mensajes")).newBuilder()
                .addQueryParameter("select", "id,contenido,fecha,conversacion_id,sender_id:usuarios(nombre,profile_image_url,foren_uid)")
                .addQueryParameter("conversacion_id", "eq." + conversacionId)
                .addQueryParameter("order", "fecha.asc")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(obtenerHeadersAuth())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    procesarMensajesRecibidos(response.body().string());
                }
            }
        });
    }

    private void procesarMensajesRecibidos(String json) {
        try {
            List<Mensaje> nuevosMensajes = Arrays.asList(new Gson().fromJson(json, Mensaje[].class));
            runOnUiThread(() -> {
                adapter.setMensajes(nuevosMensajes);
                recyclerMensajes.smoothScrollToPosition(adapter.getItemCount() - 1);
            });
        } catch (Exception e) {
            mostrarError("Error procesando mensajes");
        }
    }


    private void cargarDatosContacto() {
        if (contactoId == null) return;

        HttpUrl url = HttpUrl.parse(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios")
                .newBuilder()
                .addQueryParameter("foren_uid", "eq." + contactoId)
                .addQueryParameter("select", "nombre,profile_image_url")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(obtenerHeadersAuth())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Chat", "Error cargando contacto", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    procesarDatosContacto(response.body().string());
                }
            }
        });
    }
    private void procesarDatosContacto(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            if (jsonArray.length() > 0) {
                JSONObject contacto = jsonArray.getJSONObject(0);
                String nombre = contacto.getString("nombre");
                String fotoUrl = contacto.optString("profile_image_url", "");

                runOnUiThread(() -> {
                    tvNombre.setText(nombre);
                    cargadorImagenes.loadProfileImage(fotoUrl, imgPerfil, Chat.this);
                });
            }
        } catch (JSONException e) {
            Log.e("Chat", "Error procesando contacto", e);
        }
    }


    // ########################################
    // ##         TIEMPO REAL (WS)           ##
    // ########################################

    private void iniciarConexionTiempoReal() {
        iniciarWebSocket();
    }

    private void iniciarWebSocket() {
        Request request = new Request.Builder()
                .url("wss://" + SupabaseConfig.getSupabaseUrl() + "/realtime/v1/websocket")
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                autenticarWebSocket(webSocket);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                procesarMensajeWebSocket(text);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                reconectarWebSocket();
            }
        });
    }

    private void autenticarWebSocket(WebSocket ws) {
        ws.send(String.format(WEBSOCKET_AUTH_MSG, SupabaseConfig.getSupabaseKey()));
        ws.send(WEBSOCKET_SUB_MSG);
    }

    private void procesarMensajeWebSocket(String mensaje) {
        try {
            JSONObject obj = new JSONObject(mensaje);
            JSONObject payload = obj.getJSONObject("payload");

            if (payload.has("data")) {
                JSONObject data = payload.getJSONObject("data");
                JSONObject record = data.getJSONObject("record");

                Mensaje nuevo = new Gson().fromJson(record.toString(), Mensaje.class);
                if (nuevo.getConversacionId().equals(conversacionId)) {
                    runOnUiThread(() -> {
                        adapter.getMensajes().add(nuevo);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                        recyclerMensajes.smoothScrollToPosition(adapter.getItemCount() - 1);
                    });
                }
            }
        } catch (JSONException e) {
            Log.e("WebSocket", "Error procesando mensaje", e);
        }
    }

    private void reconectarWebSocket() {
        handler.postDelayed(this::iniciarWebSocket, 5000);
    }

    private void cerrarWebSocket() {
        if (webSocket != null) webSocket.close(1000, "Activity closed");
    }

    // ########################################
    // ##              POLLING               ##
    // ########################################

    private void iniciarPolling() {
        if (pollingRunnable != null) return;

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed()) {
                    cargarMensajes();
                    handler.postDelayed(this, POLLING_INTERVAL);
                }
            }
        };
        handler.postDelayed(pollingRunnable, POLLING_INTERVAL);
    }

    private void detenerPolling() {
        if (pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
            pollingRunnable = null;
        }
    }

    // ########################################
    // ##         MÉTODOS AUXILIARES         ##
    // ########################################

    private List<String> obtenerParticipantesOrdenados() {
        List<String> participantes = Arrays.asList(
                sessionManager.getUserIdString(),
                contactoId
        );
        Collections.sort(participantes);
        return participantes;
    }

    private Headers obtenerHeadersAuth() {
        return new Headers.Builder()
                .add("apikey", SupabaseConfig.getSupabaseKey())
                .add("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();
    }

    private String construirUrlVerificacionConversacion(List<String> participantes) {
        return SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones?" +
                "select=id" +
                "&and=(participante1.eq." + participantes.get(0) +
                ",participante2.eq." + participantes.get(1) + ")";
    }

    private void mostrarErrorYSalir(String mensaje) {
        runOnUiThread(() -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(Chat.this, mensaje, Toast.LENGTH_SHORT).show());
    }
}
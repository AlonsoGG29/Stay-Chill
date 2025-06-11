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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aka.staychill.adapters.MensajesAdapter;
import com.aka.staychill.types.Mensaje;
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
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");
    private static final String WEBSOCKET_AUTH_MSG = "{\"event\":\"access_token\",\"payload\":\"%s\",\"ref\":0}";
    private static final String WEBSOCKET_SUB_MSG =
            "{\"event\":\"phx_join\",\"payload\":{" +
                    "\"config\":{" +
                    "\"broadcast\":{\"self\":true}," +
                    "\"postgres_changes\":[{\"event\":\"INSERT\",\"schema\":\"public\",\"table\":\"mensajes\"}]" +
                    "}" +
                    "},\"ref\":1,\"topic\":\"realtime:public:mensajes\"}";
    private static final long POLL_INTERVAL_MS = 3_000;


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
    private final Handler handler = new Handler();
    private Runnable pollingRunnable;

    // === ESTADO ===
    private String conversacionId;
    private String contactoId;

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
        iniciarPolling();
    }

    @Override
    protected void onDestroy() {
        cerrarWebSocket();
        stopPolling();
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
            Exception error = new Exception("Ambos IDs (conversación y contacto) son nulos");
            mostrarError("Error de validación inicial", error);
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
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión", e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
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
                runOnUiThread(this::cargarMensajes);
            }
        } catch (JSONException e) {
            mostrarError("Error procesando respuesta", e);
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
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mostrarError("Error creando conversación", e);
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        procesarNuevaConversacion(response.body().string(), contenido);
                    }
                }
            });
        } catch (JSONException e) {
            mostrarError("Error creando mensaje", e);
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
            mostrarError("Error procesando respuesta", e);
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

            client.newCall(mensajeRequest).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mostrarError("Error enviando mensaje", e);
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        crearNotificacionMensaje(contenido);
                        actualizarConversacion(contenido);
                        cargarMensajes();
                    }
                }
            });

        } catch (JSONException e) {
            mostrarError("Error creando mensaje", e);
        }
    }

    private void actualizarConversacion(String contenido) {
        try {
            JSONObject conversacionBody = new JSONObject();
            conversacionBody.put("ultimo_mensaje", contenido);
            conversacionBody.put("fecha", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));

            Request request = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones?id=eq." + conversacionId)
                    .patch(RequestBody.create(conversacionBody.toString(), JSON_MEDIA_TYPE))
                    .headers(obtenerHeadersAuth())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) {}
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("Chat", "Error actualizando conversación", e);
                }
            });
        } catch (JSONException e) {
            Log.e("Chat", "Error creando JSON conversación", e);
        }
    }

    // ==========================
    // === MÉTODOS DE POLLING ===
    // ==========================

    private void iniciarPolling() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                cargarMensajes();  // vuelve a hacer fetch de mensajes
                handler.postDelayed(this, POLL_INTERVAL_MS);
            }
        };
        handler.postDelayed(pollingRunnable, POLL_INTERVAL_MS);
    }

    private void stopPolling() {
        if (pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
    }


    // ########################################
    // ##        CARGA DE MENSAJES           ##
    // ########################################

    private void cargarMensajes() {
        HttpUrl url = getBaseUrlBuilder("mensajes")
                .addQueryParameter("select", "id,contenido,fecha,conversacion_id,sender_id:usuarios(nombre,profile_image_url,foren_uid)")
                .addQueryParameter("conversacion_id", "eq." + conversacionId)
                .addQueryParameter("order", "fecha.asc")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(getDefaultHeaders())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión", e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
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
            mostrarError("Error procesando mensajes", e);
        }
    }

    private void cargarDatosContacto() {
        if (contactoId == null) return;

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios"))
                .newBuilder()
                .addQueryParameter("foren_uid", "eq." + contactoId)
                .addQueryParameter("select", "nombre,profile_image_url")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(obtenerHeadersAuth())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Chat", "Error cargando contacto", e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
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
        // Convertimos https://... a wss://... y añadimos apikey en query
        String base = SupabaseConfig.getSupabaseUrl(); // e.g. "https://xyz.supabase.co"
        String wsUrl = base.replaceFirst("^https?://", "wss://")
                + "/realtime/v1/websocket?apikey="
                + SupabaseConfig.getSupabaseKey();

        Request wsRequest = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(wsRequest, new WebSocketListener() {
            @Override public void onOpen(@NonNull WebSocket ws, @NonNull Response resp) {
                Log.d("WS","Conexión abierta (101)");
                autenticarWebSocket(ws);
            }
            @Override public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                Log.d("WS","<<< "+text);
                // Loguear phx_reply para verificar suscripción
                if (text.contains("\"event\":\"phx_reply\"")) {
                    Log.d("WS","→ phx_reply recibido");
                }
                procesarMensajeWebSocket(text);
            }
            @Override public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, @Nullable Response r) {
                Log.e("WS","Fallo WS",t);
                reconectarWebSocket();
            }
            @Override public void onClosed(@NonNull WebSocket ws, int code, @NonNull String reason) {
                Log.d("WS","Cerrado WS: "+code+" / "+reason);
                reconectarWebSocket();
            }
        });
    }



    private void autenticarWebSocket(WebSocket ws) {
        // Aquí enviamos el JWT del usuario, no la anon key
        String jwt = sessionManager.getAccessToken();
        String authMsg = String.format(WEBSOCKET_AUTH_MSG, jwt);
        Log.d("WS","Enviando AUTH con JWT: " + authMsg);
        ws.send(authMsg);

        Log.d("WS","Enviando SUBSCRIBE: " + WEBSOCKET_SUB_MSG);
        ws.send(WEBSOCKET_SUB_MSG);
    }

    private void procesarMensajeWebSocket(String mensaje) {
        try {
            JSONObject obj = new JSONObject(mensaje);
            if (obj.has("payload")) {
                JSONObject payload = obj.getJSONObject("payload");
                if (payload.has("data") && payload.getJSONObject("data").has("record")) {
                    JSONObject record = payload.getJSONObject("data").getJSONObject("record");
                    Mensaje nuevo = new Gson().fromJson(record.toString(), Mensaje.class);
                    if (nuevo.getConversacionId().equals(conversacionId)) {
                        runOnUiThread(() -> {
                            if (!adapter.getMensajes().contains(nuevo)) {
                                adapter.getMensajes().add(nuevo);
                                adapter.notifyItemInserted(adapter.getItemCount() - 1);
                                recyclerMensajes.smoothScrollToPosition(adapter.getItemCount() - 1);
                            }
                        });
                    }
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

    private void mostrarError(String contexto, Exception e) {
        String mensaje = contexto + ": " + e.getMessage();
        Log.e("ChatError", mensaje, e);
        runOnUiThread(() ->
                Toast.makeText(Chat.this, mensaje, Toast.LENGTH_LONG).show()
        );
    }


    private void crearNotificacionMensaje(String contenido) {
        try {
            JSONObject notificacionBody = new JSONObject();
            notificacionBody.put("user_id", contactoId);
            notificacionBody.put("sender_id", sessionManager.getUserIdString());
            notificacionBody.put("mensaje", "Nuevo mensaje");
            notificacionBody.put("tipo", "mensaje");
            notificacionBody.put("relacion_id", conversacionId);

            Request insertReq = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/notificaciones")
                    .post(RequestBody.create(notificacionBody.toString(), JSON_MEDIA_TYPE))
                    .headers(obtenerHeadersServicio())
                    .build();

            client.newCall(insertReq).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("Notificacion","Error creando registro",e);
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("Notificacion","Insert status: "+response.code());
                        return;
                    }

                    Request fnReq = new Request.Builder()
                            .url(SupabaseConfig.getSupabaseUrl() + "/functions/v1/send_push_notification")
                            .post(RequestBody.create(notificacionBody.toString(), JSON_MEDIA_TYPE))
                            .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                            .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                            .build();

                    client.newCall(fnReq).enqueue(new Callback() {
                        @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("Notificacion","Error llamando Edge Function",e);
                        }
                        @Override public void onResponse(@NonNull Call call, @NonNull Response fnResp) throws IOException {
                            if (!fnResp.isSuccessful()) {
                                Log.e("Notificacion","Func status: "+fnResp.code()+" / "+fnResp.body().string());
                            } else {
                                Log.d("Notificacion","Edge Function invocada OK");
                            }
                        }
                    });
                }
            });

        } catch (JSONException e) {
            Log.e("Notificacion","JSON error",e);
        }
    }


    private Headers obtenerHeadersServicio() {
        return new Headers.Builder()
                .add("apikey", SupabaseConfig.getSupabaseKey())
                .add("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();
    }

    private HttpUrl.Builder getBaseUrlBuilder(String tabla) {
        return Objects.requireNonNull(HttpUrl.parse(SupabaseConfig.getSupabaseUrl() + "/rest/v1/" + tabla)).newBuilder();
    }

    private Headers getDefaultHeaders() {
        return new Headers.Builder()
                .add("apikey", SupabaseConfig.getSupabaseKey())
                .add("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();
    }
}

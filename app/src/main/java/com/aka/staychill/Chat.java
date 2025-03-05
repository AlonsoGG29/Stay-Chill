package com.aka.staychill;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Chat extends AppCompatActivity {

    private RecyclerView recyclerMensajes;
    private EditText etMensaje;
    private ImageButton btnEnviar;
    private MensajesAdapter adapter;
    private SessionManager sessionManager;
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();
    private String contactoId;
    // Suponemos que la clase Conversacion ahora incluye un campo "id" y campos para participantes.
    // Por ejemplo:
    // private String id, participant1, participant2, ultimo_mensaje, fecha;
    // Con sus respectivos getters y setters.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);

        sessionManager = new SessionManager(this);

        contactoId = getIntent().getStringExtra("contacto_id");
        if (contactoId == null) {
            Toast.makeText(this, "Error: Contacto no válido", Toast.LENGTH_SHORT).show();
            finish();
        }

        recyclerMensajes = findViewById(R.id.recyclerMensajes);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);

        configurarRecyclerView();
        cargarMensajes();
        configurarEnvio();
    }

    private void configurarRecyclerView() {
        adapter = new MensajesAdapter(new ArrayList<>(), sessionManager.getUserIdString());
        recyclerMensajes.setAdapter(adapter);
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));
    }

    private void cargarMensajes() {
        String usuarioActualId = sessionManager.getUserIdString();
        // Si usas conversación, lo ideal es cargar los mensajes filtrando por el conversation_id de la conversación actual.
        // Este ejemplo asume que ya se obtuvo o se tiene el conversation_id, o que se consulta según los participantes.
        // Por simplicidad, aquí se consulta todos los mensajes entre ambos usuarios.
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/mensajes?" +
                "select=*&and=(or(sender_id.eq." + usuarioActualId + ",receiver_id.eq." + usuarioActualId + ")" +
                ",or(sender_id.eq." + contactoId + ",receiver_id.eq." + contactoId + "))" +
                "&order=fecha.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> mostrarError("Error al cargar mensajes"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Mensaje[] mensajes = gson.fromJson(json, Mensaje[].class);
                    actualizarMensajes(mensajes);
                }
            }
        });
    }

    private void actualizarMensajes(Mensaje[] mensajes) {
        runOnUiThread(() -> {
            if (!isDestroyed() && !isFinishing()) { // <-- Verificación añadida
                if (mensajes != null && mensajes.length > 0) {
                    adapter.setMensajes(Arrays.asList(mensajes));
                    int ultimaPosicion = adapter.getItemCount() - 1;
                    if (ultimaPosicion >= 0) {
                        recyclerMensajes.smoothScrollToPosition(ultimaPosicion);
                    }
                }
            }
        });
    }

    private void configurarEnvio() {
        btnEnviar.setOnClickListener(v -> {
            String contenido = etMensaje.getText().toString().trim();
            if (!contenido.isEmpty()) {
                enviarMensaje(contenido);
                etMensaje.setText("");
            }
        });
    }

    // Método que gestiona el envío del mensaje y la creación/actualización de la conversación
    private void enviarMensaje(String contenido) {
        String userId = sessionManager.getUserIdString();
        // URL corregida
        // URL corregida para buscar conversaciones sin importar el orden
        String convQueryUrl = SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones?" +
                "select=*" +
                "&or=(and(participante1.eq." + userId + ",participante2.eq." + contactoId + ")" +
                ",and(participante1.eq." + contactoId + ",participante2.eq." + userId + "))";

        Request convRequest = new Request.Builder()
                .url(convQueryUrl)
                .get()
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();


        client.newCall(convRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> mostrarError("Error al verificar conversación"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String convJson = response.body().string();
                    Conversacion[] convs = gson.fromJson(convJson, Conversacion[].class);
                    if (convs != null && convs.length > 0) {
                        // Ya existe la conversación: usar su id y actualizar la conversación.
                        String conversationId = convs[0].getId();
                        insertarMensaje(conversationId, contenido);
                        actualizarConversacion(conversationId, contenido);
                    } else {
                        // No existe la conversación: crearla y luego insertar el mensaje.
                        crearConversacion(userId, contactoId, contenido);
                    }
                } else {
                    runOnUiThread(() -> mostrarError("Error al verificar conversación"));
                }
            }
        });
    }

    // Inserta un mensaje en la tabla mensajes asociado a una conversación existente.
    private void insertarMensaje(String conversationId, String contenido) {
        try {
            JSONObject body = new JSONObject();
            body.put("sender_id", sessionManager.getUserIdString());
            body.put("receiver_id", contactoId);
            body.put("contenido", contenido);
            // Puedes agregar "fecha" o dejar que la base de datos asigne el valor por defecto.

            Request request = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/mensajes")
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> mostrarError("Error al enviar mensaje"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Recargar mensajes o actualizar UI según convenga.
                        cargarMensajes();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Crea una nueva conversación y, al completarse, inserta el mensaje
    private void crearConversacion(String userId, String contactoId, String contenido) {
        try {
            // Ordenar IDs alfabéticamente para evitar duplicados
            String part1 = userId.compareTo(contactoId) < 0 ? userId : contactoId;
            String part2 = userId.compareTo(contactoId) < 0 ? contactoId : userId;

            JSONObject body = new JSONObject();
            body.put("participante1", part1);
            body.put("participante2", part2);
            body.put("ultimo_mensaje", contenido);
            // Elimina la línea de fecha para usar el valor por defecto de la base de datos

            Request request = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones")
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .addHeader("Prefer", "return=representation") // Para recibir la conversación creada
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> mostrarError("Error al crear conversación"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String convJson = response.body().string();
                        Conversacion[] conversaciones = gson.fromJson(convJson, Conversacion[].class); // <- Parsea como array
                        if (conversaciones != null && conversaciones.length > 0) {
                            Conversacion conv = conversaciones[0];
                            if (conv != null && conv.getId() != null) {
                                insertarMensaje(conv.getId(), contenido);
                            }
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Actualiza la conversación (por ejemplo, el último mensaje y la fecha) cuando se envía un mensaje
    private void actualizarConversacion(String conversationId, String contenido) {
        try {
            JSONObject body = new JSONObject();
            body.put("ultimo_mensaje", contenido);
            // Si necesitas actualizar la fecha manualmente:
            // String currentTime = Instant.now().toString();
            // body.put("fecha", currentTime);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones?id=eq." + conversationId)
                    .patch(RequestBody.create(body.toString(), MediaType.get("application/json"))) // Usa PATCH directamente
                    .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Puedes mostrar un error o ignorar si la actualización falla
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // No es necesario recargar mensajes aquí, ya que insertarMensaje se encarga de ello.
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(Chat.this, mensaje, Toast.LENGTH_SHORT).show());
    }
}

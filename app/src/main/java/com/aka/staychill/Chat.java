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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat); // Asegúrate de que este layout existe

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Obtener contactoId desde el Intent
        contactoId = getIntent().getStringExtra("contacto_id");
        if (contactoId == null) {
            Toast.makeText(this, "Error: Contacto no válido", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Inicializar vistas
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
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this)); // Usar this en Activity
    }

    private void cargarMensajes() {
        String usuarioActualId = sessionManager.getUserIdString();
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/mensajes?" +
                "select=*" +
                "&and=(or(sender_id.eq." + usuarioActualId + ",receiver_id.eq." + usuarioActualId + ")" +
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
            if (mensajes != null && mensajes.length > 0) {
                adapter.setMensajes(Arrays.asList(mensajes));

                // Verifica que la posición sea válida
                int ultimaPosicion = adapter.getItemCount() - 1;
                if (ultimaPosicion >= 0) {
                    recyclerMensajes.smoothScrollToPosition(ultimaPosicion);
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

    private void enviarMensaje(String contenido) {
        JSONObject body = new JSONObject();
        try {
            body.put("sender_id", sessionManager.getUserIdString());
            body.put("receiver_id", contactoId);
            body.put("contenido", contenido);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    cargarMensajes();
                }
            }
        });
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
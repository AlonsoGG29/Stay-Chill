package com.aka.staychill;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventoClick extends AppCompatActivity {

    private SessionManager sessionManager;
    private Evento evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento_click);

        sessionManager = new SessionManager(this);
        String eventoJson = getIntent().getStringExtra("EVENTO_DATA");
        evento = new Gson().fromJson(eventoJson, Evento.class);

        ImageView imagenEvento = findViewById(R.id.imagenEvento);
        TextView tituloEvento = findViewById(R.id.tituloEvento);
        ImageView perfilFoto = findViewById(R.id.perfilFoto);
        TextView nombreOrganizador = findViewById(R.id.nombreOrganizador);
        TextView ubicacionEvento = findViewById(R.id.ubicacionEvento);
        TextView fechaEvento = findViewById(R.id.fechaEvento);
        TextView horaEvento = findViewById(R.id.horaEvento);
        TextView descripcionEvento = findViewById(R.id.descripcionEvento);


        //Unirse a un evento
        ImageView btnVamos = findViewById(R.id.btn_vamos);
        btnVamos.setOnClickListener(v -> unirseAlEvento());

        // Cargar datos
        tituloEvento.setText(evento.getNombre());
        nombreOrganizador.setText(String.format("%s %s",
                evento.getCreadorNombre(), evento.getCreadorApellido()));
        ubicacionEvento.setText(String.format("%s, %s",
                evento.getCreadorPais(), evento.getLocalizacion()));

        // Formatear fecha y hora
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        fechaEvento.setText(dateFormat.format(evento.getFecha()));
        horaEvento.setText(timeFormat.format(evento.getHora()));

        descripcionEvento.setText(evento.getDescripcion());

        clearGlideCache();
        // Cargar imágenes con Glide

        String urlConCacheBuster = evento.getCreadorProfileImage() + "?v=" + System.currentTimeMillis();
        Glide.with(this)
                .load(urlConCacheBuster)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .into(perfilFoto);


        Glide.with(this)
                .load(evento.getImagenDelEvento())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imagenEvento);


    }
    private void clearGlideCache() {
        new Thread(() -> {
            Glide.get(EventoClick.this).clearDiskCache();
            runOnUiThread(() -> Glide.get(EventoClick.this).clearMemory());
        }).start();
    }
    private void unirseAlEvento() {
        UUID userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/asistentes_eventos";

        JSONObject json = new JSONObject();
        try {
            json.put("usuario_id", userId.toString());
            json.put("evento_id", evento.getIdEvento()); // Asegúrate que Evento tenga un campo id
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(EventoClick.this, "Error de conexión", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(EventoClick.this, "¡Te has unido al evento!", Toast.LENGTH_SHORT).show();
                        actualizarBoton();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(EventoClick.this, "Error al unirse", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void actualizarBoton() {
        ImageView btnVamos = findViewById(R.id.btn_vamos);
        // Cambiar imagen para indicar que ya está unido
        btnVamos.setImageResource(R.drawable.img_btn_unido);
        btnVamos.setEnabled(false);
    }
}
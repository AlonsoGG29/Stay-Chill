package com.aka.staychill;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aka.staychill.types.Evento;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private static final String DATE_FORMAT = "d MMM yyyy";
    private static final String TIME_FORMAT = "HH:mm";

    private Evento evento;
    private SessionManager sessionManager;
    private ImageView btnEliminar, btnAbandonar, btnUnirse, btnVerParticipantes;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento_click);

        sessionManager = new SessionManager(this);
        procesarIntent();
    }

    private void procesarIntent() {
        Intent intent = getIntent();
        if (!validarDatosIntent(intent)) {
            mostrarErrorYSalir("Datos del evento no válidos");
            return;
        }

        cargarDatosEvento(intent);
        if (evento == null) {
            mostrarErrorYSalir("Error cargando evento");
            return;
        }

        configurarInterfaz();
    }

    private void configurarInterfaz() {
        configurarFondo();
        inicializarVistas();
        configurarBotonUnirse();
        actualizarUI();
    }

    private void configurarBotonUnirse() {
        btnUnirse = findViewById(R.id.btn_vamos);
        btnEliminar = findViewById(R.id.btn_eliminar);
        btnAbandonar = findViewById(R.id.btn_abandonar);
        btnVerParticipantes = findViewById(R.id.btn_ver_participantes);

        UUID userId = sessionManager.getUserId();
        UUID creadorId = evento.getCreadorId();

        if (userId == null || creadorId == null) {
            btnUnirse.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
            btnAbandonar.setVisibility(View.GONE);
            return;
        }

        boolean esCreador = creadorId.equals(userId);
        boolean estaUnido = evento.getAsistentes().contains(userId);

        if (esCreador) {
            configurarBotonCreador();
        } else if (estaUnido) {
            configurarBotonAsistente();
        } else {
            configurarBotonNormal();
        }

        // Deshabilitar botón si el evento está lleno
        if (evento.getNumeroActualParticipantes() >= evento.getLimitePersonas()) {
            btnUnirse.setEnabled(false);
            btnUnirse.setAlpha(0.5f);
        }
    }

    private void configurarBotonCreador() {
        btnEliminar.setVisibility(View.VISIBLE);
        btnUnirse.setVisibility(View.GONE);
        btnAbandonar.setVisibility(View.GONE);

        btnVerParticipantes.setVisibility(View.VISIBLE);
        btnVerParticipantes.setOnClickListener(v -> {
            Intent intent = new Intent(EventoClick.this, VerParticipantes.class);

            intent.putExtra("EVENT_ID", evento.getId().toString());
            startActivity(intent);
        });

        btnEliminar.setOnClickListener(v -> mostrarDialogoConfirmacionEliminar());
    }

    private void configurarBotonAsistente() {
        btnAbandonar.setVisibility(View.VISIBLE);
        btnUnirse.setVisibility(View.GONE);
        btnEliminar.setVisibility(View.GONE);

        btnAbandonar.setOnClickListener(v -> abandonarEvento());
    }

    private void configurarBotonNormal() {
        btnUnirse.setVisibility(View.VISIBLE);
        btnEliminar.setVisibility(View.GONE);
        btnAbandonar.setVisibility(View.GONE);

        btnUnirse.setOnClickListener(v -> manejarUnionEvento());
    }

    private void actualizarUI() {
        // Actualizar contador de participantes
        TextView limiteParticipantes = findViewById(R.id.limitePersonasEvento);
        limiteParticipantes.setText(
                evento.getNumeroActualParticipantes() + "/" + evento.getLimitePersonas()
        );

        // Actualizar estado del botón
        configurarBotonUnirse();
    }

    private void manejarUnionEvento() {
        if (!sessionManager.isLoggedIn()) {
            mostrarToast("Debes iniciar sesión");
            return;
        }

        if (evento.getNumeroActualParticipantes() >= evento.getLimitePersonas()) {
            mostrarToast("El evento está lleno");
            return;
        }

        realizarPeticionUnion();
    }

    private void realizarPeticionUnion() {
        OkHttpClient client = SupabaseConfig.getClient();
        UUID userId = sessionManager.getUserId();

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("event_id", evento.getId());
        jsonBody.addProperty("user_id", userId.toString());

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/rpc/unirse_a_evento")
                .post(RequestBody.create(
                        jsonBody.toString(),
                        MediaType.get("application/json; charset=utf-8")
                ))
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> mostrarToast("Error de conexión"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", responseBody); // Log detallado
                Log.d("DEBUG", "Evento ID = " + evento.getId()
                        + ", actual = " + evento.getNumeroActualParticipantes()
                        + ", límite = " + evento.getLimitePersonas());
                Log.d("DEBUG", "Payload = " + jsonBody);


                if (response.isSuccessful()) {
                    try {
                        JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
                        boolean updated = jsonResponse.get("updated").getAsBoolean();

                        runOnUiThread(() -> {
                            if (updated) {
                                int newCount = jsonResponse.get("current_count").getAsInt();
                                evento.setNumeroActualParticipantes(newCount);
                                actualizarUI();
                                mostrarToast("¡Unido al evento!");
                            } else {
                                String mensaje = jsonResponse.get("message").getAsString();
                                mostrarToast(mensaje);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("PARSE_ERROR", "Error parsing JSON: " + e.getMessage());
                        runOnUiThread(() -> mostrarToast("Error procesando respuesta"));
                    }
                } else {
                    Log.e("API_ERROR", "Code: " + response.code() + " Body: " + responseBody);
                    runOnUiThread(() -> mostrarToast("Error del servidor"));
                }
            }
        });
    }

    private void abandonarEvento() {
        OkHttpClient client = SupabaseConfig.getClient();
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("event_id", evento.getId());
        jsonBody.addProperty("user_id", sessionManager.getUserId().toString());

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/rpc/abandonar_evento")
                .post(RequestBody.create(jsonBody.toString(), MediaType.get("application/json")))
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> mostrarToast("Error de conexión"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JsonObject res = new Gson().fromJson(body, JsonObject.class);
                        boolean updated = res.get("updated").getAsBoolean();

                        runOnUiThread(() -> {
                            if (updated) {
                                int newCount = res.get("current_count").getAsInt();
                                evento.setNumeroActualParticipantes(newCount);
                                actualizarUI();
                                mostrarToast("Has abandonado el evento");
                            } else {
                                mostrarToast(res.has("message")
                                        ? res.get("message").getAsString()
                                        : "No estabas registrado en el evento");
                            }
                        });
                    } catch (Exception e) {
                        Log.e("PARSE_ERROR", "Error parsing JSON: " + e.getMessage());
                        runOnUiThread(() -> mostrarToast("Error procesando respuesta"));
                    }
                } else {
                    runOnUiThread(() -> mostrarToast("Error del servidor: " + response.code()));
                }
            }

        });
    }

    private void mostrarDialogoConfirmacionEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar evento")
                .setMessage("¿Estás seguro de que quieres eliminar este evento?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarEvento())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEvento() {
        OkHttpClient client = SupabaseConfig.getClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/eventos?id=eq." + evento.getId())
                .delete()
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> mostrarToast("Error de conexión"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        mostrarToast("Evento eliminado");
                        finish();
                    });
                } else {
                    runOnUiThread(() -> mostrarToast("Error al eliminar: " + response.code()));
                }
            }
        });
    }

    private boolean validarDatosIntent(Intent intent) {
        return intent != null && intent.hasExtra("EVENTO_DATA");
    }

    private void cargarDatosEvento(Intent intent) {
        try {
            String json = intent.getStringExtra("EVENTO_DATA");
            evento = new Gson().fromJson(json, Evento.class);
        } catch (Exception e) {
            Log.e("EventoClick", "Error deserializando evento", e);
        }
    }

    private void configurarFondo() {
        View rootLayout = findViewById(R.id.eventoClick);
        int colorRes = evento.getColorResId();
        rootLayout.setBackgroundColor(ContextCompat.getColor(this, colorRes));
    }

    private void inicializarVistas() {
        configurarTextoBasico();
        configurarDatosCreador();
        configurarFechas();
        cargarImagenes();
    }

    private void configurarTextoBasico() {
        ((TextView) findViewById(R.id.tituloEvento)).setText(evento.getNombreEvento());
        ((TextView) findViewById(R.id.descripcionEvento)).setText(evento.getDescripcion());
    }

    private void configurarDatosCreador() {
        TextView nombreOrganizador = findViewById(R.id.nombreOrganizador);
        String nombreCompleto = String.format("%s %s", evento.getCreadorNombre(), evento.getCreadorApellido());
        nombreOrganizador.setText(nombreCompleto);

        // Al pulsar el nombre, redirigimos al Chat con este usuario
        nombreOrganizador.setOnClickListener(v -> {
            Intent chatIntent = new Intent(EventoClick.this, Chat.class);
            // Asumiendo que Chat espera el ID con la key "contacto_id"
            chatIntent.putExtra("contacto_id", evento.getCreadorId().toString());
            startActivity(chatIntent);
        });
    }


    private void configurarFechas() {
        TextView fecha = findViewById(R.id.fechaEvento);
        TextView hora = findViewById(R.id.horaEvento);

        try {
            SimpleDateFormat parserFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaDate = parserFecha.parse(evento.getFechaStr());
            fecha.setText(dateFormatter.format(fechaDate));
        } catch (ParseException e) {
            Log.e("EventoClick", "Error parseando fecha", e);
            fecha.setText(evento.getFechaStr());
        }

        hora.setText(evento.getHoraStr());
    }

    private void cargarImagenes() {
        cargarImagenEvento();
        cargarImagenPerfil();
    }

    private void cargarImagenEvento() {
        ImageView imagenEvento = findViewById(R.id.imagenEvento);
        int resId = evento.getImagenDelEvento(this);

        Glide.with(this)
                .load(resId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.img_default)
                .error(R.drawable.img_default)
                .into(imagenEvento);
    }

    private void cargarImagenPerfil() {
        ImageView perfilFoto = findViewById(R.id.perfilFoto);
        String url = evento.getCreadorProfileImage();

        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.img_default)
                    .into(perfilFoto);
        }
    }

    private void mostrarErrorYSalir(String mensaje) {
        mostrarToast(mensaje);
        new Handler().postDelayed(this::finish, 2000);
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
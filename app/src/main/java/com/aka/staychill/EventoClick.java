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
    private ImageView btnEliminar, btnAbandonar, btnUnirse;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());

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
    private void configurarBotonUnirse() {
        btnUnirse = findViewById(R.id.btn_vamos);
        btnEliminar = findViewById(R.id.btn_eliminar);
        btnAbandonar = findViewById(R.id.btn_abandonar);

        Log.d("BotonesDebug", "Botones inicializados: "
                + (btnUnirse != null) + ", "
                + (btnEliminar != null) + ", "
                + (btnAbandonar != null));

        UUID userId = sessionManager.getUserId();
        UUID creadorId = evento.getCreadorId();

        Log.d("BotonesDebug", "User ID: " + userId);
        Log.d("BotonesDebug", "Creador ID: " + creadorId);

        if (userId == null || creadorId == null) {
            btnUnirse.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
            btnAbandonar.setVisibility(View.GONE);
            return;
        }

        boolean esCreador = creadorId.equals(userId);
        boolean estaUnido = evento.getAsistentes().contains(userId);
        Log.d("DEBUG", "User ID: " + userId);
        Log.d("DEBUG", "Creador ID: " + evento.getCreadorDatos());

        if (esCreador) {
            configurarBotonCreador();
        } else if (estaUnido) {
            configurarBotonAsistente();
        } else {
            configurarBotonNormal();
        }
    }
    private void configurarBotonCreador() {
        btnEliminar.setVisibility(View.VISIBLE);
        btnUnirse.setVisibility(View.GONE);
        btnAbandonar.setVisibility(View.GONE);

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

    private void mostrarDialogoConfirmacionEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar evento")
                .setMessage("¿Estás seguro de que quieres eliminar este evento? Esta acción no se puede deshacer.")
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

    private void abandonarEvento() {
        OkHttpClient client = SupabaseConfig.getClient();
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/asistentes_eventos?" +
                "usuario_id=eq." + sessionManager.getUserId() +
                "&evento_id=eq." + evento.getId();

        Request request = new Request.Builder()
                .url(url)
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
                        mostrarToast("Has abandonado el evento");
                        configurarBotonNormal();
                    });
                } else {
                    runOnUiThread(() -> mostrarToast("Error al abandonar: " + response.code()));
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

    private void configurarInterfaz() {
        configurarFondo();
        inicializarVistas();
        configurarBotonUnirse();
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
        nombreOrganizador.setText(String.format("%s %s",
                evento.getCreadorNombre(),
                evento.getCreadorApellido()));

        TextView ubicacion = findViewById(R.id.ubicacionEvento);
        ubicacion.setText(String.format("%s, %s",
                evento.getCreadorPais(),
                evento.getLocalizacion()));
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

    private void manejarUnionEvento() {
        if (!sessionManager.isLoggedIn()) {
            mostrarToast("Debes iniciar sesión");
            return;
        }

        realizarPeticionUnion();
    }

    private void realizarPeticionUnion() {
        OkHttpClient client = SupabaseConfig.getClient();
        RequestBody body = crearCuerpoPeticion();

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/asistentes_eventos")
                .post(body)
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
                manejarRespuestaUnion(response);
            }
        });
    }

    private RequestBody crearCuerpoPeticion() {
        return RequestBody.create(
                String.format("{\"usuario_id\":\"%s\",\"evento_id\":%d}",
                        sessionManager.getUserId().toString(),
                        evento.getId()),
                MediaType.get("application/json")
        );
    }

    private void manejarRespuestaUnion(Response response) {
        runOnUiThread(() -> {
            if (response.isSuccessful()) {
                mostrarToast("¡Te has unido al evento!");
                actualizarBotonUnido(findViewById(R.id.btn_vamos));
            } else {
                mostrarToast("Error al unirse: " + response.code());
            }
        });
    }

    private void actualizarBotonUnido(ImageView boton) {
        boton.setImageResource(R.drawable.img_btn_unido);
        boton.setEnabled(false);
    }

    private void mostrarErrorYSalir(String mensaje) {
        mostrarToast(mensaje);
        new Handler().postDelayed(this::finish, 2000);
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
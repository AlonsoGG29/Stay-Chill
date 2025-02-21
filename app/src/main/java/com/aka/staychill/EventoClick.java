package com.aka.staychill;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventoClick extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento_click);

        String eventoJson = getIntent().getStringExtra("EVENTO_DATA");
        Evento evento = new Gson().fromJson(eventoJson, Evento.class);

        ImageView imagenEvento = findViewById(R.id.imagenEvento);
        TextView tituloEvento = findViewById(R.id.tituloEvento);
        ImageView perfilFoto = findViewById(R.id.perfilFoto);
        TextView nombreOrganizador = findViewById(R.id.nombreOrganizador);
        TextView ubicacionEvento = findViewById(R.id.ubicacionEvento);
        TextView fechaEvento = findViewById(R.id.fechaEvento);
        TextView horaEvento = findViewById(R.id.horaEvento);
        TextView descripcionEvento = findViewById(R.id.descripcionEvento);

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
}
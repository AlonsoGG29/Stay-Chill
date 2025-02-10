package com.aka.staychill;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EventoClick extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento_click);

        TextView tituloEvento = findViewById(R.id.tituloEvento);
        TextView ubicacionEvento = findViewById(R.id.ubicacionEvento);
        TextView descripcionEvento = findViewById(R.id.descripcionEvento);
        TextView fechaEvento = findViewById(R.id.fechaEvento);
        TextView horaEvento = findViewById(R.id.horaEvento);

        // Obtener los datos del intent
        String nombre = getIntent().getStringExtra("nombreEvento");
        String ubicacion = getIntent().getStringExtra("ubicacionEvento");
        String descripcion = getIntent().getStringExtra("descripcionEvento");
        String fecha = getIntent().getStringExtra("fechaEvento");
        String hora = getIntent().getStringExtra("horaEvento");

        // Setear los datos en los TextView correspondientes
        tituloEvento.setText(nombre);
        ubicacionEvento.setText(ubicacion);
        descripcionEvento.setText(descripcion);
        fechaEvento.setText(fecha);
        horaEvento.setText(hora);
    }
}
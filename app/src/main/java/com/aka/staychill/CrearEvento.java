package com.aka.staychill;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CrearEvento extends AppCompatActivity {

    private EditText inputNombre, inputLocalizacion, inputDescripcion, inputFecha, inputHora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        inputNombre = findViewById(R.id.inputNombre);
        inputLocalizacion = findViewById(R.id.inputLocalizacion);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputFecha = findViewById(R.id.inputFecha);
        inputHora = findViewById(R.id.inputHora);
        Button btnCrearEvento = findViewById(R.id.btnCrearEvento);

        btnCrearEvento.setOnClickListener(view -> {
            String nombre = inputNombre.getText().toString();
            String localizacion = inputLocalizacion.getText().toString();
            String descripcion = inputDescripcion.getText().toString();
            String fecha = inputFecha.getText().toString();
            String hora = inputHora.getText().toString();

            if (nombre.isEmpty() || localizacion.isEmpty() || descripcion.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
                Toast.makeText(CrearEvento.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // Guardar el evento en la lista estática
                Evento nuevoEvento = new Evento(nombre, localizacion, descripcion, fecha, hora);
                EventoManejador.addEvento(nuevoEvento);

                // Mostrar un mensaje de éxito
                Toast.makeText(CrearEvento.this, "Evento creado exitosamente", Toast.LENGTH_SHORT).show();

                // Regresar a la vista principal
                finish();
            }
        });
    }
}
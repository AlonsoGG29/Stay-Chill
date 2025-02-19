package com.aka.staychill;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import okhttp3.*;

public class CrearEvento extends AppCompatActivity {

    private EditText inputNombre, inputLocalizacion, inputDescripcion, inputFecha, inputHora;
    private ImageView inputImagen;
    private Spinner spinnerTipoDeEvento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        // Inicializar vistas
        inputNombre = findViewById(R.id.inputNombre);
        inputLocalizacion = findViewById(R.id.inputLocalizacion);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputFecha = findViewById(R.id.inputFecha);
        inputHora = findViewById(R.id.inputHora);
        inputImagen = findViewById(R.id.EventoImagen);
        spinnerTipoDeEvento = findViewById(R.id.spinnerTipoDeEvento);
        Button btnCrearEvento = findViewById(R.id.btnCrearEvento);

        // Configurar el Spinner con opciones para el tipo de evento
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Concierto", "Teatro", "Deporte", "Fiesta"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDeEvento.setAdapter(adapter);

        // Asociar imagen según el tipo de evento seleccionado
        spinnerTipoDeEvento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipoSeleccionado = parent.getItemAtPosition(position).toString();
                switch (tipoSeleccionado) {
                    case "Concierto":
                        inputImagen.setImageResource(R.drawable.event_musica);
                        inputImagen.setTag("concierto_vector");
                        break;
                    case "Teatro":
                        inputImagen.setImageResource(R.drawable.event_cultura_arte);
                        inputImagen.setTag("teatro_vector");
                        break;
                    case "Deporte":
                        inputImagen.setImageResource(R.drawable.event_deporte1);
                        inputImagen.setTag("deporte_vector");
                        break;
                    case "Fiesta":
                        inputImagen.setImageResource(R.drawable.event_fiesta_social);
                        inputImagen.setTag("fiesta_vector");
                        break;
                    default:
                        inputImagen.setImageResource(R.drawable.event_deporte1);
                        inputImagen.setTag("default_image");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnCrearEvento.setOnClickListener(view -> {
            String nombre = inputNombre.getText().toString().trim();
            String localizacion = inputLocalizacion.getText().toString().trim();
            String descripcion = inputDescripcion.getText().toString().trim();
            String fechaStr = inputFecha.getText().toString().trim();
            String horaStr = inputHora.getText().toString().trim();
            String imagen = inputImagen.getTag() != null ? inputImagen.getTag().toString() : "";
            String tipoEvento = spinnerTipoDeEvento.getSelectedItem().toString();

            if (nombre.isEmpty() || localizacion.isEmpty() || descripcion.isEmpty() ||
                    fechaStr.isEmpty() || horaStr.isEmpty() || imagen.isEmpty()) {
                Toast.makeText(CrearEvento.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy");
                    Date fecha = sdfInput.parse(fechaStr);
                    OffsetTime hora = OffsetTime.parse(horaStr);

                    Evento nuevoEvento = new Evento(nombre, localizacion, descripcion, fecha, hora, tipoEvento, imagen);
                    subirEventoASupabase(nuevoEvento);

                } catch (ParseException e) {
                    Toast.makeText(CrearEvento.this, "Formato de fecha incorrecto. Use dd/MM/yyyy", Toast.LENGTH_SHORT).show();
                } catch (DateTimeParseException e) {
                    Toast.makeText(CrearEvento.this, "Formato de hora incorrecto. Ejemplo: 10:15:30+01:00", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void subirEventoASupabase(Evento evento) {
        OkHttpClient client = SupabaseConfig.getClient();
        HashMap<String, Object> data = new HashMap<>();
        data.put("nombre_evento", evento.getNombre());
        data.put("localizacion", evento.getLocalizacion());
        data.put("descripcion", evento.getDescripcion());
        SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd");
        data.put("fecha_evento", sdfOutput.format(evento.getFecha()));
        data.put("hora_evento", evento.getHora().toString());
        data.put("tipo_de_evento", evento.getTipoDeEvento());
        data.put("imagen_del_evento", evento.getImagenDelEvento());

        Gson gson = new Gson();
        String jsonString = gson.toJson(data);

        RequestBody body = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl())
                .post(body)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey())
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CrearEvento.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> Toast.makeText(CrearEvento.this, response.isSuccessful() ? "Evento subido" : "Error al subir", Toast.LENGTH_SHORT).show());
            }
        });
    }
}

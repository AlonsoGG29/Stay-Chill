package com.aka.staychill;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CrearEvento extends AppCompatActivity {

    private EditText inputNombre, inputLocalizacion, inputDescripcion, inputFecha, inputHora;
    private ImageView inputImagen;
    private Spinner spinnerTipoDeEvento;
    private SessionManager sessionManager;
    private HashMap<String, Integer> imagenesPorTipo = new HashMap<>();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        sessionManager = new SessionManager(this);
        configurarMapeoImagenes();
        inicializarVistas();
        configurarPickers();
        configurarSpinner();
    }

    private void configurarMapeoImagenes() {
        imagenesPorTipo.put("Concierto", R.drawable.event_musica);
        imagenesPorTipo.put("Teatro", R.drawable.event_cultura_arte);
        imagenesPorTipo.put("Deporte", R.drawable.event_deporte1);
        imagenesPorTipo.put("Fiesta", R.drawable.event_fiesta_social);
        imagenesPorTipo.put("Aprendizaje", R.drawable.event_aprendizaje);
    }

    private void inicializarVistas() {
        inputNombre = findViewById(R.id.inputNombre);
        inputLocalizacion = findViewById(R.id.inputLocalizacion);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputFecha = findViewById(R.id.inputFecha);
        inputHora = findViewById(R.id.inputHora);
        inputImagen = findViewById(R.id.eventoImagen);
        spinnerTipoDeEvento = findViewById(R.id.spinnerTipoDeEvento);

        Button btnCrearEvento = findViewById(R.id.btnCrearEvento);
        btnCrearEvento.setOnClickListener(v -> crearNuevoEvento());
    }

    private void configurarPickers() {
        inputFecha.setOnClickListener(v -> mostrarDatePicker());
        inputHora.setOnClickListener(v -> mostrarTimePicker());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, day);

                    // Validar fecha no pasada
                    Calendar hoy = Calendar.getInstance();
                    hoy.set(Calendar.HOUR_OF_DAY, 0);
                    hoy.set(Calendar.MINUTE, 0);
                    hoy.set(Calendar.SECOND, 0);
                    hoy.set(Calendar.MILLISECOND, 0);

                    if (selectedDate.before(hoy)) {
                        mostrarError("No se pueden seleccionar fechas pasadas");
                        inputFecha.setText("");
                    } else {
                        inputFecha.setText(dateFormatter.format(selectedDate.getTime()));
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(Calendar.HOUR_OF_DAY, hour);
            selectedTime.set(Calendar.MINUTE, minute);
            inputHora.setText(timeFormatter.format(selectedTime.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void configurarSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(imagenesPorTipo.keySet()));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDeEvento.setAdapter(adapter);

        spinnerTipoDeEvento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = parent.getItemAtPosition(position).toString();
                Integer imagenId = imagenesPorTipo.get(tipo);
                if (imagenId != null) {
                    inputImagen.setImageResource(imagenId);
                    inputImagen.setTag(imagenId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                inputImagen.setImageResource(R.drawable.img_label);
            }
        });
    }

    private void crearNuevoEvento() {
        if (!validarCampos() || !validarSesion()) return;

        try {
            Date fecha = dateFormatter.parse(inputFecha.getText().toString());
            Date hora = timeFormatter.parse(inputHora.getText().toString());

            // Validación adicional de fecha
            Calendar fechaEvento = Calendar.getInstance();
            fechaEvento.setTime(fecha);
            Calendar hoy = Calendar.getInstance();
            hoy.set(Calendar.HOUR_OF_DAY, 0);
            hoy.set(Calendar.MINUTE, 0);
            hoy.set(Calendar.SECOND, 0);
            hoy.set(Calendar.MILLISECOND, 0);

            if (fechaEvento.before(hoy)) {
                mostrarError("La fecha del evento no puede ser pasada");
                return;
            }

            Evento nuevoEvento = new Evento(
                    null,
                    inputNombre.getText().toString().trim(),
                    inputLocalizacion.getText().toString().trim(),
                    inputDescripcion.getText().toString().trim(),
                    fecha,
                    hora,
                    spinnerTipoDeEvento.getSelectedItem().toString(),
                    (int) inputImagen.getTag(),
                    sessionManager.getUserId(),
                    "" ,
                    "",
                    "",
                    ""
            );

            subirEventoASupabase(nuevoEvento);

        } catch (Exception e) {
            mostrarError("Error en formato de fecha/hora: " + e.getMessage());
            Log.e("CrearEvento", "Error parsing date/time", e);
        }
    }

    private boolean validarCampos() {
        if (inputNombre.getText().toString().trim().isEmpty() ||
                inputLocalizacion.getText().toString().trim().isEmpty() ||
                inputFecha.getText().toString().isEmpty() ||
                inputHora.getText().toString().isEmpty()) {

            mostrarError("Complete todos los campos obligatorios");
            return false;
        }
        return true;
    }

    private boolean validarSesion() {
        if (!sessionManager.isLoggedIn()) {
            mostrarError("Debe iniciar sesión primero");
            return false;
        }
        return true;
    }

    private void subirEventoASupabase(Evento evento) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = crearCuerpoPeticion(evento);

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/eventos")
                .post(body)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> mostrarError("Error de red: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(CrearEvento.this, "Evento creado exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = "Error " + response.code() + ": " + responseBody;
                        mostrarError(errorMsg);
                        Log.e("SupabaseError", errorMsg);
                    }
                });
            }
        });
    }

    private RequestBody crearCuerpoPeticion(Evento evento) {
        JsonObject payload = new JsonObject();
        try {
            // Formato de fecha correcto (yyyy-MM-dd)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            payload.addProperty("fecha_evento", dateFormat.format(evento.getFecha()));

            // Formato de hora corregido (sin timezone)
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            payload.addProperty("hora_evento", timeFormat.format(evento.getHora()));

            // Resto de campos
            payload.addProperty("nombre_evento", evento.getNombre());
            payload.addProperty("localizacion", evento.getLocalizacion());
            payload.addProperty("descripcion", evento.getDescripcion());
            payload.addProperty("tipo_de_evento", evento.getTipoDeEvento());

            // Nombre del recurso drawable (ej: "event_musica")
            String resourceName = getResources().getResourceEntryName(evento.getImagenDelEvento());
            payload.addProperty("imagen_del_evento", resourceName);

            // UUID del creador
            payload.addProperty("creador_id", evento.getCreadorId().toString());

        } catch (Exception e) {
            Log.e("CrearEvento", "Error creando payload", e);
        }

        return RequestBody.create(
                payload.toString(),
                MediaType.get("application/json; charset=utf-8")
        );
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}
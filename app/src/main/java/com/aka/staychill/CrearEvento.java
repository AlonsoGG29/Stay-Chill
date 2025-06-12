package com.aka.staychill;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.aka.staychill.types.Evento;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CrearEvento extends AppCompatActivity {


    private static final String DATE_API_FORMAT = "yyyy-MM-dd";
    private static final String TIME_API_FORMAT = "HH:mm:ss";
    private static final String DATE_UI_FORMAT = "dd/MM/yyyy";
    private static final String TIME_UI_FORMAT = "HH:mm";

    private EditText inputNombre, inputLocalizacion, inputDescripcion, inputFecha, inputHora, inputLimitePartipantes;
    private ImageView inputImagen;
    private Spinner spinnerTipoDeEvento;
    private ImageButton btnBack;

    private SessionManager sessionManager;
    private final HashMap<String, Integer> imagenesPorTipo = new HashMap<>();
    private final SimpleDateFormat uiDateFormatter = new SimpleDateFormat(DATE_UI_FORMAT, Locale.getDefault());
    private final SimpleDateFormat uiTimeFormatter = new SimpleDateFormat(TIME_UI_FORMAT, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        inicializarDependencias();
        configurarMapeoImagenes();
        inicializarVistas();
        configurarSpinner();
    }

    private void inicializarDependencias() {
        sessionManager = new SessionManager(this);
    }

    private void configurarMapeoImagenes() {
        imagenesPorTipo.put("Deporte", R.drawable.event_deporte);
        imagenesPorTipo.put("Comida y Bebida", R.drawable.event_comida);
        imagenesPorTipo.put("Cultura y Arte", R.drawable.event_cultura_arte);
        imagenesPorTipo.put("Música y Entretenimiento", R.drawable.event_musica);
        imagenesPorTipo.put("Naturaleza y Aire Libre", R.drawable.event_naturaleza);
        imagenesPorTipo.put("Fiestas y Social", R.drawable.event_fiesta_social);
        imagenesPorTipo.put("Aprendizaje y Desarrollo ", R.drawable.event_aprendizaje);
        imagenesPorTipo.put("Gaming y Tecnología", R.drawable.event_gaming);
        imagenesPorTipo.put("Mascotas y Animales", R.drawable.event_mascota);
        imagenesPorTipo.put("Viajes y Escapadas", R.drawable.event_viajes);
        imagenesPorTipo.put("Fotografía y Creatividad", R.drawable.event_fotografia);
        imagenesPorTipo.put("Salud y Bienestar", R.drawable.event_salud_bienestar);
        imagenesPorTipo.put("Motor y Aventura", R.drawable.event_motor_evento);
    }

    private void inicializarVistas() {
        enlazarVistas();
        configurarPickers();
        configurarBotonCreacion();
    }

    private void enlazarVistas() {
        inputNombre = findViewById(R.id.inputNombre);
        inputLocalizacion = findViewById(R.id.inputLocalizacion);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputFecha = findViewById(R.id.inputFecha);
        inputHora = findViewById(R.id.inputHora);
        inputImagen = findViewById(R.id.eventoImagen);
        inputLimitePartipantes = findViewById(R.id.inputLimiteParticipantes);
        spinnerTipoDeEvento = findViewById(R.id.spinnerTipoDeEvento);
        btnBack = findViewById(R.id.btnBack);
    }

    private void configurarPickers() {
        inputFecha.setOnClickListener(v -> mostrarDatePicker());
        inputHora.setOnClickListener(v -> mostrarTimePicker());
        btnBack.setOnClickListener(v -> volverMenu());
    }

    private void configurarBotonCreacion() {
        Button btnCrearEvento = findViewById(R.id.btnCrearEvento);
        btnCrearEvento.setOnClickListener(v -> manejarCreacionEvento());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> validarFechaSeleccionada(year, month, day),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void validarFechaSeleccionada(int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        resetCalendarTime(hoy);

        if (selectedDate.before(hoy)) {
            mostrarError("No se pueden seleccionar fechas pasadas");
            inputFecha.setText("");
        } else {
            inputFecha.setText(uiDateFormatter.format(selectedDate.getTime()));
        }
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(Calendar.HOUR_OF_DAY, hour);
            selectedTime.set(Calendar.MINUTE, minute);
            inputHora.setText(uiTimeFormatter.format(selectedTime.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void configurarSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner,
                new ArrayList<>(imagenesPorTipo.keySet())
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDeEvento.setAdapter(adapter);

        spinnerTipoDeEvento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarImagenEvento(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                inputImagen.setImageResource(R.drawable.img_label);
            }
        });
    }

    private void actualizarImagenEvento(String tipoEvento) {
        Integer imagenId = imagenesPorTipo.get(tipoEvento);
        if (imagenId != null) {
            inputImagen.setImageResource(imagenId);
            inputImagen.setTag(imagenId);
        }
    }

    private void manejarCreacionEvento() {
        if (!validarFormulario()) return;

        try {
            Date fecha = parsearFecha(inputFecha.getText().toString());
            Date hora = parsearHora(inputHora.getText().toString());

            if (!validarFechaFutura(fecha)) return;

            subirEventoASupabase(construirEvento(fecha, hora));

        } catch (Exception e) {
            manejarErrorFechas(e);
        }
    }

    private boolean validarFormulario() {
        return validarCampos() && validarSesion();
    }

    private Date parsearFecha(String fecha) throws Exception {
        return uiDateFormatter.parse(fecha);
    }

    private Date parsearHora(String hora) throws Exception {
        return uiTimeFormatter.parse(hora);
    }

    private boolean validarFechaFutura(Date fecha) {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        resetCalendarTime(hoy);

        Calendar fechaEvento = Calendar.getInstance();
        fechaEvento.setTime(fecha);

        if (fechaEvento.before(hoy)) {
            mostrarError("La fecha debe ser futura");
            return false;
        }
        return true;
    }

    private void resetCalendarTime(Calendar calendar) {
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
    }

    private Evento construirEvento(Date fecha, Date hora) {
        Evento evento = new Evento();
        evento.setNombreEvento(inputNombre.getText().toString().trim());
        evento.setLocalizacion(inputLocalizacion.getText().toString().trim());
        evento.setDescripcion(inputDescripcion.getText().toString().trim());
        evento.setTipoDeEvento(spinnerTipoDeEvento.getSelectedItem().toString());
        evento.setFechaStr(new SimpleDateFormat(DATE_API_FORMAT).format(fecha));
        evento.setHoraStr(new SimpleDateFormat(TIME_API_FORMAT).format(hora));
        evento.setImagenNombre(obtenerNombreImagen());
        evento.setLimiteParticipantes(Integer.parseInt(inputLimitePartipantes.getText().toString()));
        evento.setCreadorId(sessionManager.getUserId());
        evento.setNumeroActualParticipantes(0);
        return evento;
    }

    private String obtenerNombreImagen() {
        return getResources().getResourceEntryName((Integer) inputImagen.getTag());
    }

    private void subirEventoASupabase(Evento evento) {
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/eventos")
                .post(crearCuerpoPeticion(evento))
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> mostrarError("Error de conexión: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                manejarRespuestaServidor(response);
            }
        });
    }

    private RequestBody crearCuerpoPeticion(Evento evento) {
        JsonObject payload = new JsonObject();
        payload.addProperty("nombre_evento", evento.getNombreEvento());
        payload.addProperty("localizacion", evento.getLocalizacion());
        payload.addProperty("descripcion", evento.getDescripcion());
        payload.addProperty("fecha_evento", evento.getFechaStr());
        payload.addProperty("hora_evento", evento.getHoraStr());
        payload.addProperty("tipo_de_evento", evento.getTipoDeEvento());
        payload.addProperty("imagen_del_evento", evento.getImagenDelEvento(this));
        payload.addProperty("creador_id", evento.getCreadorDatos().toString());
        payload.addProperty("limite_de_participantes", evento.getLimitePersonas());
        payload.addProperty("numero_actual_participantes", evento.getNumeroActualParticipantes());

        return RequestBody.create(payload.toString(), MediaType.get("application/json"));
    }

    private void manejarRespuestaServidor(Response response) throws IOException {
        String responseBody = response.body().string();
        runOnUiThread(() -> {
            if (response.isSuccessful()) {
                mostrarExito();
            } else {
                mostrarErrorServidor(response.code(), responseBody);
            }
        });
    }

    private void mostrarExito() {
        Toast.makeText(this, "Evento creado exitosamente", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void mostrarErrorServidor(int codigo, String respuesta) {
        String errorMsg = "Error " + codigo + ": " + respuesta;
        Log.e("SupabaseError", errorMsg);
        mostrarError(errorMsg);
    }

    private void manejarErrorFechas(Exception e) {
        String errorMsg = "Error en fechas: " + e.getMessage();
        Log.e("CrearEvento", errorMsg, e);
        mostrarError(errorMsg);
    }

    private boolean validarCampos() {
        if (inputNombre.getText().toString().trim().isEmpty() ||
                inputLocalizacion.getText().toString().trim().isEmpty() ||
                inputFecha.getText().toString().isEmpty() ||
                inputHora.getText().toString().isEmpty() ||
                inputLimitePartipantes.getText().toString().isEmpty()){

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
    private void volverMenu() {
        runOnUiThread(() -> {
            Intent intent = new Intent(this, Main_bn.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            intent.putExtra("start_tab", 2);
            startActivity(intent);
            finish();
        });
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}
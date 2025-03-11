package com.aka.staychill;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Conf_cuenta extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageButton fotoPerfil;
    private EditText inputNombre, inputApellido, inputFechaNacimiento;
    private Spinner inputPais;
    private Uri imagenTempUri = null;
    private boolean hayCambiosImagen = false;
    private UUID userId;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private ArrayAdapter<CharSequence> adapter;
    private SessionManager sessionManager;
    private CargarImagenes cargarImagenes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_cuenta);

        sessionManager = new SessionManager(this);
        cargarImagenes = CargarImagenes.getInstance(this);
        if (!verificarSesion()) return;

        inicializarComponentes();
        configurarSpinner();
        cargarDatosUsuario();
    }

    private boolean verificarSesion() {
        if (sessionManager.getAccessToken() == null || sessionManager.getUserId() == null) {
            runOnUiThread(this::manejarSesionExpirada);
            return false;
        }
        userId = sessionManager.getUserId();
        return true;
    }

    private void inicializarComponentes() {
        fotoPerfil = findViewById(R.id.fotoPerfil);
        inputNombre = findViewById(R.id.inputNombre);
        inputApellido = findViewById(R.id.inputApellido);
        inputFechaNacimiento = findViewById(R.id.inputFechaNacimiento);
        inputPais = findViewById(R.id.inputPais);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.botoncuenta).setOnClickListener(v -> guardarCambios());

        configurarDatePicker();
        fotoPerfil.setOnClickListener(v -> manejarPermisosImagen());
    }

    private void configurarDatePicker() {
        inputFechaNacimiento.setFocusable(false);
        inputFechaNacimiento.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, anio, mes, dia) -> {
                        Calendar fechaSeleccionada = Calendar.getInstance();
                        fechaSeleccionada.set(anio, mes, dia);

                        if (fechaSeleccionada.after(Calendar.getInstance())) {
                            mostrarError("La fecha no puede ser futura");
                        } else {
                            inputFechaNacimiento.setText(String.format(Locale.getDefault(),
                                    "%04d-%02d-%02d", anio, mes + 1, dia));
                        }
                    },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });
    }

    private void configurarSpinner() {
        adapter = ArrayAdapter.createFromResource(this,
                R.array.paises,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPais.setAdapter(adapter);
    }

    private void cargarDatosUsuario() {
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        procesarRespuestaUsuario(response);
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        mostrarError("Error: " + response.code() + " - " + errorBody);
                    }
                } catch (Exception e) {
                    mostrarError("Error procesando respuesta");
                }
            }
        });
    }

    private void procesarRespuestaUsuario(Response response) throws IOException {
        assert response.body() != null;
        JsonArray jsonArray = gson.fromJson(response.body().string(), JsonArray.class);
        if (jsonArray.isEmpty()) {
            mostrarError("Perfil no encontrado");
            return;
        }

        JsonObject usuario = jsonArray.get(0).getAsJsonObject();
        runOnUiThread(() -> actualizarUI(usuario));
    }

    private void actualizarUI(JsonObject usuario) {
        try {
            if (usuario.has("profile_image_url") &&
                    !usuario.get("profile_image_url").isJsonNull() &&
                    !usuario.get("profile_image_url").getAsString().isEmpty()) {

                // Reemplaza Glide con ImageManager
                cargarImagenes.loadProfileImage(
                        usuario.get("profile_image_url").getAsString(),
                        fotoPerfil,
                        Conf_cuenta.this
                );
            } else {
                fotoPerfil.setImageResource(R.drawable.img_default);
            }

            inputNombre.setText(usuario.has("nombre") && !usuario.get("nombre").isJsonNull() ?
                    usuario.get("nombre").getAsString() : "");

            inputApellido.setText(usuario.has("apellido") && !usuario.get("apellido").isJsonNull() ?
                    usuario.get("apellido").getAsString() : "");

            if (usuario.has("fecha_nacimiento") &&
                    !usuario.get("fecha_nacimiento").isJsonNull()) {
                inputFechaNacimiento.setText(usuario.get("fecha_nacimiento").getAsString());
            }

            if (usuario.has("pais") &&
                    !usuario.get("pais").isJsonNull() &&
                    !usuario.get("pais").getAsString().isEmpty()) {

                String pais = usuario.get("pais").getAsString();
                int posicion = adapter.getPosition(pais);
                if (posicion >= 0) {
                    inputPais.setSelection(posicion);
                }
            }
        } catch (Exception e) {
            mostrarError("Error cargando datos del perfil");
        }
    }

    private void manejarPermisosImagen() {
        String permiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permiso}, PERMISSION_REQUEST_CODE);
        } else {
            iniciarSeleccionImagen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarSeleccionImagen();
            } else {
                mostrarError("Permiso necesario para seleccionar imágenes");
            }
        }
    }

    private void iniciarSeleccionImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SELECT_PICTURE) {
                iniciarRecorteImagen(data.getData());
            } else if (requestCode == UCrop.REQUEST_CROP) {
                procesarImagenRecortada(UCrop.getOutput(data));
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            mostrarError("Error al recortar imagen");
        }
    }

    private void iniciarRecorteImagen(Uri sourceUri) {
        try {
            UCrop.Options options = new UCrop.Options();
            options.setCircleDimmedLayer(true);
            options.setCompressionQuality(85);
            options.setHideBottomControls(true);
            options.setShowCropGrid(false);

            File tempFile = File.createTempFile("crop_temp", ".jpg", getCacheDir());
            UCrop.of(sourceUri, Uri.fromFile(tempFile))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(512, 512)
                    .withOptions(options)
                    .start(this);
        } catch (Exception e) {
            mostrarError("Error preparando el editor de imagen");
        }
    }

    private void procesarImagenRecortada(Uri imagenUri) {
        try {
            cargarImagenes.loadProfileImage(
                    imagenUri.toString(),
                    fotoPerfil,
                    Conf_cuenta.this
            );

            imagenTempUri = imagenUri;
            hayCambiosImagen = true;
        } catch (Exception e) {
            mostrarError("Error al cargar imagen");
        }
    }

    private void guardarCambios() {
        if (validarCampos()) {
            new Thread(this::procesarGuardado).start();
        }
    }

    private boolean validarCampos() {
        if (inputNombre.getText().toString().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }

        String fecha = inputFechaNacimiento.getText().toString().trim();
        if (!fecha.isEmpty()) {
            try {
                Calendar fechaNacimiento = Calendar.getInstance();
                fechaNacimiento.setTime(Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)));

                if (fechaNacimiento.after(Calendar.getInstance())) {
                    mostrarError("La fecha no puede ser futura");
                    return false;
                }
            } catch (ParseException e) {
                mostrarError("Formato de fecha inválido");
                return false;
            }
        }
        return true;
    }

    private void procesarGuardado() {
        try {
            String nuevaImagenUrl = hayCambiosImagen ? subirImagenPerfil() : null;
            actualizarPerfilUsuario(nuevaImagenUrl);

            runOnUiThread(() -> {
                if (hayCambiosImagen) {
                    // Actualizar versión de la imagen
                    cargarImagenes.updateImageVersion("profile_" + userId.toString());

                    // Recargar imagen inmediatamente si es necesario
                    if (nuevaImagenUrl != null) {
                        cargarImagenes.loadProfileImage(nuevaImagenUrl, fotoPerfil, Conf_cuenta.this);
                    }
                }

                Toast.makeText(Conf_cuenta.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                hayCambiosImagen = false;
                finish();
            });
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private String subirImagenPerfil() throws IOException {
        // Nombre único basado en el userId que sobrescribirá la imagen anterior
        String nombreArchivo = "user_uploads/" + userId + "_avatar.jpg";

        try (InputStream inputStream = getContentResolver().openInputStream(imagenTempUri)) {
            byte[] bytes = toByteArray(inputStream);

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", nombreArchivo,
                            RequestBody.create(bytes, MediaType.parse("image/jpeg")))
                    .build();


            // Subimos la nueva imagen
            Request uploadRequest = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/user_files/" + nombreArchivo)
                    .put(body)
                    .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .addHeader("Cache-Control", "no-cache, max-age=0") // Nuevo: Deshabilitar caché en Supabase
                    .build();

            try (Response uploadResponse = client.newCall(uploadRequest).execute()) {
                if (!uploadResponse.isSuccessful()) {
                    throw new IOException("Error: " + uploadResponse.code() + " - " + uploadResponse.body().string());
                }

                // Obtener URL real desde la respuesta JSON
                JsonObject responseJson = gson.fromJson(uploadResponse.body().string(), JsonObject.class);
                String path = responseJson.get("Key").getAsString(); // O el campo correcto de tu respuesta
                return SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/public/" + path;
            }
        }
    }

    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private void actualizarPerfilUsuario(String imagenUrl) throws IOException {
        JsonObject datos = new JsonObject();
        datos.addProperty("nombre", inputNombre.getText().toString().trim());

        if (!inputApellido.getText().toString().trim().isEmpty()) {
            datos.addProperty("apellido", inputApellido.getText().toString().trim());
        }

        if (!inputFechaNacimiento.getText().toString().trim().isEmpty()) {
            datos.addProperty("fecha_nacimiento", inputFechaNacimiento.getText().toString().trim());
        }

        if (inputPais.getSelectedItemPosition() > 0) {
            datos.addProperty("pais", inputPais.getSelectedItem().toString());
        }

        if (imagenUrl != null) {
            datos.addProperty("profile_image_url", imagenUrl);
        }

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .patch(RequestBody.create(datos.toString(), MediaType.get("application/json")))
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error: " + response.code() + " - " + response.body().string());
            }
        }
    }

    private void finalizarGuardadoExitoso() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
            hayCambiosImagen = false;
            finish();
        });
    }

    private void manejarSesionExpirada() {
        new AlertDialog.Builder(this)
                .setTitle("Sesión expirada")
                .setMessage("Debes iniciar sesión nuevamente")
                .setPositiveButton("Aceptar", (d, w) -> {
                    sessionManager.logout();
                    startActivity(new Intent(this, Login.class));
                    finishAffinity();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        if (imagenTempUri != null) {
            new File(imagenTempUri.getPath()).delete();
        }
        super.onDestroy();
    }
}
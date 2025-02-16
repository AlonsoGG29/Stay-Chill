package com.aka.staychill;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Calendar;
import java.util.Locale;

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
    private static final String TAG = "Conf_cuenta";

    // Componentes UI
    private ImageView fotoPerfil;
    private EditText inputNombre, inputApellido, inputFechaNacimiento;
    private Spinner inputPais;

    // Variables de estado
    private Uri imagenTempUri = null;
    private boolean hayCambiosImagen = false;
    private String userId;

    // Dependencias
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private ArrayAdapter<CharSequence> adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_cuenta);

        sessionManager = new SessionManager(this);
        inicializarComponentes();
        configurarSpinner();
        verificarUsuario();
    }

    private void inicializarComponentes() {
        fotoPerfil = findViewById(R.id.fotoPerfil);
        ImageView btnBack = findViewById(R.id.btnBack);
        inputNombre = findViewById(R.id.inputNombre);
        inputApellido = findViewById(R.id.inputApellido);
        inputFechaNacimiento = findViewById(R.id.inputFechaNacimiento);
        inputPais = findViewById(R.id.inputPais);

        Button btnGuardar = findViewById(R.id.botoncuenta);
        btnGuardar.setOnClickListener(v -> guardarCambios());

        inputFechaNacimiento.setFocusable(false);
        inputFechaNacimiento.setClickable(true);
        inputFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        btnBack.setOnClickListener(v -> finish());
        fotoPerfil.setOnClickListener(v -> manejarPermisosImagen());
    }

    private void configurarSpinner() {
        adapter = ArrayAdapter.createFromResource(this,
                R.array.paises,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPais.setAdapter(adapter);
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, año, mes, dia) -> inputFechaNacimiento.setText(
                        String.format(Locale.getDefault(), "%04d-%02d-%02d", año, mes + 1, dia)),
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void verificarUsuario() {
        userId = sessionManager.getUserId();
        if (userId == null) {
            mostrarError("Usuario no autenticado");
            finish();
            return;
        }
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + obtenerTokenUsuario())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    mostrarError("Error del servidor: " + response.code());
                    return;
                }

                try {
                    assert response.body() != null;
                    JsonArray jsonArray = gson.fromJson(response.body().string(), JsonArray.class);
                    if (jsonArray.isEmpty()) {
                        mostrarError("Usuario no encontrado");
                        return;
                    }
                    actualizarUI(jsonArray.get(0).getAsJsonObject());
                } catch (Exception e) {
                    mostrarError("Error procesando datos: " + e.getMessage());
                }
            }
        });
    }

    private void actualizarUI(JsonObject usuario) {
        runOnUiThread(() -> {
            try {
                if (campoValido(usuario, "profile_image_url")) {
                    String urlImagen = usuario.get("profile_image_url").getAsString();
                    Glide.with(Conf_cuenta.this)
                            .load(urlImagen + "?t=" + System.currentTimeMillis())
                            .skipMemoryCache(true)
                            .into(fotoPerfil);
                }

                inputNombre.setText(obtenerValorSeguro(usuario, "nombre"));
                inputApellido.setText(obtenerValorSeguro(usuario, "apellido"));
                inputFechaNacimiento.setText(obtenerValorSeguro(usuario, "fecha_nacimiento"));

                if (campoValido(usuario, "pais")) {
                    String pais = usuario.get("pais").getAsString();
                    inputPais.setSelection(adapter.getPosition(pais));
                }
            } catch (Exception e) {
                mostrarError("Error actualizando UI: " + e.getMessage());
            }
        });
    }

    private String obtenerValorSeguro(JsonObject json, String clave) {
        return campoValido(json, clave) ? json.get(clave).getAsString() : "";
    }

    private boolean campoValido(JsonObject json, String clave) {
        return json.has(clave) && !json.get(clave).isJsonNull();
    }

    private void manejarPermisosImagen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            seleccionarImagen();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            seleccionarImagen();
        }
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE && data != null) {
                Uri sourceUri = data.getData();
                iniciarRecorte(sourceUri);
            } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
                handleResultadoRecorte(data);
            }
        }
    }

    private void iniciarRecorte(Uri sourceUri) {
        Uri destinoUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

        UCrop.of(sourceUri, destinoUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(fotoPerfil.getWidth(), fotoPerfil.getHeight())
                .start(this, UCrop.REQUEST_CROP);
    }

    private void handleResultadoRecorte(Intent result) {
        Uri resultadoUri = UCrop.getOutput(result);
        if (resultadoUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultadoUri);
                fotoPerfil.setImageBitmap(bitmap);
                imagenTempUri = resultadoUri;
                hayCambiosImagen = true;
            } catch (Exception e) {
                mostrarError("Error al cargar imagen recortada");
            }
        }
    }

    private void guardarCambios() {
        if (inputNombre.getText().toString().trim().isEmpty() ||
                inputApellido.getText().toString().trim().isEmpty()) {
            mostrarError("Nombre y apellido son obligatorios");
            return;
        }

        new Thread(() -> {
            try {
                String nuevaUrlImagen = null;
                if (hayCambiosImagen && imagenTempUri != null) {
                    nuevaUrlImagen = subirImagenYActualizarUrl();
                }

                actualizarDatosUsuario(nuevaUrlImagen);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
                    hayCambiosImagen = false;
                    finish();
                });

            } catch (Exception e) {
                mostrarError("Error al guardar cambios: " + e.getMessage());
            }
        }).start();
    }

    private String subirImagenYActualizarUrl() throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenTempUri);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
        byte[] imageData = byteArray.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", userId + "_profile.png",
                        RequestBody.create(imageData, MediaType.parse("image/png")))
                .build();

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/user_files/" + userId + "_profile.png")
                .put(requestBody)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + obtenerTokenUsuario())
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error subiendo imagen: " + response.code());
        }

        return SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/public/user_files/" + userId + "_profile.png";
    }

    private void actualizarDatosUsuario(String nuevaUrlImagen) {
        JsonObject datos = new JsonObject();

        datos.addProperty("nombre", inputNombre.getText().toString().trim());
        datos.addProperty("apellido", inputApellido.getText().toString().trim());
        datos.addProperty("pais", inputPais.getSelectedItem().toString());
        datos.addProperty("fecha_nacimiento", inputFechaNacimiento.getText().toString().trim());

        if (nuevaUrlImagen != null) {
            datos.addProperty("profile_image_url", nuevaUrlImagen);
        }

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .patch(RequestBody.create(
                        gson.toJson(datos),
                        MediaType.parse("application/json")))
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + obtenerTokenUsuario())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error actualizando datos: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String obtenerTokenUsuario() {
        String token = sessionManager.getUserToken();
        if (token == null) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Sesión expirada, por favor vuelve a iniciar sesión", Toast.LENGTH_LONG).show();
                sessionManager.logout();
                finish();
            });
        }
        return token;
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
        Log.e(TAG, mensaje);
    }
}
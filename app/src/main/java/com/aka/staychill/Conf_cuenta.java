package com.aka.staychill;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    // Dependencias
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private ArrayAdapter<CharSequence> adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_cuenta);

        inicializarComponentes();
        configurarSpinner();
        verificarUsuario();
    }

    private void inicializarComponentes() {
        fotoPerfil = findViewById(R.id.fotoPerfil);
        inputNombre = findViewById(R.id.inputNombre);
        inputApellido = findViewById(R.id.inputApellido);
        inputFechaNacimiento = findViewById(R.id.inputFechaNacimiento);
        inputPais = findViewById(R.id.inputPais);

        Button btnGuardar = findViewById(R.id.botoncuenta);
        btnGuardar.setOnClickListener(v -> guardarCambios());

        fotoPerfil.setOnClickListener(v -> manejarPermisosImagen());
    }

    private void configurarSpinner() {
        adapter = ArrayAdapter.createFromResource(this,
                R.array.paises,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPais.setAdapter(adapter);
    }

    private void verificarUsuario() {
        userId = obtenerUserId();
        if (userId == null) {
            mostrarError("Usuario no autenticado");
            finish();
            return;
        }
        cargarDatosUsuario();
    }

    private String obtenerUserId() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }

    private void cargarDatosUsuario() {
        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mostrarError("Error del servidor: " + response.code());
                    return;
                }

                try {
                    JsonArray jsonArray = gson.fromJson(response.body().string(), JsonArray.class);
                    if (jsonArray.size() == 0) {
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
                // Imagen de perfil
                if (campoValido(usuario, "profile_image_url")) {
                    Glide.with(this)
                            .load(usuario.get("profile_image_url").getAsString())
                            .error(R.drawable.img_usuario) // Imagen por defecto
                            .into(fotoPerfil);
                }

                // Campos de texto
                inputNombre.setText(obtenerValorSeguro(usuario, "nombre"));
                inputApellido.setText(obtenerValorSeguro(usuario, "apellido"));
                inputFechaNacimiento.setText(obtenerValorSeguro(usuario, "fecha_nacimiento"));

                // Spinner de país
                if (campoValido(usuario, "pais")) {
                    String pais = usuario.get("pais").getAsString();
                    int posicion = adapter.getPosition(pais);
                    inputPais.setSelection(posicion >= 0 ? posicion : 0);
                }
            } catch (Exception e) {
                mostrarError("Error actualizando interfaz: " + e.getMessage());
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
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            Uri imagenUri = data.getData();
            if (imagenUri != null) {
                fotoPerfil.setImageURI(imagenUri);
                subirNuevaImagen(imagenUri);
            }
        }
    }

    private void subirNuevaImagen(Uri imagenUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenUri);
            File archivoTemp = new File(getCacheDir(), "temp_image.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(archivoTemp));

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", userId + "_profile.png",
                            RequestBody.create(archivoTemp, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url(SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/user_files/" + userId + "_profile.png")
                    .post(requestBody)
                    .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + obtenerTokenUsuario())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mostrarError("Error subiendo imagen: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        actualizarUrlImagenPerfil();
                    } else {
                        mostrarError("Error del servidor: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            mostrarError("Error procesando imagen: " + e.getMessage());
        }
    }

    private String obtenerTokenUsuario() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("user_token", SupabaseConfig.getSupabaseKey());
    }

    private void actualizarUrlImagenPerfil() {
        String nuevaUrl = SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/public/user_files/" + userId + "_profile.png";

        JsonObject datosActualizacion = new JsonObject();
        datosActualizacion.addProperty("profile_image_url", nuevaUrl);

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .patch(RequestBody.create(
                        gson.toJson(datosActualizacion),
                        MediaType.parse("application/json")))
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mostrarError("Error actualizando perfil: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Glide.with(Conf_cuenta.this).load(nuevaUrl).into(fotoPerfil));
                }
            }
        });
    }

    private void guardarCambios() {
        // Implementar lógica para guardar otros campos
        Toast.makeText(this, "Guardando cambios...", Toast.LENGTH_SHORT).show();
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
        Log.e(TAG, mensaje);
    }
}
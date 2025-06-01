// ReportProblemActivity.java
package com.aka.staychill;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Conf_reportar extends AppCompatActivity {

    private EditText descInput;
    private ImageView imgPlaceholder;
    private Button submitButton;
    private SessionManager sessionManager;

    // Nombre de tu bucket público
    private static final String STORAGE_BUCKET = "imagen-problema";

    // URI de la imagen seleccionada
    private Uri imageUri;

    // Launcher para la galería
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_reportar);

        descInput      = findViewById(R.id.descInput);
        imgPlaceholder = findViewById(R.id.imgPlaceholder);
        submitButton   = findViewById(R.id.submitButton);
        sessionManager = new SessionManager(this);

        // Si no hay sesión válida, cerramos
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1) Configuramos el launcher para escoger imagen
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            imgPlaceholder.setImageURI(imageUri);
                        }
                    }
                }
        );

        // 2) Click en el placeholder → pedir permiso o abrir galería
        imgPlaceholder.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                        1001
                );
            } else {
                openGallery();
            }
        });

        // 3) Click en Enviar → validamos y subimos
        submitButton.setOnClickListener(v -> {
            String desc = descInput.getText().toString().trim();
            if (desc.isEmpty()) {
                Toast.makeText(this, "Describe el problema", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageUri == null) {
                Toast.makeText(this, "Agrega una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImageToSupabase(imageUri, new UploadCallback() {
                @Override
                public void onSuccess(String publicUrl) {
                    insertProblem(desc, publicUrl, new InsertCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(Conf_reportar.this,
                                        "Reporte enviado 😊", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() -> Toast.makeText(
                                    Conf_reportar.this,
                                    "Error al guardar reporte: " + errorMessage,
                                    Toast.LENGTH_LONG
                            ).show());
                        }
                    });
                }
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(
                            Conf_reportar.this,
                            "Error al subir imagen: " + errorMessage,
                            Toast.LENGTH_LONG
                    ).show());
                }
            });
        });
    }

    // Abre la galería
    private void openGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galleryLauncher.launch(intent);
    }

    // Manejo de permisos
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this,
                    "Permiso requerido para seleccionar imagen",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------
    // 1) Subida de imagen a Supabase Storage
    // ---------------------------
    private void uploadImageToSupabase(
            Uri uri,
            UploadCallback callback
    ) {
        try {
            // Leer bitmap y convertir a JPEG
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), uri
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();

            // Generar nombre único
            String fileName = "reports/" + UUID.randomUUID().toString() + ".jpg";
            String uploadUrl = SupabaseConfig.getSupabaseUrl()
                    + "/storage/v1/object/" + STORAGE_BUCKET + "/" + fileName;

            RequestBody body = RequestBody.create(
                    imageBytes,
                    MediaType.parse("image/jpeg")
            );

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .put(body)
                    .header("apikey", SupabaseConfig.getSupabaseKey())
                    .header("Authorization",
                            "Bearer " + sessionManager.getAccessToken()
                    )
                    .build();

            SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onError(e.getLocalizedMessage());
                }
                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response)
                        throws IOException {
                    if (response.isSuccessful()) {
                        String publicUrl = SupabaseConfig.getSupabaseUrl()
                                + "/storage/v1/object/public/"
                                + STORAGE_BUCKET + "/" + fileName;
                        callback.onSuccess(publicUrl);
                    } else {
                        callback.onError("HTTP " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // ---------------------------
    // 2) Insertar registro en usuario_problemas
    // ---------------------------
    private void insertProblem(
            String description,
            String imageUrl,
            InsertCallback callback
    ) {
        String insertUrl = SupabaseConfig.getSupabaseUrl()
                + "/rest/v1/usuario_problemas";

        List<ProblemRow> rows = new ArrayList<>();
        rows.add(new ProblemRow(description, imageUrl));
        String json = new Gson().toJson(rows);

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(insertUrl)
                .post(body)
                .header("apikey", SupabaseConfig.getSupabaseKey())
                .header("Authorization",
                        "Bearer " + sessionManager.getAccessToken()
                )
                .header("Content-Type", "application/json")
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getLocalizedMessage());
            }
            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("HTTP " + response.code());
                }
            }
        });
    }

    // Modelo para Gson
    private static class ProblemRow {
        String problema;
        String imagen_problema;
        ProblemRow(String p, String img) {
            this.problema      = p;
            this.imagen_problema = img;
        }
    }

    // Callbacks
    private interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(String errorMessage);
    }
    private interface InsertCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}

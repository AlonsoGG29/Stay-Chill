package com.aka.staychill;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Conf_cuenta extends AppCompatActivity {
    private EditText inputNombre;
    private EditText inputCorreoElectronico;
    private EditText inputNuevoCorreo;
    private EditText inputContraseniaActual;
    private EditText inputNuevaContrasenia;
    private EditText inputRepetirNuevaContrasenia;
    private ImageView fotoPerfil;
    private UserProfileManager profileManager;
    private Uri profileImageUri;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    fotoPerfil.setImageURI(profileImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_cuenta);

        profileManager = new UserProfileManager();

        // Inicializar vistas
        inputNombre = findViewById(R.id.inputNombre);
        inputCorreoElectronico = findViewById(R.id.inputCorreoActual);
        inputNuevoCorreo = findViewById(R.id.inputNuevoCorreo);
        inputContraseniaActual = findViewById(R.id.inputContraseniaActual);
        inputNuevaContrasenia = findViewById(R.id.inputNuevaContrasenia);
        inputRepetirNuevaContrasenia = findViewById(R.id.inputRepetirNuevaContrasenia);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        Button botonGuardar = findViewById(R.id.botoncuenta);

        // Obtener el userId de SharedPreferences
        String userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_id", null);
        if (userId != null) {
            Log.d("Conf_cuenta", "Usuario autenticado con userId: " + userId);
            cargarDatosPerfil(userId);
        } else {
            Toast.makeText(this, "Error: usuario no autenticado. Por favor, regístrate.", Toast.LENGTH_SHORT).show();
            Log.e("Conf_cuenta", "Usuario no autenticado");
            startActivity(new Intent(this, Signup.class));
        }

        // Listener para seleccionar imagen de perfil
        fotoPerfil.setOnClickListener(v -> seleccionarImagenPerfil());

        // Listener para guardar datos del perfil
        botonGuardar.setOnClickListener(v -> guardarDatosPerfil(userId));
    }

    private void cargarDatosPerfil(String userId) {
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?id=eq." + userId;
        String apiKey = SupabaseConfig.getSupabaseKey();

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .get()
                .build();

        OkHttpClient client = SupabaseConfig.getClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Conf_cuenta.this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        runOnUiThread(() -> {
                            try {
                                inputNombre.setText(jsonObject.optString("nombre"));
                                inputCorreoElectronico.setText(jsonObject.optString("email"));
                                String profileImageUrl = jsonObject.optString("profile_image_url");
                                if (!profileImageUrl.isEmpty()) {
                                    Glide.with(Conf_cuenta.this).load(profileImageUrl).into(fotoPerfil);
                                }
                            } catch (Exception e) {  // Corregir el bloque de captura de excepción
                                e.printStackTrace();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Conf_cuenta.this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void seleccionarImagenPerfil() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        seleccionarImagenLauncher.launch(intent);
    }

    private void guardarImagenPerfil(String userId) {
        if (profileImageUri != null && userId != null) {
            profileManager.subirImagenPerfil(userId, profileImageUri, (uri, e) -> {
                runOnUiThread(() -> {
                    if (e == null) {
                        Map<String, Object> profileData = new HashMap<>();
                        profileData.put("profile_image_url", uri.toString());
                        profileManager.guardarPerfilUsuario(userId, profileData);
                        Toast.makeText(Conf_cuenta.this, "Imagen de perfil guardada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Conf_cuenta.this, "Error al subir imagen de perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Conf_cuenta", "Error al subir imagen de perfil: " + e.getMessage());
                    }
                });
            }, this);
        }
    }

    private void guardarDatosPerfil(String userId) {
        String nombre = inputNombre.getText().toString();
        String correoActual = inputCorreoElectronico.getText().toString();
        String nuevoCorreo = inputNuevoCorreo.getText().toString();
        String contraseniaActual = inputContraseniaActual.getText().toString();
        String nuevaContrasenia = inputNuevaContrasenia.getText().toString();
        String repetirNuevaContrasenia = inputRepetirNuevaContrasenia.getText().toString();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("nombre", nombre);
        profileData.put("email", correoActual);

        // Guardar los datos del perfil
        profileManager.guardarPerfilUsuario(userId, profileData);

        // Cambiar correo electrónico si se ha proporcionado uno nuevo
        if (!nuevoCorreo.isEmpty()) {
            cambiarCorreo(userId, correoActual, nuevoCorreo, contraseniaActual);
        }

        // Cambiar contraseña si se ha proporcionado una nueva y coinciden
        if (!nuevaContrasenia.isEmpty() && nuevaContrasenia.equals(repetirNuevaContrasenia)) {
            cambiarContrasenia(userId, contraseniaActual, nuevaContrasenia);
        }

        Toast.makeText(this, "Datos del perfil guardados", Toast.LENGTH_SHORT).show();
    }

    private void cambiarCorreo(String userId, String correoActual, String nuevoCorreo, String contraseniaActual) {
        // Implementar lógica para cambiar el correo electrónico
        // Asegúrate de autenticar al usuario con la contraseña actual antes de cambiar el correo
    }

    private void cambiarContrasenia(String userId, String contraseniaActual, String nuevaContrasenia) {
        // Implementar lógica para cambiar la contraseña
        // Asegúrate de autenticar al usuario con la contraseña actual antes de cambiar la contraseña
    }
}

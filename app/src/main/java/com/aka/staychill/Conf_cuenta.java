package com.aka.staychill;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Conf_cuenta extends AppCompatActivity {
    private EditText inputNombre;
    private EditText inputCorreoElectronico;
    private EditText inputFechaNacimiento;
    private Spinner inputPais;
    private ImageView fotoPerfil;
    private UserProfileManager profileManager;
    private Uri profileImageUri;

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
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
        inputFechaNacimiento = findViewById(R.id.inputFechaNacimiento);
        inputPais = findViewById(R.id.inputPais);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        Button botonGuardar = findViewById(R.id.botoncuenta);

        // Inicializar el Spinner de países
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.paises, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPais.setAdapter(adapter);

        // Obtener el userId del usuario actual
        String userId = getIntent().getStringExtra("USER_ID"); // Asegúrate de pasar el userId a esta actividad
        if (userId != null) {
            cargarDatosPerfil(userId);
        } else {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        // Listener para seleccionar imagen de perfil
        fotoPerfil.setOnClickListener(v -> seleccionarImagenPerfil());

        // Listener para guardar datos del perfil
        botonGuardar.setOnClickListener(v -> guardarDatosPerfil(userId));
    }

    private void cargarDatosPerfil(String userId) {
        profileManager.getUserProfile(userId, (profile, e) -> {
            if (e == null && profile != null) {
                inputNombre.setText(profile.optString("nombre"));
                inputCorreoElectronico.setText(profile.optString("email"));
                inputFechaNacimiento.setText(profile.optString("fechaNacimiento"));
                String profileImageUrl = profile.optString("profileImageUrl");
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this).load(profileImageUrl).into(fotoPerfil);
                }
            } else {
                Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seleccionarImagenPerfil() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }

    private void guardarDatosPerfil(String userId) {
        String nombre = inputNombre.getText().toString();
        String email = inputCorreoElectronico.getText().toString();
        String fechaNacimiento = inputFechaNacimiento.getText().toString();
        Object selectedPais = inputPais.getSelectedItem(); // Obtener el objeto seleccionado del spinner

        if (selectedPais == null) {
            Toast.makeText(this, "Por favor, selecciona un país", Toast.LENGTH_SHORT).show();
            return;
        }

        String pais = selectedPais.toString(); // Convertir a String si no es null

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("nombre", nombre);
        profileData.put("email", email);
        profileData.put("fechaNacimiento", fechaNacimiento);
        profileData.put("pais", pais);

        profileManager.saveUserProfile(userId, profileData);

        if (profileImageUri != null) {
            profileManager.uploadProfileImage(userId, profileImageUri, new UserProfileManager.ProfileImageCallback() {
                @Override
                public void onComplete(Uri uri, Exception e) {
                    if (e == null) {
                        profileData.put("profileImageUrl", uri.toString());
                        profileManager.saveUserProfile(userId, profileData);
                        Toast.makeText(Conf_cuenta.this, "Datos del perfil guardados", Toast.LENGTH_SHORT).show();
                        // Regresar a la pantalla de configuración
                        startActivity(new Intent(Conf_cuenta.this, Conf_cuenta.class));
                    } else {
                        Toast.makeText(Conf_cuenta.this, "Error al subir imagen de perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Datos del perfil guardados", Toast.LENGTH_SHORT).show();
            // Regresar a la pantalla de configuración
            startActivity(new Intent(this, Conf_cuenta.class));
        }
    }
}

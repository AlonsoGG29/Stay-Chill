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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Conf_cuenta extends AppCompatActivity {
    private EditText inputNombre;
    private EditText inputCorreoElectronico;
    private EditText inputFechaNacimiento;
    private Spinner inputPais;
    private ImageView fotoPerfil;
    private FirebaseUser currentUser;
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

        // Inicializar Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        profileManager = new UserProfileManager();

        // Inicializar vistas
        inputNombre = findViewById(R.id.inputNombre);
        inputCorreoElectronico = findViewById(R.id.inputCorreoElectronico);
        inputFechaNacimiento = findViewById(R.id.inputFechaNacimiento);
        inputPais = findViewById(R.id.inputPais);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        Button botonGuardar = findViewById(R.id.botoncuenta);

        // Inicializar el Spinner de países
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.paises, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPais.setAdapter(adapter);

        // Cargar datos del perfil si ya existen
        if (currentUser != null) {
            cargarDatosPerfil(currentUser.getUid());
        }

        // Listener para seleccionar imagen de perfil
        fotoPerfil.setOnClickListener(v -> seleccionarImagenPerfil());

        // Listener para guardar datos del perfil
        botonGuardar.setOnClickListener(v -> guardarDatosPerfil());
    }

    private void cargarDatosPerfil(String userId) {
        profileManager.getUserProfile(userId, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    inputNombre.setText(document.getString("nombre"));
                    inputCorreoElectronico.setText(document.getString("email"));
                    inputFechaNacimiento.setText(document.getString("fechaNacimiento"));
                    String profileImageUrl = document.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this).load(profileImageUrl).into(fotoPerfil);
                    }
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

    private void guardarDatosPerfil() {
        String nombre = inputNombre.getText().toString();
        String email = inputCorreoElectronico.getText().toString();
        String fechaNacimiento = inputFechaNacimiento.getText().toString();
        Object selectedPais = inputPais.getSelectedItem(); // Obtener el objeto seleccionado del spinner

        if (selectedPais == null) {
            Toast.makeText(this, "Por favor, selecciona un país", Toast.LENGTH_SHORT).show();
            return;
        }

        String pais = selectedPais.toString(); // Convertir a String si no es null

        if (currentUser != null) {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("nombre", nombre);
            profileData.put("email", email);
            profileData.put("fechaNacimiento", fechaNacimiento);
            profileData.put("pais", pais);

            profileManager.saveUserProfile(currentUser.getUid(), profileData);

            if (profileImageUri != null) {
                profileManager.uploadProfileImage(currentUser.getUid(), profileImageUri, uri -> {
                    profileData.put("profileImageUrl", uri.toString());
                    profileManager.saveUserProfile(currentUser.getUid(), profileData);
                    Toast.makeText(this, "Datos del perfil guardados", Toast.LENGTH_SHORT).show();
                    // Regresar a la pantalla de configuración
                    startActivity(new Intent(this, Conf_cuenta.class));
                }, e -> Toast.makeText(this, "Error al subir imagen de perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Datos del perfil guardados", Toast.LENGTH_SHORT).show();
                // Regresar a la pantalla de configuración
                startActivity(new Intent(this, Conf_cuenta.class));
            }
        }
    }
}

package com.aka.staychill;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Conf_privacidad extends AppCompatActivity {

    private static final String TAG = "Conf_privacidad";
    private SessionManager sessionManager;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_privacidad);

        // Ajuste para evitar solapamientos con la barra de estado o navegación.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_conf_privacidad), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        Log.d(TAG, "Activity iniciada. SessionManager inicializado.");

        findViewById(R.id.btnBack).setOnClickListener( v -> {
            runOnUiThread(() -> {
                Intent intent = new Intent(this, Main_bn.class);
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                );
                intent.putExtra("start_tab", 4);
                startActivity(intent);
                finish();
            });
        });

        // Opción: Cambiar correo.
        findViewById(R.id.opcionCambiarCorreo).setOnClickListener(v -> {
            showInputDialog("Cambiar correo", "Ingresa el nuevo correo",
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, new InputDialogCallback() {
                        @Override
                        public void onInputEntered(String newEmail) {
                            Log.d(TAG, "Nuevo email ingresado: " + newEmail);
                            showInputDialog("Confirmación", "Ingresa tu contraseña actual",
                                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, new InputDialogCallback() {
                                        @Override
                                        public void onInputEntered(String currentPassword) {
                                            Log.d(TAG, "Contraseña para cambiar email ingresada: " + (currentPassword != null ? "********" : "null"));
                                            try {
                                                JSONObject payload = new JSONObject();
                                                payload.put("user_id", sessionManager.getUserIdString());
                                                payload.put("email", newEmail);
                                                payload.put("current_password", currentPassword);
                                                Log.d(TAG, "Payload para actualizar email: " + payload.toString());
                                                callEdgeFunction(payload, new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        Log.e(TAG, "Fallo en llamada a Edge Function (actualizar email): ", e);
                                                        runOnUiThread(() ->
                                                                Toast.makeText(Conf_privacidad.this, "Error al actualizar el correo", Toast.LENGTH_SHORT).show());
                                                    }
                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        String respStr = response.body().string();
                                                        Log.d(TAG, "Respuesta actualizar email: Código " + response.code() + " - " + respStr);
                                                        runOnUiThread(() -> {
                                                            if (response.isSuccessful()) {
                                                                Toast.makeText(Conf_privacidad.this, "Correo actualizado exitosamente", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(Conf_privacidad.this, "Error al actualizar el correo", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                Log.e(TAG, "Excepción JSON en payload actualizar email", e);
                                            }
                                        }
                                    });
                        }
                    });
        });

        // Opción: Cambiar contraseña.
        findViewById(R.id.opcionCambiarContrasenia).setOnClickListener(v -> {
            showInputDialog("Cambiar contraseña", "Ingresa la nueva contraseña",
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, new InputDialogCallback() {
                        @Override
                        public void onInputEntered(String newPassword) {
                            Log.d(TAG, "Nueva contraseña ingresada: " + (newPassword != null ? "********" : "null"));
                            showInputDialog("Confirmación", "Ingresa tu contraseña actual",
                                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, new InputDialogCallback() {
                                        @Override
                                        public void onInputEntered(String currentPassword) {
                                            Log.d(TAG, "Contraseña actual para cambio ingresada: " + (currentPassword != null ? "********" : "null"));
                                            try {
                                                JSONObject payload = new JSONObject();
                                                payload.put("user_id", sessionManager.getUserIdString());
                                                payload.put("password", newPassword);
                                                payload.put("current_password", currentPassword);
                                                Log.d(TAG, "Payload para actualizar contraseña: " + payload.toString());
                                                callEdgeFunction(payload, new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        Log.e(TAG, "Fallo en llamada a Edge Function (actualizar contraseña): ", e);
                                                        runOnUiThread(() ->
                                                                Toast.makeText(Conf_privacidad.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show());
                                                    }
                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        String respStr = response.body().string();
                                                        Log.d(TAG, "Respuesta actualizar contraseña: Código " + response.code() + " - " + respStr);
                                                        runOnUiThread(() -> {
                                                            if (response.isSuccessful()) {
                                                                Toast.makeText(Conf_privacidad.this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(Conf_privacidad.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                Log.e(TAG, "Excepción JSON en payload actualizar contraseña", e);
                                            }
                                        }
                                    });
                        }
                    });
        });

        // Opción: Borrar cuenta.
        findViewById(R.id.opcionBorrarCuenta).setOnClickListener(v -> {
            showInputDialog("Borrar cuenta", "Ingresa tu contraseña actual para confirmar",
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, new InputDialogCallback() {
                        @Override
                        public void onInputEntered(String currentPassword) {
                            Log.d(TAG, "Contraseña para borrar cuenta ingresada: " + (currentPassword != null ? "********" : "null"));
                            new AlertDialog.Builder(Conf_privacidad.this)
                                    .setTitle("Confirmación")
                                    .setMessage("¿Estás seguro de borrar tu cuenta?")
                                    .setPositiveButton("Eliminar", (dialog, which) -> {
                                        try {
                                            JSONObject payload = new JSONObject();
                                            payload.put("user_id", sessionManager.getUserIdString());
                                            payload.put("deleteAccount", true);
                                            payload.put("current_password", currentPassword);
                                            Log.d(TAG, "Payload para borrar cuenta: " + payload.toString());
                                            callEdgeFunction(payload, new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    Log.e(TAG, "Fallo en llamada a Edge Function (borrar cuenta): ", e);
                                                    runOnUiThread(() ->
                                                            Toast.makeText(Conf_privacidad.this, "Error al borrar la cuenta", Toast.LENGTH_SHORT).show());
                                                }
                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String respStr = response.body().string();
                                                    Log.d(TAG, "Respuesta borrar cuenta: Código " + response.code() + " - " + respStr);
                                                    runOnUiThread(() -> {
                                                        if (response.isSuccessful()) {
                                                            Toast.makeText(Conf_privacidad.this, "Cuenta borrada exitosamente. Saliendo de la aplicación...", Toast.LENGTH_SHORT).show();
                                                            finishAffinity();
                                                        } else {
                                                            Toast.makeText(Conf_privacidad.this, "Error al borrar la cuenta. Verifica tu contraseña.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                        } catch (JSONException e) {
                                            Log.e(TAG, "Excepción JSON en payload borrar cuenta", e);
                                        }
                                    })
                                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    });
        });

        // Opción: Aviso Legal.
        findViewById(R.id.opcionAvisoLegal).setOnClickListener(v -> {
            new AlertDialog.Builder(Conf_privacidad.this)
                    .setTitle("Aviso Legal")
                    .setMessage("AVISO LEGAL\n\nLa aplicación \"Stay Chill\" es un proyecto de fin de curso desarrollado exclusivamente con fines educativos y de demostración. No representa un producto o servicio comercial, ni está destinada a ser utilizada en entornos reales sin el adecuado proceso de validación.\n\nToda la información, funcionalidades y contenidos incluidos son simulados y se proporcionan sin garantías expresas o implícitas respecto a su veracidad, integridad o actualidad. El desarrollador no se hace responsable de cualquier error, omisión o interpretación errónea que pudiera derivarse del uso de esta aplicación.\n\nAl utilizar esta aplicación, el usuario asume la plena responsabilidad de su uso y acepta estas condiciones, eximiendo al autor de cualquier responsabilidad legal. Este aviso legal podrá ser modificado en cualquier momento sin previo aviso.")
                    .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Opción: Sobre esta aplicación.
        findViewById(R.id.opcionSobreApp).setOnClickListener(v -> {
            new AlertDialog.Builder(Conf_privacidad.this)
                    .setTitle("Sobre esta aplicación")
                    .setMessage("Stay Chill\n\nEsta aplicación fue creada como parte de un proyecto académico. El objetivo es demostrar la integración de diversas funcionalidades en un entorno simulado. Cualquier parecido con aplicaciones reales es pura coincidencia.")
                    .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    // Se envía la petición a la Edge Function "updateUser" (usando el endpoint correspondiente).
    private void callEdgeFunction(JSONObject payload, Callback callback) {
        String url = SupabaseConfig.getSupabaseUrl() + "/functions/v1/updateUser";
        RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                // Agregamos los encabezados necesarios para que Supabase reciba la autorización
                .header("apikey", SupabaseConfig.getSupabaseKey())
                .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build();
        Log.d(TAG, "Llamando a Edge Function en: " + url);
        Log.d(TAG, "Payload enviado: " + payload.toString());
        client.newCall(request).enqueue(callback);
    }

    // Muestra un diálogo para solicitar input del usuario y devuelve el texto ingresado.
    private void showInputDialog(String title, String message, int inputType, InputDialogCallback callback) {
        final EditText inputField = new EditText(this);
        inputField.setInputType(inputType);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setView(inputField)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String inputText = inputField.getText().toString().trim();
                    Log.d(TAG, "Input recibido (" + title + "): " + (inputText.isEmpty() ? "vacío" : inputText));
                    if (!inputText.isEmpty()) {
                        callback.onInputEntered(inputText);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    interface InputDialogCallback {
        void onInputEntered(String input);
    }
}

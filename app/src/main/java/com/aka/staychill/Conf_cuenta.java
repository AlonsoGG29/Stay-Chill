package com.aka.staychill;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Conf_cuenta extends AppCompatActivity {

    private static final String TAG = "Conf_cuenta";

    private EditText inputNombre, inputCorreoActual, inputNuevoCorreo, inputContraseniaActual,
            inputNuevaContrasenia, inputRepetirNuevaContrasenia, inputFechaNacimiento;
    private Spinner inputPais;
    private ImageView fotoPerfil;
    private Button botonGuardar;

    private OkHttpClient client;
    private Gson gson = new Gson();
    private String userId; // ID del usuario logeado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_cuenta);

        inputNombre = findViewById(R.id.inputNombre);
        inputCorreoActual = findViewById(R.id.inputCorreoActual);
        inputNuevoCorreo = findViewById(R.id.inputNuevoCorreo);
        inputContraseniaActual = findViewById(R.id.inputContraseniaActual);
        inputNuevaContrasenia = findViewById(R.id.inputNuevaContrasenia);
        inputRepetirNuevaContrasenia = findViewById(R.id.inputRepetirNuevaContrasenia);
        inputFechaNacimiento = findViewById(R.id.inputFechaNacimiento);
        inputPais = findViewById(R.id.inputPais);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        botonGuardar = findViewById(R.id.botoncuenta);

        client = SupabaseConfig.getClient();

        // Obtén el ID del usuario logeado
        userId = obtenerIdUsuarioLogeado();

        if (userId == null) {
            Toast.makeText(this, "Error: No se encontró el ID del usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "ID del usuario logeado: " + userId);

        // Rellenar el Spinner con una lista de países
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.paises, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPais.setAdapter(adapter);

        // Obtener los datos del usuario desde Supabase
        obtenerDatosUsuario();

        // Obtener el correo electrónico del usuario desde la tabla de autenticación
        obtenerCorreoUsuario();

        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementar lógica de guardado aquí
                guardarDatosUsuario();
            }
        });
    }

    private String obtenerIdUsuarioLogeado() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getString("user_id", null); // Obtén el UUID del usuario logeado
    }

    private void obtenerDatosUsuario() {
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId;
        Log.d(TAG, "URL de Supabase: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey()) // Añadir encabezado de autorización
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error en la solicitud: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Respuesta de Supabase: " + responseBody);

                    JsonArray jsonArray = gson.fromJson(responseBody, JsonArray.class);
                    if (jsonArray.size() > 0) {
                        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();

                        runOnUiThread(() -> {
                            if (jsonObject.has("nombre") && !jsonObject.get("nombre").isJsonNull()) {
                                inputNombre.setText(jsonObject.get("nombre").getAsString());
                            }
                            if (jsonObject.has("correo_actual") && !jsonObject.get("correo_actual").isJsonNull()) {
                                inputCorreoActual.setText(jsonObject.get("correo_actual").getAsString());
                            }
                            if (jsonObject.has("fecha_nacimiento") && !jsonObject.get("fecha_nacimiento").isJsonNull()) {
                                inputFechaNacimiento.setText(jsonObject.get("fecha_nacimiento").getAsString());
                            }
                            if (jsonObject.has("pais") && !jsonObject.get("pais").isJsonNull()) {
                                String pais = jsonObject.get("pais").getAsString();
                                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) inputPais.getAdapter();
                                int position = adapter.getPosition(pais);
                                inputPais.setSelection(position);
                            }
                            if (jsonObject.has("profile_image_url") && !jsonObject.get("profile_image_url").isJsonNull()) {
                                // Cargar la imagen de perfil
                                String imageUrl = jsonObject.get("profile_image_url").getAsString();
                                Glide.with(Conf_cuenta.this).load(imageUrl).into(fotoPerfil);
                            }
                        });
                    } else {
                        Log.d(TAG, "No se encontraron datos para el usuario.");
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta de Supabase: " + response.code() + " " + response.message());
                    String responseBody = response.body().string();
                    Log.e(TAG, "Cuerpo de la respuesta de error: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void guardarDatosUsuario() {
        String nombre = inputNombre.getText().toString();
        String correoActual = inputCorreoActual.getText().toString();
        String nuevoCorreo = inputNuevoCorreo.getText().toString();
        String contraseniaActual = inputContraseniaActual.getText().toString();
        String nuevaContrasenia = inputNuevaContrasenia.getText().toString();
        String repetirNuevaContrasenia = inputRepetirNuevaContrasenia.getText().toString();
        String fechaNacimiento = inputFechaNacimiento.getText().toString();
        String pais = inputPais.getSelectedItem().toString();

        // Crear el JSON dinámico con los datos a guardar
        JsonObject jsonObject = new JsonObject();
        if (!nombre.isEmpty()) {
            jsonObject.addProperty("nombre", nombre);
        }
        if (!correoActual.isEmpty()) {
            jsonObject.addProperty("correo_actual", correoActual);
        }
        if (!nuevoCorreo.isEmpty()) {
            jsonObject.addProperty("nuevo_correo", nuevoCorreo);
        }
        if (!contraseniaActual.isEmpty() && !nuevaContrasenia.isEmpty() && !repetirNuevaContrasenia.isEmpty()) {
            if (nuevaContrasenia.equals(repetirNuevaContrasenia)) {
                jsonObject.addProperty("contrasenia_actual", contraseniaActual);
                jsonObject.addProperty("nueva_contrasenia", nuevaContrasenia);
            } else {
                Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!fechaNacimiento.isEmpty()) {
            jsonObject.addProperty("fecha_nacimiento", fechaNacimiento);
        }
        if (!pais.isEmpty()) {
            jsonObject.addProperty("pais", pais);
        }

        if (jsonObject.size() == 0) {
            Toast.makeText(getApplicationContext(), "No hay datos para actualizar", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Datos a guardar: " + jsonObject.toString());

        // Realizar la petición PATCH a Supabase para guardar los datos
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId;
        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey())
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error en la solicitud de guardado: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "Código de respuesta de Supabase: " + response.code());
                Log.d(TAG, "Mensaje de respuesta de Supabase: " + response.message());
                Log.d(TAG, "Cuerpo de respuesta de Supabase: " + responseBody);

                if (response.isSuccessful()) {
                    Log.d(TAG, "Datos guardados correctamente.");
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                        obtenerDatosUsuario(); // Recargar los datos del usuario para confirmar visualmente la actualización
                    });
                } else {
                    Log.e(TAG, "Error en la respuesta de Supabase al guardar: " + response.code() + " " + response.message());
                    Log.e(TAG, "Cuerpo de la respuesta de error: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al guardar datos: " + response.message(), Toast.LENGTH_LONG).show());
                }
            }

        });
    }
    private void obtenerCorreoUsuario() {
        String url = SupabaseConfig.getSupabaseUrl() + "/auth/v1/users?id=eq." + userId;
        Log.d(TAG, "URL de Supabase Auth: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error en la solicitud Auth: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al obtener correo", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Respuesta de Supabase Auth: " + responseBody);

                    JsonArray jsonArray = gson.fromJson(responseBody, JsonArray.class);
                    if (jsonArray.size() > 0) {
                        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                        Log.d(TAG, "Usuario autenticado JSON: " + jsonObject.toString());

                        runOnUiThread(() -> {
                            if (jsonObject.has("email") && !jsonObject.get("email").isJsonNull()) {
                                inputCorreoActual.setText(jsonObject.get("email").getAsString());
                            } else {
                                Log.e(TAG, "Campo 'email' no encontrado o nulo");
                            }
                        });
                    } else {
                        Log.e(TAG, "No se encontraron datos de correo para el usuario.");
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta Auth de Supabase: " + response.code() + " " + response.message());
                    String responseBody = response.body().string();
                    Log.e(TAG, "Cuerpo de la respuesta de error: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error al obtener correo", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }







}

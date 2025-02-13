package com.aka.staychill;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

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
import java.io.InputStream;
import java.util.Map;

public class UserProfileManager {
    private final OkHttpClient client;

    public UserProfileManager() {
        client = SupabaseConfig.getClient();
    }

    public void guardarPerfilUsuario(String userId, Map<String, Object> profileData) {
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/profiles";
        String apiKey = SupabaseConfig.getSupabaseKey();

        JSONObject jsonBody = new JSONObject(profileData);
        try {
            jsonBody.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Error guardando el perfil del usuario");
                }
            }
        });
    }

    public void obtenerPerfilUsuario(String userId, PerfilCallback callback) {
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/profiles?user_id=eq." + userId;
        String apiKey = SupabaseConfig.getSupabaseKey();

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onComplete(null, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        callback.onComplete(new JSONObject(responseBody), null);
                    } catch (JSONException e) {
                        callback.onComplete(null, e);
                    }
                } else {
                    callback.onComplete(null, new IOException("Error obteniendo el perfil del usuario"));
                }
            }
        });
    }

    public void subirImagenPerfil(String userId, Uri imageUri, PerfilImagenCallback callback, Context context) {
        try {
            InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            assert imageStream != null;
            byte[] imageBytes = new byte[imageStream.available()];
            imageStream.read(imageBytes);
            imageStream.close();

            String url = SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/storage-bucket/" + userId + ".jpg";
            String apiKey = SupabaseConfig.getSupabaseKey();

            RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));

            Request request = new Request.Builder()
                    .url(url)
                    .header("apikey", apiKey)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "image/jpeg")
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onComplete(null, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        String imageUrl = SupabaseConfig.getSupabaseUrl() + "/storage/v1/object/public/storage-bucket/" + userId + ".jpg";
                        callback.onComplete(Uri.parse(imageUrl), null);
                    } else {
                        callback.onComplete(null, new IOException("Error subiendo la imagen"));
                    }
                }
            });
        } catch (IOException e) {
            callback.onComplete(null, e);
        }
    }

    public interface PerfilCallback {
        void onComplete(JSONObject profile, Exception e);
    }

    public interface PerfilImagenCallback {
        void onComplete(Uri uri, Exception e);
    }
}

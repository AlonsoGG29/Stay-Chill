package com.aka.staychill;

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
import java.util.Map;

public class UserProfileManager {
    private OkHttpClient client;

    public UserProfileManager() {
        client = SupabaseConfig.getClient();
    }

    public void saveUserProfile(String userId, Map<String, Object> profileData) {
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
                    throw new IOException("Error saving user profile");
                }
            }
        });
    }

    public void getUserProfile(String userId, ProfileCallback callback) {
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
                        String responseBody = response.body().string();
                        callback.onComplete(new JSONObject(responseBody), null);
                    } catch (JSONException e) {
                        callback.onComplete(null, e);
                    }
                } else {
                    callback.onComplete(null, new IOException("Error getting user profile"));
                }
            }
        });
    }

    public void uploadProfileImage(String userId, Uri imageUri, ProfileImageCallback callback) {
        // Implementación de la subida de imagen a Supabase Storage o tu método de almacenamiento preferido
    }

    public interface ProfileCallback {
        void onComplete(JSONObject profile, Exception e);
    }

    public interface ProfileImageCallback {
        void onComplete(Uri uri, Exception e);
    }
}

package com.aka.staychill;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SessionManager {

    private static final String PREFS_NAME = "staychill_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, String refreshToken, UUID userId) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId.toString())
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_ACCESS_TOKEN) &&
                prefs.contains(KEY_USER_ID) &&
                isValidUuid(prefs.getString(KEY_USER_ID, null));
    }

    @Nullable
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    @Nullable
    public UUID getUserId() {
        String uuidString = prefs.getString(KEY_USER_ID, null);
        try {
            return uuidString != null ? UUID.fromString(uuidString) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public String getUserIdString() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void logout() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .apply();
    }

    private boolean isValidUuid(String uuid) {
        if (uuid == null) return false;
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void guardarFCMToken( String token) { // ✅ Añade Context como parámetro
        OkHttpClient client = new OkHttpClient();

        JSONObject body = new JSONObject();
        try {
            body.put("fcm_token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Usa el contexto recibido para obtener el token
        String accessToken = getAccessToken(); // ✅ Usa el método existente

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + getUserIdString())
                .patch(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .headers(new Headers.Builder()
                        .add("apikey", SupabaseConfig.getSupabaseKey())
                        .add("Authorization", "Bearer " + accessToken) // ✅ Usa el token de la instancia actual
                        .build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("FCM_ERROR", "Error de red: " + e.getMessage()); // Detalle del error
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("FCM_RESPONSE", "Código: " + response.code()); // Log del código HTTP
                if (!response.isSuccessful()) {
                    Log.e("FCM_ERROR", "Cuerpo de error: " + response.body().string()); // Log del cuerpo
                }
            }
        });
    }
}
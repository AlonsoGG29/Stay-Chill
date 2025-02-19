package com.aka.staychill;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import java.util.UUID;

public class SessionManager {
    private static final String PREFS_NAME = "staychill_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Mét0do mejorado para guardar todos los datos de sesión
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

    // Mét0do para obtener el ID de usuario como String (útil para algunas operaciones)
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
}
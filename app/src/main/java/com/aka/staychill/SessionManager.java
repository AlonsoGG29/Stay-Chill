package com.aka.staychill;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_TOKEN);
    }

    public void saveAuthTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_USER_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserToken() {
        return prefs.getString(KEY_USER_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
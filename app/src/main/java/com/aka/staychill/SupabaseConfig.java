package com.aka.staychill;

import okhttp3.OkHttpClient;

public class SupabaseConfig {
    private static final String SUPABASE_URL = "";
    private static final String SUPABASE_KEY = "";

    private static OkHttpClient client;

    private SupabaseConfig() {
        // Evitar instanciación
    }

    public static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient();
        }
        return client;
    }

    public static String getSupabaseUrl() {
        return SUPABASE_URL;
    }

    public static String getSupabaseKey() {
        return SUPABASE_KEY;
    }
}

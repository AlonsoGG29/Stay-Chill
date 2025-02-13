package com.aka.staychill;

import okhttp3.OkHttpClient;

public class SupabaseConfig {
    private static final String SUPABASE_URL = "https://mvzvxavwzejtxyqsqivs.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im12enZ4YXZ3emVqdHh5cXNxaXZzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkzOTU1NzMsImV4cCI6MjA1NDk3MTU3M30.K9oVAyQstMN2_IQrn8joNhqmHz9zOWop_hNVhkap6hw";

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

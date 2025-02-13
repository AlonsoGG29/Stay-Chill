package com.aka.staychill;

import okhttp3.OkHttpClient;

public class SupabaseConfig {
    private static final String SUPABASE_URL = "https://mvzvxavwzejtxyqsqivs.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im12enZ4YXZ3emVqdHh5cXNxaXZzIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTM5NTU3MywiZXhwIjoyMDU0OTcxNTczfQ.Hko0mSjjkL5YlRmzB3gtXJqQOpW65XnCvk2m57Az8iA";

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

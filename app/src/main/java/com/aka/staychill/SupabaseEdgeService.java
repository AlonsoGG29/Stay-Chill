package com.aka.staychill;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseEdgeService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Envía el payload a la función Edge para actualizar datos o borrar la cuenta.
    public static void updateUserEdge(String userId, JSONObject dataPayload, String jwtToken) {
        String functionUrl = SupabaseConfig.getSupabaseUrl() + "/functions/v1/updateUser";
        try {
            dataPayload.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(dataPayload.toString(), JSON);
        Request request = new Request.Builder()
                .url(functionUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SupabaseEdgeService", "Error en Edge Function: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("SupabaseEdgeService", "Respuesta exitosa: " + response.body().string());
                } else {
                    Log.e("SupabaseEdgeService", "Error en Edge Function: " + response.message());
                }
            }
        });
    }
}

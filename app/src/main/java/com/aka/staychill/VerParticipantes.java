package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aka.staychill.adapters.ParticipantesAdapter;
import com.aka.staychill.types.AsistenciaEvento;
import com.aka.staychill.types.Usuario;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VerParticipantes extends AppCompatActivity {

    private RecyclerView recycler;
    private TextView txtSinParticipantes;
    private ParticipantesAdapter adapter;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_participantes);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.verParticipantesEvento),
                (v, insets) -> {
                    Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                    return insets;
                }
        );

        eventId = getIntent().getStringExtra("EVENT_ID");
        recycler = findViewById(R.id.recyclerParticipantes);
        txtSinParticipantes = findViewById(R.id.sinParticipantes);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        // Aquí se pasa el listener
        // Después de inicializar el RecyclerView y el adapter
        adapter = new ParticipantesAdapter(this, new ArrayList<>());
        adapter.setOnItemClickListener(usuario -> {
            // Aquí obtenemos el uid directamente con getId() de la clase Usuario
            Intent chatIntent = new Intent(VerParticipantes.this, Chat.class);
            chatIntent.putExtra("contacto_id", usuario.getId());
            startActivity(chatIntent);
        });
        recycler.setAdapter(adapter);


        cargarParticipantes();
    }

    private void cargarParticipantes() {
        String url = SupabaseConfig.getSupabaseUrl()
                + "/rest/v1/asistentes_eventos?"
                + "select=usuario_id,"
                + "usuarios!asistentes_eventos_usuario_id_fkey("
                + "foren_uid,nombre,apellido,profile_image_url,pais"
                + ")"
                + "&evento_id=eq." + eventId;

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + new SessionManager(this).getAccessToken())
                .build();

        OkHttpClient client = SupabaseConfig.getClient();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(VerParticipantes.this, "Error de conexión", Toast.LENGTH_SHORT).show()
                );
            }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(VerParticipantes.this, "Error del servidor", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                String body = resp.body().string();
                try {
                    // Parsear el array de AsistenciaEvento y extraer la lista de Usuarios
                    AsistenciaEvento[] arr = new Gson().fromJson(body, AsistenciaEvento[].class);
                    List<Usuario> lista = new ArrayList<>();
                    for (AsistenciaEvento ae : arr) {
                        lista.add(ae.getUsuario());
                    }
                    runOnUiThread(() -> {
                        if (lista.isEmpty()) {
                            txtSinParticipantes.setVisibility(View.VISIBLE);
                            recycler.setVisibility(View.GONE);
                        } else {
                            txtSinParticipantes.setVisibility(View.GONE);
                            recycler.setVisibility(View.VISIBLE);
                            adapter.setParticipantes(lista);
                        }
                    });
                } catch (Exception e) {
                    Log.e("VerParticipantes", "Parse error", e);
                    runOnUiThread(() ->
                            Toast.makeText(VerParticipantes.this, "Error procesando datos", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}

package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuscarUsuario extends AppCompatActivity {
    private RecyclerView vistaReciclada;
    private SearchView buscador;
    private SessionManager sessionManager;
    private SwipeRefreshLayout refrescarLayout;
    private UsuarioAdapter adaptador;
    private OkHttpClient clienteHttp = new OkHttpClient();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_usuario);

        // Inicializar vistas
        vistaReciclada = findViewById(R.id.recyclerView);
        buscador = findViewById(R.id.searchView);
        refrescarLayout = findViewById(R.id.swipeRefresh);

        sessionManager = new SessionManager(this);


        configurarVistaReciclada();
        configurarBuscador();
        configurarRefrescar();
    }

    private void configurarVistaReciclada() {
        vistaReciclada.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new UsuarioAdapter(new ArrayList<>(), new UsuarioAdapter.OnUsuarioClickListener() {
            @Override
            public void onUsuarioClick(String usuarioId) {
                abrirChat(usuarioId);
            }
        });
        vistaReciclada.setAdapter(adaptador);
    }

    private void abrirChat(String usuarioId) {
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("contacto_id", usuarioId); // <- clave correcta
        startActivity(intent);
    }
    private void configurarBuscador() {
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String consulta) {
                buscarUsuarios(consulta);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String nuevoTexto) {
                if (nuevoTexto.length() >= 3) {
                    buscarUsuarios(nuevoTexto);
                }
                return true;
            }
        });
    }

    private void configurarRefrescar() {
        refrescarLayout.setOnRefreshListener(() -> {
            if (buscador.getQuery() != null) {
                buscarUsuarios(buscador.getQuery().toString());
            }
        });
    }

    private void buscarUsuarios(String consulta) {
        String urlSupabase = SupabaseConfig.getSupabaseUrl()+"/rest/v1/usuarios";
        String tokenAcceso = sessionManager.getAccessToken();

        try {
            String consultaCodificada = URLEncoder.encode("%" + consulta + "%", "UTF-8");
            String url = String.format(
                    "%s?select=id,nombre,apellido,profile_image_url&or=(nombre.ilike.%s,apellido.ilike.%s)",
                    urlSupabase,
                    consultaCodificada,
                    consultaCodificada
            );

            Request solicitud = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + tokenAcceso)
                    .build();

            clienteHttp.newCall(solicitud).enqueue(new Callback() {
                @Override
                public void onFailure(Call llamada, IOException e) {
                    runOnUiThread(() -> {
                        refrescarLayout.setRefreshing(false);
                        Toast.makeText(BuscarUsuario.this, "Error de red", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call llamada, Response respuesta) throws IOException {
                    if (respuesta.isSuccessful()) {
                        String json = respuesta.body().string();
                        Log.d("SUPABASE_RESPONSE", json);
                        Usuario[] arregloUsuarios = gson.fromJson(json, Usuario[].class);
                        List<Usuario> usuarios = Arrays.asList(arregloUsuarios);

                        runOnUiThread(() -> {
                            refrescarLayout.setRefreshing(false);
                            adaptador.actualizarLista(usuarios);
                        });
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            Log.e("ENCODING_ERROR", "Error al codificar: " + e.getMessage());
        }
    }
}
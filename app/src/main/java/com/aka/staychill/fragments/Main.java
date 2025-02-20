package com.aka.staychill.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aka.staychill.Evento;
//import com.aka.staychill.EventoAdaptador;
//import com.aka.staychill.EventoManejador;
import com.aka.staychill.EventoDeserializer;
import com.aka.staychill.EventosAdapter;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.SupabaseConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main extends Fragment {

    private RecyclerView recyclerView;
    private EventosAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OkHttpClient client;
    private SessionManager sessionManager;
    // URL de tu endpoint en SupaBase (reemplaza por el de tu proyecto)
    private static final String URL_EVENTOS = SupabaseConfig.getSupabaseUrl()+"/rest/v1/eventos";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventosAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Inicializar el SessionManager para obtener el token de acceso
        sessionManager = new SessionManager(getContext());

        client = new OkHttpClient();

        // Cargar eventos inicialmente
        cargarEventos();

        // Configurar la funcionalidad de "pull-to-refresh"
        swipeRefreshLayout.setOnRefreshListener(() -> cargarEventos());

        return view;
    }

    private void cargarEventos() {
        Request.Builder requestBuilder = new Request.Builder()
                .url(URL_EVENTOS)
                // Si SupaBase requiere la key anónima, se agrega en la cabecera:
                .addHeader("apikey", SupabaseConfig.getSupabaseKey());

        // Añadimos el token de acceso, obtenido de SessionManager
        String accessToken = sessionManager.getAccessToken();
        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Error al cargar eventos", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    // Configurar Gson con el deserializador personalizado para la clase Evento
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Evento.class, new EventoDeserializer(getContext()))
                            .create();
                    Evento[] eventosArray = gson.fromJson(json, Evento[].class);
                    List<Evento> listaEventos = Arrays.asList(eventosArray);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.setEventos(listaEventos);
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }
}
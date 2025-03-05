package com.aka.staychill.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aka.staychill.Evento;
import com.aka.staychill.EventoClick;
import com.aka.staychill.EventoDeserializer;
import com.aka.staychill.EventosAdapter;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.SupabaseConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OtrosEventos extends Fragment {

    private RecyclerView recyclerView;
    private EventosAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noEventos;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otros_eventos, container, false);

        sessionManager = new SessionManager(requireContext());
        recyclerView = view.findViewById(R.id.recyclerOtrosEventos);
        noEventos = view.findViewById(R.id.noEventos);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);

        setupRecyclerView();
        setupRefreshLayout();
        cargarEventos();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventosAdapter(getContext(), new ArrayList<>());
        adapter.setOnItemClickListener(evento -> {
            Intent intent = new Intent(getActivity(), EventoClick.class);
            intent.putExtra("EVENTO_DATA", new Gson().toJson(evento));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            cargarEventos();
            // Ocultar el spinner después de 5 segundos máximo
            new Handler().postDelayed(() -> {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 5000);
        });
    }

    private void cargarEventos() {
        UUID userId = sessionManager.getUserId();
        if (userId == null) {
            mostrarError("com.aka.staychill.Usuario no autenticado");
            return;
        }

        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/eventos?" +
                "select=*,asistentes_eventos!inner(usuario_id),usuarios!creador_id(nombre,apellido,pais,profile_image_url)" +
                "&asistentes_eventos.usuario_id=eq." + userId.toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Cache-Control", "no-cache")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    mostrarError("Error de conexión: " + e.getMessage());
                    Log.e("EventosUnidos", "Error en la solicitud", e);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalles";
                    requireActivity().runOnUiThread(() -> {
                        mostrarError("Error del servidor: " + response.code());
                        Log.e("EventosUnidos", "Código: " + response.code() + " - Respuesta: " + errorBody);
                    });
                    return;
                }

                try {
                    String json = response.body().string();
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Evento.class, new EventoDeserializer(getContext()))
                            .create();

                    Evento[] eventosArray = gson.fromJson(json, Evento[].class);
                    List<Evento> eventos = Arrays.asList(eventosArray);

                    requireActivity().runOnUiThread(() -> {
                        if (eventos.isEmpty()) {
                            noEventos.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            noEventos.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        adapter.setEventos(eventos);
                    });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        mostrarError("Error procesando datos");
                        Log.e("EventosUnidos", "Error parsing JSON", e);
                    });
                }
            }
        });
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
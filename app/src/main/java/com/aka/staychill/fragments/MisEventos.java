package com.aka.staychill.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aka.staychill.CrearEvento;
import com.aka.staychill.types.Evento;
import com.aka.staychill.EventoClick;
import com.aka.staychill.adapters.EventosAdapter;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.Signup;
import com.aka.staychill.SupabaseConfig;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MisEventos extends Fragment {

    private RecyclerView recyclerView;
    private EventosAdapter adapter;
    private TextView noEventos;
    private SessionManager sessionManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_eventos, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerMisEventos);
        noEventos = view.findViewById(R.id.noEventos);
        sessionManager = new SessionManager(requireContext());

        setupAgregarEventosButton(view);
        setupRecyclerView();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearGlideCache();
            loadUserEvents();
        });

        loadUserEvents();

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

    private void clearGlideCache() {
        new Thread(() -> {
            Glide.get(requireContext()).clearDiskCache();
            requireActivity().runOnUiThread(() -> Glide.get(requireContext()).clearMemory());
        }).start();
    }

    private void loadUserEvents() {
        UUID userId = sessionManager.getUserId();
        if (userId == null) return;

        String url = SupabaseConfig.getSupabaseUrl() +
                "/rest/v1/eventos?creador_id=eq." + userId +
                "&select=*,usuarios:creador_id(*)";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Cache-Control", "no-cache")
                .build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Error al cargar eventos", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String json = response.body().string();
                    Gson gson = new Gson();

                    Evento[] eventosArray = gson.fromJson(json, Evento[].class);
                    List<Evento> eventos = Arrays.asList(eventosArray);

                    requireActivity().runOnUiThread(() -> {
                        adapter.setEventos(eventos);
                        noEventos.setVisibility(eventos.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerView.setVisibility(eventos.isEmpty() ? View.GONE : View.VISIBLE); // Mejor consistencia
                        swipeRefreshLayout.setRefreshing(false);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Error del servidor", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void setupAgregarEventosButton(View view) {
        view.findViewById(R.id.agregarEventos).setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                Intent intent = new Intent(getActivity(), CrearEvento.class);
                startActivity(intent);
            } else {
                mostrarDialogoRegistro();
            }
        });
    }

    private void mostrarDialogoRegistro() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Regístrate")
                .setMessage("Para crear un evento, ¡tienes que registrarte!")
                .setPositiveButton("Registrarse", (dialog, which) -> {
                    // Redirigir a actividad de login
                    startActivity(new Intent(getActivity(), Signup.class));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
package com.aka.staychill.fragments; // Clean What can i say?

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aka.staychill.Evento;
import com.aka.staychill.EventoClick;

import com.aka.staychill.EventosAdapter;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.SupabaseConfig;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main extends Fragment {

    private static final String URL_EVENTOS = SupabaseConfig.getSupabaseUrl()
            + "/rest/v1/eventos?select=*,usuarios!creador_id(*),asistentes_eventos(*)";

    private RecyclerView recyclerView;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EventosAdapter adapter;
    private List<Evento> listaCompletaEventos = new ArrayList<>();
    private OkHttpClient client;
    private SessionManager sessionManager;
    private TextView txtRegistroNecesario;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initializeViews(view);
        setupHttpClient();
        setupRecyclerView();
        setupRefreshLayout();
        setupSearchView();
        setupItemClickListener();
        cargarEventos();
        return view;
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        sessionManager = new SessionManager(requireContext());
        txtRegistroNecesario = view.findViewById(R.id.txtRegistroNecesario);

    }

    private void setupHttpClient() {
        client = new OkHttpClient.Builder().cache(null).build();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventosAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearGlideCache();
            cargarEventos();
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarEventos(newText.toLowerCase());
                return true;
            }
        });
    }

    private void setupItemClickListener() {
        adapter.setOnItemClickListener(evento -> {
            Intent intent = new Intent(getActivity(), EventoClick.class);
            intent.putExtra("EVENTO_DATA", new Gson().toJson(evento));
            startActivity(intent);
        });
    }

    private void clearGlideCache() {
        new Thread(() -> {
            Glide.get(requireContext()).clearDiskCache();
            runOnUiThread(() -> Glide.get(requireContext()).clearMemory());
        }).start();
    }

    private void filtrarEventos(String texto) {
        List<Evento> filtrados = new ArrayList<>();

        for (Evento evento : listaCompletaEventos) {
            if (evento.getNombreEvento().toLowerCase().contains(texto) ||
                    evento.getTipoDeEvento().toLowerCase().contains(texto)) {
                filtrados.add(evento);
            }
        }
        adapter.setEventos(texto.isEmpty() ? listaCompletaEventos : filtrados);
    }

    private void cargarEventos() {
        if (!sessionManager.isLoggedIn()) {
            mostrarMensajeRegistro();
            return;
        }
        Request request = buildRequest();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showError("Error al cargar eventos");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    updateEventosList(response);
                } else {
                    showError("Error en la respuesta del servidor");
                }
            }
        });
    }

    private Request buildRequest() {
        Request.Builder builder = new Request.Builder()
                .url(URL_EVENTOS)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Cache-Control", "no-cache");

        String token = sessionManager.getAccessToken();
        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return builder.build();
    }

    private void updateEventosList(Response response) throws IOException {
        String json = response.body().string();
        Gson gson = new Gson();
        Log.d("SupabaseDebug", "Respuesta JSON: " + json);

        Evento[] eventos = gson.fromJson(json, Evento[].class);
        listaCompletaEventos = new ArrayList<>(Arrays.asList(eventos));

        runOnUiThread(() -> {
            adapter.setEventos(listaCompletaEventos);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showError(String mensaje) {

        if (!sessionManager.isLoggedIn()) return;

        runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        });
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }
    private void mostrarMensajeRegistro() {
        getActivity().runOnUiThread(() -> {
            recyclerView.setVisibility(View.GONE);
            txtRegistroNecesario.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

}
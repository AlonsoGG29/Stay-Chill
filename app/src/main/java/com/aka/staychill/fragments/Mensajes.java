package com.aka.staychill.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aka.staychill.BuscarUsuario;
import com.aka.staychill.Chat;
import com.aka.staychill.Conversacion;
import com.aka.staychill.ConversacionesAdapter;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.Signup;
import com.aka.staychill.SupabaseConfig;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Mensajes extends Fragment {
    private RecyclerView recyclerConversaciones;
    private ConversacionesAdapter adapter;
    private SessionManager sessionManager;
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<Intent> buscarUsuarioLauncher;

    private TextView noEventos; // Añade esta variable

    @Override
    public void onResume() {
        super.onResume();
        cargarConversaciones();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mensajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buscarUsuarioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cargarConversaciones();
                    }
                }
        );

        sessionManager = new SessionManager(requireContext());
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerConversaciones = view.findViewById(R.id.recyclerConversacion);
        recyclerConversaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ConversacionesAdapter(new ArrayList<>(), this::abrirChat, requireContext());
        recyclerConversaciones.setAdapter(adapter); // Adjuntar adaptador aquí
        noEventos = view.findViewById(R.id.noEventos);

        view.findViewById(R.id.mandarMensaje).setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                Intent intent = new Intent(getActivity(), BuscarUsuario.class);
                buscarUsuarioLauncher.launch(intent);
            } else {
                mostrarDialogoRegistro();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearGlideCache();
            cargarConversaciones();
        });
    }

    private void cargarConversaciones() {
        if (!sessionManager.isLoggedIn()) {
            actualizarUI(new ArrayList<>());
            return;
        }

        String userId = sessionManager.getUserIdString();
        String token = sessionManager.getAccessToken();

        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones?" +
                "select=id,participante1,participante2,fecha,ultimo_mensaje," +
                "usuario1:participante1(nombre,profile_image_url)," + // Alias "usuario1"
                "usuario2:participante2(nombre,profile_image_url)" +  // Alias "usuario2"
                "&or=(participante1.eq." + userId + ",participante2.eq." + userId + ")" +
                "&order=fecha.desc";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mostrarError("Error de red");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(json);
                        List<Conversacion> listaConversaciones = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            Conversacion conversacion = parsearConversacion(obj, userId);
                            if (conversacion != null) listaConversaciones.add(conversacion);
                        }

                        Collections.sort(listaConversaciones, (c1, c2) -> c2.getFecha().compareTo(c1.getFecha()));
                        actualizarUI(listaConversaciones);
                    } catch (JSONException e) {
                        mostrarError("Error al procesar datos");
                    }
                } else {
                    mostrarError("Error al cargar conversaciones");
                }
            }
        });
    }

    private Conversacion parsearConversacion(JSONObject obj, String userId) throws JSONException {
        String participante1Id = obj.getString("participante1");
        String participante2Id = obj.getString("participante2");

        // Obtener objetos anidados usando los nuevos alias
        JSONObject usuario1 = obj.optJSONObject("usuario1");
        JSONObject usuario2 = obj.optJSONObject("usuario2");

        Conversacion conversacion = new Conversacion();
        conversacion.setId(obj.getString("id"));
        conversacion.setParticipante1(participante1Id);
        conversacion.setParticipante2(participante2Id);

        JSONObject usuarioContacto = participante1Id.equals(userId) ? usuario2 : usuario1;
        if (usuarioContacto == null) return null;

        conversacion.setNombre(usuarioContacto.getString("nombre"));
        conversacion.setFoto(usuarioContacto.optString("profile_image_url", ""));
        conversacion.setUltimoMensaje(obj.optString("ultimo_mensaje", ""));
        conversacion.setFecha(obj.getString("fecha"));
        return conversacion;
    }

    private void actualizarUI(List<Conversacion> nuevasConversaciones) {
        requireActivity().runOnUiThread(() -> {
            adapter.actualizarDatos(nuevasConversaciones);

            if (nuevasConversaciones.isEmpty()) {

                noEventos.setVisibility(View.VISIBLE);
                recyclerConversaciones.setVisibility(View.GONE);
            } else {
                noEventos.setVisibility(View.GONE);
                recyclerConversaciones.setVisibility(View.VISIBLE);
            }

            swipeRefreshLayout.setRefreshing(false);
        });
    }
    private void abrirChat(String conversacionId) {
        Intent intent = new Intent(getActivity(), Chat.class);
        intent.putExtra("conversacion_id", conversacionId);

        // Obtener el ID del otro participante desde los IDs originales
        for (Conversacion conversacion : adapter.getConversaciones()) {
            if (conversacion.getId().equals(conversacionId)) {
                String otroParticipante = sessionManager.getUserIdString().equals(conversacion.getParticipante1())
                        ? conversacion.getParticipante2()
                        : conversacion.getParticipante1();
                intent.putExtra("contacto_id", otroParticipante);
                break;
            }
        }
        startActivity(intent);
    }

    private void mostrarError(String mensaje) {
        if (!sessionManager.isLoggedIn()) return;

        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
        );
        swipeRefreshLayout.setRefreshing(false);
    }


    private void mostrarDialogoRegistro() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Regístrate")
                .setMessage("Para enviar mensajes, debes estar registrado")
                .setPositiveButton("Registrarse", (dialog, which) -> {
                    // Redirigir a actividad de login
                    startActivity(new Intent(getActivity(), Signup.class));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void clearGlideCache() {
        new Thread(() -> {
            Glide.get(getContext()).clearDiskCache();
            getActivity().runOnUiThread(() -> Glide.get(getContext()).clearMemory());
        }).start();
    }
}

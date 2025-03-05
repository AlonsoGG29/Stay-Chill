package com.aka.staychill.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aka.staychill.BuscarUsuario;
import com.aka.staychill.Chat;
import com.aka.staychill.Conversacion;
import com.aka.staychill.ConversacionesAdapter;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.SupabaseConfig;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

    @Override
    public void onResume() {
        super.onResume();
        cargarConversaciones(); // Recargar al volver
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mensajes, container, false);
        nuevoMensanje(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerConversaciones = view.findViewById(R.id.recyclerConversacion);
        recyclerConversaciones.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearGlideCache();
            cargarConversaciones();
        });
    }

    private void cargarConversaciones() {
        String userId = sessionManager.getUserIdString();
        String token = sessionManager.getAccessToken();

        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/conversaciones?" +
                "select=*,usuario1:participante1(foren_uid,nombre,profile_image_url),usuario2:participante2(foren_uid,nombre,profile_image_url)" +
                "&or=(participante1.eq." + userId + ",participante2.eq." + userId + ")" + // ✅ Correcto
                "&order=fecha.desc";

        Log.d("URL_DEBUG", url); // Antes de client.newCall(...)

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
                    Log.d("Mensajes", "JSON recibido: " + json);
                    try {
                        JSONArray jsonArray = new JSONArray(json);
                        List<Conversacion> listaConversaciones = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            // 1. Extraer participantes
                            String participante1 = obj.getString("participante1");
                            String participante2 = obj.getString("participante2");
                            String userId = sessionManager.getUserIdString();

                            // Determinar quién es el contacto
                            String otroParticipanteId = participante1.equals(userId) ? participante2 : participante1;
                            String usuarioKey = participante1.equals(userId) ? "usuario2" : "usuario1";

                            // Validar existencia del usuario
                            if (!obj.has(usuarioKey)) {
                                Log.e("Mensajes", "No se encontró " + usuarioKey);
                                continue;
                            }

                            JSONObject usuario = obj.getJSONObject(usuarioKey);

                            // 4. Validar que el usuario corresponde al participante correcto
                            if (!usuario.getString("foren_uid").equals(otroParticipanteId)) {
                                Log.w("Mensajes", "El usuario no coincide con el participante");
                                continue;
                            }

                            // 5. Construir objeto Conversacion
                            Conversacion conversacion = new Conversacion();
                            conversacion.setContactoId(otroParticipanteId);
                            conversacion.setNombre(usuario.getString("nombre"));
                            conversacion.setFoto(usuario.getString("profile_image_url"));
                            conversacion.setUltimoMensaje(obj.getString("ultimo_mensaje"));
                            conversacion.setFecha(obj.getString("fecha"));

                            listaConversaciones.add(conversacion);
                        }
                        actualizarUI(listaConversaciones);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mostrarError("Error al procesar datos");
                    }
                } else {
                    mostrarError("Error al cargar conversaciones");
                }
            }
        });
    }

    private void actualizarUI(List<Conversacion> conversaciones) {
        requireActivity().runOnUiThread(() -> {
            adapter = new ConversacionesAdapter(
                    conversaciones,
                    this::abrirChat,
                    requireContext()
            );
            recyclerConversaciones.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void abrirChat(String contactoId) {
        Intent intent = new Intent(getActivity(), Chat.class);
        intent.putExtra("contacto_id", contactoId);
        startActivity(intent);
    }

    private void mostrarError(String mensaje) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
        );
        swipeRefreshLayout.setRefreshing(false);
    }

    public void nuevoMensanje(View view) {
        view.findViewById(R.id.agregarEventos).setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), BuscarUsuario.class));
            }
        });
    }

    private void clearGlideCache() {
        new Thread(() -> {
            Glide.get(getContext()).clearDiskCache();
            getActivity().runOnUiThread(() -> Glide.get(getContext()).clearMemory());
        }).start();
    }
}

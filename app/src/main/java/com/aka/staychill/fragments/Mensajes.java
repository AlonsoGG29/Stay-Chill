package com.aka.staychill.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aka.staychill.BuscarUsuario;
import com.aka.staychill.Chat;
import com.aka.staychill.Conf_cuenta;
import com.aka.staychill.Conversacion;
import com.aka.staychill.ConversacionesAdapter;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.SupabaseConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Mensajes extends Fragment {
    private RecyclerView recyclerConversaciones;
    private ConversacionesAdapter adapter;
    private SessionManager sessionManager;
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        recyclerConversaciones = view.findViewById(R.id.recyclerConversacion);
        recyclerConversaciones.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarConversaciones();
    }


    private void cargarConversaciones() {

        String userId = sessionManager.getUserIdString();
        String token = sessionManager.getAccessToken();

        // Construye la URL con parámetros escapados correctamente
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/mensajes?" +
                "select=sender_id,receiver_id,contenido,fecha,usuarios:receiver_id(nombre,apellido,profile_image_url)" + // ⚠️ Relación corregida
                "&or=(sender_id.eq." + userId + ",receiver_id.eq." + userId + ")" +
                "&order=fecha.desc";

        Request request = new Request.Builder()
                .url(url)
                .get() // ⚠️ Cambia a GET
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
                        Conversacion[] conversaciones = gson.fromJson(json, Conversacion[].class);
                        actualizarUI(conversaciones);
                    } catch (JsonSyntaxException e) {
                        mostrarError("Error al procesar datos");
                    }
                } else {
                    mostrarError("Error al cargar conversaciones");
                }
            }
        });
    }

    private void actualizarUI(Conversacion[] conversaciones) {
        requireActivity().runOnUiThread(() -> {
            adapter = new ConversacionesAdapter(
                    Arrays.asList(conversaciones),
                    this::abrirChat,
                    requireContext()
            );
            recyclerConversaciones.setAdapter(adapter);
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
    }

    public void nuevoMensanje(View view){
        view.findViewById(R.id.agregarEventos).setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), BuscarUsuario.class));
            }
        });
    }
}
package com.aka.staychill.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aka.staychill.Conf_cuenta;
import com.aka.staychill.Conf_notificaciones;
import com.aka.staychill.Conf_privacidad;
import com.aka.staychill.Conf_reportar;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.SupabaseConfig;
import com.aka.staychill.Welcome;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Configuracion extends Fragment {

    private SessionManager sessionManager;
    private OkHttpClient client;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sessionManager = new SessionManager(requireContext());
        client = SupabaseConfig.getClient();
        // Infla el diseño del fragmento
        View rootView = inflater.inflate(R.layout.fragment_configuracion, container, false);

        // Configura los listeners de los botones
        configurarListenersBotones(rootView);

        return rootView;
    }

    private void configurarListenersBotones(View rootView) {
        rootView.findViewById(R.id.conf_cuenta).setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), Conf_cuenta.class));
            }
        });

        rootView.findViewById(R.id.conf_privacidad).setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), Conf_privacidad.class));
            }
        });

        rootView.findViewById(R.id.conf_notificaciones).setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), Conf_notificaciones.class));
            }
        });

        rootView.findViewById(R.id.conf_reportar).setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), Conf_reportar.class));
            }
        });

        rootView.findViewById(R.id.conf_cerrar).setOnClickListener(v -> {
            if (getActivity() != null) {
                // Cerrar sesión y navegar a la actividad de bienvenida
                cerrarSesion();
                startActivity(new Intent(getActivity(), Welcome.class));
                getActivity().finish(); // Opcional: cerrar la actividad actual
            }
        });
    }

    private void cerrarSesion() {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            logoutLocal();
            return;
        }

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/auth/v1/logout")
                .post(RequestBody.create(new byte[0], null))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                logoutLocal();
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Error cerrando sesión", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void logoutLocal() {
        sessionManager.logout();
        requireActivity().runOnUiThread(() -> {
            startActivity(new Intent(getActivity(), Welcome.class));
            requireActivity().finish();
        });
    }
}

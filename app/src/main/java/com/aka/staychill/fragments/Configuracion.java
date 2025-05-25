package com.aka.staychill.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aka.staychill.CargarImagenes;
import com.aka.staychill.Conf_cuenta;
import com.aka.staychill.Conf_privacidad;
import com.aka.staychill.R;
import com.aka.staychill.SessionManager;
import com.aka.staychill.Signup;
import com.aka.staychill.SupabaseConfig;
import com.aka.staychill.types.Usuario;
import com.aka.staychill.Welcome;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Configuracion extends Fragment {

    private SessionManager sessionManager;
    private OkHttpClient client;
    private TextView nombreUsuario;
    private ImageView imagenPerfil;

    private CargarImagenes cargarImagenes;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sessionManager = new SessionManager(requireContext());
        cargarImagenes = CargarImagenes.getInstance(requireContext());
        client = SupabaseConfig.getClient();

        View rootView = inflater.inflate(R.layout.fragment_configuracion, container, false);
        nombreUsuario = rootView.findViewById(R.id.nombrePerfil);
        imagenPerfil = rootView.findViewById(R.id.fotoPerfil);


        configurarListenersBotones(rootView);
        cargarDatosUsuario();

        return rootView;
    }
    private void cargarDatosUsuario() {
        if (!sessionManager.isLoggedIn()) return;
        String accessToken = sessionManager.getAccessToken();
        String userId = obtenerUserIdDesdeToken(accessToken);

        if (userId == null) {
            Toast.makeText(getContext(), "Error identificando usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(SupabaseConfig.getSupabaseUrl() + "/rest/v1/usuarios?foren_uid=eq." + userId)
                .addHeader("apikey", SupabaseConfig.getSupabaseKey())
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    Usuario[] usuarios = new Gson().fromJson(jsonData, Usuario[].class);

                    requireActivity().runOnUiThread(() -> {
                        if (usuarios != null && usuarios.length > 0) {
                            Usuario usuario = usuarios[0];
                            actualizarUI(usuario);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void actualizarUI(Usuario usuario) {

        if(usuario.getApellido() != null){
            nombreUsuario.setText(usuario.getNombre() + " " + usuario.getApellido());
        }
        else{
            nombreUsuario.setText(usuario.getNombre());
        }



        if (usuario.getImagenPerfil() != null && !usuario.getImagenPerfil().isEmpty()) {
            cargarImagenes.loadProfileImage(
                    usuario.getImagenPerfil(),
                    imagenPerfil,
                    requireContext()
            );
        } else {
            imagenPerfil.setImageResource(R.drawable.img_default);
        }
    }

    private String obtenerUserIdDesdeToken(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject jsonObject = new Gson().fromJson(payload, JsonObject.class);
            return jsonObject.get("sub").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void configurarListenersBotones(View rootView) {
        rootView.findViewById(R.id.conf_cuenta).setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                mostrarDialogoRegistro();
            }else if (getActivity() != null) {
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
                desarrollo();
                //startActivity(new Intent(getActivity(), Conf_notificaciones.class));
            }
        });

        rootView.findViewById(R.id.conf_reportar).setOnClickListener(v -> {
            if (getActivity() != null) {
                desarrollo();
                //startActivity(new Intent(getActivity(), Conf_reportar.class));
            }
        });

        rootView.findViewById(R.id.conf_cerrar).setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {logoutLocal();}
            if (getActivity() != null) {

                cerrarSesion();
                startActivity(new Intent(getActivity(), Welcome.class));
                getActivity().finish();
            }
        });
    }

    private void cerrarSesion() {
        if (!sessionManager.isLoggedIn()) return;
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
    private void  mostrarDialogoRegistro() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Regístrate")
                .setMessage("Para configurar tu perfil, debes estar registrado")
                .setPositiveButton("Registrarse", (dialog, which) -> {

                    startActivity(new Intent(getActivity(), Signup.class));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void desarrollo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("En desarollo")
                .setMessage("Este apartado se está desarrrollando, ¡disculpe la molestia! \uD83D\uDE4F")
                .setNegativeButton("Aceptar", null)
                .show();
    }
}

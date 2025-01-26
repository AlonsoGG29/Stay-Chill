package com.aka.staychill.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.aka.staychill.Conf_cuenta;
import com.aka.staychill.Conf_notificaciones;
import com.aka.staychill.Conf_privacidad;
import com.aka.staychill.Conf_reportar;
import com.aka.staychill.R;

import com.aka.staychill.Welcome;

public class Configuracion extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el diseño del fragmento
        View rootView = inflater.inflate(R.layout.fragment_configuracion, container, false);

        // Configura los listeners de los botones
        rootView.findViewById(R.id.conf_icon_cuenta).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Conf_cuenta.class))
        );

        rootView.findViewById(R.id.conf_privacidad).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Conf_privacidad.class))
        );

        rootView.findViewById(R.id.conf_notificaciones).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Conf_notificaciones.class))
        );

        rootView.findViewById(R.id.report_problem_section).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Conf_reportar.class))
        );

        rootView.findViewById(R.id.logout_section).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Welcome.class))
        );

        return rootView;
    }
}

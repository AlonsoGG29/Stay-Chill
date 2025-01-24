package com.aka.staychill.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aka.staychill.Cuenta;
import com.aka.staychill.Notificaciones;
import com.aka.staychill.PrivacidadSeguridad;
import com.aka.staychill.R;
import com.aka.staychill.ReportarProblema;
import com.aka.staychill.welcome;

public class configuracion extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el diseño del fragmento
        View rootView = inflater.inflate(R.layout.fragment_configuracion, container, false);

        // Configura los listeners de los botones
        rootView.findViewById(R.id.account_section).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Cuenta.class))
        );

        rootView.findViewById(R.id.privacy_security_section).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PrivacidadSeguridad.class))
        );

        rootView.findViewById(R.id.notifications_section).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Notificaciones.class))
        );

        rootView.findViewById(R.id.report_problem_section).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ReportarProblema.class))
        );

        rootView.findViewById(R.id.logout_section).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), welcome.class))
        );

        return rootView;
    }
}

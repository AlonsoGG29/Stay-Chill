package com.aka.staychill.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.aka.staychill.CrearEvento;
import com.aka.staychill.R;

public class MisEventos extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_eventos, container, false);

        setupAgregarEventosButton(view);

        return view;
    }

    private void setupAgregarEventosButton(View view) {
        ImageButton agregarEventos = view.findViewById(R.id.agregarEventos);
        agregarEventos.setOnClickListener(v -> navigateToCrearEvento());
    }

    private void navigateToCrearEvento() {
        Intent intent = new Intent(getActivity(), CrearEvento.class);
        startActivity(intent);
    }
}
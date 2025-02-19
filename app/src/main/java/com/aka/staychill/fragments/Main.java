package com.aka.staychill.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import java.util.List;

import com.aka.staychill.Evento;
//import com.aka.staychill.EventoAdaptador;
//import com.aka.staychill.EventoManejador;
import com.aka.staychill.R;

public class Main extends Fragment {

    //private EventoAdaptador mAdapter;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        //List<Evento> itemList = EventoManejador.getEventos();
        //mAdapter = new EventoAdaptador(itemList);
        //recyclerView.setAdapter(mAdapter);

        configurarAparienciaSearchView();
        configurarBusqueda();
        configurarClickFondoSearchView();

        return view;
    }

    private void configurarAparienciaSearchView() {
    }

    private void configurarBusqueda() {

    }

    private void configurarClickFondoSearchView() {
        searchView.setOnClickListener(v -> searchView.setIconified(false));
    }
}
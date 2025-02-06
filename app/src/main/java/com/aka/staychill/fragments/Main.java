package com.aka.staychill.fragments;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.aka.staychill.Evento;
import com.aka.staychill.EventoAdaptador;
import com.aka.staychill.R;

import java.util.ArrayList;
import java.util.List;

public class Main extends Fragment {

    private EventoAdaptador mAdapter;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.buscador);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        List<Evento> itemList = new ArrayList<>();
        itemList.add(new Evento("https://i.pinimg.com/736x/15/79/6d/15796d466bf8be86af9c6a9219d87b0d.jpg", "Usuario 1", "https://www.tooltyp.com/wp-content/uploads/2014/10/1900x920-8-beneficios-de-usar-imagenes-en-nuestros-sitios-web.jpg"));
        itemList.add(new Evento("https://imagenes.20minutos.es/files/image_1920_1080/uploads/imagenes/2024/06/24/homelander-antony-starr-en-the-boys.jpeg", "Usuario 2", "https://www.tooltyp.com/wp-content/uploads/2014/10/1900x920-8-beneficios-de-usar-imagenes-en-nuestros-sitios-web.jpg"));
        itemList.add(new Evento("https://upload.wikimedia.org/wikipedia/commons/f/f1/Dwayne_Johnson_2%2C_2013.jpg", "Usuario 3", "https://www.tooltyp.com/wp-content/uploads/2014/10/1900x920-8-beneficios-de-usar-imagenes-en-nuestros-sitios-web.jpg"));

        mAdapter = new EventoAdaptador(itemList);
        recyclerView.setAdapter(mAdapter);

        // Ajusta los colores del texto del SearchView
        ajustarColoresSearchView();

        // Configura el listener de búsqueda
        configurarBusqueda();

        // Configura el listener de clics para el fondo del SearchView
        configurarClickFondoSearchView();

        return view;
    }

    private void ajustarColoresSearchView() {
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black), PorterDuff.Mode.SRC_IN);

        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black), PorterDuff.Mode.SRC_IN);

        ImageView searchHintIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
        searchHintIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black), PorterDuff.Mode.SRC_IN);
    }

    private void configurarBusqueda() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void configurarClickFondoSearchView() {
        searchView.setOnClickListener(v -> searchView.setIconified(false));
    }
}

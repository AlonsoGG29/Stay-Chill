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
import androidx.appcompat.widget.SearchView; // Asegúrate de usar el correcto de androidx

import com.aka.staychill.Evento;
import com.aka.staychill.EventoAdaptador;
import com.aka.staychill.R;

import java.util.ArrayList;
import java.util.List;

public class Main extends Fragment {

    private RecyclerView recyclerView;
    private EventoAdaptador mAdapter;
    private List<Evento> itemList;
    private SearchView searchView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.buscador);

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        itemList = new ArrayList<>();
        itemList.add(new Evento("https://i.pinimg.com/736x/15/79/6d/15796d466bf8be86af9c6a9219d87b0d.jpg", "Usuario 1", "https://www.tooltyp.com/wp-content/uploads/2014/10/1900x920-8-beneficios-de-usar-imagenes-en-nuestros-sitios-web.jpg"));
        itemList.add(new Evento("https://imagenes.20minutos.es/files/image_1920_1080/uploads/imagenes/2024/06/24/homelander-antony-starr-en-the-boys.jpeg", "Usuario 2", "https://www.tooltyp.com/wp-content/uploads/2014/10/1900x920-8-beneficios-de-usar-imagenes-en-nuestros-sitios-web.jpg"));
        itemList.add(new Evento("https://upload.wikimedia.org/wikipedia/commons/f/f1/Dwayne_Johnson_2%2C_2013.jpg", "Usuario 3", "https://www.tooltyp.com/wp-content/uploads/2014/10/1900x920-8-beneficios-de-usar-imagenes-en-nuestros-sitios-web.jpg"));

        mAdapter = new EventoAdaptador(itemList);
        recyclerView.setAdapter(mAdapter);

        // Ajusta los colores del texto del SearchView
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.black)); // Cambia el color del texto
        searchEditText.setHintTextColor(ContextCompat.getColor(getContext(), R.color.black)); // Cambia el color del texto de sugerencia

        // Cambia el color del ícono de la lupa
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);

        // Cambia el color del botón de borrar (la 'X')
        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);

        // Cambia el color del ícono de la sugerencia (si es aplicable)
        ImageView searchHintIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
        searchHintIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false; // No need to handle
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        // Configura el listener de clics para el fondo del SearchView
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false); // Abre el SearchView al hacer clic en el fondo
            }
        });

        return view;
    }

}

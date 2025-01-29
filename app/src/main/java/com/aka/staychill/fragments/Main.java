package com.aka.staychill.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        itemList.add(new Evento("https://m.media-amazon.com/images/I/61In3cwYPDL.jpg", "Luffy Gear 4", "300 €"));
        itemList.add(new Evento("https://wonderlandcomics.es/9633/figuarts-zero-monkey-d-luffy-second-gear-one-piece.jpg", "Luffy Gear 2", "200 €"));
        itemList.add(new Evento("https://cdn.wallapop.com/images/10420/hv/e1/__/c10420p1080647914/i5307034083.jpg?pictureSize=W640", "Luffy Gear 3", "250 €"));

        mAdapter = new EventoAdaptador(itemList);
        recyclerView.setAdapter(mAdapter);

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

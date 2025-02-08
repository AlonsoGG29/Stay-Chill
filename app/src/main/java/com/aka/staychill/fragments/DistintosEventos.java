package com.aka.staychill.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aka.staychill.R;

public class EventosActivity extends AppCompatActivity {

    private TextView misEventos;
    private TextView otrosEventos;
    private View indicadorMisEventos;
    private View indicadorOtrosEventos;
    private ImageButton agregarEventos;
    private TextView noEventos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_distintos_eventos);

        misEventos = findViewById(R.id.misEventos);
        otrosEventos = findViewById(R.id.otrosEventos);
        indicadorMisEventos = findViewById(R.id.indicadorMisEventos);
        indicadorOtrosEventos = findViewById(R.id.indicadorOtrosEventos);
        agregarEventos = findViewById(R.id.agregarEventos);
        noEventos = findViewById(R.id.noEventos);

        misEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarFragmento(new MisEventosFragment());
                indicadorMisEventos.setVisibility(View.VISIBLE);
                indicadorOtrosEventos.setVisibility(View.GONE);
            }
        });

        otrosEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarFragmento(new OtrosEventosFragment());
                indicadorMisEventos.setVisibility(View.GONE);
                indicadorOtrosEventos.setVisibility(View.VISIBLE);
            }
        });

        // Inicialmente mostrar "Mis Eventos"
        misEventos.performClick();
    }

    private void mostrarFragmento(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
package com.aka.staychill.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.aka.staychill.R;
import com.aka.staychill.ui.main.EventosPagerAdapter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Eventos extends Fragment {
    private TextView diaSemana, texto1, texto2;
    private final TextView[] diasSemana = new TextView[7];
    private final TextView[] diasNumeros = new TextView[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventos, container, false);

        inicializarTextViews(view);

        configurarViewPagerTabLayout(view);

        actualizarDiaActual();

        actualizarDiasSemana();

        return view;
    }

    private void inicializarTextViews(View view) {
        diaSemana = view.findViewById(R.id.diaSemana);
        texto1 = view.findViewById(R.id.texto1);
        texto2 = view.findViewById(R.id.texto2);

        diasSemana[0] = view.findViewById(R.id.dia0);
        diasSemana[1] = view.findViewById(R.id.dia1);
        diasSemana[2] = view.findViewById(R.id.dia2);
        diasSemana[3] = view.findViewById(R.id.dia3);
        diasSemana[4] = view.findViewById(R.id.dia4);
        diasSemana[5] = view.findViewById(R.id.dia5);
        diasSemana[6] = view.findViewById(R.id.dia6);

        diasNumeros[0] = view.findViewById(R.id.numero0);
        diasNumeros[1] = view.findViewById(R.id.numero1);
        diasNumeros[2] = view.findViewById(R.id.numero2);
        diasNumeros[3] = view.findViewById(R.id.numero3);
        diasNumeros[4] = view.findViewById(R.id.numero4);
        diasNumeros[5] = view.findViewById(R.id.numero5);
        diasNumeros[6] = view.findViewById(R.id.numero6);
    }

    private void configurarViewPagerTabLayout(View view) {
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        EventosPagerAdapter pagerAdapter = new EventosPagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Mis Eventos");
                    break;
                case 1:
                    tab.setText("Otros Eventos");
                    break;
            }
        }).attach();
    }

    private void actualizarDiaActual() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String diaDeLaSemana = sdf.format(calendar.getTime());
        int diaDelMes = calendar.get(Calendar.DAY_OF_MONTH);

        diaSemana.setText(String.valueOf(diaDelMes));
        texto1.setText(diaDeLaSemana);
        texto2.setText(new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).format(calendar.getTime()));
    }

    private void actualizarDiasSemana() {
        Calendar calendar = Calendar.getInstance();
        int diaDeLaSemana = calendar.get(Calendar.DAY_OF_WEEK);
        int diaDelMes = calendar.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < 7; i++) {
            int indiceDia = (diaDeLaSemana - 4 + i + 7) % 7; // Índice ajustado para que dia3 sea el día actual
            int numeroDia = (diaDelMes - 3 + i + 31) % 31;
            if (numeroDia == 0) {
                numeroDia = 31;
            }
            diasSemana[i].setText(obtenerNombreDia(indiceDia));
            diasNumeros[i].setText(String.valueOf(numeroDia));
        }
    }

    private String obtenerNombreDia(int indiceDia) {
        String[] dias = {"D", "L", "M", "X", "J", "V", "S"};
        return dias[indiceDia];
    }
}
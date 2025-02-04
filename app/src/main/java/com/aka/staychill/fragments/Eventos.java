package com.aka.staychill.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.aka.staychill.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Eventos extends Fragment {
    private TextView diaSemana, texto1, texto2;
    private TextView[] diasSemana = new TextView[7];
    private TextView[] diasNumeros = new TextView[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventos, container, false);

        // Referencias a los TextViews
        diaSemana = view.findViewById(R.id.diaSemana);
        texto1 = view.findViewById(R.id.texto1);
        texto2 = view.findViewById(R.id.texto2);
        diasSemana[0] = view.findViewById(R.id.dia0); // ID de los TextViews de los días de la semana
        diasSemana[1] = view.findViewById(R.id.dia1);
        diasSemana[2] = view.findViewById(R.id.dia2);
        diasSemana[3] = view.findViewById(R.id.dia3); // Día central
        diasSemana[4] = view.findViewById(R.id.dia4);
        diasSemana[5] = view.findViewById(R.id.dia5);
        diasSemana[6] = view.findViewById(R.id.dia6);

        diasNumeros[0] = view.findViewById(R.id.numero0); // ID de los TextViews de los números de los días
        diasNumeros[1] = view.findViewById(R.id.numero1);
        diasNumeros[2] = view.findViewById(R.id.numero2);
        diasNumeros[3] = view.findViewById(R.id.numero3); // Número central
        diasNumeros[4] = view.findViewById(R.id.numero4);
        diasNumeros[5] = view.findViewById(R.id.numero5);
        diasNumeros[6] = view.findViewById(R.id.numero6);

        // Obtener el día actual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String dayOfTheWeek = sdf.format(calendar.getTime());
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Ajustar los TextViews del layout_fecha
        diaSemana.setText(String.valueOf(dayOfMonth));
        texto1.setText(dayOfTheWeek);
        texto2.setText(new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).format(calendar.getTime()));

        // Ajustar los días de la semana y los números
        for (int i = 0; i < 7; i++) {
            int dayIndex = (dayOfWeek - 4 + i + 7) % 7; // Índice ajustado para que dia3 sea el día actual
            int dayNumber = (dayOfMonth - 3 + i + 31) % 31;
            if (dayNumber == 0) {
                dayNumber = 31;
            }
            diasSemana[i].setText(getDayName(dayIndex));
            diasNumeros[i].setText(String.valueOf(dayNumber));
        }

        return view;
    }

    private String getDayName(int dayIndex) {
        String[] days = {"D", "L", "M", "X", "J", "V", "S"};
        return days[dayIndex];
    }
}

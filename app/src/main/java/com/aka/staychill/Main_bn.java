package com.aka.staychill;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.aka.staychill.ui.main.SectionsPagerAdapter;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Main_bn extends AppCompatActivity {

    private MenuItem prevMenuItem;
    private ImageView notificacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bn);


        // Configurar el adaptador para ViewPager
        configurarAdaptadorViewPager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Configurar badges
        configurarBadges(bottomNavigationView);

        // Configurar listener de selección de navegación
        configurarSeleccionNavegacion(bottomNavigationView);

        notificacion = findViewById(R.id.notif_esquina);
        configurarNotificaciones();


    }
    private void configurarNotificaciones() {
        notificacion.setOnClickListener(v -> mostrarDialogoDesarrollo());
    }

    private void mostrarDialogoDesarrollo() {
        new AlertDialog.Builder(this)
                .setTitle("En desarrollo")
                .setMessage("Este apartado se está desarrollando, ¡disculpe la molestia! \uD83D\uDE4F")
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void configurarAdaptadorViewPager() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                MenuItem selectedItem = bottomNavigationView.getMenu().getItem(position);
                selectedItem.setChecked(true);
                prevMenuItem = selectedItem;
                eliminarBadge(bottomNavigationView, selectedItem.getItemId());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void configurarBadges(BottomNavigationView bottomNavigationView) {
        // Configura los badges para cada elemento
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.home);

        badge = bottomNavigationView.getOrCreateBadge(R.id.eventos);

        badge = bottomNavigationView.getOrCreateBadge(R.id.mensaje);

        badge = bottomNavigationView.getOrCreateBadge(R.id.opciones);
    }

    private void configurarSeleccionNavegacion(BottomNavigationView bottomNavigationView) {
        // Configurar listener de selección de navegación
        bottomNavigationView.setOnItemSelectedListener(item -> {
            manejarSeleccionNavegacion(item);
            return true;
        });

    }

    private void manejarSeleccionNavegacion(@NonNull MenuItem item) {
        int position = -1;

        if (item.getItemId() == R.id.home) {
            position = 0; // Página 1
        } else if (item.getItemId() == R.id.eventos) {
            position = 1; // Página 2
        } else if (item.getItemId() == R.id.mensaje) {
            position = 2; // Página 3
        } else if (item.getItemId() == R.id.opciones) {
            position = 3; // Página 4
        }

        if (position != -1) {
            ViewPager viewPager = findViewById(R.id.view_pager);
            viewPager.setCurrentItem(position);
            item.setChecked(true);

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                eliminarBadge(bottomNavigationView, item.getItemId());
            }
        }
    }

    private void eliminarBadge(BottomNavigationView bottomNavigationView, int itemId) {
        BadgeDrawable badge = bottomNavigationView.getBadge(itemId);
        if (badge != null) {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }
}

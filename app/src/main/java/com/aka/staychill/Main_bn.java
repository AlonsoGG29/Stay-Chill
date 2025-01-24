package com.aka.staychill;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.aka.staychill.ui.main.SectionsPagerAdapter;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Main_bn extends AppCompatActivity {

    private MenuItem prevMenuItem;
    private SectionsPagerAdapter sectionsPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bn);

        // Solo pasamos getSupportFragmentManager() al adaptador
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBadges(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            handleNavigationSelection(item, viewPager);
            return true;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                MenuItem selectedItem = bottomNavigationView.getMenu().getItem(position);
                selectedItem.setChecked(true);
                prevMenuItem = selectedItem;
                removeBadge(bottomNavigationView, selectedItem.getItemId());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    private void setupBadges(BottomNavigationView bottomNavigationView) {
        // Configura los badges para cada elemento
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.home);
        badge.setVisible(true);
        badge.setNumber(5);

        badge = bottomNavigationView.getOrCreateBadge(R.id.eventos);
        badge.setVisible(true);
        badge.setNumber(2);

        badge = bottomNavigationView.getOrCreateBadge(R.id.mensaje);
        badge.setVisible(true);

        badge = bottomNavigationView.getOrCreateBadge(R.id.opciones);
        badge.setVisible(false); // Oculto inicialmente
    }

    private void handleNavigationSelection(@NonNull MenuItem item, ViewPager viewPager) {
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
            viewPager.setCurrentItem(position);
            item.setChecked(true);

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                removeBadge(bottomNavigationView, item.getItemId());
            }
        }


        item.setChecked(true);
        viewPager.setCurrentItem(position);

        // Verifica que el objeto bottomNavigationView esté inicializado
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            removeBadge(bottomNavigationView, item.getItemId());
        }
    }

    private void removeBadge(BottomNavigationView bottomNavigationView, int itemId) {
        BadgeDrawable badge = bottomNavigationView.getBadge(itemId);
        if (badge != null) {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }
}
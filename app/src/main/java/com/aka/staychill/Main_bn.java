package com.aka.staychill;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.aka.staychill.ui.main.SectionsPagerAdapter;

public class Main_bn extends AppCompatActivity {
    private static final int REQ_NOTIF = 1001;
    private MenuItem prevMenuItem;
    private ImageView notificacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Pedir permiso POST_NOTIFICATIONS en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                    REQ_NOTIF
            );
        } else {
            suscribirATopics();
        }

        setContentView(R.layout.activity_main_bn);

        // 2) ViewPager + BottomNavigation
        configurarAdaptadorViewPager();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        configurarBadges(bottomNav);
        configurarSeleccionNavegacion(bottomNav);

        // 3) Botón de notificaciones
        notificacion = findViewById(R.id.notif_esquina);
        notificacion.setOnClickListener(v ->
                startActivity(new Intent(this, Notificaciones.class))
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            suscribirATopics();
        }
    }

    private void suscribirATopics() {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();

        String myTopic = "u_" + new SessionManager(this).getUserIdString();
        fm.subscribeToTopic(myTopic)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        Log.e("FCM_MAIN", "subscribe " + myTopic + " failed", task.getException());
                });

        // Log del token para debug
        fm.getToken().addOnSuccessListener(token ->
                Log.d("FCM_MAIN", "Token: " + token)
        );
    }


    private void configurarAdaptadorViewPager() {
        SectionsPagerAdapter adapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int pos, float off, int offPx) {}
            @Override public void onPageSelected(int pos) {
                if (prevMenuItem != null)
                    prevMenuItem.setChecked(false);

                BottomNavigationView nav = findViewById(R.id.bottom_navigation);
                MenuItem item = nav.getMenu().getItem(pos);
                item.setChecked(true);
                prevMenuItem = item;
                eliminarBadge(nav, item.getItemId());
            }
            @Override public void onPageScrollStateChanged(int state) {}
        });
    }

    private void configurarBadges(BottomNavigationView bottomNav) {
        bottomNav.getOrCreateBadge(R.id.home);
        bottomNav.getOrCreateBadge(R.id.eventos);
        bottomNav.getOrCreateBadge(R.id.mensaje);
        bottomNav.getOrCreateBadge(R.id.opciones);
    }

    private void configurarSeleccionNavegacion(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            manejarSeleccionNavegacion(item);
            return true;
        });
    }

    private void manejarSeleccionNavegacion(@NonNull MenuItem item) {
        int pos = -1;
        if (item.getItemId() == R.id.home)    pos = 0;
        if (item.getItemId() == R.id.eventos) pos = 1;
        if (item.getItemId() == R.id.mensaje) pos = 2;
        if (item.getItemId() == R.id.opciones)pos = 3;

        if (pos != -1) {
            ViewPager vp = findViewById(R.id.view_pager);
            vp.setCurrentItem(pos);
            item.setChecked(true);
            eliminarBadge(findViewById(R.id.bottom_navigation), item.getItemId());
        }
    }

    private void eliminarBadge(BottomNavigationView bottomNav, int itemId) {
        BadgeDrawable badge = bottomNav.getBadge(itemId);
        if (badge != null) {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }
}

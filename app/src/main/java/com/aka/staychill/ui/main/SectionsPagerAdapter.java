package com.aka.staychill.ui.main;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.aka.staychill.fragments.Eventos;
import com.aka.staychill.fragments.Main;
import com.aka.staychill.fragments.Configuracion;
import com.aka.staychill.fragments.Mensaje;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new Main();
        } else if (position == 1) {
            return new Eventos();
        } else if (position == 2) {
            return new Mensaje();
        } else if (position == 3) {
            return new Configuracion();
        }
        else {
            throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
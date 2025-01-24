package com.aka.staychill.ui.main;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.aka.staychill.fragments.eventos;
import com.aka.staychill.fragments.main;
import com.aka.staychill.fragments.configuracion;
import com.aka.staychill.fragments.mensaje;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new main();
        } else if (position == 1) {
            return new eventos();
        } else if (position == 2) {
            return new mensaje();
        } else if (position == 3) {
            return new configuracion();
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
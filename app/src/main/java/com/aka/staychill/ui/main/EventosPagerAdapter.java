package com.aka.staychill.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.aka.staychill.fragments.MisEventos;
import com.aka.staychill.fragments.OtrosEventos;

public class EventosPagerAdapter extends FragmentStateAdapter {

    public EventosPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MisEventos();
            case 1:
                return new OtrosEventos();
            default:
                return new MisEventos();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
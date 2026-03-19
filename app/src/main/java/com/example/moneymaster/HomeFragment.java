package com.example.moneymaster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.MainActivity;
import com.example.moneymaster.R;

/**
 * Fragment de Inicio (Dashboard personal).
 * Implementación completa en cards #19 y siguientes (Sprint 2).
 */
public class HomeFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        // Actualiza el título de la toolbar cuando este fragment está visible
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle(getString(R.string.nav_home));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}

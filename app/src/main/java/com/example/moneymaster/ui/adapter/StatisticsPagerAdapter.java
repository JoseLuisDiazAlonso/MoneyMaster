package com.example.moneymaster.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.moneymaster.StatisticsCustomFragment;
import com.example.moneymaster.StatisticsMonthFragment;
import com.example.moneymaster.StatisticsYearFragment;

/**
 * Adaptador para el ViewPager2 de la pantalla de Estadísticas.
 *
 * Gestiona tres pestañas:
 *  0 → StatisticsMonthFragment   (Mes actual)
 *  1 → StatisticsYearFragment    (Año)
 *  2 → StatisticsCustomFragment  (Personalizado)
 *
 * FragmentStateAdapter destruye los fragmentos cuando se alejan demasiado del
 * fragmento visible. Combinado con offscreenPageLimit(2) en el ViewPager2,
 * los tres fragmentos permanecen vivos mientras el usuario está en esta pantalla.
 */
public class StatisticsPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;

    public StatisticsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new StatisticsMonthFragment();
            case 1: return new StatisticsYearFragment();
            case 2: return new StatisticsCustomFragment();
            default: throw new IllegalArgumentException("Posición de pestaña no válida: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
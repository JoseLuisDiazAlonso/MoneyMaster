package com.example.moneymaster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moneymaster.databinding.FragmentStatisticsBinding;
import com.example.moneymaster.ui.adapter.StatisticsPagerAdapter;
import com.example.moneymaster.ui.ViewModel.StatisticsViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Fragment principal de la pantalla de Estadísticas.
 *
 * Actúa como contenedor raíz para las tres pestañas:
 *  - Mes Actual  → StatisticsMonthFragment
 *  - Año         → StatisticsYearFragment
 *  - Personalizado → StatisticsCustomFragment
 *
 * Utiliza ViewPager2 + TabLayout gestionados por TabLayoutMediator.
 * El ViewModel compartido (StatisticsViewModel) es el único punto de
 * verdad para los datos; los fragmentos hijos lo observan directamente.
 */
public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatisticsViewModel viewModel;
    private StatisticsPagerAdapter pagerAdapter;

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel con ámbito de Activity para compartir datos entre pestañas
        viewModel = new ViewModelProvider(requireActivity())
                .get(StatisticsViewModel.class);

        setupViewPager();
        setupTabLayout();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Setup helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Configura el ViewPager2 con el adaptador de las tres pestañas.
     * offscreenPageLimit(2) mantiene los tres fragmentos vivos en memoria para
     * evitar recargas al cambiar de pestaña.
     */
    private void setupViewPager() {
        pagerAdapter = new StatisticsPagerAdapter(this);
        binding.viewPagerStatistics.setAdapter(pagerAdapter);
        binding.viewPagerStatistics.setOffscreenPageLimit(2);

        // Sincronizar selección de pestaña con el ViewModel
        binding.viewPagerStatistics.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        viewModel.setSelectedTab(position);
                    }
                });
    }

    /**
     * Enlaza TabLayout con ViewPager2 mediante TabLayoutMediator.
     * Los títulos de pestaña se asignan aquí para tenerlos centralizados.
     */
    private void setupTabLayout() {
        String[] tabTitles = {"Mes actual", "Año", "Personalizado"};

        new TabLayoutMediator(
                binding.tabLayoutStatistics,
                binding.viewPagerStatistics,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}
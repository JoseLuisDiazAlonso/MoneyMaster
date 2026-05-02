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

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatisticsViewModel viewModel;
    private StatisticsPagerAdapter pagerAdapter;

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

    private void setupViewPager() {
        pagerAdapter = new StatisticsPagerAdapter(this);
        binding.viewPagerStatistics.setAdapter(pagerAdapter);
        binding.viewPagerStatistics.setOffscreenPageLimit(2);

        binding.viewPagerStatistics.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        viewModel.setSelectedTab(position);
                    }
                });
    }

    private void setupTabLayout() {
        // Títulos leídos de strings.xml → se traducen automáticamente
        String[] tabTitles = {
                getString(R.string.tab_mes_actual),
                getString(R.string.tab_anio),
                getString(R.string.tab_personalizado)
        };

        new TabLayoutMediator(
                binding.tabLayoutStatistics,
                binding.viewPagerStatistics,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}
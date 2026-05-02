package com.example.moneymaster.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moneymaster.R;
import com.example.moneymaster.databinding.FragmentCategoriesBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class CategoriesFragment extends Fragment {

    private FragmentCategoriesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
    }

    private void setupViewPager() {
        CategoryPagerAdapter adapter = new CategoryPagerAdapter(this);
        binding.viewPagerCategories.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayoutCategories, binding.viewPagerCategories,
                (tab, position) -> {
                    if (position == 0) {
                        // FIX: texto desde strings.xml
                        tab.setText(R.string.cat_gastos_tab);
                        tab.setIcon(R.drawable.ic_category_expense);
                    } else {
                        tab.setText(R.string.cat_ingresos_tab);
                        tab.setIcon(R.drawable.ic_category_income);
                    }
                }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
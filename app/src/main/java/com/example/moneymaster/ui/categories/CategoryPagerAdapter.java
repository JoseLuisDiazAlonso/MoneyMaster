package com.example.moneymaster.ui.categories;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adaptador del ViewPager2 para las pestañas de Categorías.
 * Gestiona dos páginas: Gastos (posición 0) e Ingresos (posición 1).
 */
public class CategoryPagerAdapter extends FragmentStateAdapter {

    public CategoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Crea el sub-fragment pasando el tipo como argumento
        if (position == 0) {
            return CategoryListFragment.newInstance(CategoryListFragment.TYPE_GASTOS);
        } else {
            return CategoryListFragment.newInstance(CategoryListFragment.TYPE_INGRESOS);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Gastos + Ingresos
    }
}
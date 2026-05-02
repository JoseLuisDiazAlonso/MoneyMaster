package com.example.moneymaster.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.databinding.FragmentCategoryListBinding;

public class CategoryListFragment extends Fragment {

    public static final String TYPE_GASTOS   = "gastos";
    public static final String TYPE_INGRESOS = "ingresos";
    private static final String ARG_TYPE     = "category_type";

    private FragmentCategoryListBinding binding;
    private CategoriesViewModel viewModel;
    private CategoryAdapter adapter;
    private String categoryType;

    public static CategoryListFragment newInstance(String type) {
        CategoryListFragment fragment = new CategoryListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryType = getArguments().getString(ARG_TYPE, TYPE_GASTOS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment())
                .get(CategoriesViewModel.class);

        setupRecyclerView();
        setupFab();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(categoryType, category -> showDeleteConfirmation(category));

        binding.recyclerViewCategories.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewCategories.setAdapter(adapter);

        binding.recyclerViewCategories.setAlpha(0f);
        binding.recyclerViewCategories.animate().alpha(1f).setDuration(300).start();
    }

    private void setupFab() {
        binding.fabAddCategory.setOnClickListener(v -> {
            AddCategoryDialog dialog = AddCategoryDialog.newInstance(categoryType);
            dialog.show(getChildFragmentManager(), "AddCategoryDialog");
        });
    }

    private void observeData() {
        if (TYPE_GASTOS.equals(categoryType)) {
            viewModel.getCategoriasGasto().observe(getViewLifecycleOwner(), categorias -> {
                if (categorias != null) {
                    adapter.submitListGastos(categorias);
                    updateEmptyState(categorias.isEmpty());
                }
            });
        } else {
            viewModel.getCategoriasIngreso().observe(getViewLifecycleOwner(), categorias -> {
                if (categorias != null) {
                    adapter.submitListIngresos(categorias);
                    updateEmptyState(categorias.isEmpty());
                }
            });
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewCategories.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showDeleteConfirmation(Object category) {
        // FIX: textos del diálogo desde strings.xml
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.eliminar_categoria)
                .setMessage(R.string.confirmar_eliminar_categoria)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                    if (TYPE_GASTOS.equals(categoryType) && category instanceof CategoriaGasto) {
                        viewModel.deleteCategoriaGasto((CategoriaGasto) category);
                        Toast.makeText(requireContext(),
                                R.string.categoria_eliminada, Toast.LENGTH_SHORT).show();
                    } else if (category instanceof CategoriaIngreso) {
                        viewModel.deleteCategoriaIngreso((CategoriaIngreso) category);
                        Toast.makeText(requireContext(),
                                R.string.categoria_eliminada, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
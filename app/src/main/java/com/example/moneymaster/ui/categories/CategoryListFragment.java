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

import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.databinding.FragmentCategoryListBinding;

/**
 * Sub-fragment que muestra la lista de categorías para un tipo específico (Gastos o Ingresos).
 *
 * ARQUITECTURA:
 * - Recibe el tipo (gastos/ingresos) como argumento via newInstance()
 * - Observa LiveData del ViewModel para actualizar la UI reactivamente
 * - Categorías predefinidas: solo visualización (no se pueden eliminar)
 * - Categorías custom: se pueden eliminar con botón de borrado
 * - FAB abre el AddCategoryDialog para crear nuevas categorías
 */
public class CategoryListFragment extends Fragment {

    // Constantes para el tipo de categoría
    public static final String TYPE_GASTOS = "gastos";
    public static final String TYPE_INGRESOS = "ingresos";
    private static final String ARG_TYPE = "category_type";

    private FragmentCategoryListBinding binding;
    private CategoriesViewModel viewModel;
    private CategoryAdapter adapter;
    private String categoryType;

    /**
     * Factory method para crear una instancia con el tipo correcto.
     * IMPORTANTE: Siempre usa newInstance() en lugar del constructor directo
     * para pasar argumentos de forma segura ante rotaciones de pantalla.
     */
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

        // El ViewModel se comparte con el Fragment padre (CategoriesFragment)
        // Esto permite que ambas pestañas usen la misma instancia
        viewModel = new ViewModelProvider(requireParentFragment())
                .get(CategoriesViewModel.class);

        setupRecyclerView();
        setupFab();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(categoryType, category -> {
            // Callback de eliminación - solo disponible para custom
            showDeleteConfirmation(category);
        });

        binding.recyclerViewCategories.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewCategories.setAdapter(adapter);

        // Animación de entrada suave
        binding.recyclerViewCategories.setAlpha(0f);
        binding.recyclerViewCategories.animate().alpha(1f).setDuration(300).start();
    }

    private void setupFab() {
        binding.fabAddCategory.setOnClickListener(v -> {
            // Abre el diálogo para añadir nueva categoría
            AddCategoryDialog dialog = AddCategoryDialog.newInstance(categoryType);
            dialog.show(getChildFragmentManager(), "AddCategoryDialog");
        });
    }

    private void observeData() {
        if (TYPE_GASTOS.equals(categoryType)) {
            // Observa categorías de gastos
            viewModel.getCategoriasGasto().observe(getViewLifecycleOwner(), categorias -> {
                if (categorias != null) {
                    adapter.submitListGastos(categorias);
                    updateEmptyState(categorias.isEmpty());
                }
            });
        } else {
            // Observa categorías de ingresos
            viewModel.getCategoriasIngreso().observe(getViewLifecycleOwner(), categorias -> {
                if (categorias != null) {
                    adapter.submitListIngresos(categorias);
                    updateEmptyState(categorias.isEmpty());
                }
            });
        }
    }

    /**
     * Muestra/oculta el estado vacío cuando no hay categorías.
     */
    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewCategories.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Diálogo de confirmación antes de eliminar una categoría custom.
     * Recibe un Object genérico porque el adapter trabaja con ambos tipos.
     */
    private void showDeleteConfirmation(Object category) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar categoría")
                .setMessage("¿Estás seguro de que quieres eliminar esta categoría?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (TYPE_GASTOS.equals(categoryType) && category instanceof CategoriaGasto) {
                        viewModel.deleteCategoriaGasto((CategoriaGasto) category);
                        Toast.makeText(requireContext(),
                                "Categoría eliminada", Toast.LENGTH_SHORT).show();
                    } else if (category instanceof CategoriaIngreso) {
                        viewModel.deleteCategoriaIngreso((CategoriaIngreso) category);
                        Toast.makeText(requireContext(),
                                "Categoría eliminada", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
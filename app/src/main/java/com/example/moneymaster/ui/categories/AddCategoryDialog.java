package com.example.moneymaster.ui.categories;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.databinding.DialogAddCategoryBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialog para crear una nueva categoría personalizada.
 *
 * CAMPOS:
 * - Nombre de la categoría (obligatorio)
 * - Icono (selección de grid de iconos)
 * - Color (selección de paleta de colores)
 *
 * IMPLEMENTACIÓN:
 * Usa DialogFragment en lugar de AlertDialog directamente para sobrevivir
 * rotaciones de pantalla sin perder el estado del formulario.
 */
public class AddCategoryDialog extends DialogFragment {

    private static final String ARG_TYPE = "category_type";

    private DialogAddCategoryBinding binding;
    private CategoriesViewModel viewModel;

    private String selectedIcon = "ic_category_default";
    private String selectedColor = "#6750A4"; // Color primario Material 3 por defecto
    private String categoryType;

    // Lista de iconos disponibles para categorías
    private static final String[] AVAILABLE_ICONS = {
            "ic_cat_food", "ic_cat_transport", "ic_cat_shopping", "ic_cat_health",
            "ic_cat_education", "ic_cat_entertainment", "ic_cat_home", "ic_cat_travel",
            "ic_cat_sport", "ic_cat_pets", "ic_cat_beauty", "ic_cat_technology",
            "ic_cat_salary", "ic_cat_investment", "ic_cat_gift", "ic_cat_other"
    };

    // Paleta de colores predefinidos
    private static final String[] AVAILABLE_COLORS = {
            "#F44336", "#E91E63", "#9C27B0", "#6750A4",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFC107", "#FF9800", "#FF5722", "#795548"
    };

    public static AddCategoryDialog newInstance(String categoryType) {
        AddCategoryDialog dialog = new AddCategoryDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, categoryType);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryType = getArguments().getString(ARG_TYPE,
                    CategoryListFragment.TYPE_GASTOS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAddCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Comparte el ViewModel con el Fragment padre
        viewModel = new ViewModelProvider(requireParentFragment())
                .get(CategoriesViewModel.class);

        setupIconGrid();
        setupColorPicker();
        setupButtons();
        updatePreview();
    }

    /**
     * Configura el grid de iconos (4 columnas).
     */
    private void setupIconGrid() {
        IconGridAdapter iconAdapter = new IconGridAdapter(
                AVAILABLE_ICONS,
                selectedIcon,
                icon -> {
                    selectedIcon = icon;
                    updatePreview();
                });

        binding.recyclerViewIcons.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.recyclerViewIcons.setAdapter(iconAdapter);
    }

    /**
     * Configura el selector de color con chips de colores.
     * Crea un chip por cada color disponible dinámicamente.
     */
    private void setupColorPicker() {
        ColorPickerAdapter colorAdapter = new ColorPickerAdapter(
                AVAILABLE_COLORS,
                selectedColor,
                color -> {
                    selectedColor = color;
                    updatePreview();
                });

        binding.recyclerViewColors.setLayoutManager(
                new GridLayoutManager(requireContext(), 8));
        binding.recyclerViewColors.setAdapter(colorAdapter);
    }

    private void setupButtons() {
        binding.buttonCancel.setOnClickListener(v -> dismiss());
        binding.buttonSave.setOnClickListener(v -> saveCategory());
    }

    /**
     * Actualiza la vista previa en tiempo real al cambiar icono o color.
     */
    private void updatePreview() {
        // Preview del icono con color seleccionado
        int iconResId = requireContext().getResources().getIdentifier(
                selectedIcon, "drawable", requireContext().getPackageName());

        if (iconResId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), iconResId);
            if (drawable != null) {
                drawable = drawable.mutate();
                drawable.setColorFilter(Color.parseColor(selectedColor),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                binding.imagePreviewIcon.setImageDrawable(drawable);
            }
        }

        // Fondo del preview con el color seleccionado (20% opacidad)
        int color = Color.parseColor(selectedColor);
        int bgColor = Color.argb(51,
                Color.red(color), Color.green(color), Color.blue(color));
        binding.viewPreviewBackground.setBackgroundColor(bgColor);
    }

    /**
     * Valida y guarda la nueva categoría en la base de datos.
     */
    private void saveCategory() {
        String nombre = binding.editTextCategoryName.getText() != null
                ? binding.editTextCategoryName.getText().toString().trim()
                : "";

        // Validación del nombre
        if (TextUtils.isEmpty(nombre)) {
            binding.inputLayoutCategoryName.setError("El nombre es obligatorio");
            return;
        }

        if (nombre.length() > 30) {
            binding.inputLayoutCategoryName.setError("Máximo 30 caracteres");
            return;
        }

        binding.inputLayoutCategoryName.setError(null);

        // Guarda según el tipo de categoría
        if (CategoryListFragment.TYPE_GASTOS.equals(categoryType)) {
            CategoriaGasto nuevaCategoria = new CategoriaGasto();
            nuevaCategoria.nombre = nombre;
            nuevaCategoria.icono = selectedIcon;
            nuevaCategoria.color = selectedColor;
            nuevaCategoria.esPredefinida = 0; // Es custom, no predefinida
            viewModel.insertCategoriaGasto(nuevaCategoria);
        } else {
            CategoriaIngreso nuevaCategoria = new CategoriaIngreso();
            nuevaCategoria.nombre = nombre;
            nuevaCategoria.icono = selectedIcon;
            nuevaCategoria.color = selectedColor;
            nuevaCategoria.esPredefinida = 0;
            viewModel.insertCategoriaIngreso(nuevaCategoria);
        }

        Toast.makeText(requireContext(),
                "Categoría creada correctamente", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Configura el Dialog para usar el estilo de Material 3.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Usamos un tema de dialog de Material 3
        return new MaterialAlertDialogBuilder(requireContext(),
                com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .create();
    }
}
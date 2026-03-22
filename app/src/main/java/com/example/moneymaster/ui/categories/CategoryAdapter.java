package com.example.moneymaster.ui.categories;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.databinding.ItemCategoryBinding;
import com.example.moneymaster.databinding.ItemCategorySectionHeaderBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para la lista de categorías con soporte para:
 * - Cabeceras de sección ("Predefinidas" / "Mis categorías")
 * - Categorías predefinidas (solo vista, sin botón eliminar)
 * - Categorías personalizadas (con botón eliminar)
 *
 * PATRÓN VIEW TYPE:
 * Usamos dos tipos de vista (VIEW_TYPE_HEADER y VIEW_TYPE_ITEM) para renderizar
 * cabeceras de sección mezcladas con los items en un único RecyclerView.
 * Esto es más eficiente que usar múltiples RecyclerViews anidados.
 */
public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Tipos de vistas para el RecyclerView
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    // Interfaz para el callback de eliminación
    public interface OnDeleteClickListener {
        void onDeleteClick(Object category);
    }

    private final String categoryType;
    private final OnDeleteClickListener deleteListener;

    // Lista interna que mezcla headers e items
    // Cada elemento es: String (header) o CategoriaGasto/CategoriaIngreso
    private final List<Object> displayList = new ArrayList<>();

    public CategoryAdapter(String categoryType, OnDeleteClickListener deleteListener) {
        this.categoryType = categoryType;
        this.deleteListener = deleteListener;
    }

    /**
     * Actualiza la lista completa de categorías de gasto.
     * Separa predefinidas de custom y añade cabeceras de sección.
     */
    public void submitListGastos(List<CategoriaGasto> categorias) {
        displayList.clear();

        List<CategoriaGasto> predefinidas = new ArrayList<>();
        List<CategoriaGasto> custom = new ArrayList<>();

        for (CategoriaGasto cat : categorias) {
            if (cat.esPredefinida == 1) {
                predefinidas.add(cat);
            } else {
                custom.add(cat);
            }
        }

        if (!predefinidas.isEmpty()) {
            displayList.add("Predefinidas");
            displayList.addAll(predefinidas);
        }

        if (!custom.isEmpty()) {
            displayList.add("Mis categorías");
            displayList.addAll(custom);
        }

        notifyDataSetChanged();
    }

    /**
     * Actualiza la lista completa de categorías de ingreso.
     */
    public void submitListIngresos(List<CategoriaIngreso> categorias) {
        displayList.clear();

        List<CategoriaIngreso> predefinidas = new ArrayList<>();
        List<CategoriaIngreso> custom = new ArrayList<>();

        for (CategoriaIngreso cat : categorias) {
            if (cat.esPredefinida == 1) {
                predefinidas.add(cat);
            } else {
                custom.add(cat);
            }
        }

        if (!predefinidas.isEmpty()) {
            displayList.add("Predefinidas");
            displayList.addAll(predefinidas);
        }

        if (!custom.isEmpty()) {
            displayList.add("Mis categorías");
            displayList.addAll(custom);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position) instanceof String ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            ItemCategorySectionHeaderBinding binding =
                    ItemCategorySectionHeaderBinding.inflate(inflater, parent, false);
            return new HeaderViewHolder(binding);
        } else {
            ItemCategoryBinding binding =
                    ItemCategoryBinding.inflate(inflater, parent, false);
            return new CategoryViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayList.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) item);
        } else if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    // ─── ViewHolder para cabeceras de sección ─────────────────────────────────

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategorySectionHeaderBinding binding;

        HeaderViewHolder(ItemCategorySectionHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String title) {
            binding.textSectionTitle.setText(title);
        }
    }

    // ─── ViewHolder para items de categoría ───────────────────────────────────

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Object item) {
            String nombre;
            String icono;
            String color;
            boolean esPredefinida;

            if (item instanceof CategoriaGasto) {
                CategoriaGasto cat = (CategoriaGasto) item;
                nombre = cat.nombre;
                icono = cat.icono;
                color = cat.color;
                esPredefinida = cat.esPredefinida == 1;
            } else {
                CategoriaIngreso cat = (CategoriaIngreso) item;
                nombre = cat.nombre;
                icono = cat.icono;
                color = cat.color;
                esPredefinida = cat.esPredefinida == 1;
            }

            // Nombre de la categoría
            binding.textCategoryName.setText(nombre);

            // Icono dinámico: resuelve el drawable por nombre y aplica tint de color
            int iconResId = binding.getRoot().getContext().getResources().getIdentifier(
                    icono, "drawable", binding.getRoot().getContext().getPackageName());

            if (iconResId != 0) {
                Drawable drawable = ContextCompat.getDrawable(
                        binding.getRoot().getContext(), iconResId);
                if (drawable != null) {
                    drawable = drawable.mutate(); // Importante: mutate() para no afectar otras vistas
                    drawable.setColorFilter(Color.parseColor(color),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                    binding.imageCategoryIcon.setImageDrawable(drawable);
                }
            } else {
                // Fallback si no se encuentra el drawable
                binding.imageCategoryIcon.setImageResource(R.drawable.ic_category_default);
            }

            // Fondo circular con el color de la categoría (con opacidad reducida)
            binding.viewIconBackground.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor(color) & 0x33FFFFFF | 0x33000000));

            // Botón eliminar: solo visible para categorías custom
            if (esPredefinida) {
                binding.buttonDeleteCategory.setVisibility(View.GONE);
                binding.chipPredefined.setVisibility(View.VISIBLE);
            } else {
                binding.buttonDeleteCategory.setVisibility(View.VISIBLE);
                binding.chipPredefined.setVisibility(View.GONE);
                binding.buttonDeleteCategory.setOnClickListener(v ->
                        deleteListener.onDeleteClick(item));
            }
        }
    }
}

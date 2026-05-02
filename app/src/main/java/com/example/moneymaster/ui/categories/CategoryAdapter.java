package com.example.moneymaster.ui.categories;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.databinding.ItemCategoryBinding;
import com.example.moneymaster.databinding.ItemCategorySectionHeaderBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "CategoryAdapter";
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM   = 1;

    public interface OnDeleteClickListener {
        void onDeleteClick(Object category);
    }

    private final String categoryType;
    private final OnDeleteClickListener deleteListener;
    private final List<Object> displayList = new ArrayList<>();

    private static final Map<String, Integer> CATEGORIA_STRING_IDS = new HashMap<>();
    static {
        CATEGORIA_STRING_IDS.put("cat_alimentacion",   R.string.cat_alimentacion);
        CATEGORIA_STRING_IDS.put("cat_transporte",     R.string.cat_transporte);
        CATEGORIA_STRING_IDS.put("cat_vivienda",       R.string.cat_vivienda);
        CATEGORIA_STRING_IDS.put("cat_salud",          R.string.cat_salud);
        CATEGORIA_STRING_IDS.put("cat_educacion",      R.string.cat_educacion);
        CATEGORIA_STRING_IDS.put("cat_ocio",           R.string.cat_ocio);
        CATEGORIA_STRING_IDS.put("cat_ropa",           R.string.cat_ropa);
        CATEGORIA_STRING_IDS.put("cat_tecnologia",     R.string.cat_tecnologia);
        CATEGORIA_STRING_IDS.put("cat_viajes",         R.string.cat_viajes);
        CATEGORIA_STRING_IDS.put("cat_mascotas",       R.string.cat_mascotas);
        CATEGORIA_STRING_IDS.put("cat_restaurantes",   R.string.cat_restaurantes);
        CATEGORIA_STRING_IDS.put("cat_supermercado",   R.string.cat_supermercado);
        CATEGORIA_STRING_IDS.put("cat_seguros",        R.string.cat_seguros);
        CATEGORIA_STRING_IDS.put("cat_suscripciones",  R.string.cat_suscripciones);
        CATEGORIA_STRING_IDS.put("cat_otros_gastos",   R.string.cat_otros_gastos);
        CATEGORIA_STRING_IDS.put("cat_otros",          R.string.cat_otros);
        CATEGORIA_STRING_IDS.put("cat_salario",        R.string.cat_salario);
        CATEGORIA_STRING_IDS.put("cat_freelance",      R.string.cat_freelance);
        CATEGORIA_STRING_IDS.put("cat_inversiones",    R.string.cat_inversiones);
        CATEGORIA_STRING_IDS.put("cat_alquiler",       R.string.cat_alquiler);
        CATEGORIA_STRING_IDS.put("cat_regalo",         R.string.cat_regalo);
        CATEGORIA_STRING_IDS.put("cat_reembolso",      R.string.cat_reembolso);
        CATEGORIA_STRING_IDS.put("cat_venta",          R.string.cat_venta);
        CATEGORIA_STRING_IDS.put("cat_otros_ingresos", R.string.cat_otros_ingresos);
    }

    public CategoryAdapter(String categoryType, OnDeleteClickListener deleteListener) {
        this.categoryType = categoryType;
        this.deleteListener = deleteListener;
    }

    public void submitListGastos(List<CategoriaGasto> categorias) {
        displayList.clear();
        List<CategoriaGasto> predefinidas = new ArrayList<>();
        List<CategoriaGasto> custom       = new ArrayList<>();
        for (CategoriaGasto cat : categorias) {
            if (cat.esSistema == 1) predefinidas.add(cat);
            else                    custom.add(cat);
        }
        if (!predefinidas.isEmpty()) {
            displayList.add(R.string.cat_predefinidas);
            displayList.addAll(predefinidas);
        }
        if (!custom.isEmpty()) {
            displayList.add(R.string.cat_mis_categorias);
            displayList.addAll(custom);
        }
        notifyDataSetChanged();
    }

    public void submitListIngresos(List<CategoriaIngreso> categorias) {
        displayList.clear();
        List<CategoriaIngreso> predefinidas = new ArrayList<>();
        List<CategoriaIngreso> custom       = new ArrayList<>();
        for (CategoriaIngreso cat : categorias) {
            if (cat.esSistema == 1) predefinidas.add(cat);
            else                    custom.add(cat);
        }
        if (!predefinidas.isEmpty()) {
            displayList.add(R.string.cat_predefinidas);
            displayList.addAll(predefinidas);
        }
        if (!custom.isEmpty()) {
            displayList.add(R.string.cat_mis_categorias);
            displayList.addAll(custom);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position) instanceof Integer ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
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
            ((HeaderViewHolder) holder).bind((Integer) item);
        } else if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategorySectionHeaderBinding binding;
        HeaderViewHolder(ItemCategorySectionHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(int stringResId) {
            binding.textSectionTitle.setText(stringResId);
        }
    }

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
                nombre        = cat.nombre;
                icono         = cat.icono;
                color         = cat.color;
                esPredefinida = cat.esSistema == 1;
            } else {
                CategoriaIngreso cat = (CategoriaIngreso) item;
                nombre        = cat.nombre;
                icono         = cat.icono;
                color         = cat.color;
                esPredefinida = cat.esSistema == 1;
            }

            // LOG para depuración — ver exactamente qué llega de la BD
            Log.d(TAG, "nombre BD='" + nombre + "' length=" + (nombre != null ? nombre.length() : -1));
            if (nombre != null) {
                StringBuilder hex = new StringBuilder("hex: ");
                for (char c : nombre.toCharArray()) hex.append(Integer.toHexString(c)).append(" ");
                Log.d(TAG, hex.toString());
            }
            Log.d(TAG, "resId encontrado=" + CATEGORIA_STRING_IDS.containsKey(nombre));

            binding.textCategoryName.setText(resolverNombre(binding.getRoot().getContext(), nombre));

            int iconResId = binding.getRoot().getContext().getResources().getIdentifier(
                    icono, "drawable", binding.getRoot().getContext().getPackageName());
            if (iconResId != 0) {
                Drawable drawable = ContextCompat.getDrawable(binding.getRoot().getContext(), iconResId);
                if (drawable != null) {
                    drawable = drawable.mutate();
                    drawable.setColorFilter(Color.parseColor(color), android.graphics.PorterDuff.Mode.SRC_IN);
                    binding.imageCategoryIcon.setImageDrawable(drawable);
                }
            } else {
                binding.imageCategoryIcon.setImageResource(R.drawable.ic_category_default);
            }

            binding.viewIconBackground.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor(color) & 0x33FFFFFF | 0x33000000));

            if (esPredefinida) {
                binding.buttonDeleteCategory.setVisibility(View.GONE);
                binding.chipPredefined.setVisibility(View.VISIBLE);
            } else {
                binding.buttonDeleteCategory.setVisibility(View.VISIBLE);
                binding.chipPredefined.setVisibility(View.GONE);
                binding.buttonDeleteCategory.setOnClickListener(v -> deleteListener.onDeleteClick(item));
            }
        }
    }

    public static String resolverNombre(Context ctx, String nombre) {
        if (nombre == null || nombre.isEmpty()) return "";
        Integer resId = CATEGORIA_STRING_IDS.get(nombre);
        if (resId != null) return ctx.getString(resId);
        return nombre;
    }
}
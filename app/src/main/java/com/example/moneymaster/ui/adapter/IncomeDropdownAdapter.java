package com.example.moneymaster.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaIngreso;

import java.util.ArrayList;
import java.util.List;

/**
 * IncomeDropdownAdapter
 * ───────────────────────────────────────────────────────────────────────────
 * Adapter personalizado para el ExposedDropdownMenu de categorías de ingreso.
 *
 * DIFERENCIAS con CategoryDropdownAdapter (gastos):
 *  - Trabaja con {@link CategoriaIngreso} en lugar de CategoriaGasto.
 *  - El círculo de fondo del icono usa el color verde de ingresos.
 *
 * RESOLUCIÓN DINÁMICA DE ICONOS:
 *  Las categorías almacenan el nombre del drawable (p.ej. "ic_salary") en
 *  la columna `icono`. En getView() se resuelve el recurso con
 *  {@code getResources().getIdentifier()} para no hardcodear IDs.
 *
 * PATRÓN VIEWHOLDER:
 *  Evita llamadas repetidas a findViewById() cacheando las vistas en una
 *  clase interna estática, mejorando el rendimiento del scroll.
 */
public class IncomeDropdownAdapter extends ArrayAdapter<CategoriaIngreso> {

    // ── Estado interno ────────────────────────────────────────────────────────
    private final List<CategoriaIngreso> allCategories = new ArrayList<>();
    private final LayoutInflater         inflater;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param context Contexto de la Activity, necesario para inflar vistas.
     */
    public IncomeDropdownAdapter(@NonNull Context context) {
        super(context, R.layout.item_category_dropdown, new ArrayList<>());
        this.inflater = LayoutInflater.from(context);
    }

    // ── Métodos de ArrayAdapter ───────────────────────────────────────────────

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return buildView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return buildView(position, convertView, parent);
    }

    // ── Construcción de vistas ────────────────────────────────────────────────

    /**
     * Infla o reutiliza una fila del dropdown y puebla con los datos de la
     * categoría en la posición indicada.
     */
    private View buildView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_category_dropdown, parent, false);
            holder            = new ViewHolder();
            holder.iconFrame  = convertView.findViewById(R.id.view_icon_bg);
            holder.icon       = convertView.findViewById(R.id.img_category_icon);
            holder.name       = convertView.findViewById(R.id.tv_category_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CategoriaIngreso cat = getItem(position);
        if (cat == null) return convertView;

        // ── Texto ──────────────────────────────────────────────────────────
        holder.name.setText(cat.nombre);

        // ── Color del fondo del icono (verde para ingresos) ────────────────
        int colorRes = R.color.income_green;   // definido en colors.xml
        int color    = ContextCompat.getColor(getContext(), colorRes);
        holder.iconFrame.setBackgroundTintList(ColorStateList.valueOf(color));

        // ── Icono dinámico desde nombre de drawable ────────────────────────
        if (cat.icono != null && !cat.icono.isEmpty()) {
            int resId = getContext().getResources().getIdentifier(
                    cat.icono, "drawable", getContext().getPackageName());
            if (resId != 0) {
                Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
                holder.icon.setImageDrawable(drawable);
                // Tintamos el icono en blanco para contraste sobre el fondo verde
                holder.icon.setColorFilter(
                        ContextCompat.getColor(getContext(), android.R.color.white));
            } else {
                holder.icon.setImageResource(R.drawable.ic_category_default);
                holder.icon.setColorFilter(
                        ContextCompat.getColor(getContext(), android.R.color.white));
            }
        }

        return convertView;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Actualiza el contenido del adapter con una nueva lista de categorías.
     * Llamado cuando el LiveData emite una actualización.
     *
     * @param newCategories Nueva lista desde Room.
     */
    public void updateCategories(@NonNull List<CategoriaIngreso> newCategories) {
        allCategories.clear();
        allCategories.addAll(newCategories);
        clear();
        addAll(newCategories);
        notifyDataSetChanged();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    /** Cache de referencias a las vistas de cada ítem. */
    private static class ViewHolder {
        View      iconFrame;
        ImageView icon;
        TextView  name;
    }
}
package com.example.moneymaster.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.TopCategoriasItem;

import java.util.List;
import java.util.Locale;


public class TopCategoriasAdapter extends ListAdapter<TopCategoriasItem, TopCategoriasAdapter.ViewHolder> {

    //DiffUtil

    private static final DiffUtil.ItemCallback<TopCategoriasItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TopCategoriasItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull TopCategoriasItem a, @NonNull TopCategoriasItem b) {
                    return a.nombreCategoria.equals(b.nombreCategoria);
                }

                @Override
                public boolean areContentsTheSame(@NonNull TopCategoriasItem a, @NonNull TopCategoriasItem b) {
                    return a.total == b.total
                            && a.icono.equals(b.icono)
                            && a.color.equals(b.color);
                }
            };

    //Estado interno

    /** Total sumado de todos los ítems de la lista; usado para calcular el % de cada uno. */
    private double totalGeneral = 0.0;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public TopCategoriasAdapter() {
        super(DIFF_CALLBACK);
    }

    // ─── submitList override ──────────────────────────────────────────────────

    /**
     * Calcula el totalGeneral antes de pasar la lista al ListAdapter.
     * Así onBindViewHolder puede calcular porcentajes sin iterar de nuevo.
     */
    @Override
    public void submitList(List<TopCategoriasItem> list) {
        totalGeneral = 0.0;
        if (list != null) {
            for (TopCategoriasItem item : list) {
                totalGeneral += item.total;
            }
        }
        super.submitList(list);
    }

    //Ciclo de vida del ViewHolder

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_categoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopCategoriasItem item = getItem(position);
        Context ctx = holder.itemView.getContext();

        //Ranking (1-indexed)
        holder.tvRanking.setText(String.valueOf(position + 1));

        //Nombre
        holder.tvNombre.setText(item.nombreCategoria);

        //Cantidad formateada
        holder.tvCantidad.setText(formatearEuros(item.total));

        //Porcentaje
        int porcentaje = totalGeneral > 0
                ? (int) Math.round((item.total / totalGeneral) * 100)
                : 0;
        holder.tvPorcentaje.setText(porcentaje + "%");

        //Barra de progreso
        holder.progressBar.setProgress(porcentaje);

        //Color de la categoría
        int color = parseColor(item.color);
        holder.progressBar.setProgressTintList(ColorStateList.valueOf(color));

        // Chip de ranking con color de la categoría como fondo semitransparente
        holder.tvRanking.setBackgroundTintList(
                ColorStateList.valueOf(applyAlpha(color, 0.15f)));
        holder.tvRanking.setTextColor(color);

        //Icono vector coloreado
        int iconResId = resolverIcono(ctx, item.icono);
        if (iconResId != 0) {
            Drawable drawable = ContextCompat.getDrawable(ctx, iconResId);
            if (drawable != null) {
                drawable = drawable.mutate();
                drawable.setTint(color);
                holder.ivIcono.setImageDrawable(drawable);
            }
        } else {
            // Icono de fallback si el drawable no existe
            holder.ivIcono.setImageResource(R.drawable.ic_category);
            holder.ivIcono.setColorFilter(color);
        }

        // Fondo de la tarjeta con tinte muy suave de la categoría
        holder.itemView.setBackgroundTintList(
                ColorStateList.valueOf(applyAlpha(color, 0.08f)));
    }

    //ViewHolder

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView   tvRanking;
        final ImageView  ivIcono;
        final TextView   tvNombre;
        final TextView   tvCantidad;
        final TextView   tvPorcentaje;
        final ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRanking    = itemView.findViewById(R.id.tv_ranking);
            ivIcono      = itemView.findViewById(R.id.iv_icono_categoria);
            tvNombre     = itemView.findViewById(R.id.tv_nombre_categoria);
            tvCantidad   = itemView.findViewById(R.id.tv_cantidad);
            tvPorcentaje = itemView.findViewById(R.id.tv_porcentaje);
            progressBar  = itemView.findViewById(R.id.progress_categoria);
        }
    }

    //Helpers privados

    /**
     * Formatea un double como moneda en español
     */
    private String formatearEuros(double cantidad) {
        return String.format(new Locale("es", "ES"), "%,.2f €", cantidad);
    }

    /**
     * Resuelve el nombre del drawable a su R.drawable.id.
     * Retorna 0 si no existe (el llamador debe manejar el fallback).
     */
    private int resolverIcono(Context ctx, String nombreIcono) {
        if (nombreIcono == null || nombreIcono.isEmpty()) return 0;
        return ctx.getResources().getIdentifier(nombreIcono, "drawable", ctx.getPackageName());
    }

    /**
     * Parsea un String de color hex con fallback a gris.
     */
    private int parseColor(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.parseColor("#9E9E9E"); // gris Material
        }
    }

    /**
     * Aplica un canal alfa (0f–1f) a un color ARGB.
     */
    private int applyAlpha(int color, float alpha) {
        int a = Math.round(alpha * 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }
}

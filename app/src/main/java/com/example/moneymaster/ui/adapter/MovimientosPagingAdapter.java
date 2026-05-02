package com.example.moneymaster.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.GastoPersonal;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MovimientosPagingAdapter
        extends PagingDataAdapter<GastoPersonal, MovimientosPagingAdapter.ViewHolder> {

    //DiffUtil — obligatorio para PagingDataAdapter
    private static final DiffUtil.ItemCallback<GastoPersonal> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GastoPersonal>() {
                @Override
                public boolean areItemsTheSame(@NonNull GastoPersonal a, @NonNull GastoPersonal b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull GastoPersonal a, @NonNull GastoPersonal b) {
                    return a.id == b.id
                            && Double.compare(a.monto, b.monto) == 0
                            && a.fecha == b.fecha
                            && safeEquals(a.descripcion, b.descripcion)
                            && safeEquals(a.categoria_id, b.categoria_id);
                }

                private boolean safeEquals(Object x, Object y) {
                    if (x == null && y == null) return true;
                    if (x == null || y == null) return false;
                    return x.equals(y);
                }
            };

    //Interfaz de click
    public interface OnItemClickListener {
        void onItemClick(GastoPersonal gasto);
    }

    private OnItemClickListener listener;

    public MovimientosPagingAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //ViewHolder

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion;
        TextView tvMonto;
        TextView tvFecha;
        ImageView ivIcono;
        View itemView;

        public ViewHolder(@NonNull View view) {
            super(view);
            itemView   = view;
            tvDescripcion = view.findViewById(R.id.tv_descripcion);
            tvMonto       = view.findViewById(R.id.tvMonto);
            tvFecha       = view.findViewById(R.id.tv_fecha);
            ivIcono       = view.findViewById(R.id.ivLogo);
        }
    }

    //Inflate / Bind

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GastoPersonal gasto = getItem(position);
        if (gasto == null) {
            // Paging 3 puede pasar null como placeholder mientras carga
            holder.tvDescripcion.setText("Cargando…");
            holder.tvMonto.setText("");
            holder.tvFecha.setText("");
            return;
        }

        Context ctx = holder.itemView.getContext();

        // Descripción
        holder.tvDescripcion.setText(
                (gasto.descripcion != null && !gasto.descripcion.isEmpty())
                        ? gasto.descripcion
                        : ctx.getString(R.string.sin_descripcion));

        // Monto formateado
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        holder.tvMonto.setText(nf.format(gasto.monto));
        holder.tvMonto.setTextColor(ctx.getColor(R.color.expense_red));

        // Fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText(sdf.format(new Date(gasto.fecha)));


        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(gasto);
        });
    }

    //LoadState footer helper

    /**
     * Devuelve true si el adapter está vacío (útil para mostrar empty state).
     */
    public boolean isEmpty() {
        return getItemCount() == 0;
    }
}

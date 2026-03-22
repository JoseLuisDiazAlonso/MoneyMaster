package com.example.moneymaster.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.moneymaster.R;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.databinding.ItemGroupExpenseBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GroupExpenseAdapter
        extends ListAdapter<GastoGrupo, GroupExpenseAdapter.ExpenseViewHolder> {

    // ─── Interfaces ───────────────────────────────────────────────────────────

    public interface OnExpenseClickListener {
        void onExpenseClick(GastoGrupo gasto);
    }

    // Card #33 — click en miniatura para ver foto ampliada
    public interface OnFotoClickListener {
        void onFotoClick(GastoGrupo gasto, String rutaFoto);
    }

    // ─── DiffUtil ─────────────────────────────────────────────────────────────

    private static final DiffUtil.ItemCallback<GastoGrupo> DIFF =
            new DiffUtil.ItemCallback<GastoGrupo>() {
                @Override
                public boolean areItemsTheSame(@NonNull GastoGrupo a, @NonNull GastoGrupo b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull GastoGrupo a, @NonNull GastoGrupo b) {
                    return a.monto == b.monto
                            && a.fecha == b.fecha
                            && strEq(a.descripcion, b.descripcion)
                            && strEq(a.pagadoPorNombre, b.pagadoPorNombre)
                            && intEq(a.fotoReciboId, b.fotoReciboId); // Card #33
                }

                private boolean strEq(String a, String b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }

                private boolean intEq(Integer a, Integer b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
            };

    // ─── Campos ───────────────────────────────────────────────────────────────

    private final OnExpenseClickListener clickListener;
    private OnFotoClickListener          fotoClickListener; // Card #33 — opcional

    // Card #33 — mapa de fotoReciboId → rutaArchivo, cargado desde fuera
    private java.util.Map<Integer, String> rutasFoto = new java.util.HashMap<>();

    // ─── Constructor ─────────────────────────────────────────────────────────

    public GroupExpenseAdapter(OnExpenseClickListener listener) {
        super(DIFF);
        this.clickListener = listener;
    }

    // ─── API pública Card #33 ─────────────────────────────────────────────────

    /** Registra el listener para clicks en miniaturas de foto. */
    public void setOnFotoClickListener(OnFotoClickListener listener) {
        this.fotoClickListener = listener;
    }

    /**
     * Actualiza el mapa de rutas de fotos y refresca el adapter.
     * Llama desde el Fragment cuando el LiveData de fotos cambie.
     *
     * @param rutas Map<fotoReciboId, rutaArchivo>
     */
    public void setRutasFoto(java.util.Map<Integer, String> rutas) {
        this.rutasFoto = rutas != null ? rutas : new java.util.HashMap<>();
        notifyDataSetChanged();
    }

    // ─── Ciclo de vida del adapter ────────────────────────────────────────────

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExpenseViewHolder(ItemGroupExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener, fotoClickListener, rutasFoto);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ViewHolder
    // ═════════════════════════════════════════════════════════════════════════

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private final ItemGroupExpenseBinding binding;
        private static final SimpleDateFormat SDF =
                new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

        ExpenseViewHolder(ItemGroupExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GastoGrupo gasto,
                  OnExpenseClickListener clickListener,
                  OnFotoClickListener fotoClickListener,
                  java.util.Map<Integer, String> rutasFoto) {

            // ── Datos existentes ──────────────────────────────────────────
            binding.textViewExpenseDescription.setText(
                    gasto.descripcion != null && !gasto.descripcion.isEmpty()
                            ? gasto.descripcion : "Sin descripción");

            binding.textViewExpensePaidBy.setText(
                    "Pagó: " + (gasto.pagadoPorNombre != null
                            ? gasto.pagadoPorNombre : "Desconocido"));

            binding.textViewExpenseAmount.setText(
                    String.format(new Locale("es", "ES"), "%.2f €", gasto.monto));

            binding.textViewExpenseDate.setText(SDF.format(new Date(gasto.fecha)));

            binding.getRoot().setOnClickListener(v -> clickListener.onExpenseClick(gasto));

            // ── Card #33: miniatura de foto ───────────────────────────────
            String rutaFoto = gasto.fotoReciboId != null
                    ? rutasFoto.get(gasto.fotoReciboId) : null;

            if (rutaFoto != null && !rutaFoto.isEmpty()) {
                binding.ivFotoMiniatura.setVisibility(View.VISIBLE);

                Glide.with(binding.getRoot().getContext())
                        .load(rutaFoto)
                        .apply(new RequestOptions()
                                .centerCrop()
                                .transform(new RoundedCorners(16))
                                .placeholder(R.drawable.ic_receipt_long)
                                .error(R.drawable.ic_receipt_long))
                        .into(binding.ivFotoMiniatura);

                final String rutaFinal = rutaFoto;
                binding.ivFotoMiniatura.setOnClickListener(v -> {
                    if (fotoClickListener != null) {
                        fotoClickListener.onFotoClick(gasto, rutaFinal);
                    }
                });
            } else {
                binding.ivFotoMiniatura.setVisibility(View.GONE);
                Glide.with(binding.getRoot().getContext())
                        .clear(binding.ivFotoMiniatura);
            }
        }
    }
}
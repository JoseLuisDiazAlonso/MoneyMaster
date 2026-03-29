package com.example.moneymaster.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.databinding.ItemGroupExpenseBinding;
import com.example.moneymaster.util.SwipeDeleteManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class GroupExpenseAdapter
        extends RecyclerView.Adapter<GroupExpenseAdapter.ViewHolder>
        implements SwipeDeleteManager.AdapterContract<GastoGrupo> {

    public interface OnItemClickListener {
        void onItemClick(GastoGrupo gasto);
    }

    public interface OnFotoClickListener {
        void onFotoClick(GastoGrupo gasto, String rutaFoto);
    }

    private List<GastoGrupo>        items = new ArrayList<>();
    private Map<Integer, String>    rutasFoto;
    private OnItemClickListener     clickListener;
    private OnFotoClickListener     fotoClickListener;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

    public GroupExpenseAdapter(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnFotoClickListener(OnFotoClickListener listener) {
        this.fotoClickListener = listener;
    }

    public void setRutasFoto(Map<Integer, String> rutas) {
        this.rutasFoto = rutas;
        notifyDataSetChanged();
    }

    // ─── DiffUtil ─────────────────────────────────────────────────────────────

    public void submitList(List<GastoGrupo> newList) {
        List<GastoGrupo> safeNew = newList != null ? newList : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return safeNew.size(); }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return items.get(o).id == safeNew.get(n).id;
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                GastoGrupo a = items.get(o), b = safeNew.get(n);
                return a.monto == b.monto
                        && a.fecha == b.fecha
                        && safeEquals(a.descripcion, b.descripcion);
            }

            private boolean safeEquals(String a, String b) {
                return a == null ? b == null : a.equals(b);
            }
        });
        items = new ArrayList<>(safeNew);
        result.dispatchUpdatesTo(this);
    }

    // ─── SwipeDeleteManager.AdapterContract ──────────────────────────────────

    @Override
    public GastoGrupo getItemAt(int position) {
        if (position >= 0 && position < items.size()) return items.get(position);
        return null;
    }

    @Override
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public void restoreItem(GastoGrupo item, int position) {
        int safePos = Math.min(position, items.size());
        items.add(safePos, item);
        notifyItemInserted(safePos);
    }

    // ─── RecyclerView.Adapter ─────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupExpenseBinding binding = ItemGroupExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemGroupExpenseBinding binding;

        public ViewHolder(@NonNull ItemGroupExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (clickListener != null && pos != RecyclerView.NO_ID && pos < items.size()) {
                    clickListener.onItemClick(items.get(pos));
                }
            });
        }

        public void bind(GastoGrupo gasto) {
            // Descripción
            binding.textViewExpenseDescription.setText(
                    gasto.descripcion != null && !gasto.descripcion.isEmpty()
                            ? gasto.descripcion : "Sin descripción");

            // Pagado por
            if (binding.textViewExpensePaidBy != null) {
                binding.textViewExpensePaidBy.setText(
                        gasto.pagadoPorNombre != null
                                ? "Pagado por: " + gasto.pagadoPorNombre : "");
            }

            // Fecha
            if (binding.textViewExpenseDate != null) {
                binding.textViewExpenseDate.setText(
                        dateFormat.format(new Date(gasto.fecha)));
            }

            // Monto
            binding.textViewExpenseAmount.setText(
                    currencyFormat.format(gasto.monto));

            // Miniatura foto (Card #33)
            if (binding.ivFotoMiniatura != null) {
                String ruta = (rutasFoto != null && gasto.foto_recibo_id != null)
                        ? rutasFoto.get(gasto.foto_recibo_id) : null;

                if (ruta != null && !ruta.isEmpty()) {
                    binding.ivFotoMiniatura.setVisibility(View.VISIBLE);
                    Glide.with(binding.getRoot().getContext())
                            .load(ruta)
                            .centerCrop()
                            .into(binding.ivFotoMiniatura);

                    binding.ivFotoMiniatura.setOnClickListener(v -> {
                        int pos = getAdapterPosition();
                        if (fotoClickListener != null && pos != RecyclerView.NO_ID) {
                            fotoClickListener.onFotoClick(items.get(pos), ruta);
                        }
                    });
                } else {
                    binding.ivFotoMiniatura.setVisibility(View.GONE);
                }
            }
        }
    }
}
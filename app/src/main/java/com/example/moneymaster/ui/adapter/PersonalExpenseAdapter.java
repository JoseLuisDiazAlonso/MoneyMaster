package com.example.moneymaster.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.databinding.ItemMovimientoBinding;
import com.example.moneymaster.utils.SwipeDeleteManager;
import com.example.moneymaster.utils.TransitionHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PersonalExpenseAdapter
        extends RecyclerView.Adapter<PersonalExpenseAdapter.ViewHolder>
        implements SwipeDeleteManager.AdapterContract<GastoPersonal> {

    public interface OnItemClickListener {
        void onItemClick(GastoPersonal gasto);
    }

    private final Context context;
    private List<GastoPersonal> items = new ArrayList<>();
    private OnItemClickListener clickListener;
    private int lastAnimatedPosition = -1;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

    public PersonalExpenseAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    //DiffUtil

    public void submitList(List<GastoPersonal> newList) {
        List<GastoPersonal> safeNew = newList != null ? newList : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return safeNew.size(); }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return items.get(o).id == safeNew.get(n).id;
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                GastoPersonal a = items.get(o), b = safeNew.get(n);
                return a.monto == b.monto && a.fecha == b.fecha;
            }
        });
        items = new ArrayList<>(safeNew);
        result.dispatchUpdatesTo(this);
    }

    //SwipeDeleteManager.AdapterContract

    @Override
    public GastoPersonal getItemAt(int position) {
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
    public void restoreItem(GastoPersonal item, int position) {
        int safePos = Math.min(position, items.size());
        items.add(safePos, item);
        notifyItemInserted(safePos);
    }

    //RecyclerView.Adapter

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovimientoBinding binding = ItemMovimientoBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));

        if (position > lastAnimatedPosition) {
            TransitionHelper.animateItemEntry(holder.itemView, position);
            lastAnimatedPosition = position;
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    //ViewHolder

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMovimientoBinding binding;

        public ViewHolder(@NonNull ItemMovimientoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (clickListener != null && pos != RecyclerView.NO_ID && pos < items.size()) {
                    clickListener.onItemClick(items.get(pos));
                }
            });
        }

        public void bind(GastoPersonal gasto) {
            // Descripción
            String desc = gasto.descripcion != null && !gasto.descripcion.isEmpty()
                    ? gasto.descripcion : "Sin descripción";
            binding.tvTitulo.setText(desc);
            binding.tvDescripcion.setVisibility(View.GONE);

            // Fecha
            binding.tvFecha.setText(dateFormat.format(new Date(gasto.fecha)));

            // Monto — gastos siempre en rojo
            binding.tvCantidad.setText("-" + currencyFormat.format(gasto.monto));
            binding.tvCantidad.setTextColor(context.getColor(R.color.expense_red));

            // Icono de categoría por defecto
            binding.ivCategoriaIcono.setImageResource(R.drawable.ic_category_default);

            // Foto miniatura oculta (gastos personales sin foto en este adapter)
            binding.ivFotoMiniatura.setVisibility(View.GONE);
        }
    }
}

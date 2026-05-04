package com.example.moneymaster.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moneymaster.R;
import com.example.moneymaster.data.model.MovimientoReciente;
import com.example.moneymaster.databinding.ItemMovimientoBinding;
import com.example.moneymaster.utils.SwipeDeleteManager;
import com.example.moneymaster.utils.TransitionHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovimientosAdapter
        extends RecyclerView.Adapter<MovimientosAdapter.ViewHolder>
        implements SwipeDeleteManager.AdapterContract<MovimientoReciente> {

    public interface OnItemClickListener {
        void onItemClick(MovimientoReciente movimiento);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(MovimientoReciente movimiento);
    }

    private final Context context;
    private List<MovimientoReciente> items = new ArrayList<>();
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    private int lastAnimatedPosition = -1;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

    public MovimientosAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    //DiffUtil

    public void submitList(List<MovimientoReciente> newList) {
        List<MovimientoReciente> safeNew = newList != null ? newList : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return safeNew.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return items.get(oldPos).getId() == safeNew.get(newPos).getId()
                        && items.get(oldPos).getTipo() == safeNew.get(newPos).getTipo();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return items.get(oldPos).equals(safeNew.get(newPos));
            }
        });
        items = new ArrayList<>(safeNew);
        result.dispatchUpdatesTo(this);
    }

    //Eliminación con animación

    public void removeItemWithAnimation(RecyclerView recyclerView, int position,
                                        Runnable afterRemove) {
        RecyclerView.ViewHolder holder =
                recyclerView.findViewHolderForAdapterPosition(position);
        if (holder == null) {
            if (position < items.size()) {
                items.remove(position);
                notifyItemRemoved(position);
            }
            if (afterRemove != null) afterRemove.run();
            return;
        }

        Animation removeAnim = TransitionHelper.getItemRemoveAnimation(context);
        removeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a) { }
            @Override public void onAnimationRepeat(Animation a) { }
            @Override
            public void onAnimationEnd(Animation a) {
                if (position < items.size()) {
                    items.remove(position);
                    notifyItemRemoved(position);
                }
                if (afterRemove != null) afterRemove.run();
            }
        });
        holder.itemView.startAnimation(removeAnim);
    }

    //SwipeDeleteManager.AdapterContract

    @Override
    public MovimientoReciente getItemAt(int position) {
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
    public void restoreItem(MovimientoReciente item, int position) {
        int safePos = Math.min(position, items.size());
        items.add(safePos, item);
        notifyItemInserted(safePos);
    }

    //RecyclerView.Adapter ─

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovimientoBinding binding = ItemMovimientoBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
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
                if (clickListener != null && pos != RecyclerView.NO_ID
                        && pos < items.size()) {
                    clickListener.onItemClick(items.get(pos));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_ID
                        && pos < items.size()) {
                    return longClickListener.onItemLongClick(items.get(pos));
                }
                return false;
            });
        }

        public void bind(MovimientoReciente mov) {

            // Título
            binding.tvTitulo.setText(
                    com.example.moneymaster.ui.categories.CategoryAdapter.resolverNombre(
                            context, mov.getNombreCategoria()));

            // Descripción
            String desc = mov.getDescripcion();
            if (desc != null && !desc.isEmpty()) {
                binding.tvDescripcion.setVisibility(View.VISIBLE);
                binding.tvDescripcion.setText(desc);
            } else {
                binding.tvDescripcion.setVisibility(View.GONE);
            }

            // Fecha
            binding.tvFecha.setText(dateFormat.format(new Date(mov.getFecha())));

            // Importe
            String importeStr = currencyFormat.format(Math.abs(mov.getImporte()));
            if (mov.getTipo() == MovimientoReciente.Tipo.GASTO) {
                binding.tvCantidad.setText("-" + importeStr);
                binding.tvCantidad.setTextColor(context.getColor(R.color.expense_red));
            } else {
                binding.tvCantidad.setText("+" + importeStr);
                binding.tvCantidad.setTextColor(context.getColor(R.color.income_green));
            }

            // Color del icono
            try {
                int color = Color.parseColor(mov.getColorCategoria());
                int alpha30 = Color.argb(77,
                        Color.red(color), Color.green(color), Color.blue(color));
                binding.viewIconoBg.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(alpha30));
                binding.ivCategoriaIcono.setColorFilter(color);
            } catch (IllegalArgumentException ignored) { }

            // Icono — con fallback seguro ante SVGs inválidos
            String iconoNombre = mov.getIconoNombre();
            int iconoRes = 0;
            if (iconoNombre != null && !iconoNombre.isEmpty()) {
                iconoRes = context.getResources().getIdentifier(
                        iconoNombre, "drawable", context.getPackageName());
            }
            try {
                if (iconoRes != 0) {
                    binding.ivCategoriaIcono.setImageResource(iconoRes);
                } else {
                    binding.ivCategoriaIcono.setImageResource(R.drawable.ic_category_default);
                }
            } catch (Exception e) {
                binding.ivCategoriaIcono.setImageResource(R.drawable.ic_category_default);
            }

            // Miniatura foto
            String fotoRuta = mov.getFotoRuta();
            if (fotoRuta != null && !fotoRuta.isEmpty()) {
                binding.ivFotoMiniatura.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(fotoRuta)
                        .centerCrop()
                        .into(binding.ivFotoMiniatura);
            } else {
                binding.ivFotoMiniatura.setVisibility(View.GONE);
            }
        }
    }
}

package com.example.moneymaster.ui.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.moneymaster.R;
import com.example.moneymaster.data.model.MovimientoReciente;
import com.example.moneymaster.databinding.ItemMovimientoBinding;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MovimientoAdapter extends ListAdapter<MovimientoReciente, MovimientoAdapter.MovimientoViewHolder> {

    //Callbacks

    public interface OnMovimientoClickListener {
        void onMovimientoClick(MovimientoReciente item);
    }

    public interface OnMovimientoDeleteListener {
        void onMovimientoDelete(MovimientoReciente item, int position);
    }

    //ahora recibe fotoId además de ruta para abrir ImageViewerActivity
    public interface OnFotoClickListener {
        void onFotoClick(int fotoId, String fotoRuta);
    }

    private final OnMovimientoClickListener  clickListener;
    private final OnMovimientoDeleteListener deleteListener;
    private OnFotoClickListener              fotoClickListener;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

    private static final DiffUtil.ItemCallback<MovimientoReciente> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MovimientoReciente>() {
                @Override
                public boolean areItemsTheSame(@NonNull MovimientoReciente o,
                                               @NonNull MovimientoReciente n) {
                    return o.getId() == n.getId() && o.getTipo() == n.getTipo();
                }

                @Override
                public boolean areContentsTheSame(@NonNull MovimientoReciente o,
                                                  @NonNull MovimientoReciente n) {
                    return o.equals(n);
                }
            };

    //Constructor

    public MovimientoAdapter(OnMovimientoClickListener clickListener,
                             OnMovimientoDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.clickListener  = clickListener;
        this.deleteListener = deleteListener;
    }

    public void setOnFotoClickListener(OnFotoClickListener listener) {
        this.fotoClickListener = listener;
    }

    //Ciclo de vida del adapter

    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovimientoBinding binding = ItemMovimientoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MovimientoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        holder.bind(getItem(position));
        setEntryAnimation(holder.itemView, position);
    }

    public MovimientoReciente getItemAt(int position) { return getItem(position); }
    public void notifyDeleteAt(int position)          { notifyItemRemoved(position); }

    private void setEntryAnimation(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationX(60f);
        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator fadeIn  = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(view, "translationX", 60f, 0f);
        animSet.playTogether(fadeIn, slideIn);
        animSet.setDuration(280);
        animSet.setStartDelay(position * 40L);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.start();
    }


    //  ViewHolder


    public class MovimientoViewHolder extends RecyclerView.ViewHolder {

        private final ItemMovimientoBinding binding;

        public MovimientoViewHolder(@NonNull ItemMovimientoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MovimientoReciente item) {
            Context ctx = binding.getRoot().getContext();

            binding.tvTitulo.setText(item.getDescripcion());
            binding.tvDescripcion.setText(
                    com.example.moneymaster.ui.categories.CategoryAdapter.resolverNombre(
                            ctx, item.getNombreCategoria()));

            boolean esGasto  = item.getTipo() == MovimientoReciente.Tipo.GASTO;
            String  cantidad = (esGasto ? "- " : "+ ") + formatCantidad(item.getImporte());
            binding.tvCantidad.setText(cantidad);
            binding.tvCantidad.setTextColor(ContextCompat.getColor(ctx,
                    esGasto ? R.color.expense_red : R.color.income_green));

            binding.tvFecha.setText(DATE_FORMAT.format(new Date(item.getFecha())));

            bindIcono(ctx, item);

            bindClickPrincipal(item);
        }


        private void bindClickPrincipal(MovimientoReciente item) {
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) clickListener.onMovimientoClick(item);
            });
        }

        private void bindIcono(Context ctx, MovimientoReciente item) {
            int iconResId = ctx.getResources().getIdentifier(
                    item.getIconoNombre(), "drawable", ctx.getPackageName());
            binding.ivCategoriaIcono.setImageResource(
                    iconResId != 0 ? iconResId : R.drawable.ic_receipt_long);
            try {
                int color = Color.parseColor(item.getColorCategoria());
                binding.ivCategoriaIcono.setColorFilter(
                        new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                binding.viewIconoBg.getBackground().setColorFilter(
                        new PorterDuffColorFilter(
                                adjustAlpha(color, 0.15f), PorterDuff.Mode.SRC_IN));
            } catch (IllegalArgumentException e) {
                binding.ivCategoriaIcono.clearColorFilter();
            }
        }

        private String formatCantidad(double cantidad) {
            return String.format(new Locale("es", "ES"), "%,.2f €", cantidad);
        }

        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        }
    }
}
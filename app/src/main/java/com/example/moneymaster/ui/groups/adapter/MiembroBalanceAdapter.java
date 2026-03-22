package com.example.moneymaster.ui.groups.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.databinding.ItemMiembroBalanceBinding;
import com.example.moneymaster.ui.groups.model.MiembroBalanceItem;

import java.util.Locale;

/**
 * Adapter para la lista de balances por miembro.
 * Cada CardView muestra verde (acreedor), rojo (deudor) o gris (equilibrado).
 */
public class MiembroBalanceAdapter
        extends ListAdapter<MiembroBalanceItem, MiembroBalanceAdapter.ViewHolder> {

    private static final double EPSILON = 0.01;

    // Colores Material 3 hardcodeados (compatibles con API 21+)
    private static final int COLOR_VERDE  = 0xFF2E7D32; // green 800
    private static final int COLOR_ROJO   = 0xFFB71C1C; // red 900
    private static final int COLOR_GRIS   = 0xFF757575; // grey 600
    private static final int COLOR_VERDE_BG = 0xFFE8F5E9;
    private static final int COLOR_ROJO_BG  = 0xFFFFEBEE;
    private static final int COLOR_GRIS_BG  = 0xFFF5F5F5;

    private static final DiffUtil.ItemCallback<MiembroBalanceItem> DIFF =
            new DiffUtil.ItemCallback<MiembroBalanceItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull MiembroBalanceItem a,
                                               @NonNull MiembroBalanceItem b) {
                    return a.nombreMiembro.equals(b.nombreMiembro);
                }
                @Override
                public boolean areContentsTheSame(@NonNull MiembroBalanceItem a,
                                                  @NonNull MiembroBalanceItem b) {
                    return Double.compare(a.balanceNeto, b.balanceNeto) == 0
                            && Double.compare(a.totalPagado, b.totalPagado) == 0;
                }
            };

    public MiembroBalanceAdapter() { super(DIFF); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMiembroBalanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMiembroBalanceBinding binding;

        ViewHolder(ItemMiembroBalanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MiembroBalanceItem item) {
            // Nombre e inicial
            binding.textViewNombreMiembro.setText(item.nombreMiembro);
            String initial = item.nombreMiembro.isEmpty()
                    ? "?" : String.valueOf(item.nombreMiembro.charAt(0)).toUpperCase();
            binding.textViewInicialMiembro.setText(initial);

            // Color avatar
            try {
                if (item.colorMiembro != null) {
                    binding.viewAvatarColor.setBackgroundColor(
                            Color.parseColor(item.colorMiembro));
                }
            } catch (IllegalArgumentException ignored) {}

            // Cuota ideal
            binding.textViewCuotaIdeal.setText(
                    String.format(new Locale("es", "ES"), "Cuota: %.2f EUR", item.cuotaIdeal));

            // Balance neto con color
            String signo = item.balanceNeto > 0 ? "+" : "";
            binding.textViewBalanceNeto.setText(
                    String.format(new Locale("es", "ES"), "%s%.2f EUR", signo, item.balanceNeto));

            // Texto descriptivo y colores según estado
            int colorTexto, colorFondo;
            String etiqueta;

            if (item.balanceNeto > EPSILON) {
                // Acreedor: recupera dinero
                colorTexto = COLOR_VERDE;
                colorFondo = COLOR_VERDE_BG;
                etiqueta   = "Recupera dinero";
            } else if (item.balanceNeto < -EPSILON) {
                // Deudor: debe dinero
                colorTexto = COLOR_ROJO;
                colorFondo = COLOR_ROJO_BG;
                etiqueta   = "Debe dinero";
            } else {
                // Equilibrado
                colorTexto = COLOR_GRIS;
                colorFondo = COLOR_GRIS_BG;
                etiqueta   = "Al dia";
            }

            binding.textViewBalanceNeto.setTextColor(colorTexto);
            binding.textViewEstado.setText(etiqueta);
            binding.textViewEstado.setTextColor(colorTexto);
            binding.cardBalance.setCardBackgroundColor(colorFondo);
        }
    }
}
package com.example.moneymaster.ui.groups.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.databinding.ItemDeudaBinding;
import com.example.moneymaster.ui.groups.model.DeudaItem;

import java.util.Locale;

/**
 * Adapter para la lista de transacciones sugeridas.
 * Cada fila muestra "X debe Y€ a Z" con un botón "Marcar como pagado".
 * Las deudas ya pagadas se muestran tachadas y deshabilitadas.
 */
public class DeudaAdapter extends ListAdapter<DeudaItem, DeudaAdapter.DeudaViewHolder> {

    public interface OnMarcarPagadaListener {
        void onMarcarPagada(int posicion);
    }

    private static final DiffUtil.ItemCallback<DeudaItem> DIFF =
            new DiffUtil.ItemCallback<DeudaItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull DeudaItem a, @NonNull DeudaItem b) {
                    return a.nombreDeudor.equals(b.nombreDeudor)
                            && a.nombreAcreedor.equals(b.nombreAcreedor);
                }
                @Override
                public boolean areContentsTheSame(@NonNull DeudaItem a, @NonNull DeudaItem b) {
                    return Double.compare(a.monto, b.monto) == 0
                            && a.pagado == b.pagado;
                }
            };

    private final OnMarcarPagadaListener listener;

    public DeudaAdapter(OnMarcarPagadaListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeudaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeudaViewHolder(ItemDeudaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeudaViewHolder holder, int position) {
        holder.bind(getItem(position), position, listener);
    }

    static class DeudaViewHolder extends RecyclerView.ViewHolder {

        private final ItemDeudaBinding binding;

        DeudaViewHolder(ItemDeudaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DeudaItem item, int position, OnMarcarPagadaListener listener) {
            // Texto principal: "Carlos debe 30,00 EUR a Ana"
            String texto = String.format(
                    new Locale("es", "ES"),
                    "%s debe %.2f EUR a %s",
                    item.nombreDeudor,
                    item.monto,
                    item.nombreAcreedor);
            binding.textViewDeuda.setText(texto);

            // Color avatar del deudor
            try {
                if (item.colorDeudor != null) {
                    binding.viewDeudorColor.setBackgroundColor(
                            Color.parseColor(item.colorDeudor));
                }
            } catch (IllegalArgumentException ignored) {}

            // Inicial del deudor
            String initial = item.nombreDeudor.isEmpty()
                    ? "?" : String.valueOf(item.nombreDeudor.charAt(0)).toUpperCase();
            binding.textViewDeudorInicial.setText(initial);

            // Estado pagado/pendiente
            if (item.pagado) {
                binding.textViewDeuda.setAlpha(0.4f);
                binding.textViewEstadoDeuda.setText("Pagado");
                binding.textViewEstadoDeuda.setVisibility(View.VISIBLE);
                binding.buttonMarcarPagado.setVisibility(View.GONE);
            } else {
                binding.textViewDeuda.setAlpha(1f);
                binding.textViewEstadoDeuda.setVisibility(View.GONE);
                binding.buttonMarcarPagado.setVisibility(View.VISIBLE);
                binding.buttonMarcarPagado.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) listener.onMarcarPagada(pos);
                });
            }
        }
    }
}

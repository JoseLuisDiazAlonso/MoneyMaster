package com.example.moneymaster.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.data.model.GroupWithDetails;
import com.example.moneymaster.databinding.ItemGroupBinding;

import java.util.Locale;

/**
 * Adapter para la lista de grupos.
 *
 * Usa ListAdapter + DiffUtil para actualizar sólo las filas que cambian,
 * evitando parpadeos y animando inserciones/eliminaciones automáticamente.
 */
public class GroupAdapter extends ListAdapter<GroupWithDetails, GroupAdapter.GroupViewHolder> {

    // ─── DiffUtil ────────────────────────────────────────────────────────────

    private static final DiffUtil.ItemCallback<GroupWithDetails> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GroupWithDetails>() {

                @Override
                public boolean areItemsTheSame(@NonNull GroupWithDetails oldItem,
                                               @NonNull GroupWithDetails newItem) {
                    // Compara por PK: mismo registro físico
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull GroupWithDetails oldItem,
                                                  @NonNull GroupWithDetails newItem) {
                    // Compara contenido: si cambia algo refresca la tarjeta
                    return oldItem.nombre.equals(newItem.nombre)
                            && oldItem.numMiembros == newItem.numMiembros
                            && Double.compare(oldItem.balanceTotal, newItem.balanceTotal) == 0;
                }
            };

    // ─── Interfaz de click ───────────────────────────────────────────────────

    public interface OnGroupClickListener {
        void onGroupClick(GroupWithDetails group);
    }

    private final OnGroupClickListener listener;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public GroupAdapter(OnGroupClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // ─── ViewHolder ──────────────────────────────────────────────────────────

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    // ─── ViewHolder class ────────────────────────────────────────────────────

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        private final ItemGroupBinding binding;

        GroupViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupWithDetails group, OnGroupClickListener listener) {

            // Nombre del grupo
            binding.textViewGroupName.setText(group.nombre);

            // Número de miembros
            String miembros = group.numMiembros == 1
                    ? "1 miembro"
                    : group.numMiembros + " miembros";
            binding.textViewGroupMembers.setText(miembros);

            // Balance total formateado con signo
            String balance = String.format(
                    new Locale("es", "ES"),
                    "%s%.2f €",
                    group.balanceTotal >= 0 ? "+" : "",
                    group.balanceTotal
            );
            binding.textViewGroupBalance.setText(balance);

            // Color del balance: verde positivo, rojo negativo, gris cero
            int colorRes;
            if (group.balanceTotal > 0) {
                colorRes = com.google.android.material.R.attr.colorTertiary;
            } else if (group.balanceTotal < 0) {
                colorRes = com.google.android.material.R.attr.colorError;
            } else {
                colorRes = com.google.android.material.R.attr.colorOutline;
            }
            // Resolvemos el color desde el atributo del tema Material 3
            int[] attrs = {colorRes};
            android.util.TypedValue typedValue = new android.util.TypedValue();
            binding.getRoot().getContext().getTheme().resolveAttribute(colorRes, typedValue, true);
            binding.textViewGroupBalance.setTextColor(typedValue.data);

            // Inicial del grupo como avatar (primera letra en mayúscula)
            String initial = group.nombre.isEmpty()
                    ? "?"
                    : String.valueOf(group.nombre.charAt(0)).toUpperCase();
            binding.textViewGroupInitial.setText(initial);

            // Click listener
            binding.getRoot().setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}

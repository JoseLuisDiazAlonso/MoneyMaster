package com.example.moneymaster.ui.groups.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.databinding.ItemMemberBalanceBinding;
import com.example.moneymaster.ui.groups.model.MemberBalanceItem;

import java.util.Locale;

public class MemberBalanceAdapter
        extends ListAdapter<MemberBalanceItem, MemberBalanceAdapter.BalanceViewHolder> {

    private static final DiffUtil.ItemCallback<MemberBalanceItem> DIFF =
            new DiffUtil.ItemCallback<MemberBalanceItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull MemberBalanceItem a,
                                               @NonNull MemberBalanceItem b) {
                    return a.nombreMiembro.equals(b.nombreMiembro);
                }
                @Override
                public boolean areContentsTheSame(@NonNull MemberBalanceItem a,
                                                  @NonNull MemberBalanceItem b) {
                    return Double.compare(a.totalPagado, b.totalPagado) == 0;
                }
            };

    public MemberBalanceAdapter() { super(DIFF); }

    @NonNull
    @Override
    public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BalanceViewHolder(ItemMemberBalanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class BalanceViewHolder extends RecyclerView.ViewHolder {

        private final ItemMemberBalanceBinding binding;

        BalanceViewHolder(ItemMemberBalanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemberBalanceItem item) {
            // Nombre
            binding.textViewBalanceName.setText(item.nombreMiembro);

            // Total pagado
            binding.textViewBalanceAmount.setText(
                    String.format(new Locale("es", "ES"), "%.2f €", item.totalPagado));

            // Inicial del nombre
            String initial = item.nombreMiembro.isEmpty()
                    ? "?" : String.valueOf(item.nombreMiembro.charAt(0)).toUpperCase();
            binding.textViewBalanceInitial.setText(initial);

            // Color del avatar
            try {
                if (item.colorMiembro != null) {
                    binding.viewBalanceColor.setBackgroundColor(
                            Color.parseColor(item.colorMiembro));
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
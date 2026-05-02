package com.example.moneymaster.ui.adapter;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.databinding.ItemMemberInputBinding;
import com.example.moneymaster.ui.groups.model.MemberInputItem;

import java.util.ArrayList;
import java.util.List;

public class MemberInputAdapter extends RecyclerView.Adapter<MemberInputAdapter.MemberViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    private final List<MemberInputItem> items = new ArrayList<>();
    private final OnRemoveClickListener removeListener;

    public MemberInputAdapter(OnRemoveClickListener removeListener) {
        this.removeListener = removeListener;
    }

    //Gestión de la lista

    public void addMember(String color) {
        items.add(new MemberInputItem("", color));
        notifyItemInserted(items.size() - 1);
    }

    public void removeMember(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size() - position);
        }
    }

    public List<String> getMemberNames() {
        List<String> nombres = new ArrayList<>();
        for (MemberInputItem item : items) nombres.add(item.nombre);
        return nombres;
    }

    public List<String> getMemberColors() {
        List<String> colores = new ArrayList<>();
        for (MemberInputItem item : items) colores.add(item.color);
        return colores;
    }

    //Adapter overrides

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberInputBinding binding = ItemMemberInputBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(items.get(position), position, removeListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //ViewHolder

    static class MemberViewHolder extends RecyclerView.ViewHolder {

        private final ItemMemberInputBinding binding;
        private TextWatcher currentWatcher;

        MemberViewHolder(ItemMemberInputBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemberInputItem item, int position, OnRemoveClickListener removeListener) {

            // Color del avatar
            try {
                binding.viewMemberColor.setBackgroundColor(Color.parseColor(item.color));
            } catch (IllegalArgumentException e) {
                binding.viewMemberColor.setBackgroundColor(Color.GRAY);
            }

            // Inicial en el avatar
            String initial = item.nombre.isEmpty()
                    ? String.valueOf(position + 1)
                    : String.valueOf(item.nombre.charAt(0)).toUpperCase();
            binding.textViewMemberInitial.setText(initial);

            // Desvincula watcher anterior antes de setText para evitar bucles
            if (currentWatcher != null) {
                binding.editTextMemberName.removeTextChangedListener(currentWatcher);
            }

            binding.editTextMemberName.setText(item.nombre);

            // Vincula nuevo watcher
            currentWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    item.nombre = s.toString();
                    String ini = item.nombre.isEmpty()
                            ? String.valueOf(getAdapterPosition() + 1)
                            : String.valueOf(item.nombre.charAt(0)).toUpperCase();
                    binding.textViewMemberInitial.setText(ini);
                }
            };
            binding.editTextMemberName.addTextChangedListener(currentWatcher);

            // Hint dinámico con número de miembro
            binding.editTextMemberName.setHint("Miembro " + (position + 1));

            // Botón eliminar
            binding.buttonRemoveMember.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_ID && removeListener != null) {
                    removeListener.onRemove(pos);
                }
            });
        }
    }
}
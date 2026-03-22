package com.example.moneymaster.ui.categories;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.databinding.ItemIconGridBinding;

/**
 * Adapter para el grid de selección de iconos en AddCategoryDialog.
 *
 * Muestra cada icono disponible en un botón cuadrado.
 * El icono seleccionado se resalta con un fondo de color primario.
 */
public class IconGridAdapter extends RecyclerView.Adapter<IconGridAdapter.IconViewHolder> {

    public interface OnIconSelectedListener {
        void onIconSelected(String iconName);
    }

    private final String[] icons;
    private String selectedIcon;
    private final OnIconSelectedListener listener;

    public IconGridAdapter(String[] icons, String selectedIcon,
                           OnIconSelectedListener listener) {
        this.icons = icons;
        this.selectedIcon = selectedIcon;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIconGridBinding binding = ItemIconGridBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new IconViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        holder.bind(icons[position]);
    }

    @Override
    public int getItemCount() {
        return icons.length;
    }

    class IconViewHolder extends RecyclerView.ViewHolder {
        private final ItemIconGridBinding binding;

        IconViewHolder(ItemIconGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String iconName) {
            // Resuelve el drawable por nombre
            int resId = binding.getRoot().getContext().getResources().getIdentifier(
                    iconName, "drawable",
                    binding.getRoot().getContext().getPackageName());

            if (resId != 0) {
                binding.imageIcon.setImageResource(resId);
            } else {
                binding.imageIcon.setImageResource(R.drawable.ic_category_default);
            }

            // Resalta el icono seleccionado
            boolean isSelected = iconName.equals(selectedIcon);
            binding.getRoot().setSelected(isSelected);

            if (isSelected) {
                binding.getRoot().setBackgroundResource(R.drawable.bg_icon_selected);
                int[] attrsSelected = {com.google.android.material.R.attr.colorPrimary};
                android.util.TypedValue typedValue = new android.util.TypedValue();
                binding.getRoot().getContext().getTheme().resolveAttribute(
                        com.google.android.material.R.attr.colorPrimary, typedValue, true);
                binding.imageIcon.setColorFilter(typedValue.data);
            } else {
                binding.getRoot().setBackgroundResource(R.drawable.bg_icon_normal);
                android.util.TypedValue typedValue2 = new android.util.TypedValue();
                binding.getRoot().getContext().getTheme().resolveAttribute(
                        com.google.android.material.R.attr.colorOnSurface, typedValue2, true);
                binding.imageIcon.setColorFilter(typedValue2.data);
            }

            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                String previous = selectedIcon;
                selectedIcon = iconName;
                // Actualiza solo los dos items afectados para eficiencia
                int prevPos = findPosition(previous);
                if (prevPos != -1) notifyItemChanged(prevPos);
                notifyItemChanged(getAdapterPosition());
                listener.onIconSelected(iconName);
            });
        }

        private int findPosition(String iconName) {
            for (int i = 0; i < icons.length; i++) {
                if (icons[i].equals(iconName)) return i;
            }
            return -1;
        }
    }
}

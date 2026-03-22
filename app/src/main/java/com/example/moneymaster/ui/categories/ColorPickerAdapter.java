package com.example.moneymaster.ui.categories;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.databinding.ItemColorPickerBinding;

/**
 * Adapter para el selector de colores en AddCategoryDialog.
 * Muestra círculos de color, con un checkmark en el color seleccionado.
 */
public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder> {

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    private final String[] colors;
    private String selectedColor;
    private final OnColorSelectedListener listener;

    public ColorPickerAdapter(String[] colors, String selectedColor,
                              OnColorSelectedListener listener) {
        this.colors = colors;
        this.selectedColor = selectedColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemColorPickerBinding binding = ItemColorPickerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ColorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        holder.bind(colors[position]);
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    class ColorViewHolder extends RecyclerView.ViewHolder {
        private final ItemColorPickerBinding binding;

        ColorViewHolder(ItemColorPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String color) {
            // Dibuja el círculo de color usando GradientDrawable
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor(color));
            binding.viewColorCircle.setBackground(circle);

            // Muestra/oculta el checkmark según selección
            boolean isSelected = color.equals(selectedColor);
            binding.imageCheckmark.setVisibility(isSelected
                    ? android.view.View.VISIBLE
                    : android.view.View.GONE);

            // Borde más grueso en el seleccionado
            if (isSelected) {
                circle.setStroke(4, android.graphics.Color.WHITE);
            }

            binding.getRoot().setOnClickListener(v -> {
                String previous = selectedColor;
                selectedColor = color;
                int prevPos = findPosition(previous);
                if (prevPos != -1) notifyItemChanged(prevPos);
                notifyItemChanged(getAdapterPosition());
                listener.onColorSelected(color);
            });
        }

        private int findPosition(String color) {
            for (int i = 0; i < colors.length; i++) {
                if (colors[i].equals(color)) return i;
            }
            return -1;
        }
    }
}

package com.example.moneymaster.ui.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.GroupWithDetails;
import com.example.moneymaster.databinding.ItemGroupBinding;

import java.util.Locale;

public class GroupAdapter extends ListAdapter<GroupWithDetails, GroupAdapter.GroupViewHolder> {

    //DiffUtil

    private static final DiffUtil.ItemCallback<GroupWithDetails> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GroupWithDetails>() {
                @Override
                public boolean areItemsTheSame(@NonNull GroupWithDetails o,
                                               @NonNull GroupWithDetails n) {
                    return o.id == n.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull GroupWithDetails o,
                                                  @NonNull GroupWithDetails n) {
                    return o.nombre.equals(n.nombre)
                            && o.numMiembros == n.numMiembros
                            && Double.compare(o.balanceTotal, n.balanceTotal) == 0;
                }
            };

    //Interfaces

    public interface OnGroupClickListener {
        void onGroupClick(GroupWithDetails group);
    }

    public interface OnGroupDeleteListener {
        void onGroupDelete(GroupWithDetails group);
    }

    private final OnGroupClickListener  clickListener;
    private OnGroupDeleteListener       deleteListener;

    //Constructor

    public GroupAdapter(OnGroupClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    public void setOnGroupDeleteListener(OnGroupDeleteListener listener) {
        this.deleteListener = listener;
    }

    //ItemTouchHelper (swipe-to-delete)

    public ItemTouchHelper createSwipeToDelete() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID && deleteListener != null) {
                    deleteListener.onGroupDelete(getItem(pos));
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    android.view.View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    paint.setColor(0xFFB00020); // rojo

                    float cornerRadius = 16f *
                            recyclerView.getContext().getResources()
                                    .getDisplayMetrics().density;

                    RectF background;
                    if (dX < 0) {
                        // swipe izquierda
                        background = new RectF(
                                itemView.getRight() + dX,
                                itemView.getTop(),
                                itemView.getRight(),
                                itemView.getBottom());
                    } else {
                        // swipe derecha
                        background = new RectF(
                                itemView.getLeft(),
                                itemView.getTop(),
                                itemView.getLeft() + dX,
                                itemView.getBottom());
                    }
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                    // Icono papelera
                    Drawable icon = ContextCompat.getDrawable(
                            recyclerView.getContext(), R.drawable.ic_delete);
                    if (icon != null) {
                        int iconSize  = (int) (24 * recyclerView.getContext()
                                .getResources().getDisplayMetrics().density);
                        int iconMargin = (itemView.getHeight() - iconSize) / 2;
                        int iconTop    = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + iconSize;

                        if (dX < 0) {
                            int iconRight = itemView.getRight() - iconMargin;
                            icon.setBounds(iconRight - iconSize, iconTop,
                                    iconRight, iconBottom);
                        } else {
                            int iconLeft = itemView.getLeft() + iconMargin;
                            icon.setBounds(iconLeft, iconTop,
                                    iconLeft + iconSize, iconBottom);
                        }
                        icon.setTint(0xFFFFFFFF);
                        icon.draw(c);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive);
            }
        });
    }

    //Inflate

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener);
    }

    //ViewHolder

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        private final ItemGroupBinding binding;

        GroupViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupWithDetails group, OnGroupClickListener listener) {
            binding.textViewGroupName.setText(group.nombre);

            String miembros = group.numMiembros == 1
                    ? "1 miembro"
                    : group.numMiembros + " miembros";
            binding.textViewGroupMembers.setText(miembros);

            String balance = String.format(
                    new Locale("es", "ES"),
                    "%s%.2f €",
                    group.balanceTotal >= 0 ? "+" : "",
                    group.balanceTotal);
            binding.textViewGroupBalance.setText(balance);

            int colorAttr;
            if (group.balanceTotal > 0) {
                colorAttr = com.google.android.material.R.attr.colorTertiary;
            } else if (group.balanceTotal < 0) {
                colorAttr = com.google.android.material.R.attr.colorError;
            } else {
                colorAttr = com.google.android.material.R.attr.colorOutline;
            }
            android.util.TypedValue typedValue = new android.util.TypedValue();
            binding.getRoot().getContext().getTheme()
                    .resolveAttribute(colorAttr, typedValue, true);
            binding.textViewGroupBalance.setTextColor(typedValue.data);

            String initial = group.nombre.isEmpty()
                    ? "?"
                    : String.valueOf(group.nombre.charAt(0)).toUpperCase();
            binding.textViewGroupInitial.setText(initial);

            binding.getRoot().setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}

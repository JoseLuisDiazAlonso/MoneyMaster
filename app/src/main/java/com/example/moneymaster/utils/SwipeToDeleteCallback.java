package com.example.moneymaster.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.R;


public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    public interface OnSwipeDeleteListener {
        void onDelete(int position);
    }

    private final Context context;
    private final OnSwipeDeleteListener listener;
    private final ColorDrawable background;
    private final Drawable deleteIcon;
    private final Paint paint;
    private static final int ICON_MARGIN = 48;

    public SwipeToDeleteCallback(Context context, OnSwipeDeleteListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.listener = listener;

        background = new ColorDrawable(Color.parseColor("#FFCDD2")); // rojo suave Material
        deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);

        paint = new Paint();
        paint.setColor(Color.parseColor("#D32F2F"));
        paint.setAntiAlias(true);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false; // no drag & drop
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        View itemView = viewHolder.itemView;

        // Animación fade-out + slide antes de notificar la eliminación
        Animation removeAnim = AnimationUtils.loadAnimation(context, R.anim.item_remove);
        removeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                itemView.setVisibility(View.INVISIBLE);
                listener.onDelete(position);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        itemView.startAnimation(removeAnim);
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();

        // Fondo rojo
        if (dX < 0) {
            // Swipe izquierda
            background.setBounds(
                    itemView.getRight() + (int) dX,
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
        } else {
            // Swipe derecha
            background.setBounds(
                    itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getLeft() + (int) dX,
                    itemView.getBottom()
            );
        }
        background.draw(canvas);

        // Icono papelera
        if (deleteIcon != null) {
            int iconTop = itemView.getTop() + (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            int iconLeft, iconRight;

            if (dX < 0) {
                iconRight = itemView.getRight() - ICON_MARGIN;
                iconLeft = iconRight - deleteIcon.getIntrinsicWidth();
            } else {
                iconLeft = itemView.getLeft() + ICON_MARGIN;
                iconRight = iconLeft + deleteIcon.getIntrinsicWidth();
            }

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.setTint(Color.WHITE);
            deleteIcon.draw(canvas);
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.35f; // 35% del ancho para confirmar el swipe
    }
}
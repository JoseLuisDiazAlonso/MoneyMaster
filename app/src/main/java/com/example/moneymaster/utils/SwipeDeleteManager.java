package com.example.moneymaster.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;


public class SwipeDeleteManager<T> {

    //Contrato que debe implementar el Adapter

    public interface AdapterContract<T> {
        /** Devuelve el item en la posición dada. */
        T getItemAt(int position);

        /** Elimina el item de la lista interna y notifica al RecyclerView. */
        void removeItem(int position);

        /** Vuelve a insertar el item en la posición dada (para Deshacer). */
        void restoreItem(T item, int position);
    }

    //Callback de borrado real

    public interface DeleteCallback<T> {
        void onDeleteConfirmed(T item);
    }

    // Constantes

    private static final int  SNACKBAR_DURATION_MS = 3000;
    private static final int  ICON_MARGIN_PX       = 48;
    private static final int  BG_COLOR             = Color.parseColor("#FFCDD2");
    private static final int  ICON_TINT            = Color.WHITE;

    //Campos

    private final View                  anchorView;
    private final RecyclerView          recyclerView;
    private final AdapterContract<T>    adapter;
    private final String                snackbarText;
    private final DeleteCallback<T>     deleteCallback;
    private final Handler               handler = new Handler(Looper.getMainLooper());

    // Almacena el item pendiente de borrado durante el período de gracia
    private T        pendingItem;
    private int      pendingPosition;
    private Runnable pendingDeleteRunnable;
    private Snackbar currentSnackbar;

    private ItemTouchHelper touchHelper;

    //Constructor

    public SwipeDeleteManager(
            @NonNull View anchorView,
            @NonNull RecyclerView recyclerView,
            @NonNull AdapterContract<T> adapter,
            @NonNull String snackbarText,
            @NonNull DeleteCallback<T> deleteCallback) {

        this.anchorView     = anchorView;
        this.recyclerView   = recyclerView;
        this.adapter        = adapter;
        this.snackbarText   = snackbarText;
        this.deleteCallback = deleteCallback;
    }

    //API pública

    /** Adjunta el ItemTouchHelper al RecyclerView. Llamar en onViewCreated(). */
    public void attach() {
        touchHelper = new ItemTouchHelper(buildCallback());
        touchHelper.attachToRecyclerView(recyclerView);
    }

    /** Desconecta el helper y cancela cualquier borrado pendiente. */
    public void detach() {
        cancelPendingDelete();
        if (touchHelper != null) {
            touchHelper.attachToRecyclerView(null);
        }
    }

    //Construcción del ItemTouchHelper.Callback

    private ItemTouchHelper.SimpleCallback buildCallback() {
        return new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_ID) return;

                T item = adapter.getItemAt(position);
                if (item == null) return;

                // Cancelar borrado previo si el usuario hace swipe a otro item
                // antes de que expire el Snackbar anterior
                cancelPendingDelete();

                // Guardar estado para posible Deshacer
                pendingItem     = item;
                pendingPosition = position;

                // Eliminar visualmente del adapter
                adapter.removeItem(position);

                // Mostrar Snackbar
                showUndoSnackbar();

                // Programar borrado real tras SNACKBAR_DURATION_MS
                pendingDeleteRunnable = () -> {
                    deleteCallback.onDeleteConfirmed(pendingItem);
                    pendingItem     = null;
                    pendingPosition = -1;
                };
                handler.postDelayed(pendingDeleteRunnable, SNACKBAR_DURATION_MS);
            }

            //Dibujar fondo rojo + icono papelera

            @Override
            public void onChildDraw(@NonNull Canvas canvas,
                                    @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                View itemView = vh.itemView;
                ColorDrawable background = new ColorDrawable(BG_COLOR);

                if (dX < 0) {
                    background.setBounds(
                            itemView.getRight() + (int) dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom());
                } else {
                    background.setBounds(
                            itemView.getLeft(),
                            itemView.getTop(),
                            itemView.getLeft() + (int) dX,
                            itemView.getBottom());
                }
                background.draw(canvas);

                Drawable icon = ContextCompat.getDrawable(
                        rv.getContext(), android.R.drawable.ic_menu_delete);
                if (icon != null) {
                    int iconH    = icon.getIntrinsicHeight();
                    int iconW    = icon.getIntrinsicWidth();
                    int itemH    = itemView.getBottom() - itemView.getTop();
                    int iconTop  = itemView.getTop() + (itemH - iconH) / 2;
                    int iconLeft, iconRight;

                    if (dX < 0) {
                        iconRight = itemView.getRight() - ICON_MARGIN_PX;
                        iconLeft  = iconRight - iconW;
                    } else {
                        iconLeft  = itemView.getLeft() + ICON_MARGIN_PX;
                        iconRight = iconLeft + iconW;
                    }

                    icon.setBounds(iconLeft, iconTop, iconRight, iconTop + iconH);
                    icon.setTint(ICON_TINT);
                    icon.draw(canvas);
                }

                super.onChildDraw(canvas, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.35f;
            }
        };
    }

    //Snackbar con Deshacer

    private void showUndoSnackbar() {
        if (currentSnackbar != null && currentSnackbar.isShown()) {
            currentSnackbar.dismiss();
        }

        currentSnackbar = Snackbar.make(anchorView, snackbarText, SNACKBAR_DURATION_MS)
                .setAction("Deshacer", v -> undoDelete())
                .setActionTextColor(Color.YELLOW);

        currentSnackbar.show();
    }

    //Lógica de Deshacer

    private void undoDelete() {
        if (pendingItem == null) return;

        // Cancelar el borrado programado
        handler.removeCallbacks(pendingDeleteRunnable);
        pendingDeleteRunnable = null;

        // Devolver el item al adapter en su posición original
        adapter.restoreItem(pendingItem, pendingPosition);

        pendingItem     = null;
        pendingPosition = -1;
    }

    //Cancelar borrado pendiente
    /**
     * Fuerza la ejecución inmediata del borrado pendiente.
     * Llamar desde onDestroyView() para no dejar operaciones colgadas.
     */
    public void cancelPendingDelete() {
        if (pendingDeleteRunnable != null) {
            handler.removeCallbacks(pendingDeleteRunnable);
            // Ejecutar inmediatamente si hay item pendiente
            if (pendingItem != null) {
                deleteCallback.onDeleteConfirmed(pendingItem);
                pendingItem             = null;
                pendingPosition         = -1;
            }
            pendingDeleteRunnable = null;
        }
    }
}

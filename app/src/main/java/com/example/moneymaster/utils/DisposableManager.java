package com.example.moneymaster.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DisposableManager {

    private static final String TAG = "DisposableManager";

    /** Acción de limpieza que se ejecuta al hacer dispose. */
    public interface Disposable {
        void dispose();
    }

    private final List<Disposable> disposables = new ArrayList<>();
    private boolean disposed = false;

    /**
     * Registra una acción de limpieza.
     * Si el manager ya fue disposed, ejecuta la acción inmediatamente.
     */
    public void add(Disposable disposable) {
        if (disposable == null) return;
        if (disposed) {
            // Defensive: si alguien añade tras dispose, limpiar ya
            safeDispose(disposable);
        } else {
            disposables.add(disposable);
        }
    }

    /**
     * Ejecuta todas las acciones de limpieza registradas y vacía la lista.
     * Es idempotente: llamarlo varias veces no produce efectos secundarios.
     */
    public void disposeAll() {
        for (Disposable d : disposables) {
            safeDispose(d);
        }
        disposables.clear();
        disposed = true;
    }

    /** Devuelve el número de disposables pendientes. */
    public int size() {
        return disposables.size();
    }

    private void safeDispose(Disposable d) {
        try {
            d.dispose();
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar listener", e);
        }
    }
}

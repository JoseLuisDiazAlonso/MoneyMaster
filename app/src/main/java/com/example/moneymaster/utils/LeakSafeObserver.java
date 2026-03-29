package com.example.moneymaster.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import java.lang.ref.WeakReference;

/**
 * Card #64 – Manejo de memoria y leaks
 *
 * Observer de LiveData que mantiene al consumidor mediante WeakReference,
 * evitando que LiveData retenga una referencia fuerte a Activity o Fragment.
 *
 * Uso típico en un Fragment / Activity:
 * <pre>
 *   viewModel.getDatos().observe(getViewLifecycleOwner(),
 *       new LeakSafeObserver<>(this, (fragment, lista) -> {
 *           fragment.binding.recycler.setAdapter(new MiAdapter(lista));
 *       }));
 * </pre>
 *
 * @param <O> Tipo del observador (Activity, Fragment, etc.)
 * @param <T> Tipo del dato emitido por LiveData
 */
public class LeakSafeObserver<O, T> implements Observer<T> {

    /** Contrato que recibe el observador y el dato de forma segura. */
    public interface Callback<O, T> {
        void onChanged(@NonNull O observer, T value);
    }

    private final WeakReference<O> weakRef;
    private final Callback<O, T> callback;

    public LeakSafeObserver(@NonNull O observer, @NonNull Callback<O, T> callback) {
        this.weakRef = new WeakReference<>(observer);
        this.callback = callback;
    }

    @Override
    public void onChanged(T value) {
        O observer = weakRef.get();
        if (observer != null) {
            callback.onChanged(observer, value);
        }
        // Si el observador fue recolectado por el GC, simplemente no hacemos nada.
    }
}

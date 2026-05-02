package com.example.moneymaster.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.example.moneymaster.utils.DisposableManager;


public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    private VB _binding;

    /** Acceso seguro al binding (sólo válido entre onCreateView y onDestroyView). */
    protected VB binding;

    /** Gestor centralizado de limpieza de listeners. */
    protected final DisposableManager disposables = new DisposableManager();


    // Contrato para subclases

    /**
     * Infla el ViewBinding específico del Fragment.
     * Implementar en cada subclase:
     * <pre>
     *   return FragmentMiBinding.inflate(inflater, container, false);
     * </pre>
     */
    protected abstract VB inflateBinding(@NonNull LayoutInflater inflater,
                                         @Nullable ViewGroup container);


    // Ciclo de vida

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater,
                                   @Nullable ViewGroup container,
                                   @Nullable Bundle savedInstanceState) {
        _binding = inflateBinding(inflater, container);
        binding = _binding;
        return _binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 1. Limpiar todos los listeners registrados
        disposables.disposeAll();

        // 2. Cancelar peticiones Glide pendientes en este Fragment
        try {
            Glide.with(this).pauseRequests();
        } catch (Exception ignored) {
            // Glide puede lanzar si el contexto ya no es válido
        }

        // 3. Anular la referencia al binding para liberar la jerarquía de vistas
        binding = null;
        _binding = null;
    }
}
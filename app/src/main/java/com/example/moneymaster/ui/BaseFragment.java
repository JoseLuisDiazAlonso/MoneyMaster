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

/**
 * Card #64 – Manejo de memoria y leaks
 *
 * Fragment base que elimina automáticamente las tres fuentes más comunes
 * de leaks en Fragments:
 *
 *  1. ViewBinding: la referencia al binding se anula en onDestroyView()
 *     para no retener la jerarquía de vistas cuando el Fragment vive
 *     en el back-stack.
 *
 *  2. Listeners / callbacks: DisposableManager ejecuta todas las
 *     acciones de limpieza registradas.
 *
 *  3. Glide: se cancela cualquier petición pendiente ligada al Fragment.
 *
 * Uso:
 * <pre>
 *   public class MiFragment extends BaseFragment<FragmentMiBinding> {
 *
 *       {@literal @}Override
 *       protected FragmentMiBinding inflateBinding(LayoutInflater inflater,
 *                                                   ViewGroup container) {
 *           return FragmentMiBinding.inflate(inflater, container, false);
 *       }
 *
 *       {@literal @}Override
 *       public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
 *           super.onViewCreated(view, savedInstanceState);
 *
 *           // Registrar listeners que necesiten limpieza:
 *           TextWatcher watcher = new MyWatcher();
 *           binding.etSearch.addTextChangedListener(watcher);
 *           disposables.add(() -> binding.etSearch.removeTextChangedListener(watcher));
 *       }
 *   }
 * </pre>
 *
 * @param <VB> Tipo del ViewBinding generado por el Fragment
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    private VB _binding;

    /** Acceso seguro al binding (sólo válido entre onCreateView y onDestroyView). */
    protected VB binding;

    /** Gestor centralizado de limpieza de listeners. */
    protected final DisposableManager disposables = new DisposableManager();

    // ─────────────────────────────────────────────────────────────────────────
    // Contrato para subclases
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Infla el ViewBinding específico del Fragment.
     * Implementar en cada subclase:
     * <pre>
     *   return FragmentMiBinding.inflate(inflater, container, false);
     * </pre>
     */
    protected abstract VB inflateBinding(@NonNull LayoutInflater inflater,
                                         @Nullable ViewGroup container);

    // ─────────────────────────────────────────────────────────────────────────
    // Ciclo de vida
    // ─────────────────────────────────────────────────────────────────────────

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
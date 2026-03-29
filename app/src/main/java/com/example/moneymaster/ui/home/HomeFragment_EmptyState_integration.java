package com.example.moneymaster.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.databinding.EmptyStateBinding;
import com.example.moneymaster.databinding.FragmentHomeBinding;
import com.example.moneymaster.R;
import com.example.moneymaster.utils.EmptyStateHelper;

/**
 * Fragmento de ejemplo mostrando SÓLO el código nuevo del Card #56.
 * Integrar estos bloques en el HomeFragment existente.
 */
public class HomeFragment_EmptyState_integration extends Fragment {

    private FragmentHomeBinding binding;
    private EmptyStateHelper emptyStateHelper;   // NUEVO — Card #56

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Card #56: Inicializar EmptyStateHelper ────────────────────────
        EmptyStateBinding emptyBinding = EmptyStateBinding.bind(
                binding.getRoot().findViewById(R.id.layout_empty_state));

        emptyStateHelper = new EmptyStateHelper(emptyBinding);
        emptyStateHelper.setup(
                R.drawable.ic_empty_movements,
                R.string.empty_movements_title,
                R.string.empty_movements_subtitle,
                R.string.empty_movements_cta,
                v -> {
                    // Abrir diálogo o Activity de añadir movimiento
                    // Sustituir por la navegación que ya uses en HomeFragment:
                    // startActivity(new Intent(requireContext(), AgregarGastoActivity.class));
                }
        );

    }
}
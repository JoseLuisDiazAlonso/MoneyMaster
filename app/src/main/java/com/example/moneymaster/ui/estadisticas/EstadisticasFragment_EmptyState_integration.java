package com.example.moneymaster.ui.estadisticas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.R;
import com.example.moneymaster.databinding.EmptyStateBinding;
import com.example.moneymaster.databinding.FragmentEstadisticasBinding;
import com.example.moneymaster.utils.EmptyStateHelper;

public class EstadisticasFragment_EmptyState_integration extends Fragment {

    private FragmentEstadisticasBinding binding;
    private EmptyStateHelper emptyStateHelper;   // NUEVO — Card #56

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEstadisticasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Inicializar EmptyStateHelper
        EmptyStateBinding emptyBinding = EmptyStateBinding.bind(
                binding.getRoot().findViewById(R.id.layout_empty_state));

        emptyStateHelper = new EmptyStateHelper(emptyBinding);
        emptyStateHelper.setup(
                R.drawable.ic_empty_statistics,
                R.string.empty_statistics_title,
                R.string.empty_statistics_subtitle,
                R.string.empty_statistics_cta,
                v -> {
                    // Navegar a añadir gasto
                    // startActivity(new Intent(requireContext(), AgregarGastoActivity.class));
                }
        );

    }
}

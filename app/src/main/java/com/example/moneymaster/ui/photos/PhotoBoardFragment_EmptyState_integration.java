package com.example.moneymaster.ui.photos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.R;
import com.example.moneymaster.databinding.EmptyStateBinding;
import com.example.moneymaster.databinding.FragmentPhotoBoardBinding;
import com.example.moneymaster.utils.EmptyStateHelper;

public class PhotoBoardFragment_EmptyState_integration extends Fragment {

    private FragmentPhotoBoardBinding binding;
    private EmptyStateHelper emptyStateHelper;   // NUEVO — Card #56

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhotoBoardBinding.inflate(inflater, container, false);
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
                R.drawable.ic_empty_photos,
                R.string.empty_photos_title,
                R.string.empty_photos_subtitle,
                R.string.empty_photos_cta,
                v -> {
                    // Abrir cámara o galería para añadir foto
                    // Sustituir por la lógica de cámara ya existente en el fragment:
                    // launchCamera();
                }
        );

    }
}
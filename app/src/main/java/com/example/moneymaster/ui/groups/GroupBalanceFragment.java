package com.example.moneymaster.ui.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moneymaster.databinding.FragmentGroupBalanceBinding;
import com.example.moneymaster.ui.groups.adapter.MiembroBalanceAdapter;
import com.example.moneymaster.ui.groups.adapter.DeudaAdapter;

/**
 * Fragment que muestra el balance del grupo:
 *   - Sección 1: CardView por miembro con balance neto (verde/rojo/gris)
 *   - Sección 2: Lista de transacciones sugeridas con botón "Marcar como pagado"
 *
 * Recibe grupoId como argumento de Bundle (clave ARG_GRUPO_ID).
 */
public class GroupBalanceFragment extends Fragment {

    public static final String ARG_GRUPO_ID = "grupoId";

    private FragmentGroupBalanceBinding binding;
    private GroupBalanceViewModel viewModel;
    private MiembroBalanceAdapter miembroBalanceAdapter;
    private DeudaAdapter deudaAdapter;

    public static GroupBalanceFragment newInstance(int grupoId) {
        GroupBalanceFragment f = new GroupBalanceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GRUPO_ID, grupoId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupBalanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int grupoId = getArguments() != null
                ? getArguments().getInt(ARG_GRUPO_ID, -1) : -1;
        if (grupoId == -1) {
            requireActivity().onBackPressed();
            return;
        }

        viewModel = new ViewModelProvider(this).get(GroupBalanceViewModel.class);
        viewModel.init(grupoId);

        setupRecyclerViews();
        observeData();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void setupRecyclerViews() {
        // RecyclerView balances por miembro
        miembroBalanceAdapter = new MiembroBalanceAdapter();
        binding.recyclerViewMiembrosBalance.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewMiembrosBalance.setAdapter(miembroBalanceAdapter);
        binding.recyclerViewMiembrosBalance.setNestedScrollingEnabled(false);

        // RecyclerView deudas sugeridas
        deudaAdapter = new DeudaAdapter(posicion ->
                viewModel.marcarPagada(posicion));
        binding.recyclerViewDeudas.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewDeudas.setAdapter(deudaAdapter);
        binding.recyclerViewDeudas.setNestedScrollingEnabled(false);
    }

    // ─── Observadores ─────────────────────────────────────────────────────────

    private void observeData() {
        // Balances por miembro
        viewModel.getBalancesMiembros().observe(getViewLifecycleOwner(), balances -> {
            miembroBalanceAdapter.submitList(balances);
            binding.recyclerViewMiembrosBalance.setVisibility(
                    balances == null || balances.isEmpty() ? View.GONE : View.VISIBLE);
        });

        // Deudas sugeridas
        viewModel.getDeudas().observe(getViewLifecycleOwner(), deudas -> {
            // Filtrar las ya pagadas para la lista
            deudaAdapter.submitList(deudas);

            boolean hayDeudas = deudas != null && !deudas.isEmpty();
            boolean todasPagadas = hayDeudas && deudas.stream().allMatch(d -> d.pagado);

            binding.recyclerViewDeudas.setVisibility(
                    hayDeudas && !todasPagadas ? View.VISIBLE : View.GONE);
            binding.layoutSaldado.setVisibility(
                    todasPagadas ? View.VISIBLE : View.GONE);
            binding.layoutSinDeudas.setVisibility(
                    !hayDeudas ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
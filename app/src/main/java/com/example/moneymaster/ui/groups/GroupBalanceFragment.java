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
import com.example.moneymaster.ui.groups.adapter.DeudaAdapter;
import com.example.moneymaster.ui.groups.adapter.MiembroBalanceAdapter;
import com.example.moneymaster.ui.groups.model.DeudaItem;

import java.util.List;

public class GroupBalanceFragment extends Fragment {

    public static final String ARG_GRUPO_ID = "grupoId";

    private FragmentGroupBalanceBinding binding;
    private GroupBalanceViewModel       viewModel;
    private MiembroBalanceAdapter       miembroBalanceAdapter;
    private DeudaAdapter                deudaAdapter;

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

    private void setupRecyclerViews() {
        miembroBalanceAdapter = new MiembroBalanceAdapter();
        binding.recyclerViewMiembrosBalance.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewMiembrosBalance.setAdapter(miembroBalanceAdapter);
        binding.recyclerViewMiembrosBalance.setNestedScrollingEnabled(false);

        deudaAdapter = new DeudaAdapter(posicion -> viewModel.marcarPagada(posicion));
        binding.recyclerViewDeudas.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewDeudas.setAdapter(deudaAdapter);
        binding.recyclerViewDeudas.setNestedScrollingEnabled(false);
    }

    private void observeData() {
        viewModel.getBalancesMiembros().observe(getViewLifecycleOwner(), balances -> {
            miembroBalanceAdapter.submitList(balances);
            binding.recyclerViewMiembrosBalance.setVisibility(
                    balances == null || balances.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getDeudas().observe(getViewLifecycleOwner(), deudas -> {
            if (deudas == null || deudas.isEmpty()) {
                binding.recyclerViewDeudas.setVisibility(View.GONE);
                binding.layoutSaldado.setVisibility(View.GONE);
                binding.layoutSinDeudas.setVisibility(View.VISIBLE);
                return;
            }

            boolean todasPagadas = todasPagadas(deudas);
            deudaAdapter.submitList(deudas);

            if (todasPagadas) {
                binding.recyclerViewDeudas.setVisibility(View.GONE);
                binding.layoutSaldado.setVisibility(View.VISIBLE);
                binding.layoutSinDeudas.setVisibility(View.GONE);
            } else {
                binding.recyclerViewDeudas.setVisibility(View.VISIBLE);
                binding.layoutSaldado.setVisibility(View.GONE);
                binding.layoutSinDeudas.setVisibility(View.GONE);
            }
        });
    }

    private boolean todasPagadas(List<DeudaItem> deudas) {
        for (DeudaItem d : deudas) {
            if (!d.pagado) return false;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
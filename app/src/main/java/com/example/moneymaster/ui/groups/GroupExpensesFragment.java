package com.example.moneymaster.ui.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.databinding.FragmentGroupExpensesBinding;
import com.example.moneymaster.ui.adapter.GroupExpenseAdapter;
import com.example.moneymaster.ui.expenses.FotoViewerDialog;
import com.example.moneymaster.ui.groups.adapter.MemberBalanceAdapter;
import com.example.moneymaster.util.SwipeDeleteManager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * GroupExpensesFragment — Card #57
 *
 * Cambios respecto a la versión anterior:
 *  - Añade swipeDeleteManager para gestionar swipe-to-delete con Deshacer
 *  - onDestroyView() llama a swipeDeleteManager.cancelPendingDelete()
 *    para forzar borrado inmediato si el fragment se destruye con un
 *    item pendiente de eliminar
 *  - El resto del fragment permanece sin cambios
 */
public class GroupExpensesFragment extends Fragment {

    public static final String ARG_GRUPO_ID = "grupoId";

    private FragmentGroupExpensesBinding    binding;
    private GroupExpensesViewModel          viewModel;
    private GroupExpenseAdapter             expenseAdapter;
    private MemberBalanceAdapter            balanceAdapter;
    private SwipeDeleteManager<GastoGrupo>  swipeDeleteManager;  // Card #57

    public static GroupExpensesFragment newInstance(int grupoId) {
        GroupExpensesFragment f = new GroupExpensesFragment();
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
        binding = FragmentGroupExpensesBinding.inflate(inflater, container, false);
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

        viewModel = new ViewModelProvider(this).get(GroupExpensesViewModel.class);
        viewModel.init(grupoId);

        setupRecyclerViews();
        setupSwipeDelete();   // Card #57
        setupFab(grupoId);
        observeData();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void setupRecyclerViews() {
        // RecyclerView de balances por miembro
        balanceAdapter = new MemberBalanceAdapter();
        binding.recyclerViewBalances.setLayoutManager(
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewBalances.setAdapter(balanceAdapter);

        // RecyclerView de gastos
        expenseAdapter = new GroupExpenseAdapter(gasto ->
                Toast.makeText(requireContext(),
                        "Gasto: " + gasto.descripcion,
                        Toast.LENGTH_SHORT).show());


        binding.recyclerViewExpenses.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewExpenses.setAdapter(expenseAdapter);
        binding.recyclerViewExpenses.setNestedScrollingEnabled(false);
    }

    // ─── Card #57: Swipe to delete ────────────────────────────────────────────

    private void setupSwipeDelete() {
        swipeDeleteManager = new SwipeDeleteManager<>(
                requireView(),
                binding.recyclerViewExpenses,
                expenseAdapter,
                "Gasto eliminado",
                gasto -> viewModel.eliminarGasto(gasto)
        );
        swipeDeleteManager.attach();
    }

    private void setupFab(int grupoId) {
        binding.fabAddExpense.setOnClickListener(v -> {
            AddGroupExpenseDialog dialog = AddGroupExpenseDialog.newInstance(grupoId);
            dialog.show(getChildFragmentManager(), AddGroupExpenseDialog.TAG);
        });
    }

    // ─── Observadores ─────────────────────────────────────────────────────────

    private void observeData() {
        viewModel.getGrupo().observe(getViewLifecycleOwner(), grupo -> {
            if (grupo != null) {
                binding.textViewGroupTitle.setText(grupo.nombre);
            }
        });

        viewModel.getTotalGrupo().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                String formatted = String.format(
                        new Locale("es", "ES"), "%.2f \u20ac", total);
                binding.textViewGroupTotal.setText(formatted);
            }
        });

        viewModel.getBalancesPorMiembro().observe(getViewLifecycleOwner(), balances -> {
            balanceAdapter.submitList(balances);
            binding.recyclerViewBalances.setVisibility(
                    balances == null || balances.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getGastos().observe(getViewLifecycleOwner(), gastos -> {
            expenseAdapter.submitList(gastos);
            cargarRutasFotos(gastos);

            if (gastos == null || gastos.isEmpty()) {
                binding.recyclerViewExpenses.setVisibility(View.GONE);
                binding.layoutEmptyExpenses.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewExpenses.setVisibility(View.VISIBLE);
                binding.layoutEmptyExpenses.setVisibility(View.GONE);
            }
        });
    }

    // ─── Card #33: carga mapa fotoReciboId → rutaArchivo ─────────────────────

    private void cargarRutasFotos(List<GastoGrupo> gastos) {
        if (gastos == null || gastos.isEmpty()) {
            expenseAdapter.setRutasFoto(new HashMap<>());
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            Map<Integer, String> rutas = new HashMap<>();


            requireActivity().runOnUiThread(() ->
                    expenseAdapter.setRutasFoto(rutas));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Card #57: forzar borrado inmediato si el fragment se destruye
        // con un gasto pendiente de confirmar
        if (swipeDeleteManager != null) {
            swipeDeleteManager.cancelPendingDelete();
        }
        binding = null;
    }
}
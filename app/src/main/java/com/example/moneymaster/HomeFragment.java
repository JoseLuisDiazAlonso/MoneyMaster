package com.example.moneymaster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.GastoConCategoria;
import com.example.moneymaster.data.model.MovimientoReciente;
import com.example.moneymaster.databinding.FragmentHomeBinding;
import com.example.moneymaster.ui.ViewModel.DashboardViewModel;
import com.example.moneymaster.ui.adapter.MovimientosAdapter;
import com.example.moneymaster.ui.detalle.MovimientoDetalleActivity;
import com.example.moneymaster.ui.search.BusquedaViewModel;
import com.example.moneymaster.ui.search.FiltroGasto;
import com.example.moneymaster.ui.search.SearchFilterBottomSheet;
import com.example.moneymaster.util.SwipeDeleteManager;
import com.example.moneymaster.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding                    binding;
    private DashboardViewModel                     dashboardViewModel;
    private BusquedaViewModel                      busquedaViewModel;
    private MovimientosAdapter                     adapter;
    private SwipeDeleteManager<MovimientoReciente> swipeDeleteManager;
    private List<CategoriaGasto>                   categorias           = new ArrayList<>();
    private boolean                                hayFiltroActivo      = false;
    private List<MovimientoReciente>               ultimaListaDashboard = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dashboardViewModel = new ViewModelProvider(requireActivity())
                .get(DashboardViewModel.class);
        busquedaViewModel = new ViewModelProvider(this).get(BusquedaViewModel.class);
        busquedaViewModel.initPersonal();

        setupRecyclerView();
        setupSwipeDelete();
        setupSearch();
        setupMesSelector();
        setupBotones();
        cargarCategorias();
        observeData();
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new MovimientosAdapter(requireContext());

        // FIX: conectar click listener para abrir detalle del movimiento
        adapter.setOnItemClickListener(movimiento ->
                MovimientoDetalleActivity.start(
                        requireContext(),
                        movimiento.getId(),
                        movimiento.getTipo())
        );

        binding.rvMovimientos.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.rvMovimientos.setAdapter(adapter);
    }

    // ── Swipe delete ──────────────────────────────────────────────────────────

    private void setupSwipeDelete() {
        swipeDeleteManager = new SwipeDeleteManager<>(
                requireView(),
                binding.rvMovimientos,
                adapter,
                "Movimiento eliminado",
                movimiento -> dashboardViewModel.eliminarMovimiento(movimiento)
        );
        swipeDeleteManager.attach();
    }

    // ── Botones ───────────────────────────────────────────────────────────────

    private void setupBotones() {
        binding.btnVerTodos.setOnClickListener(v -> mostrarTodosLosMeses());
    }

    private void mostrarTodosLosMeses() {
        hayFiltroActivo = true;
        limpiarBusquedaVisual();
        busquedaViewModel.aplicarFiltro(FiltroGasto.empty());
        busquedaViewModel.setQuery("");
        binding.tvContadorResultados.setVisibility(View.GONE);
    }

    private void limpiarBusquedaVisual() {
        binding.searchView.setQuery("", false);
        binding.searchView.clearFocus();
        binding.btnFiltros.setAlpha(0.6f);
    }

    // ── SearchView + filtros ──────────────────────────────────────────────────

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                activarFiltro(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    desactivarFiltro();
                } else {
                    activarFiltro(newText.trim());
                }
                return true;
            }
        });

        binding.searchView.setOnCloseListener(() -> {
            desactivarFiltro();
            return false;
        });

        binding.btnFiltros.setOnClickListener(v -> abrirFiltrosAvanzados());
    }

    private void activarFiltro(String query) {
        hayFiltroActivo = true;
        busquedaViewModel.setQuery(query);
    }

    private void desactivarFiltro() {
        hayFiltroActivo = false;
        busquedaViewModel.aplicarFiltro(FiltroGasto.empty());
        busquedaViewModel.setQuery("");
        binding.tvContadorResultados.setVisibility(View.GONE);
        mostrarMovimientos(ultimaListaDashboard);
    }

    private void abrirFiltrosAvanzados() {
        SearchFilterBottomSheet sheet = SearchFilterBottomSheet.newInstance(
                busquedaViewModel.getFiltroSnapshot(),
                categorias
        );
        sheet.setOnFiltroAplicadoListener(filtro -> {
            hayFiltroActivo = !filtro.isEmpty();
            busquedaViewModel.aplicarFiltro(filtro);
            int count = filtro.contarFiltrosActivos();
            binding.btnFiltros.setAlpha(count > 0 ? 1.0f : 0.6f);
            if (!hayFiltroActivo) {
                mostrarMovimientos(ultimaListaDashboard);
            }
        });
        sheet.show(getChildFragmentManager(), SearchFilterBottomSheet.TAG);
    }

    private void cargarCategorias() {
        int usuarioId = new SessionManager(requireContext()).getUserId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categorias = AppDatabase.getDatabase(requireContext())
                    .categoriaGastoDao()
                    .getCategoriasSync(usuarioId);
        });
    }

    // ── Selector de mes ───────────────────────────────────────────────────────

    private void setupMesSelector() {
        binding.btnMesAnterior.setOnClickListener(
                v -> dashboardViewModel.mesAnterior());
        binding.btnMesSiguiente.setOnClickListener(
                v -> dashboardViewModel.mesSiguiente());
    }

    // ── Observadores ─────────────────────────────────────────────────────────

    private void observeData() {

        dashboardViewModel.etiquetaMes.observe(getViewLifecycleOwner(),
                label -> binding.tvMesActual.setText(label));

        dashboardViewModel.balance.observe(getViewLifecycleOwner(), balance -> {
            if (balance == null) return;
            boolean positivo = balance >= 0;
            binding.tvSignoBalance.setText(positivo ? "▲" : "▼");
            binding.tvBalance.setText(
                    String.format(new java.util.Locale("es", "ES"),
                            "%s%.2f €", positivo ? "+" : "", balance));
        });

        dashboardViewModel.totalIngresos.observe(getViewLifecycleOwner(), total -> {
            if (total != null)
                binding.tvTotalIngresos.setText(
                        String.format(new java.util.Locale("es", "ES"),
                                "%.2f €", total));
        });

        dashboardViewModel.totalGastos.observe(getViewLifecycleOwner(), total -> {
            if (total != null)
                binding.tvTotalGastos.setText(
                        String.format(new java.util.Locale("es", "ES"),
                                "%.2f €", total));
        });

        // Lista del Dashboard — solo actúa si NO hay filtro activo
        dashboardViewModel.movimientos.observe(getViewLifecycleOwner(), movimientos -> {
            if (movimientos == null) movimientos = new ArrayList<>();
            ultimaListaDashboard = movimientos;
            if (!hayFiltroActivo) {
                mostrarMovimientos(movimientos);
            }
        });

        // Resultados de búsqueda — solo actúa si HAY filtro activo
        busquedaViewModel.getResultadosPersonales().observe(
                getViewLifecycleOwner(), gastos -> {
                    if (hayFiltroActivo) {
                        mostrarMovimientos(convertirAMovimientos(gastos));
                    }
                });

        // Contador de resultados
        busquedaViewModel.getContadorPersonales().observe(
                getViewLifecycleOwner(), count -> {
                    if (count == null || !hayFiltroActivo) {
                        binding.tvContadorResultados.setVisibility(View.GONE);
                        return;
                    }
                    FiltroGasto filtro = busquedaViewModel.getFiltroSnapshot();
                    String queryActual = binding.searchView.getQuery().toString().trim();
                    if (filtro.isEmpty() && queryActual.isEmpty()) {
                        binding.tvContadorResultados.setVisibility(View.GONE);
                    } else {
                        binding.tvContadorResultados.setText(
                                count + " resultado" + (count != 1 ? "s" : ""));
                        binding.tvContadorResultados.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void mostrarMovimientos(List<MovimientoReciente> lista) {
        if (lista == null) lista = new ArrayList<>();
        adapter.submitList(new ArrayList<>(lista));
        boolean vacio = lista.isEmpty();
        binding.rvMovimientos.setVisibility(vacio ? View.GONE : View.VISIBLE);
        binding.tvSinMovimientos.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.btnVerTodos.setVisibility(
                (!vacio && !hayFiltroActivo) ? View.VISIBLE : View.GONE);
    }

    private List<MovimientoReciente> convertirAMovimientos(
            List<GastoConCategoria> gastos) {
        List<MovimientoReciente> lista = new ArrayList<>();
        if (gastos == null) return lista;
        for (GastoConCategoria g : gastos) {
            lista.add(new MovimientoReciente(
                    g.id,
                    MovimientoReciente.Tipo.GASTO,
                    g.descripcion,
                    g.nombreCategoria,
                    g.iconoNombre,
                    g.colorCategoria,
                    g.importe,
                    g.fecha
            ));
        }
        return lista;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (swipeDeleteManager != null) swipeDeleteManager.cancelPendingDelete();
        binding = null;
    }
}
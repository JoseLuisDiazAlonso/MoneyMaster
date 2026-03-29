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
import com.example.moneymaster.ui.search.BusquedaViewModel;
import com.example.moneymaster.ui.search.FiltroGasto;
import com.example.moneymaster.ui.search.SearchFilterBottomSheet;
import com.example.moneymaster.util.SwipeDeleteManager;
import com.example.moneymaster.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment — Cards #19, #55, #57, #58
 *
 * Dashboard personal con:
 *  - Selector de mes y card de balance (Card #19 / DashboardViewModel)
 *  - Swipe to delete con Deshacer (Card #57)
 *  - SearchView + filtros avanzados en Bottom Sheet (Card #58)
 *  - Contador de resultados
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding                     binding;
    private DashboardViewModel                      dashboardViewModel;
    private BusquedaViewModel                       busquedaViewModel;
    private MovimientosAdapter                      adapter;
    private SwipeDeleteManager<MovimientoReciente>  swipeDeleteManager;
    private List<CategoriaGasto>                    categorias = new ArrayList<>();

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

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        busquedaViewModel  = new ViewModelProvider(this).get(BusquedaViewModel.class);
        busquedaViewModel.initPersonal();

        setupRecyclerView();
        setupSwipeDelete();
        setupSearch();
        setupMesSelector();
        cargarCategorias();
        observeData();
    }

    // ─── RecyclerView ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new MovimientosAdapter(requireContext());
        binding.rvMovimientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMovimientos.setAdapter(adapter);
    }

    // ─── Swipe delete (Card #57) ──────────────────────────────────────────────

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

    // ─── Card #58: SearchView + filtros ──────────────────────────────────────

    private void setupSearch() {
        // SearchView directamente en el layout (no en toolbar)
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                busquedaViewModel.setQuery(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                busquedaViewModel.setQuery(newText.trim());
                return true;
            }
        });

        // Limpiar búsqueda al cerrar el SearchView
        binding.searchView.setOnCloseListener(() -> {
            busquedaViewModel.setQuery("");
            return false;
        });

        // Botón de filtros avanzados
        binding.btnFiltros.setOnClickListener(v -> abrirFiltrosAvanzados());
    }

    private void abrirFiltrosAvanzados() {
        SearchFilterBottomSheet sheet = SearchFilterBottomSheet.newInstance(
                busquedaViewModel.getFiltroSnapshot(),
                categorias
        );
        sheet.setOnFiltroAplicadoListener(filtro -> {
            busquedaViewModel.aplicarFiltro(filtro);
            // Actualizar icono del botón para indicar filtros activos
            int count = filtro.contarFiltrosActivos();
            binding.btnFiltros.setAlpha(count > 0 ? 1.0f : 0.6f);
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

    // ─── Selector de mes (DashboardViewModel) ────────────────────────────────

    private void setupMesSelector() {
        binding.btnMesAnterior.setOnClickListener(v -> dashboardViewModel.mesAnterior());
        binding.btnMesSiguiente.setOnClickListener(v -> dashboardViewModel.mesSiguiente());
    }

    // ─── Observadores ─────────────────────────────────────────────────────────

    private void observeData() {
        // Etiqueta del mes
        dashboardViewModel.etiquetaMes.observe(getViewLifecycleOwner(),
                label -> binding.tvMesActual.setText(label));

        // Balance
        dashboardViewModel.balance.observe(getViewLifecycleOwner(), balance -> {
            if (balance == null) return;
            boolean positivo = balance >= 0;
            binding.tvSignoBalance.setText(positivo ? "▲" : "▼");
            binding.tvBalance.setText(
                    String.format(new java.util.Locale("es", "ES"),
                            "%s%.2f €", positivo ? "+" : "", balance));
        });

        // Totales
        dashboardViewModel.totalIngresos.observe(getViewLifecycleOwner(), total -> {
            if (total != null)
                binding.tvTotalIngresos.setText(
                        String.format(new java.util.Locale("es", "ES"), "%.2f €", total));
        });

        dashboardViewModel.totalGastos.observe(getViewLifecycleOwner(), total -> {
            if (total != null)
                binding.tvTotalGastos.setText(
                        String.format(new java.util.Locale("es", "ES"), "%.2f €", total));
        });

        // Card #58: resultados filtrados
        busquedaViewModel.getResultadosPersonales().observe(getViewLifecycleOwner(), gastos -> {
            List<MovimientoReciente> lista = convertirAMovimientos(gastos);
            adapter.submitList(lista);

            boolean vacio = lista.isEmpty();
            binding.rvMovimientos.setVisibility(vacio ? View.GONE : View.VISIBLE);
            binding.tvSinMovimientos.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.btnVerTodos.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });

        // Card #58: contador de resultados
        busquedaViewModel.getContadorPersonales().observe(getViewLifecycleOwner(), count -> {
            if (count == null) {
                binding.tvContadorResultados.setVisibility(View.GONE);
                return;
            }
            FiltroGasto filtro = busquedaViewModel.getFiltroSnapshot();
            if (filtro.isEmpty()) {
                binding.tvContadorResultados.setVisibility(View.GONE);
            } else {
                binding.tvContadorResultados.setText(
                        count + " resultado" + (count != 1 ? "s" : ""));
                binding.tvContadorResultados.setVisibility(View.VISIBLE);
            }
        });
    }

    // ─── Conversión GastoConCategoria → MovimientoReciente ───────────────────

    private List<MovimientoReciente> convertirAMovimientos(List<GastoConCategoria> gastos) {
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
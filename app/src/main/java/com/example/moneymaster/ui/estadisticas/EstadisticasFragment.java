package com.example.moneymaster.ui.estadisticas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.R;
import com.example.moneymaster.databinding.FragmentEstadisticasBinding;
import com.example.moneymaster.ui.ViewModel.EstadisticasViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment de Estadísticas — Cards #42, #43 y #44.
 *
 * Contiene tres gráficos en una sola pantalla con ScrollView:
 *   1. PieChart  — gastos por categoría del mes activo
 *   2. BarChart  — gastos vs ingresos de los últimos N meses
 *   3. LineChart — evolución de gastos acumulados (diaria o semanal)
 *
 * Este Fragment sólo orquesta: recibe LiveData del ViewModel y delega
 * el dibujado a PieChartHelper, BarChartHelper y LineChartHelper.
 */
public class EstadisticasFragment extends Fragment {

    private FragmentEstadisticasBinding binding;
    private EstadisticasViewModel viewModel;

    // ─── Helpers de gráficos ──────────────────────────────────────────────────
    private PieChartHelper  pieChartHelper;
    private BarChartHelper  barChartHelper;
    private LineChartHelper lineChartHelper;

    // ─── Estado ───────────────────────────────────────────────────────────────
    private String  categoriaFiltroActiva = null;
    private boolean vistaLineDiaria       = true; // true = diario, false = semanal

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEstadisticasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configurarViewModel();
        configurarPieChart();
        configurarBarChart();
        configurarLineChart();
        configurarSelectorMes();
        configurarChipFiltro();
        configurarChipsRango();
        configurarChipsVista();
        observarDatos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ─── ViewModel ────────────────────────────────────────────────────────────

    private void configurarViewModel() {
        viewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("moneymaster_session", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("usuario_id", -1L);

        if (usuarioId != -1L) {
            viewModel.setUsuarioId(usuarioId);
        }
    }

    // ─── PieChart (Card #42) ──────────────────────────────────────────────────

    private void configurarPieChart() {
        pieChartHelper = new PieChartHelper(requireContext(), binding.pieChart);

        pieChartHelper.setOnSliceClickListener((nombreCategoria, total) -> {
            categoriaFiltroActiva = nombreCategoria;
            mostrarChipFiltro(nombreCategoria);
            Toast.makeText(requireContext(),
                    nombreCategoria + " — " + String.format("%.2f €", total),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void configurarSelectorMes() {
        binding.btnMesAnterior.setOnClickListener(v -> {
            int mes  = viewModel.getMesActual();
            int anio = viewModel.getAnioActual();
            if (mes == 1) { mes = 12; anio--; } else { mes--; }
            viewModel.setFiltroMes(mes, anio);
            viewModel.setRangoLineChart(mes, anio);
            actualizarEtiquetaMes(mes, anio);
            limpiarFiltroCategoria();
        });

        binding.btnMesSiguiente.setOnClickListener(v -> {
            int mes  = viewModel.getMesActual();
            int anio = viewModel.getAnioActual();
            if (mes == 12) { mes = 1; anio++; } else { mes++; }
            viewModel.setFiltroMes(mes, anio);
            viewModel.setRangoLineChart(mes, anio);
            actualizarEtiquetaMes(mes, anio);
            limpiarFiltroCategoria();
        });

        actualizarEtiquetaMes(viewModel.getMesActual(), viewModel.getAnioActual());
    }

    private void configurarChipFiltro() {
        binding.chipFiltroCategoria.setOnCloseIconClickListener(v -> limpiarFiltroCategoria());
    }

    // ─── BarChart (Card #43) ──────────────────────────────────────────────────

    private void configurarBarChart() {
        barChartHelper = new BarChartHelper(requireContext(), binding.barChart);

        barChartHelper.setOnBarClickListener((etiqueta, gastos, ingresos) -> {
            String mensaje = etiqueta
                    + "\nGastos: "   + String.format("%.2f €", gastos)
                    + "\nIngresos: " + String.format("%.2f €", ingresos);
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        });

        BarTooltipMarker marker = new BarTooltipMarker(
                requireContext(), R.layout.marker_bar_tooltip);
        marker.setChartView(binding.barChart);
        binding.barChart.setMarker(marker);
    }

    private void configurarChipsRango() {
        binding.chipGroupRango.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            int meses;
            if      (id == R.id.chip_3_meses)  meses = 3;
            else if (id == R.id.chip_12_meses) meses = 12;
            else                               meses = 6;
            viewModel.setMesesHistorial(meses);
        });
    }

    // ─── LineChart (Card #44) ─────────────────────────────────────────────────

    private void configurarLineChart() {
        lineChartHelper = new LineChartHelper(requireContext(), binding.lineChart);

        lineChartHelper.setOnPuntoClickListener((etiqueta, acumulado, totalPunto) -> {
            String texto = etiqueta
                    + "  |  día: "       + String.format("%.2f €", totalPunto)
                    + "  |  acumulado: " + String.format("%.2f €", acumulado);
            binding.tvLineDetalle.setText(texto);
            binding.tvLineDetalle.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Chips Diario / Semanal — cambian la vista del LineChart sin tocar el mes.
     */
    private void configurarChipsVista() {
        binding.chipGroupVista.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            vistaLineDiaria = (id == R.id.chip_vista_diaria);
            // Re-renderizar con los datos ya cargados
            actualizarLineChart();
        });
    }

    // ─── Observadores ─────────────────────────────────────────────────────────

    private void observarDatos() {
        // ── PieChart ──────────────────────────────────────────────────────────
        viewModel.gastosPorCategoria.observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                binding.tvSinDatos.setVisibility(View.VISIBLE);
                binding.pieChart.setVisibility(View.GONE);
            } else {
                binding.tvSinDatos.setVisibility(View.GONE);
                binding.pieChart.setVisibility(View.VISIBLE);
                pieChartHelper.actualizarDatos(items);
            }
        });

        viewModel.totalGastosMes.observe(getViewLifecycleOwner(), total -> {
            if (total != null) pieChartHelper.actualizarTotalCentral(total);
        });

        // ── BarChart ──────────────────────────────────────────────────────────
        viewModel.resumenGastosMeses.observe(getViewLifecycleOwner(), gastos -> {
            boolean hayDatos = gastos != null && !gastos.isEmpty();
            binding.tvSinDatosBar.setVisibility(hayDatos ? View.GONE  : View.VISIBLE);
            binding.barChart.setVisibility(      hayDatos ? View.VISIBLE : View.GONE);
            barChartHelper.setGastos(gastos);
        });

        viewModel.resumenIngresosMeses.observe(getViewLifecycleOwner(),
                ingresos -> barChartHelper.setIngresos(ingresos));

        // ── LineChart ─────────────────────────────────────────────────────────
        viewModel.gastosDiarios.observe(getViewLifecycleOwner(), puntos -> {
            if (vistaLineDiaria) {
                mostrarOcultarLineChart(puntos != null && !puntos.isEmpty());
                lineChartHelper.actualizarDiario(puntos);
                binding.tvLineDetalle.setVisibility(View.GONE);
            }
        });

        viewModel.gastosSemanales.observe(getViewLifecycleOwner(), puntos -> {
            if (!vistaLineDiaria) {
                mostrarOcultarLineChart(puntos != null && !puntos.isEmpty());
                lineChartHelper.actualizarSemanal(puntos);
                binding.tvLineDetalle.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Re-renderiza el LineChart con la vista activa (diaria o semanal)
     * usando los datos ya cargados en el ViewModel.
     */
    private void actualizarLineChart() {
        binding.tvLineDetalle.setVisibility(View.GONE);
        if (vistaLineDiaria) {
            lineChartHelper.actualizarDiario(viewModel.gastosDiarios.getValue());
        } else {
            lineChartHelper.actualizarSemanal(viewModel.gastosSemanales.getValue());
        }
    }

    private void mostrarOcultarLineChart(boolean hayDatos) {
        binding.tvSinDatosLine.setVisibility(hayDatos ? View.GONE    : View.VISIBLE);
        binding.lineChart.setVisibility(      hayDatos ? View.VISIBLE : View.GONE);
    }

    // ─── Helpers de UI ────────────────────────────────────────────────────────

    private void actualizarEtiquetaMes(int mes, int anio) {
        Calendar cal = Calendar.getInstance();
        cal.set(anio, mes - 1, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String etiqueta = sdf.format(cal.getTime());
        etiqueta = etiqueta.substring(0, 1).toUpperCase(new Locale("es", "ES"))
                + etiqueta.substring(1);
        binding.tvMesAnio.setText(etiqueta);
    }

    private void mostrarChipFiltro(String nombreCategoria) {
        binding.chipFiltroCategoria.setText("Mostrando: " + nombreCategoria);
        binding.chipFiltroCategoria.setVisibility(View.VISIBLE);
    }

    private void limpiarFiltroCategoria() {
        categoriaFiltroActiva = null;
        binding.chipFiltroCategoria.setVisibility(View.GONE);
    }
}
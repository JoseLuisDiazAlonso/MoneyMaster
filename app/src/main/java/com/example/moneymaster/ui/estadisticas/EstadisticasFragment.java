package com.example.moneymaster.ui.estadisticas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;
import com.example.moneymaster.ui.ViewModel.EstadisticasViewModel;
import com.example.moneymaster.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EstadisticasFragment extends Fragment {

    private EstadisticasViewModel viewModel;
    private Calendar calendarActual;

    private TextView    tvMesAnio;
    private ImageButton btnMesAnterior;
    private ImageButton btnMesSiguiente;
    private PieChart    pieChart;
    private TextView    tvSinDatos;
    private BarChart    barChart;
    private TextView    tvSinDatosBar;
    private LineChart   lineChart;
    private TextView    tvSinDatosLine;
    private ChipGroup   chipGroupRango;
    private ChipGroup   chipGroupVista;

    private int usuarioId;

    private List<ResumenMensual> ultimosGastosMeses   = new ArrayList<>();
    private List<ResumenMensual> ultimosIngresosMeses = new ArrayList<>();

    private static final String[] MESES_ES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private static final int[] COLORES_PIE = {
            0xFF4CAF50, 0xFF2196F3, 0xFFF44336, 0xFFFF9800, 0xFF9C27B0,
            0xFF00BCD4, 0xFFFFEB3B, 0xFF795548, 0xFF607D8B, 0xFFE91E63
    };

    // =========================================================================
    // Ciclo de vida
    // =========================================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estadisticas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        leerSesion();
        inicializarCalendario();
        enlazarVistas(view);
        configurarGraficos();
        inicializarViewModel();
        configurarSelectorMes();
        configurarChips();
        observarDatos();
    }

    // =========================================================================
    // Inicialización
    // =========================================================================

    private void leerSesion() {
        usuarioId = new SessionManager(requireContext()).getUserId();
    }

    private void inicializarCalendario() {
        calendarActual = Calendar.getInstance();
    }

    private void enlazarVistas(View view) {
        tvMesAnio       = view.findViewById(R.id.tv_mes_anio);
        btnMesAnterior  = view.findViewById(R.id.btn_mes_anterior);
        btnMesSiguiente = view.findViewById(R.id.btn_mes_siguiente);
        pieChart        = view.findViewById(R.id.pie_chart);
        tvSinDatos      = view.findViewById(R.id.tv_sin_datos);
        barChart        = view.findViewById(R.id.bar_chart);
        tvSinDatosBar   = view.findViewById(R.id.tv_sin_datos_bar);
        lineChart       = view.findViewById(R.id.line_chart);
        tvSinDatosLine  = view.findViewById(R.id.tv_sin_datos_line);
        chipGroupRango  = view.findViewById(R.id.chip_group_rango);
        chipGroupVista  = view.findViewById(R.id.chip_group_vista);
    }

    private void inicializarViewModel() {
        viewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);
        if (usuarioId != -1) {
            viewModel.setUsuarioId((long) usuarioId);
        }
    }

    // =========================================================================
    // Configuración inicial de gráficos
    // =========================================================================

    private void configurarGraficos() {
        // ── PieChart ──────────────────────────────────────────────────────────
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.setNoDataText("Sin gastos este mes");

        // ── BarChart ──────────────────────────────────────────────────────────
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.setFitBars(true);
        barChart.setNoDataText("Sin datos disponibles");

        // ── LineChart ─────────────────────────────────────────────────────────
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.setNoDataText("Sin gastos este mes");
    }

    // =========================================================================
    // Selector de mes
    // =========================================================================

    private void configurarSelectorMes() {
        actualizarTextMes();

        btnMesAnterior.setOnClickListener(v -> {
            calendarActual.add(Calendar.MONTH, -1);
            actualizarTextMes();
            empujarFiltroMes();
        });

        btnMesSiguiente.setOnClickListener(v -> {
            Calendar ahora = Calendar.getInstance();
            boolean esAnioAnterior = calendarActual.get(Calendar.YEAR)
                    < ahora.get(Calendar.YEAR);
            boolean esMesAnterior  = calendarActual.get(Calendar.YEAR)
                    == ahora.get(Calendar.YEAR)
                    && calendarActual.get(Calendar.MONTH) < ahora.get(Calendar.MONTH);
            if (esAnioAnterior || esMesAnterior) {
                calendarActual.add(Calendar.MONTH, 1);
                actualizarTextMes();
                empujarFiltroMes();
            }
        });
    }

    // =========================================================================
    // Chips de rango y vista
    // =========================================================================

    private void configurarChips() {
        chipGroupRango.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chip_3_meses)  viewModel.setMesesHistorial(3);
            else if (id == R.id.chip_6_meses)  viewModel.setMesesHistorial(6);
            else if (id == R.id.chip_12_meses) viewModel.setMesesHistorial(12);
        });

        chipGroupVista.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            boolean semanal = id == R.id.chip_vista_semanal;
            dibujarLineChart(ultimosGastosMeses, semanal);
        });
    }

    // =========================================================================
    // Observers LiveData
    // =========================================================================

    private void observarDatos() {

        // ── PieChart: gastos por categoría ────────────────────────────────────
        viewModel.gastosPorCategoria.observe(getViewLifecycleOwner(), lista -> {
            if (lista == null || lista.isEmpty()) {
                pieChart.setVisibility(View.GONE);
                tvSinDatos.setVisibility(View.VISIBLE);
            } else {
                tvSinDatos.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);
                dibujarPieChart(lista);
            }
        });

        // ── BarChart: gastos vs ingresos ──────────────────────────────────────
        viewModel.resumenGastosMeses.observe(getViewLifecycleOwner(), gastos -> {
            ultimosGastosMeses = gastos != null ? gastos : new ArrayList<>();
            dibujarBarChart(ultimosGastosMeses, ultimosIngresosMeses);
        });

        viewModel.resumenIngresosMeses.observe(getViewLifecycleOwner(), ingresos -> {
            ultimosIngresosMeses = ingresos != null ? ingresos : new ArrayList<>();
            dibujarBarChart(ultimosGastosMeses, ultimosIngresosMeses);
        });

        // ── LineChart: evolución de gastos ────────────────────────────────────
        viewModel.resumenGastosMeses.observe(getViewLifecycleOwner(), gastos -> {
            ultimosGastosMeses = gastos != null ? gastos : new ArrayList<>();
            boolean semanal = chipGroupVista != null
                    && chipGroupVista.getCheckedChipId() == R.id.chip_vista_semanal;
            dibujarLineChart(ultimosGastosMeses, semanal);
        });
    }

    // =========================================================================
    // Dibujo de gráficos
    // =========================================================================

    private void dibujarPieChart(List<TotalPorCategoria> lista) {
        List<PieEntry> entries = new ArrayList<>();
        for (TotalPorCategoria item : lista) {
            if (item.total > 0) {
                entries.add(new PieEntry(
                        (float) item.total,
                        item.nombreCategoria != null ? item.nombreCategoria : "Otros"));
            }
        }

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvSinDatos.setVisibility(View.VISIBLE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(COLORES_PIE);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(new Locale("es", "ES"), "%.1f%%", value);
            }
        });

        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }

    private void dibujarBarChart(List<ResumenMensual> gastos,
                                 List<ResumenMensual> ingresos) {
        if ((gastos == null || gastos.isEmpty())
                && (ingresos == null || ingresos.isEmpty())) {
            barChart.setVisibility(View.GONE);
            tvSinDatosBar.setVisibility(View.VISIBLE);
            return;
        }

        tvSinDatosBar.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);

        int maxSize = Math.max(
                gastos   != null ? gastos.size()   : 0,
                ingresos != null ? ingresos.size() : 0);

        List<BarEntry> entriesGastos   = new ArrayList<>();
        List<BarEntry> entriesIngresos = new ArrayList<>();
        List<String>   etiquetas       = new ArrayList<>();

        for (int i = 0; i < maxSize; i++) {
            float g = (gastos   != null && i < gastos.size())
                    ? (float) gastos.get(i).total   : 0f;
            float inc = (ingresos != null && i < ingresos.size())
                    ? (float) ingresos.get(i).total : 0f;

            entriesGastos.add(new BarEntry(i * 2f, g));
            entriesIngresos.add(new BarEntry(i * 2f + 0.5f, inc));

            String etiqueta = "";
            if (gastos != null && i < gastos.size()) {
                etiqueta = mesCorto(gastos.get(i).mes, gastos.get(i).anio);
            } else if (ingresos != null && i < ingresos.size()) {
                etiqueta = mesCorto(ingresos.get(i).mes, ingresos.get(i).anio);
            }
            etiquetas.add(etiqueta);
        }

        BarDataSet dsGastos = new BarDataSet(entriesGastos, "Gastos");
        dsGastos.setColor(0xFFF44336);
        dsGastos.setValueTextSize(9f);

        BarDataSet dsIngresos = new BarDataSet(entriesIngresos, "Ingresos");
        dsIngresos.setColor(0xFF4CAF50);
        dsIngresos.setValueTextSize(9f);

        BarData barData = new BarData(dsGastos, dsIngresos);
        barData.setBarWidth(0.45f);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        barChart.getXAxis().setLabelCount(etiquetas.size());
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void dibujarLineChart(List<ResumenMensual> datos, boolean semanal) {
        if (datos == null || datos.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            tvSinDatosLine.setVisibility(View.VISIBLE);
            return;
        }

        tvSinDatosLine.setVisibility(View.GONE);
        lineChart.setVisibility(View.VISIBLE);

        List<Entry>  entries   = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();

        for (int i = 0; i < datos.size(); i++) {
            entries.add(new Entry(i, (float) datos.get(i).total));
            etiquetas.add(mesCorto(datos.get(i).mes, datos.get(i).anio));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gastos");
        dataSet.setColor(0xFF2196F3);
        dataSet.setCircleColor(0xFF2196F3);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0x402196F3);
        dataSet.setValueTextSize(9f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        lineChart.getXAxis().setLabelCount(etiquetas.size());
        lineChart.setData(new LineData(dataSet));
        lineChart.invalidate();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void actualizarTextMes() {
        int mes  = calendarActual.get(Calendar.MONTH);
        int anio = calendarActual.get(Calendar.YEAR);
        tvMesAnio.setText(MESES_ES[mes] + " " + anio);
    }

    private void empujarFiltroMes() {
        int mes  = calendarActual.get(Calendar.MONTH) + 1;
        int anio = calendarActual.get(Calendar.YEAR);
        viewModel.setFiltroMes(mes, anio);
    }

    private String mesCorto(int mes, int anio) {
        String[] mesesCortos = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        int idx = Math.max(0, Math.min(mes - 1, 11));
        return mesesCortos[idx] + "\n" + (anio % 100);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
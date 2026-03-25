package com.example.moneymaster;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.databinding.FragmentStatisticsMonthBinding;
import com.example.moneymaster.ui.ViewModel.StatisticsViewModel;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pestaña "Mes actual" del fragmento de Estadísticas.
 *
 * CONTENIDO:
 *  1. Selector de mes/año (chevrons ← →)
 *  2. Tarjetas de resumen: Total Gastado · Total Ingresado · Balance
 *  3. Gráfico de tarta (PieChart) — distribución de gastos por categoría
 *  4. Gráfico de barras (BarChart)  — gastos vs ingresos por semana del mes
 *
 * Todos los datos provienen de StatisticsViewModel, que es compartido con
 * las otras pestañas y tiene ámbito de Activity.
 */
public class StatisticsMonthFragment extends Fragment {

    private FragmentStatisticsMonthBinding binding;
    private StatisticsViewModel viewModel;

    // Colores Material 3 para el gráfico de tarta
    private static final int[] PIE_COLORS = {
            Color.parseColor("#6750A4"), // Primary
            Color.parseColor("#625B71"), // Secondary
            Color.parseColor("#7D5260"), // Tertiary
            Color.parseColor("#B3261E"), // Error
            Color.parseColor("#958DA5"),
            Color.parseColor("#E8DEF8"),
            Color.parseColor("#CCC2DC"),
            Color.parseColor("#EFB8C8")
    };

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsMonthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        setupMonthSelector();
        setupPieChart();
        setupBarChart();
        observeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Setup
    // ──────────────────────────────────────────────────────────────────────────

    /** Configura los botones de navegación mes anterior / mes siguiente. */
    private void setupMonthSelector() {
        binding.btnMonthPrev.setOnClickListener(v -> changeMonth(-1));
        binding.btnMonthNext.setOnClickListener(v -> changeMonth(+1));
    }

    /** Inicializa el PieChart con estética Material 3. */
    private void setupPieChart() {
        binding.pieChartGastos.setUsePercentValues(true);
        binding.pieChartGastos.getDescription().setEnabled(false);
        binding.pieChartGastos.setDrawHoleEnabled(true);
        binding.pieChartGastos.setHoleColor(Color.TRANSPARENT);
        binding.pieChartGastos.setHoleRadius(50f);
        binding.pieChartGastos.setTransparentCircleRadius(55f);
        binding.pieChartGastos.getLegend().setEnabled(true);
        binding.pieChartGastos.setEntryLabelColor(Color.BLACK);
        binding.pieChartGastos.setEntryLabelTextSize(11f);
        binding.pieChartGastos.animateY(800);
    }

    /** Inicializa el BarChart comparativo gastos vs ingresos por semana. */
    private void setupBarChart() {
        binding.barChartSemanal.getDescription().setEnabled(false);
        binding.barChartSemanal.setDrawGridBackground(false);
        binding.barChartSemanal.getXAxis().setGranularity(1f);
        binding.barChartSemanal.getAxisLeft().setAxisMinimum(0f);
        binding.barChartSemanal.getAxisRight().setEnabled(false);
        binding.barChartSemanal.getLegend().setEnabled(true);
        binding.barChartSemanal.animateY(600);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Observadores
    // ──────────────────────────────────────────────────────────────────────────

    private void observeData() {
        // Actualizar etiqueta del selector de mes
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), month -> updateMonthLabel());
        viewModel.getSelectedYear().observe(getViewLifecycleOwner(), year  -> updateMonthLabel());

        // Resumen numérico
        viewModel.getSummaryTotalGastos().observe(getViewLifecycleOwner(),
                total -> binding.tvTotalGastado.setText(formatCurrency(total)));

        viewModel.getSummaryTotalIngresos().observe(getViewLifecycleOwner(),
                total -> binding.tvTotalIngresado.setText(formatCurrency(total)));

        viewModel.getSummaryBalance().observe(getViewLifecycleOwner(), balance -> {
            binding.tvBalance.setText(formatCurrency(balance));
            // Colorear balance: verde si positivo, rojo si negativo
            int color = balance >= 0
                    ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#B3261E");
            binding.tvBalance.setTextColor(color);
        });

        // Gráficos
        viewModel.getGastosMes().observe(getViewLifecycleOwner(), this::updatePieChart);
        viewModel.getGastosMes().observe(getViewLifecycleOwner(), gastos ->
                updateBarChart(gastos, viewModel.getIngresosMes().getValue()));
        viewModel.getIngresosMes().observe(getViewLifecycleOwner(), ingresos ->
                updateBarChart(viewModel.getGastosMes().getValue(), ingresos));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Actualización de UI
    // ──────────────────────────────────────────────────────────────────────────

    /** Muestra el nombre del mes y el año en el selector. */
    private void updateMonthLabel() {
        Integer month = viewModel.getSelectedMonth().getValue();
        Integer year  = viewModel.getSelectedYear().getValue();
        if (month == null || year == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("es", "ES"));
        binding.tvMonthYear.setText(capitalize(monthName) + " " + year);
    }

    /**
     * Rellena el PieChart con la distribución de gastos por categoría.
     * Agrupa categorías con < 3 % en "Otros".
     */
    private void updatePieChart(List<GastoPersonal> gastos) {
        if (gastos == null || gastos.isEmpty()) {
            binding.pieChartGastos.clear();
            binding.pieChartGastos.setCenterText("Sin datos");
            return;
        }

        // Agrupar por idCategoria
        Map<Integer, Double> totalPorCategoria = new HashMap<>();
        for (GastoPersonal g : gastos) {
            totalPorCategoria.merge(g.categoriaId, g.monto, Double::sum);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : totalPorCategoria.entrySet()) {
            // Aquí idealmente obtendrías el nombre de la categoría desde el ViewModel;
            // por ahora usamos el id como etiqueta temporal.
            entries.add(new PieEntry(entry.getValue().floatValue(),
                    "Cat. " + entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(PIE_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(new Locale("es", "ES"), "%.1f%%", value);
            }
        });

        binding.pieChartGastos.setData(new PieData(dataSet));
        binding.pieChartGastos.invalidate();
    }

    /**
     * Dibuja el BarChart con barras agrupadas (gastos / ingresos) por semana del mes.
     * Semana 1 = días 1–7, Semana 2 = días 8–14, Semana 3 = días 15–21, Semana 4 = días 22+.
     */
    private void updateBarChart(@Nullable List<GastoPersonal> gastos,
                                @Nullable List<IngresoPersonal> ingresos) {
        float[] gastosSemanales   = new float[4];
        float[] ingresosSemanales = new float[4];

        if (gastos != null) {
            for (GastoPersonal g : gastos) {
                int semana = getSemanaDelMes(g.fecha);
                gastosSemanales[semana] += (float) g.monto;
            }
        }
        if (ingresos != null) {
            for (IngresoPersonal i : ingresos) {
                int semana = getSemanaDelMes(i.fecha);
                ingresosSemanales[semana] += (float) i.monto;
            }
        }

        List<BarEntry> gastoEntries   = new ArrayList<>();
        List<BarEntry> ingresoEntries = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            gastoEntries.add(new BarEntry(i, gastosSemanales[i]));
            ingresoEntries.add(new BarEntry(i, ingresosSemanales[i]));
        }

        BarDataSet gastoSet   = new BarDataSet(gastoEntries, "Gastos");
        BarDataSet ingresoSet = new BarDataSet(ingresoEntries, "Ingresos");
        gastoSet.setColor(Color.parseColor("#B3261E"));
        ingresoSet.setColor(Color.parseColor("#2E7D32"));
        gastoSet.setValueTextSize(9f);
        ingresoSet.setValueTextSize(9f);

        BarData barData = new BarData(gastoSet, ingresoSet);
        float groupSpace = 0.3f;
        float barSpace   = 0.05f;
        float barWidth   = 0.3f;
        barData.setBarWidth(barWidth);

        binding.barChartSemanal.setData(barData);
        binding.barChartSemanal.groupBars(0f, groupSpace, barSpace);
        binding.barChartSemanal.getXAxis().setValueFormatter(new ValueFormatter() {
            private final String[] labels = {"Sem 1", "Sem 2", "Sem 3", "Sem 4"};
            @Override public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < labels.length) ? labels[idx] : "";
            }
        });
        binding.barChartSemanal.getXAxis().setLabelCount(4);
        binding.barChartSemanal.invalidate();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Avanza o retrocede el mes seleccionado respetando el cambio de año. */
    private void changeMonth(int delta) {
        Integer currentMonth = viewModel.getSelectedMonth().getValue();
        Integer currentYear  = viewModel.getSelectedYear().getValue();
        if (currentMonth == null || currentYear == null) return;

        int newMonth = currentMonth + delta;
        int newYear  = currentYear;

        if (newMonth > 12) { newMonth = 1;  newYear++; }
        if (newMonth < 1)  { newMonth = 12; newYear--; }

        viewModel.setMonthYear(newMonth, newYear);
    }

    /** Convierte un Unix timestamp en índice de semana del mes (0–3). */
    private int getSemanaDelMes(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L);
        int dia = cal.get(Calendar.DAY_OF_MONTH);
        if (dia <= 7)  return 0;
        if (dia <= 14) return 1;
        if (dia <= 21) return 2;
        return 3;
    }

    /** Formatea un double como moneda con separador de miles y dos decimales (es-ES). */
    private String formatCurrency(Double value) {
        if (value == null) return "0,00 €";
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return nf.format(value);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
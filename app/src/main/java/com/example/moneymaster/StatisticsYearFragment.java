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
import com.example.moneymaster.databinding.FragmentStatisticsYearBinding;
import com.example.moneymaster.ui.ViewModel.StatisticsViewModel;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Pestaña "Año" del fragmento de Estadísticas.
 *
 * CONTENIDO:
 *  1. Selector de año (chevrons ← →)
 *  2. Tarjetas de resumen: Total Gastado · Total Ingresado · Balance anual
 *  3. LineChart — evolución mensual de gastos e ingresos (12 puntos)
 *  4. BarChart  — comparativa mensual de gastos (barras agrupadas por mes)
 */
public class StatisticsYearFragment extends Fragment {

    private FragmentStatisticsYearBinding binding;
    private StatisticsViewModel viewModel;

    private static final String[] MES_CORTO = {
            "Ene","Feb","Mar","Abr","May","Jun",
            "Jul","Ago","Sep","Oct","Nov","Dic"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsYearBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        setupYearSelector();
        setupLineChart();
        setupBarChart();
        observeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    // Setup


    private void setupYearSelector() {
        binding.btnYearPrev.setOnClickListener(v -> changeYear(-1));
        binding.btnYearNext.setOnClickListener(v -> changeYear(+1));
    }

    private void setupLineChart() {
        binding.lineChartAnual.getDescription().setEnabled(false);
        binding.lineChartAnual.setDrawGridBackground(false);
        binding.lineChartAnual.getAxisRight().setEnabled(false);
        binding.lineChartAnual.getXAxis().setGranularity(1f);
        binding.lineChartAnual.getXAxis().setLabelCount(12);
        binding.lineChartAnual.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < MES_CORTO.length) ? MES_CORTO[idx] : "";
            }
        });
        binding.lineChartAnual.getAxisLeft().setAxisMinimum(0f);
        binding.lineChartAnual.animateX(800);
    }

    private void setupBarChart() {
        binding.barChartMensual.getDescription().setEnabled(false);
        binding.barChartMensual.setDrawGridBackground(false);
        binding.barChartMensual.getAxisRight().setEnabled(false);
        binding.barChartMensual.getAxisLeft().setAxisMinimum(0f);
        binding.barChartMensual.getXAxis().setGranularity(1f);
        binding.barChartMensual.getXAxis().setLabelCount(12);
        binding.barChartMensual.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < MES_CORTO.length) ? MES_CORTO[idx] : "";
            }
        });
        binding.barChartMensual.animateY(600);
    }


    // Observadores


    private void observeData() {
        viewModel.getSelectedYear().observe(getViewLifecycleOwner(),
                year -> binding.tvSelectedYear.setText(String.valueOf(year)));

        viewModel.getGastosAnio().observe(getViewLifecycleOwner(), gastos -> {
            updateSummaryGastos(gastos);
            updateCharts(gastos, viewModel.getIngresosAnio().getValue());
        });

        viewModel.getIngresosAnio().observe(getViewLifecycleOwner(), ingresos -> {
            updateSummaryIngresos(ingresos);
            updateCharts(viewModel.getGastosAnio().getValue(), ingresos);
        });
    }


    // Actualización de UI


    private void updateSummaryGastos(List<GastoPersonal> gastos) {
        double total = 0;
        if (gastos != null) for (GastoPersonal g : gastos) total += g.monto;
        binding.tvTotalGastadoAnual.setText(formatCurrency(total));
    }

    private void updateSummaryIngresos(List<IngresoPersonal> ingresos) {
        double total = 0;
        if (ingresos != null) for (IngresoPersonal i : ingresos) total += i.monto;
        binding.tvTotalIngresadoAnual.setText(formatCurrency(total));

        // Recalcular balance si también tenemos gastos
        List<GastoPersonal> gastos = viewModel.getGastosAnio().getValue();
        double totalGastos = 0;
        if (gastos != null) for (GastoPersonal g : gastos) totalGastos += g.monto;
        double balance = total - totalGastos;
        binding.tvBalanceAnual.setText(formatCurrency(balance));
        binding.tvBalanceAnual.setTextColor(balance >= 0
                ? Color.parseColor("#2E7D32")
                : Color.parseColor("#B3261E"));
    }

    /**
     * Rellena LineChart y BarChart con los datos mensuales del año.
     * Ambos gráficos usan arrays de 12 posiciones (índice = mes − 1).
     */
    private void updateCharts(@Nullable List<GastoPersonal> gastos,
                              @Nullable List<IngresoPersonal> ingresos) {

        float[] gastosMensuales   = new float[12];
        float[] ingresosMensuales = new float[12];

        if (gastos != null) {
            for (GastoPersonal g : gastos) {
                int mes = getMesDeTimestamp(g.fecha);
                gastosMensuales[mes] += (float) g.monto;
            }
        }
        if (ingresos != null) {
            for (IngresoPersonal i : ingresos) {
                int mes = getMesDeTimestamp(i.fecha);
                ingresosMensuales[mes] += (float) i.monto;
            }
        }

        // LineChart
        List<Entry> lineGastos   = new ArrayList<>();
        List<Entry> lineIngresos = new ArrayList<>();
        for (int m = 0; m < 12; m++) {
            lineGastos.add(new Entry(m, gastosMensuales[m]));
            lineIngresos.add(new Entry(m, ingresosMensuales[m]));
        }

        LineDataSet gastoLine   = buildLineDataSet(lineGastos,   "Gastos",   "#B3261E");
        LineDataSet ingresoLine = buildLineDataSet(lineIngresos, "Ingresos", "#2E7D32");
        binding.lineChartAnual.setData(new LineData(gastoLine, ingresoLine));
        binding.lineChartAnual.invalidate();

        // BarChart (solo gastos mensuales para no saturar)
        List<BarEntry> barEntries = new ArrayList<>();
        for (int m = 0; m < 12; m++) {
            barEntries.add(new BarEntry(m, gastosMensuales[m]));
        }
        BarDataSet barSet = new BarDataSet(barEntries, "Gastos mensuales");
        barSet.setColor(Color.parseColor("#6750A4"));
        barSet.setValueTextSize(8f);
        binding.barChartMensual.setData(new BarData(barSet));
        binding.barChartMensual.invalidate();
    }


    // Helpers


    private void changeYear(int delta) {
        Integer year  = viewModel.getSelectedYear().getValue();
        Integer month = viewModel.getSelectedMonth().getValue();
        if (year == null || month == null) return;
        viewModel.setMonthYear(month, year + delta);
    }

    /** Devuelve el índice de mes (0–11) a partir de un Unix timestamp en segundos. */
    private int getMesDeTimestamp(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L);
        return cal.get(Calendar.MONTH); // 0–11
    }

    private LineDataSet buildLineDataSet(List<Entry> entries, String label, String hex) {
        LineDataSet ds = new LineDataSet(entries, label);
        ds.setColor(Color.parseColor(hex));
        ds.setCircleColor(Color.parseColor(hex));
        ds.setLineWidth(2f);
        ds.setCircleRadius(3f);
        ds.setDrawValues(false);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return ds;
    }

    private String formatCurrency(double value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return nf.format(value);
    }
}
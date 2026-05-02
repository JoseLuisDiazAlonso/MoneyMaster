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

import com.example.moneymaster.R;
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

public class StatisticsMonthFragment extends Fragment {

    private FragmentStatisticsMonthBinding binding;
    private StatisticsViewModel viewModel;

    private static final int[] PIE_COLORS = {
            Color.parseColor("#6750A4"),
            Color.parseColor("#625B71"),
            Color.parseColor("#7D5260"),
            Color.parseColor("#B3261E"),
            Color.parseColor("#958DA5"),
            Color.parseColor("#E8DEF8"),
            Color.parseColor("#CCC2DC"),
            Color.parseColor("#EFB8C8")
    };

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

    private void setupMonthSelector() {
        binding.btnMonthPrev.setOnClickListener(v -> changeMonth(-1));
        binding.btnMonthNext.setOnClickListener(v -> changeMonth(+1));
    }

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

    private void setupBarChart() {
        binding.barChartSemanal.getDescription().setEnabled(false);
        binding.barChartSemanal.setDrawGridBackground(false);
        binding.barChartSemanal.getXAxis().setGranularity(1f);
        binding.barChartSemanal.getAxisLeft().setAxisMinimum(0f);
        binding.barChartSemanal.getAxisRight().setEnabled(false);
        binding.barChartSemanal.getLegend().setEnabled(true);
        binding.barChartSemanal.animateY(600);
    }

    private void observeData() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), month -> updateMonthLabel());
        viewModel.getSelectedYear().observe(getViewLifecycleOwner(), year -> updateMonthLabel());

        viewModel.getSummaryTotalGastos().observe(getViewLifecycleOwner(),
                total -> binding.tvTotalGastado.setText(formatCurrency(total)));
        viewModel.getSummaryTotalIngresos().observe(getViewLifecycleOwner(),
                total -> binding.tvTotalIngresado.setText(formatCurrency(total)));
        viewModel.getSummaryBalance().observe(getViewLifecycleOwner(), balance -> {
            binding.tvBalance.setText(formatCurrency(balance));
            int color = balance >= 0
                    ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#B3261E");
            binding.tvBalance.setTextColor(color);
        });

        viewModel.getGastosMes().observe(getViewLifecycleOwner(), this::updatePieChart);
        viewModel.getGastosMes().observe(getViewLifecycleOwner(), gastos ->
                updateBarChart(gastos, viewModel.getIngresosMes().getValue()));
        viewModel.getIngresosMes().observe(getViewLifecycleOwner(), ingresos ->
                updateBarChart(viewModel.getGastosMes().getValue(), ingresos));
    }

    private void updateMonthLabel() {
        Integer month = viewModel.getSelectedMonth().getValue();
        Integer year  = viewModel.getSelectedYear().getValue();
        if (month == null || year == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);

        // FIX: Locale.getDefault() en lugar de Locale("es","ES") fijo
        Locale locale = Locale.getDefault();
        String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
        binding.tvMonthYear.setText(capitalize(monthName, locale) + " " + year);
    }

    private void updatePieChart(List<GastoPersonal> gastos) {
        if (gastos == null || gastos.isEmpty()) {
            binding.pieChartGastos.clear();
            // FIX: texto "Sin datos" desde strings.xml
            binding.pieChartGastos.setCenterText(getString(R.string.sin_datos));
            return;
        }

        Map<Integer, Double> totalPorCategoria = new HashMap<>();
        for (GastoPersonal g : gastos) {
            totalPorCategoria.merge(g.categoria_id, g.monto, Double::sum);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : totalPorCategoria.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), "Cat. " + entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(PIE_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f%%", value);
            }
        });

        binding.pieChartGastos.setData(new PieData(dataSet));
        binding.pieChartGastos.invalidate();
    }

    private void updateBarChart(@Nullable List<GastoPersonal> gastos,
                                @Nullable List<IngresoPersonal> ingresos) {
        float[] gastosSemanales   = new float[4];
        float[] ingresosSemanales = new float[4];

        if (gastos != null) {
            for (GastoPersonal g : gastos) {
                gastosSemanales[getSemanaDelMes(g.fecha)] += (float) g.monto;
            }
        }
        if (ingresos != null) {
            for (IngresoPersonal i : ingresos) {
                ingresosSemanales[getSemanaDelMes(i.fecha)] += (float) i.monto;
            }
        }

        List<BarEntry> gastoEntries   = new ArrayList<>();
        List<BarEntry> ingresoEntries = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            gastoEntries.add(new BarEntry(i, gastosSemanales[i]));
            ingresoEntries.add(new BarEntry(i, ingresosSemanales[i]));
        }

        // FIX: etiquetas de leyenda desde strings.xml
        BarDataSet gastoSet   = new BarDataSet(gastoEntries, getString(R.string.leyenda_gastos));
        BarDataSet ingresoSet = new BarDataSet(ingresoEntries, getString(R.string.leyenda_ingresos));
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

        // FIX: etiquetas de semanas desde strings.xml
        final String[] semLabels = {
                getString(R.string.sem_1),
                getString(R.string.sem_2),
                getString(R.string.sem_3),
                getString(R.string.sem_4)
        };
        binding.barChartSemanal.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < semLabels.length) ? semLabels[idx] : "";
            }
        });
        binding.barChartSemanal.getXAxis().setLabelCount(4);
        binding.barChartSemanal.invalidate();
    }

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

    private int getSemanaDelMes(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L);
        int dia = cal.get(Calendar.DAY_OF_MONTH);
        if (dia <= 7)  return 0;
        if (dia <= 14) return 1;
        if (dia <= 21) return 2;
        return 3;
    }

    private String formatCurrency(Double value) {
        if (value == null) return "0,00 €";
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return nf.format(value);
    }

    // FIX: capitalize recibe el Locale para usar el correcto en cada idioma
    private String capitalize(String s, Locale locale) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(locale) + s.substring(1);
    }
}
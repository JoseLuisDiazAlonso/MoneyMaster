package com.example.moneymaster.ui.estadisticas;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import com.example.moneymaster.data.model.ResumenMensual;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper que encapsula TODA la configuración de MPAndroidChart para el BarChart
 * del módulo de Estadísticas (Card #43).
 *
 * Muestra barras agrupadas (gastos en rojo, ingresos en verde) para los
 * últimos N meses. El eje X muestra etiquetas como "Ene", "Feb 25", etc.
 *
 * Uso básico:
 * <pre>
 *   BarChartHelper helper = new BarChartHelper(context, barChart);
 *   helper.setOnBarClickListener((etiqueta, gastos, ingresos) -> mostrarDetalle(...));
 *
 *   // Observar ambos LiveData y llamar actualizarDatos cuando ambos estén disponibles
 *   viewModel.resumenGastosMeses.observe(this,  gastos   -> helper.setGastos(gastos));
 *   viewModel.resumenIngresosMeses.observe(this, ingresos -> helper.setIngresos(ingresos));
 * </pre>
 */
public class BarChartHelper {

    // ─── Colores ──────────────────────────────────────────────────────────────
    private static final int COLOR_GASTOS   = Color.parseColor("#E53935"); // rojo Material
    private static final int COLOR_INGRESOS = Color.parseColor("#43A047"); // verde Material
    private static final int COLOR_GASTOS_T = Color.parseColor("#80E53935"); // rojo semitransparente
    private static final int COLOR_INGRESOS_T = Color.parseColor("#8043A047"); // verde semitransparente

    private final Context context;
    private final BarChart barChart;

    // Datos pendientes: se acumulan hasta tener ambas series
    private List<ResumenMensual> gastosData   = null;
    private List<ResumenMensual> ingresosData = null;

    private OnBarClickListener barClickListener;

    // ─── Interfaz de click ────────────────────────────────────────────────────
    public interface OnBarClickListener {
        /**
         * @param etiquetaMes etiqueta del mes clicado (p. ej. "Mar 25")
         * @param totalGastos total de gastos de ese mes
         * @param totalIngresos total de ingresos de ese mes
         */
        void onBarClick(String etiquetaMes, double totalGastos, double totalIngresos);
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    public BarChartHelper(Context context, BarChart barChart) {
        this.context  = context;
        this.barChart = barChart;
        configurarBarChart();
    }

    // ─── Configuración inicial ────────────────────────────────────────────────

    private void configurarBarChart() {
        // Aspecto general
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setExtraBottomOffset(16f);
        barChart.setExtraTopOffset(8f);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);

        // Eje X (meses)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.parseColor("#757575"));
        xAxis.setTypeface(Typeface.DEFAULT);
        xAxis.setLabelRotationAngle(0f);

        // Eje Y izquierdo (valores)
        YAxis yLeft = barChart.getAxisLeft();
        yLeft.setDrawGridLines(true);
        yLeft.setGridColor(Color.parseColor("#E0E0E0"));
        yLeft.setGridLineWidth(0.5f);
        yLeft.setAxisMinimum(0f);
        yLeft.setTextSize(10f);
        yLeft.setTextColor(Color.parseColor("#757575"));
        yLeft.setValueFormatter(new LargeValueFormatter()); // 1.000 → "1k"

        // Eje Y derecho (oculto)
        barChart.getAxisRight().setEnabled(false);

        // Leyenda
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(10f);
        legend.setTextSize(12f);
        legend.setTextColor(Color.parseColor("#424242"));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(24f);

        // Listener de click (tooltip)
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                if (barClickListener == null) return;
                int index = (int) e.getX();
                // Obtener las etiquetas del eje X actuales
                String etiqueta = obtenerEtiqueta(index);
                double gastos   = obtenerTotalEnIndice(gastosData,   index);
                double ingresos = obtenerTotalEnIndice(ingresosData, index);
                barClickListener.onBarClick(etiqueta, gastos, ingresos);
            }

            @Override
            public void onNothingSelected() { /* no-op */ }
        });
    }

    // ─── Actualización de datos ───────────────────────────────────────────────

    /**
     * Recibe la nueva lista de gastos mensuales desde el ViewModel.
     * Si ya hay datos de ingresos, redibuja inmediatamente.
     */
    public void setGastos(List<ResumenMensual> gastos) {
        this.gastosData = gastos;
        if (ingresosData != null) redibujar();
    }

    /**
     * Recibe la nueva lista de ingresos mensuales desde el ViewModel.
     * Si ya hay datos de gastos, redibuja inmediatamente.
     */
    public void setIngresos(List<ResumenMensual> ingresos) {
        this.ingresosData = ingresos;
        if (gastosData != null) redibujar();
    }

    /**
     * Reconstruye el gráfico con los datos actuales de ambas series.
     * Alinea ambas listas por mes/año para que las barras se correspondan.
     */
    private void redibujar() {
        // Construir un mapa unificado de meses (clave = "anio-mes")
        // para alinear gastos e ingresos aunque falten meses en alguna serie
        Map<String, ResumenMensual> mapaGastos   = agruparPorMes(gastosData);
        Map<String, ResumenMensual> mapaIngresos = agruparPorMes(ingresosData);

        // Obtener la unión de claves y ordenarla cronológicamente
        List<String> claves = new ArrayList<>();
        claves.addAll(mapaGastos.keySet());
        for (String k : mapaIngresos.keySet()) {
            if (!claves.contains(k)) claves.add(k);
        }
        Collections.sort(claves); // "2024-01", "2024-02", ..., "2025-03"

        if (claves.isEmpty()) {
            mostrarEstadoVacio();
            return;
        }

        // Construir entradas
        List<BarEntry>  entradasGastos   = new ArrayList<>();
        List<BarEntry>  entradasIngresos = new ArrayList<>();
        List<String>    etiquetas        = new ArrayList<>();

        for (int i = 0; i < claves.size(); i++) {
            String clave = claves.get(i);
            ResumenMensual g = mapaGastos.get(clave);
            ResumenMensual ing = mapaIngresos.get(clave);

            float totalGastos   = g   != null ? (float) g.total   : 0f;
            float totalIngresos = ing != null ? (float) ing.total : 0f;

            entradasGastos.add(new BarEntry(i, totalGastos));
            entradasIngresos.add(new BarEntry(i, totalIngresos));

            // Etiqueta del eje X
            ResumenMensual ref = g != null ? g : ing;
            etiquetas.add(ref != null ? ref.getEtiquetaConAnio() : clave);
        }

        // DataSets
        BarDataSet dsGastos = new BarDataSet(entradasGastos, "Gastos");
        dsGastos.setColor(COLOR_GASTOS);
        dsGastos.setHighLightColor(COLOR_GASTOS_T);
        dsGastos.setValueTextSize(9f);
        dsGastos.setValueTextColor(Color.parseColor("#B71C1C"));
        dsGastos.setValueFormatter(new LargeValueFormatter());
        dsGastos.setDrawValues(true);

        BarDataSet dsIngresos = new BarDataSet(entradasIngresos, "Ingresos");
        dsIngresos.setColor(COLOR_INGRESOS);
        dsIngresos.setHighLightColor(COLOR_INGRESOS_T);
        dsIngresos.setValueTextSize(9f);
        dsIngresos.setValueTextColor(Color.parseColor("#1B5E20"));
        dsIngresos.setValueFormatter(new LargeValueFormatter());
        dsIngresos.setDrawValues(true);

        // BarData con barras agrupadas
        BarData barData = new BarData(dsGastos, dsIngresos);
        float groupSpace = 0.2f;  // espacio entre grupos
        float barSpace   = 0.05f; // espacio entre barras del mismo grupo
        float barWidth   = 0.35f; // ancho de cada barra
        // barWidth * 2 + barSpace * 2 + groupSpace = 1.0f (suma = 1)
        barData.setBarWidth(barWidth);

        barChart.setData(barData);

        // Configurar eje X con etiquetas y agrupación
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        barChart.getXAxis().setCenterAxisLabels(true);
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(claves.size());

        barChart.groupBars(0f, groupSpace, barSpace);

        // Animación al cargar
        barChart.animateY(700, Easing.EaseInOutQuart);
        barChart.invalidate();
    }

    // ─── Estado vacío ─────────────────────────────────────────────────────────

    private void mostrarEstadoVacio() {
        barChart.setData(null);
        barChart.invalidate();
    }

    // ─── Helpers internos ─────────────────────────────────────────────────────

    /**
     * Convierte una lista de ResumenMensual en un mapa indexado por "anio-mes"
     * con cero padding (p.ej. "2025-03") para ordenación lexicográfica correcta.
     */
    private Map<String, ResumenMensual> agruparPorMes(List<ResumenMensual> lista) {
        Map<String, ResumenMensual> mapa = new HashMap<>();
        if (lista == null) return mapa;
        for (ResumenMensual r : lista) {
            String clave = String.format("%04d-%02d", r.anio, r.mes);
            mapa.put(clave, r);
        }
        return mapa;
    }

    /**
     * Devuelve la etiqueta del eje X en la posición dada,
     * o una cadena vacía si el índice está fuera de rango.
     */
    private String obtenerEtiqueta(int index) {
        try {
            IndexAxisValueFormatter f = (IndexAxisValueFormatter) barChart.getXAxis().getValueFormatter();
            return f.getFormattedValue(index);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Obtiene el total de una lista de ResumenMensual en la posición dada,
     * correspondiente al orden cronológico construido en redibujar().
     */
    private double obtenerTotalEnIndice(List<ResumenMensual> lista, int index) {
        if (lista == null || index < 0 || index >= lista.size()) return 0.0;
        return lista.get(index).total;
    }

    // ─── Setter ───────────────────────────────────────────────────────────────

    public void setOnBarClickListener(OnBarClickListener listener) {
        this.barClickListener = listener;
    }
}
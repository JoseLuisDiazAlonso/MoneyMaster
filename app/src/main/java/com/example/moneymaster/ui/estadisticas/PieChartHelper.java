package com.example.moneymaster.ui.estadisticas;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.TotalPorCategoria;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper que encapsula TODA la configuración de MPAndroidChart para el PieChart
 * del módulo de Estadísticas (Card #42).
 *
 * Diseñado para separar la lógica del gráfico del Fragment, facilitando el
 * testing y el mantenimiento.
 *
 * Uso básico:
 * <pre>
 *   PieChartHelper helper = new PieChartHelper(context, pieChart);
 *   helper.setOnSliceClickListener((categoria, total) -> filtrarPorCategoria(categoria));
 *   viewModel.gastosPorCategoria.observe(this, helper::actualizarDatos);
 * </pre>
 */
public class PieChartHelper {

    // ─── Colores de fallback cuando una categoría no tiene color definido ─────
    private static final int[] COLORES_FALLBACK = {
            Color.parseColor("#F44336"), // rojo
            Color.parseColor("#E91E63"), // rosa
            Color.parseColor("#9C27B0"), // morado
            Color.parseColor("#3F51B5"), // índigo
            Color.parseColor("#2196F3"), // azul
            Color.parseColor("#00BCD4"), // cian
            Color.parseColor("#4CAF50"), // verde
            Color.parseColor("#FF9800"), // naranja
            Color.parseColor("#FF5722"), // naranja oscuro
            Color.parseColor("#795548"), // marrón
    };

    private final Context context;
    private final PieChart pieChart;
    private OnSliceClickListener sliceClickListener;

    // ─── Interfaz para propagar clicks de sector al Fragment ─────────────────
    public interface OnSliceClickListener {
        /**
         * @param nombreCategoria nombre de la categoría clicada
         * @param total           importe total de esa categoría
         */
        void onSliceClick(String nombreCategoria, double total);
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    public PieChartHelper(Context context, PieChart pieChart) {
        this.context = context;
        this.pieChart = pieChart;
        configurarPieChart();
    }

    // ─── Configuración inicial del PieChart ───────────────────────────────────

    /**
     * Aplica todos los ajustes visuales al PieChart.
     * Se llama UNA sola vez desde el constructor.
     */
    private void configurarPieChart() {
        // Aspecto general
        pieChart.setUsePercentValues(true);      // muestra porcentajes en vez de valores absolutos
        pieChart.getDescription().setEnabled(false); // oculta la descripción por defecto
        pieChart.setExtraOffsets(16f, 16f, 16f, 16f); // padding alrededor del gráfico
        pieChart.setDragDecelerationFrictionCoef(0.95f); // inercia al girar

        // Agujero central (estilo donut)
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(52f);             // radio del agujero en %
        pieChart.setTransparentCircleRadius(57f);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);

        // Texto central
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Gastos\npor categoría");
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.setCenterTextColor(Color.parseColor("#212121"));

        // Etiquetas de cada sector
        pieChart.setDrawEntryLabels(false);      // ocultamos las etiquetas dentro del sector
        // (la leyenda ya las muestra)

        // Rotación inicial
        pieChart.setRotationAngle(270f);         // empieza en la parte superior
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Leyenda (abajo del gráfico)
        configurarLeyenda();

        // Listener de clicks
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                if (!(e instanceof PieEntry)) return;
                PieEntry entry = (PieEntry) e;
                if (sliceClickListener != null && entry.getData() instanceof TotalPorCategoria) {
                    TotalPorCategoria item = (TotalPorCategoria) entry.getData();
                    sliceClickListener.onSliceClick(item.getNombreSeguro(), item.total);
                }
            }

            @Override
            public void onNothingSelected() { /* no-op */ }
        });
    }

    /**
     * Configura la leyenda con nombres de categorías.
     */
    private void configurarLeyenda() {
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.CIRCLE);  // indicador circular
        legend.setFormSize(10f);
        legend.setTextSize(12f);
        legend.setTextColor(Color.parseColor("#424242"));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);           // permite múltiples líneas
        legend.setXEntrySpace(16f);
        legend.setYEntrySpace(6f);
    }

    // ─── Actualización de datos ───────────────────────────────────────────────

    /**
     * Punto de entrada principal. El Fragment llama este método cada vez que
     * el LiveData emite una nueva lista.
     *
     * @param items lista de categorías con totales desde la BD
     */
    public void actualizarDatos(List<TotalPorCategoria> items) {
        if (items == null || items.isEmpty()) {
            mostrarEstadoVacio();
            return;
        }

        List<PieEntry> entradas = construirEntradas(items);
        List<Integer> colores   = extraerColores(items);

        PieDataSet dataSet = new PieDataSet(entradas, "");
        aplicarEstiloDataSet(dataSet, colores);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);
        pieData.setValueTypeface(Typeface.DEFAULT_BOLD);

        pieChart.setData(pieData);
        pieChart.highlightValues(null); // limpia selección previa

        // Animación al cargar (requisito del card)
        pieChart.animateY(800, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    /**
     * Construye la lista de PieEntry a partir de los resultados de la BD.
     * Cada entrada lleva el objeto TotalPorCategoria como "data extra"
     * para recuperarlo en el listener de click.
     */
    private List<PieEntry> construirEntradas(List<TotalPorCategoria> items) {
        List<PieEntry> entradas = new ArrayList<>();
        for (TotalPorCategoria item : items) {
            if (item.total > 0) {
                PieEntry entry = new PieEntry(
                        (float) item.total,
                        item.getNombreSeguro(),
                        item                   // ← payload para el click listener
                );
                entradas.add(entry);
            }
        }
        return entradas;
    }

    /**
     * Extrae los colores de cada categoría o usa el color de fallback si la
     * categoría no tiene color definido.
     */
    private List<Integer> extraerColores(List<TotalPorCategoria> items) {
        List<Integer> colores = new ArrayList<>();
        int fallbackIndex = 0;
        for (TotalPorCategoria item : items) {
            if (item.total <= 0) continue;
            try {
                colores.add(Color.parseColor(item.color));
            } catch (Exception e) {
                // Color no válido o nulo → usamos fallback cíclico
                colores.add(COLORES_FALLBACK[fallbackIndex % COLORES_FALLBACK.length]);
                fallbackIndex++;
            }
        }
        return colores;
    }

    /**
     * Aplica el estilo visual al DataSet (bordes, espaciado, colores).
     */
    private void aplicarEstiloDataSet(PieDataSet dataSet, List<Integer> colores) {
        dataSet.setColors(colores);
        dataSet.setSliceSpace(2f);              // separación entre sectores (px)
        dataSet.setSelectionShift(8f);          // desplazamiento al seleccionar sector
        dataSet.setDrawIcons(false);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineColor(Color.parseColor("#9E9E9E"));
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
    }

    // ─── Estado vacío ─────────────────────────────────────────────────────────

    /**
     * Cuando no hay datos, limpia el gráfico y actualiza el texto central.
     */
    private void mostrarEstadoVacio() {
        pieChart.setCenterText("Sin datos\neste mes");
        pieChart.setData(null);
        pieChart.invalidate();
    }

    // ─── Texto central dinámico ───────────────────────────────────────────────

    /**
     * Actualiza el texto central con el importe total del mes.
     * Llamar desde el Fragment cuando cambia el total.
     *
     * @param total importe total en euros
     */
    public void actualizarTotalCentral(double total) {
        if (total <= 0) {
            pieChart.setCenterText("Sin datos\neste mes");
        } else {
            pieChart.setCenterText(String.format("%.2f €\ntotal", total));
        }
        pieChart.invalidate();
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setOnSliceClickListener(OnSliceClickListener listener) {
        this.sliceClickListener = listener;
    }
}

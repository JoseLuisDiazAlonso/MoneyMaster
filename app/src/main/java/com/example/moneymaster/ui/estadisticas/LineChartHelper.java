package com.example.moneymaster.ui.estadisticas;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.PuntoLinea;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;


public class LineChartHelper {

    private static final int COLOR_LINEA     = Color.parseColor("#E53935"); // rojo
    private static final int COLOR_RELLENO   = Color.parseColor("#33E53935"); // rojo 20% opacidad
    private static final int COLOR_CIRCULO   = Color.parseColor("#B71C1C"); // rojo oscuro
    private static final int COLOR_HIGHLIGHT = Color.parseColor("#FF9800"); // naranja al seleccionar

    private final Context context;
    private final LineChart lineChart;
    private OnPuntoClickListener puntoClickListener;

    //Interfaz de click
    public interface OnPuntoClickListener {
        /**
         * @param etiqueta     etiqueta del punto clicado (p.ej. "15" o "S2")
         * @param acumulado    gasto acumulado hasta ese punto
         * @param totalPunto   gasto sólo de ese día/semana
         */
        void onPuntoClick(String etiqueta, double acumulado, double totalPunto);
    }

    //Constructor

    public LineChartHelper(Context context, LineChart lineChart) {
        this.context   = context;
        this.lineChart = lineChart;
        configurarLineChart();
    }

    //Configuración inicial

    private void configurarLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);

        // Zoom y scroll (requisito del card)
        lineChart.setDragEnabled(true);
        lineChart.setScaleXEnabled(true);   // zoom horizontal
        lineChart.setScaleYEnabled(false);  // sin zoom vertical (confunde)
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(true);

        lineChart.setExtraBottomOffset(16f);
        lineChart.setExtraTopOffset(8f);

        // Eje X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.parseColor("#757575"));
        xAxis.setLabelRotationAngle(0f);

        // Eje Y izquierdo
        YAxis yLeft = lineChart.getAxisLeft();
        yLeft.setAxisMinimum(0f);
        yLeft.setDrawGridLines(true);
        yLeft.setGridColor(Color.parseColor("#E0E0E0"));
        yLeft.setGridLineWidth(0.5f);
        yLeft.setTextSize(10f);
        yLeft.setTextColor(Color.parseColor("#757575"));
        yLeft.setValueFormatter(new LargeValueFormatter());

        // Eje Y derecho — oculto
        lineChart.getAxisRight().setEnabled(false);

        // Leyenda
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setFormSize(12f);
        legend.setTextSize(12f);
        legend.setTextColor(Color.parseColor("#424242"));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // Listener de click
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (puntoClickListener == null) return;
                // e.getData() contiene el PuntoLinea original
                if (e.getData() instanceof PuntoLinea) {
                    PuntoLinea p = (PuntoLinea) e.getData();
                    puntoClickListener.onPuntoClick(p.etiqueta, p.totalAcumulado, p.total);
                }
            }

            @Override
            public void onNothingSelected() { /* no-op */ }
        });
    }

    //Actualización de datos

    /**
     * Actualiza el gráfico con datos de vista DIARIA.
     * Rellena los días sin gastos con 0 para que la línea sea continua.
     *
     * @param puntos lista de PuntoLinea desde Room (sólo días con gastos)
     */
    public void actualizarDiario(List<PuntoLinea> puntos) {
        if (puntos == null || puntos.isEmpty()) {
            mostrarEstadoVacio();
            return;
        }

        // Rellenar huecos: generar los 31 días y buscar en la lista de Room
        int maxDia = 31;
        List<PuntoLinea> completos = rellenarHuecosDiario(puntos, maxDia);
        List<String> etiquetas = new ArrayList<>();
        for (PuntoLinea p : completos) etiquetas.add(p.etiqueta);

        dibujar(completos, etiquetas, "Gastos diarios acumulados");
    }

    /**
     * Actualiza el gráfico con datos de vista SEMANAL.
     *
     * @param puntos lista de PuntoLinea desde Room (semanas con gastos)
     */
    public void actualizarSemanal(List<PuntoLinea> puntos) {
        if (puntos == null || puntos.isEmpty()) {
            mostrarEstadoVacio();
            return;
        }

        List<PuntoLinea> completos = rellenarHuecosSemanal(puntos);
        List<String> etiquetas = new ArrayList<>();
        for (PuntoLinea p : completos) etiquetas.add(p.etiqueta);

        dibujar(completos, etiquetas, "Gastos semanales acumulados");
    }

    /**
     * Construye el LineDataSet y lo pasa al gráfico.
     * Calcula el acumulado, aplica estilos y anima.
     */
    private void dibujar(List<PuntoLinea> puntos, List<String> etiquetas, String label) {
        // Calcular acumulado
        double acum = 0;
        List<Entry> entradas = new ArrayList<>();
        for (int i = 0; i < puntos.size(); i++) {
            PuntoLinea p = puntos.get(i);
            acum += p.total;
            p.totalAcumulado = acum;
            Entry entry = new Entry(i, (float) acum, p); // payload para el click
            entradas.add(entry);
        }

        LineDataSet dataSet = new LineDataSet(entradas, label);
        aplicarEstiloDataSet(dataSet);

        // Eje X con etiquetas
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        lineChart.getXAxis().setAxisMinimum(-0.5f);
        lineChart.getXAxis().setAxisMaximum(puntos.size() - 0.5f);

        LineData lineData = new LineData(dataSet);
        lineData.setValueTextSize(0f); // ocultar valores sobre cada punto (demasiado ruido)
        lineChart.setData(lineData);

        // Zoom reset al cambiar modo
        lineChart.fitScreen();

        // Animación al cargar
        lineChart.animateX(600, Easing.EaseInOutSine);
        lineChart.invalidate();
    }

    /**
     * Aplica todos los estilos visuales al DataSet:
     * color, grosor, círculos, área rellena y highlight.
     */
    private void aplicarEstiloDataSet(LineDataSet dataSet) {
        dataSet.setColor(COLOR_LINEA);
        dataSet.setLineWidth(2.5f);

        // Círculos en cada punto
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(COLOR_CIRCULO);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);

        // Área sombreada bajo la curva (requisito del card)
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(COLOR_RELLENO);
        dataSet.setFillAlpha(60);

        // Línea suavizada (cubicBezier)
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);

        // Highlight al tocar
        dataSet.setHighLightColor(COLOR_HIGHLIGHT);
        dataSet.setHighlightLineWidth(1.5f);
        dataSet.enableDashedHighlightLine(8f, 4f, 0f);

        dataSet.setDrawValues(false);
    }

    //Estado vacío

    private void mostrarEstadoVacio() {
        lineChart.setData(null);
        lineChart.invalidate();
    }

    //Helpers de relleno de huecos

    /**
     * Genera una lista de 31 PuntoLinea (uno por día).
     * Los días sin gasto tienen total = 0.
     * El acumulado se calcula en dibujar(), no aquí.
     */
    private List<PuntoLinea> rellenarHuecosDiario(List<PuntoLinea> desdeBD, int maxDia) {
        // Mapa posicion → PuntoLinea para búsqueda O(1)
        android.util.SparseArray<PuntoLinea> mapa = new android.util.SparseArray<>();
        for (PuntoLinea p : desdeBD) mapa.put(p.posicion, p);

        List<PuntoLinea> resultado = new ArrayList<>();
        for (int dia = 1; dia <= maxDia; dia++) {
            PuntoLinea p = mapa.get(dia);
            if (p != null) {
                resultado.add(p);
            } else {
                resultado.add(new PuntoLinea(String.valueOf(dia), dia, 0.0));
            }
        }
        return resultado;
    }

    /**
     * Genera una lista de 4 PuntoLinea (una por semana: S1–S4).
     * Las semanas sin gasto tienen total = 0.
     */
    private List<PuntoLinea> rellenarHuecosSemanal(List<PuntoLinea> desdeBD) {
        android.util.SparseArray<PuntoLinea> mapa = new android.util.SparseArray<>();
        for (PuntoLinea p : desdeBD) {
            // etiqueta es "S1","S2","S3","S4" → extraer número
            try {
                int semana = Integer.parseInt(p.etiqueta.replace("S", ""));
                mapa.put(semana, p);
            } catch (NumberFormatException ignored) {}
        }

        List<PuntoLinea> resultado = new ArrayList<>();
        for (int s = 1; s <= 4; s++) {
            PuntoLinea p = mapa.get(s);
            if (p != null) {
                resultado.add(p);
            } else {
                resultado.add(new PuntoLinea("S" + s, s, 0.0));
            }
        }
        return resultado;
    }

    //Setter

    public void setOnPuntoClickListener(OnPuntoClickListener listener) {
        this.puntoClickListener = listener;
    }
}
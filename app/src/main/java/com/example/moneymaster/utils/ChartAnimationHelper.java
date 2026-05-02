package com.example.moneymaster.utils;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;


public class ChartAnimationHelper {

    // Duraciones estándar
    private static final int DURATION_SHORT  = 800;
    private static final int DURATION_MEDIUM = 1100;
    private static final int DURATION_LONG   = 1400;

    /**
     * Animación para PieChart (gráfico de categorías de gasto).
     * Los sectores aparecen girando desde 0° con ease-out.
     */
    public static void animatePieChart(PieChart chart) {
        chart.animateY(DURATION_MEDIUM, Easing.EaseInOutQuad);
    }

    /**
     * Animación para BarChart (gastos vs ingresos por mes).
     * Las barras crecen desde la base con un fade simultáneo.
     */
    public static void animateBarChart(BarChart chart) {
        chart.animateXY(DURATION_MEDIUM, DURATION_SHORT, Easing.EaseInOutCubic, Easing.EaseInOutCubic);
    }

    /**
     * Animación para LineChart (evolución acumulada).
     * La línea se dibuja progresivamente de izquierda a derecha.
     */
    public static void animateLineChart(LineChart chart) {
        chart.animateX(DURATION_LONG, Easing.EaseInOutSine);
    }

    /**
     * Animación rápida para refrescos de datos (cuando el usuario cambia el filtro
     * de período sin entrar de nuevo a la pantalla).
     */
    public static void animatePieChartRefresh(PieChart chart) {
        chart.animateY(DURATION_SHORT, Easing.EaseOutCubic);
    }

    public static void animateBarChartRefresh(BarChart chart) {
        chart.animateY(DURATION_SHORT, Easing.EaseOutCubic);
    }

    public static void animateLineChartRefresh(LineChart chart) {
        chart.animateX(DURATION_SHORT, Easing.EaseOutCubic);
    }
}

package com.example.moneymaster.ui.estadisticas;

import android.content.Context;
import android.widget.TextView;

import com.example.moneymaster.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * MarkerView personalizado para el BarChart (Card #43).
 *
 * Muestra un pequeño globo flotante al tocar una barra, con:
 *   · Nombre del mes (tomado de la etiqueta del eje X)
 *   · Importe de la barra seleccionada formateado en euros
 *
 * Para usarlo:
 * <pre>
 *   BarTooltipMarker marker = new BarTooltipMarker(context, R.layout.marker_bar_tooltip);
 *   marker.setChartView(barChart);
 *   barChart.setMarker(marker);
 * </pre>
 *
 * NOTA: el layout marker_bar_tooltip.xml debe existir en res/layout/.
 * Ver el archivo marker_bar_tooltip.xml incluido en este card.
 */
public class BarTooltipMarker extends MarkerView {

    private final TextView tvContenido;
    private final NumberFormat nf =
            NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    public BarTooltipMarker(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContenido = findViewById(R.id.tv_marker_contenido);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // El valor de la barra es e.getY()
        tvContenido.setText(nf.format(e.getY()));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // Centrar horizontalmente y colocar encima de la barra
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 4f);
    }
}

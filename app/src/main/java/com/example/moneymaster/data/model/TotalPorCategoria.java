package com.example.moneymaster.data.model;

public class TotalPorCategoria {

    /** Nombre de la categoría */
    public String nombreCategoria;

    /**
     * Nombre del drawable del icono .
     * Se usa con Resources.getIdentifier() para cargar el drawable dinámicamente.
     */
    public String icono;

    /**
     * Color en formato hexadecimal .
     * Se parsea con Color.parseColor() para pintar el sector del PieChart.
     */
    public String color;

    /** Suma total de gastos de esta categoría en el periodo consultado */
    public double total;

    // ─── Helpers utilizados por PieChartHelper ────────────────────────────────

    /**
     * Devuelve una etiqueta segura: si el nombre es nulo usa "Sin categoría".
     */
    public String getNombreSeguro() {
        return (nombreCategoria != null && !nombreCategoria.isEmpty())
                ? nombreCategoria
                : "Sin categoría";
    }

    /**
     * Porcentaje de este sector respecto al total general.
     * @param totalGeneral suma de todos los sectores
     */
    public float getPorcentaje(double totalGeneral) {
        if (totalGeneral <= 0) return 0f;
        return (float) ((total / totalGeneral) * 100.0);
    }
}
package com.example.moneymaster.data.model;

/**
 * POJO que Room mapea desde queries de resumen mensual.
 *
 * Los alias en la query DEBEN coincidir con los campos:
 *   mes, anio, SUM(...) AS total
 */
public class ResumenMensual {

    public int mes;    // 1 = enero, 12 = diciembre
    public int anio;
    public double total;

    public ResumenMensual() {}

    public ResumenMensual(int mes, int anio, double total) {
        this.mes   = mes;
        this.anio  = anio;
        this.total = total;
    }

    /**
     * Etiqueta corta para el eje X del BarChart.
     * Ejemplo: mes=3, anio=2026 → "Mar"
     */
    public String getEtiquetaMes() {
        String[] nombres = {"Ene","Feb","Mar","Abr","May","Jun",
                "Jul","Ago","Sep","Oct","Nov","Dic"};
        if (mes >= 1 && mes <= 12) return nombres[mes - 1];
        return String.valueOf(mes);
    }

    /**
     * Etiqueta con año para el caso de historial largo.
     * Ejemplo: "Mar 26"
     */
    public String getEtiquetaConAnio() {
        return getEtiquetaMes() + " " + String.valueOf(anio).substring(2);
    }
}

package com.example.moneymaster.data.model;

/**
 * POJO que Room mapea desde queries con GROUP BY de categoría.
 * No es una @Entity — nunca tiene tabla propia.
 *
 * Los nombres de campo DEBEN coincidir exactamente con los alias
 * definidos en los @Query de GastoPersonalDao e IngresoPersonalDao:
 *   c.nombre AS nombreCategoria
 *   c.icono  AS icono
 *   c.color  AS color
 *   SUM(...) AS total
 */
public class TotalPorCategoria {

    public String nombreCategoria;
    public String icono;   // nombre del vector drawable, e.g. "ic_restaurant"
    public String color;   // hex string, e.g. "#FF5722"
    public double total;

    public TotalPorCategoria() {}

    public TotalPorCategoria(String nombreCategoria, String icono, String color, double total) {
        this.nombreCategoria = nombreCategoria;
        this.icono  = icono;
        this.color  = color;
        this.total  = total;
    }
}

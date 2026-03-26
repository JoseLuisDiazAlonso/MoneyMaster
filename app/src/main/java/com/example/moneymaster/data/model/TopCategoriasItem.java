package com.example.moneymaster.data.model;

/**
 * POJO que Room mapea desde la query Top 5 categorías más gastadas.
 *
 * NO es una @Entity — nunca tiene tabla propia.
 * Los nombres de campo coinciden exactamente con los alias SQL del DAO.
 *
 * Campos calculados en SQL:
 *   - porcentaje: (total / totalGeneral) * 100  → calculado en el adaptador
 *   - ranking    : posición 1-5 → asignado por el adaptador según su posición en la lista
 */
public class TopCategoriasItem {

    /** Nombre de la categoría (e.g. "Alimentación") */
    public String nombreCategoria;

    /** Nombre del drawable vector (e.g. "ic_restaurant") */
    public String icono;

    /** Color hex de la categoría (e.g. "#FF5722") */
    public String color;

    /** Suma total gastada en esta categoría en el período */
    public double total;

    // ─── Constructor vacío requerido por Room ─────────────────────────────────

    public TopCategoriasItem() {}

    public TopCategoriasItem(String nombreCategoria, String icono, String color, double total) {
        this.nombreCategoria = nombreCategoria;
        this.icono           = icono;
        this.color           = color;
        this.total           = total;
    }
}
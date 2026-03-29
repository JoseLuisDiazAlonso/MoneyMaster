package com.example.moneymaster.data.model;

public class GastoConCategoria {

    /** ID del gasto (GastoPersonal.id) */
    public int    id;

    /** Descripción opcional del gasto */
    public String descripcion;

    /** Importe del gasto (positivo) */
    public double importe;

    /** Timestamp Unix en ms (campo 'fecha' de GastoPersonal) */
    public long   fecha;

    /**
     * Nombre de la categoría (alias "nombreCategoria" en la query).
     * COALESCE garantiza que nunca sea null aunque la FK sea nullable.
     */
    public String nombreCategoria;

    /**
     * Nombre del drawable del ícono (alias "iconoNombre" en la query).
     */
    public String iconoNombre;

    /**
     * Color hex de la categoría (alias "colorCategoria" en la query).
     */
    public String colorCategoria;
}
package com.example.moneymaster.data.model;

public class IngresoConCategoria {

    /** ID del ingreso (IngresoPersonal.id) */
    public int    id;

    /** Descripción opcional del ingreso */
    public String descripcion;

    /** Importe del ingreso (positivo) */
    public double importe;

    /** Timestamp Unix en ms */
    public long   fecha;

    /** Nombre de la categoría (alias "nombreCategoria" en la query) */
    public String nombreCategoria;

    /** Nombre del drawable del ícono (alias "iconoNombre" en la query) */
    public String iconoNombre;

    /** Color hex de la categoría (alias "colorCategoria" en la query) */
    public String colorCategoria;
}

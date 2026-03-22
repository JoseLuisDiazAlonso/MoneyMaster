package com.example.moneymaster.data.model;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  GastoConCategoria.java                                          ║
 * ║  Ruta: data/model/GastoConCategoria.java                         ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  POJO de resultado para la query JOIN entre GastoPersonal y      ║
 * ║  CategoriaGasto. Room mapea las columnas de la query a estos     ║
 * ║  campos por nombre (case-insensitive).                           ║
 * ║                                                                  ║
 * ║  NOT @Entity. Solo se usa para leer resultados de @Query.        ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
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
     * Ejemplo: "ic_food", "ic_transport".
     */
    public String iconoNombre;

    /**
     * Color hex de la categoría (alias "colorCategoria" en la query).
     * Ejemplo: "#E53935"
     */
    public String colorCategoria;
}
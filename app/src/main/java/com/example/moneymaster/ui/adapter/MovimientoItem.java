package com.example.moneymaster.ui.adapter;

import java.util.Objects;

/**
 * Modelo de presentación (UI model) para un movimiento financiero.
 *
 * Desacopla las entidades Room (GastoPersonal / IngresoPersonal) del adaptador.
 * El Fragment/ViewModel transforma las entidades en MovimientoItem antes
 * de pasarlas al adapter — patrón habitual en MVVM.
 *
 * Contiene todos los campos necesarios para renderizar un ítem en la lista.
 */
public class MovimientoItem {

    // ─── Tipo de movimiento ─────────────────────────────────────────────────
    public enum Tipo { GASTO, INGRESO }

    // ─── Campos ─────────────────────────────────────────────────────────────
    private final int    id;
    private final Tipo   tipo;
    private final String titulo;
    private final String descripcion;
    private final double cantidad;
    private final long   fecha;          // Unix timestamp en segundos
    private final String categoriaNombre;
    private final String categoriaIcono; // nombre del drawable, p.ej. "ic_comida"
    private final String categoriaColor; // hex string, p.ej. "#FF5733"

    // ─── Constructor ────────────────────────────────────────────────────────
    public MovimientoItem(int id, Tipo tipo, String titulo, String descripcion,
                          double cantidad, long fecha,
                          String categoriaNombre, String categoriaIcono,
                          String categoriaColor) {
        this.id              = id;
        this.tipo            = tipo;
        this.titulo          = titulo;
        this.descripcion     = descripcion;
        this.cantidad        = cantidad;
        this.fecha           = fecha;
        this.categoriaNombre = categoriaNombre;
        this.categoriaIcono  = categoriaIcono;
        this.categoriaColor  = categoriaColor;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────
    public int    getId()              { return id; }
    public Tipo   getTipo()            { return tipo; }
    public String getTitulo()          { return titulo; }
    public String getDescripcion()     { return descripcion; }
    public double getCantidad()        { return cantidad; }
    public long   getFecha()           { return fecha; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public String getCategoriaIcono()  { return categoriaIcono; }
    public String getCategoriaColor()  { return categoriaColor; }

    // ─── equals & hashCode — requeridos por DiffUtil ─────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimientoItem)) return false;
        MovimientoItem that = (MovimientoItem) o;
        return id == that.id
                && tipo == that.tipo
                && Double.compare(that.cantidad, cantidad) == 0
                && fecha == that.fecha
                && Objects.equals(titulo, that.titulo)
                && Objects.equals(descripcion, that.descripcion)
                && Objects.equals(categoriaNombre, that.categoriaNombre)
                && Objects.equals(categoriaIcono, that.categoriaIcono)
                && Objects.equals(categoriaColor, that.categoriaColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tipo, titulo, descripcion, cantidad, fecha,
                categoriaNombre, categoriaIcono, categoriaColor);
    }

    // ─── Factory methods — para convertir desde entidades Room ───────────────

    /**
     * Crea un MovimientoItem desde un GastoPersonal + datos de categoría.
     * Usa en el ViewModel al combinar datos de múltiples tablas.
     */
    public static MovimientoItem fromGasto(int id, String titulo, String descripcion,
                                           double cantidad, long fecha,
                                           String catNombre, String catIcono,
                                           String catColor) {
        return new MovimientoItem(id, Tipo.GASTO, titulo, descripcion,
                cantidad, fecha, catNombre, catIcono, catColor);
    }

    /**
     * Crea un MovimientoItem desde un IngresoPersonal + datos de categoría.
     */
    public static MovimientoItem fromIngreso(int id, String titulo, String descripcion,
                                             double cantidad, long fecha,
                                             String catNombre, String catIcono,
                                             String catColor) {
        return new MovimientoItem(id, Tipo.INGRESO, titulo, descripcion,
                cantidad, fecha, catNombre, catIcono, catColor);
    }
}
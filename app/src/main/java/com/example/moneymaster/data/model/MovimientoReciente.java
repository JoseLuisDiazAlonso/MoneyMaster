package com.example.moneymaster.data.model;

public class MovimientoReciente {

    public enum Tipo { GASTO, INGRESO }

    private final int    id;
    private final Tipo   tipo;
    private final String descripcion;
    private final String nombreCategoria;
    private final String iconoNombre;
    private final String colorCategoria;
    private final double importe;
    private final long   fecha;
    private final String fotoRuta;   // Card #32
    private final int    fotoId;     // Card #35 — ID de FotoRecibo, 0 si no hay foto

    // ─── Constructor completo (Card #35) ─────────────────────────────────────
    public MovimientoReciente(int id,
                              Tipo tipo,
                              String descripcion,
                              String nombreCategoria,
                              String iconoNombre,
                              String colorCategoria,
                              double importe,
                              long fecha,
                              String fotoRuta,
                              int fotoId) {
        this.id              = id;
        this.tipo            = tipo;
        this.descripcion     = descripcion != null ? descripcion : "";
        this.nombreCategoria = nombreCategoria != null ? nombreCategoria : "Sin categoría";
        this.iconoNombre     = iconoNombre != null ? iconoNombre : "ic_category";
        this.colorCategoria  = colorCategoria != null ? colorCategoria : "#6200EE";
        this.importe         = importe;
        this.fecha           = fecha;
        this.fotoRuta        = fotoRuta;
        this.fotoId          = fotoId;
    }

    // ─── Constructor Card #32 — compatibilidad (fotoId = 0) ──────────────────
    public MovimientoReciente(int id,
                              Tipo tipo,
                              String descripcion,
                              String nombreCategoria,
                              String iconoNombre,
                              String colorCategoria,
                              double importe,
                              long fecha,
                              String fotoRuta) {
        this(id, tipo, descripcion, nombreCategoria,
                iconoNombre, colorCategoria, importe, fecha, fotoRuta, 0);
    }

    // ─── Constructor legacy sin foto ──────────────────────────────────────────
    public MovimientoReciente(int id,
                              Tipo tipo,
                              String descripcion,
                              String nombreCategoria,
                              String iconoNombre,
                              String colorCategoria,
                              double importe,
                              long fecha) {
        this(id, tipo, descripcion, nombreCategoria,
                iconoNombre, colorCategoria, importe, fecha, null, 0);
    }

    // ─── Getters ─────────────────────────────────────────────────────────────
    public int    getId()              { return id; }
    public Tipo   getTipo()            { return tipo; }
    public String getDescripcion()     { return descripcion; }
    public String getNombreCategoria() { return nombreCategoria; }
    public String getIconoNombre()     { return iconoNombre; }
    public String getColorCategoria()  { return colorCategoria; }
    public double getImporte()         { return importe; }
    public long   getFecha()           { return fecha; }
    public String getFotoRuta()        { return fotoRuta; }
    public int    getFotoId()          { return fotoId; }     // Card #35

    // ─── equals para DiffUtil ────────────────────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimientoReciente)) return false;
        MovimientoReciente other = (MovimientoReciente) o;
        return id == other.id
                && tipo == other.tipo
                && importe == other.importe
                && fecha == other.fecha
                && fotoId == other.fotoId
                && descripcion.equals(other.descripcion)
                && nombreCategoria.equals(other.nombreCategoria)
                && java.util.Objects.equals(fotoRuta, other.fotoRuta);
    }
}
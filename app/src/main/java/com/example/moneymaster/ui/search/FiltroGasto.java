package com.example.moneymaster.ui.search;

/**
 * FiltroGasto — Card #58
 *
 * Modelo inmutable que representa el estado completo de los filtros
 * aplicados en la pantalla de búsqueda.
 *
 * Se construye con el Builder para facilitar actualizaciones parciales:
 *
 *   FiltroGasto filtro = new FiltroGasto.Builder()
 *       .query("gasolina")
 *       .categoriaId(3)
 *       .montoMin(10.0)
 *       .montoMax(100.0)
 *       .fechaDesde(timestamp)
 *       .build();
 */
public class FiltroGasto {

    /** Texto libre para buscar en descripción. Null = sin filtro de texto. */
    public final String  query;

    /** ID de categoría seleccionada. -1 = todas las categorías. */
    public final int     categoriaId;

    /** Monto mínimo. -1 = sin límite inferior. */
    public final double  montoMin;

    /** Monto máximo. -1 = sin límite superior. */
    public final double  montoMax;

    /** Timestamp Unix ms inicio del rango de fecha. -1 = sin límite. */
    public final long    fechaDesde;

    /** Timestamp Unix ms fin del rango de fecha. -1 = sin límite. */
    public final long    fechaHasta;

    private FiltroGasto(Builder b) {
        this.query      = b.query;
        this.categoriaId = b.categoriaId;
        this.montoMin   = b.montoMin;
        this.montoMax   = b.montoMax;
        this.fechaDesde = b.fechaDesde;
        this.fechaHasta = b.fechaHasta;
    }

    /** Devuelve true si no hay ningún filtro activo. */
    public boolean isEmpty() {
        return (query == null || query.trim().isEmpty())
                && categoriaId == -1
                && montoMin    == -1
                && montoMax    == -1
                && fechaDesde  == -1
                && fechaHasta  == -1;
    }

    /** Cuenta cuántos filtros distintos al texto están activos (para badge en el botón). */
    public int contarFiltrosActivos() {
        int count = 0;
        if (categoriaId != -1) count++;
        if (montoMin    != -1 || montoMax != -1) count++;
        if (fechaDesde  != -1 || fechaHasta != -1) count++;
        return count;
    }

    /** Devuelve un Builder inicializado con los valores actuales para actualización parcial. */
    public Builder toBuilder() {
        return new Builder()
                .query(query)
                .categoriaId(categoriaId)
                .montoMin(montoMin)
                .montoMax(montoMax)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta);
    }

    // ─── Builder ─────────────────────────────────────────────────────────────

    public static class Builder {
        private String query      = null;
        private int    categoriaId = -1;
        private double montoMin   = -1;
        private double montoMax   = -1;
        private long   fechaDesde = -1;
        private long   fechaHasta = -1;

        public Builder query(String q)          { this.query       = q;  return this; }
        public Builder categoriaId(int id)      { this.categoriaId = id; return this; }
        public Builder montoMin(double v)       { this.montoMin    = v;  return this; }
        public Builder montoMax(double v)       { this.montoMax    = v;  return this; }
        public Builder fechaDesde(long ts)      { this.fechaDesde  = ts; return this; }
        public Builder fechaHasta(long ts)      { this.fechaHasta  = ts; return this; }

        public FiltroGasto build() { return new FiltroGasto(this); }
    }

    /** Filtro vacío (sin restricciones). */
    public static FiltroGasto empty() {
        return new Builder().build();
    }
}

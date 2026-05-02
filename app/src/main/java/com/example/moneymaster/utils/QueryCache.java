package com.example.moneymaster.utils;

import android.util.LruCache;


public class QueryCache {

    // ── Claves de prefijo para organizar las entradas ─────────────────────
    public static final String KEY_TOTAL_MES_GASTO    = "total_mes_gasto_";
    public static final String KEY_TOTAL_MES_INGRESO  = "total_mes_ingreso_";
    public static final String KEY_TOTAL_ANIO_GASTO   = "total_anio_gasto_";
    public static final String KEY_TOTAL_ANIO_INGRESO = "total_anio_ingreso_";
    public static final String KEY_BALANCE_GRUPO      = "balance_grupo_";

    // ── Tamaño máximo: 64 entradas en memoria ────────────────────────────
    private static final int MAX_SIZE = 64;

    private static QueryCache INSTANCE;
    private final LruCache<String, Object> cache;

    private QueryCache() {
        cache = new LruCache<>(MAX_SIZE);
    }

    public static synchronized QueryCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QueryCache();
        }
        return INSTANCE;
    }

    /** Guarda un valor en caché. */
    public void put(String key, Object value) {
        if (key != null && value != null) {
            cache.put(key, value);
        }
    }

    /**
     * Recupera un valor de caché.
     * @return el valor almacenado, o null si no existe o ha sido eviccionado.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (key == null) return null;
        return (T) cache.get(key);
    }

    /** Elimina una entrada específica (usar tras escritura). */
    public void invalidate(String key) {
        if (key != null) {
            cache.remove(key);
        }
    }

    /**
     * Invalida todas las entradas que comiencen con el prefijo dado.
     * Útil para borrar todas las entradas de un usuario o un mes concreto.
     */
    public void invalidateByPrefix(String prefix) {
        if (prefix == null) return;
        // LruCache no expone sus claves directamente; usamos snapshot()
        for (String key : cache.snapshot().keySet()) {
            if (key.startsWith(prefix)) {
                cache.remove(key);
            }
        }
    }

    /** Vacía completamente la caché (logout, cambio de usuario). */
    public void clear() {
        cache.evictAll();
    }

    // ── Helpers de construcción de claves ────────────────────────────────

    /** Clave canónica para total mensual de gastos. */
    public static String keyTotalMesGasto(int usuarioId, String mesAnio) {
        return KEY_TOTAL_MES_GASTO + usuarioId + "_" + mesAnio;
    }

    /** Clave canónica para total mensual de ingresos. */
    public static String keyTotalMesIngreso(int usuarioId, String mesAnio) {
        return KEY_TOTAL_MES_INGRESO + usuarioId + "_" + mesAnio;
    }

    /** Clave canónica para total anual de gastos. */
    public static String keyTotalAnioGasto(int usuarioId, String anio) {
        return KEY_TOTAL_ANIO_GASTO + usuarioId + "_" + anio;
    }

    /** Clave canónica para balance de grupo. */
    public static String keyBalanceGrupo(int grupoId) {
        return KEY_BALANCE_GRUPO + grupoId;
    }
}
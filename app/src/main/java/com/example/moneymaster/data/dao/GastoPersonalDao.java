package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.GastoConCategoria;
import com.example.moneymaster.data.model.TopCategoriasItem;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

/**
 * GastoPersonalDao — Card #62: métodos originales conservados + optimizaciones.
 *
 * Cambios respecto a la versión anterior:
 *  - getGastosByUsuario()        → añadido LIMIT 200 (evita cargar tabla entera)
 *  - getGastosByMes()            → sin cambios (ya filtra por mes/año, resultado acotado)
 *  - getUltimosGastos()          → conservado con :limite dinámico
 *  - getTotalGastosMes()         → sin cambios
 *  - getGastosPorCategoria()     → sin cambios (agregado, resultado pequeño)
 *  - getResumenUltimosMeses()    → sin cambios
 *  - Todos los SELECT usan las columnas indexadas (fecha, categoria_id)
 *    definidas en la entidad GastoPersonal con @Index
 *
 * Nota sobre tipos: usuarioId es long (coherente con GastoRepository y MainViewModel).
 * mes y anio son int (filtrado con strftime sobre timestamp en ms).
 */
@Dao
public interface GastoPersonalDao {

    // ─────────────────────────────────────────────────────────────────────────
    // INSERT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(GastoPersonal gasto);

    @Update
    void actualizar(GastoPersonal gasto);

    @Delete
    void eliminar(GastoPersonal gasto);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — usados por GastoRepository
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Todos los gastos del usuario ordenados por fecha DESC.
     * Card #62: LIMIT 200 añadido para evitar OOM en usuarios con muchos registros.
     * Para listados completos sin límite usar getAllSync().
     */
    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC " +
            "LIMIT 200")
    LiveData<List<GastoPersonal>> getGastosByUsuario(long usuarioId);

    /**
     * Gastos filtrados por mes y año.
     * Usa el índice de fecha: strftime convierte el timestamp ms a texto.
     * printf('%02d') garantiza que mes < 10 quede como "01", "02"…
     */
    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByMes(long usuarioId, int mes, int anio);

    /**
     * Total de gastos del mes para el balance del Dashboard.
     * Resultado: un único Double — consulta rápida con índice de fecha.
     */
    @Query("SELECT SUM(monto) FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes)")
    LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio);

    /**
     * Últimos N gastos para el widget del Dashboard.
     * Card #62: ya tenía LIMIT dinámico — se conserva tal cual.
     * MainViewModel lo llama con limite = 5.
     */
    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC " +
            "LIMIT :limite")
    LiveData<List<GastoPersonal>> getUltimosGastos(long usuarioId, int limite);

    /**
     * Gasto por ID (para edición / detalle).
     */
    @Query("SELECT * FROM gastos_personales WHERE id = :gastoId LIMIT 1")
    LiveData<GastoPersonal> getGastoById(long gastoId);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — Estadísticas (usados por GastoRepository)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Suma de gastos agrupada por categoría para el mes indicado.
     * Usa índice de categoria_id. Resultado pequeño (≤ nº categorías).
     */
    @Query("SELECT c.nombre AS nombreCategoria, " +
            "       c.color AS colorCategoria, " +
            "       c.icono AS iconoCategoria, " +
            "       SUM(g.monto) AS total " +
            "FROM gastos_personales g " +
            "LEFT JOIN categorias_gasto c ON g.categoria_id = c.id " +
            "WHERE g.usuarioId = :usuarioId " +
            "  AND strftime('%Y', g.fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', g.fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "GROUP BY g.categoria_id " +
            "ORDER BY total DESC")
    LiveData<List<TotalPorCategoria>> getGastosPorCategoria(long usuarioId, int mes, int anio);

    /**
     * Top 5 categorías con más gasto en el mes indicado.
     * Usado por EstadisticasRepository (STATS-005).
     */
    @Query("SELECT c.nombre AS nombreCategoria, " +
            "       c.icono  AS icono, " +
            "       c.color  AS color, " +
            "       SUM(g.monto) AS total " +
            "FROM gastos_personales g " +
            "LEFT JOIN categorias_gasto c ON g.categoria_id = c.id " +
            "WHERE g.usuarioId = :usuarioId " +
            "  AND strftime('%Y', g.fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', g.fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "GROUP BY g.categoria_id " +
            "ORDER BY total DESC " +
            "LIMIT 5")
    LiveData<List<TopCategoriasItem>> getTop5CategoriasMes(long usuarioId, int mes, int anio);

    /**
     * Resumen mensual (total gastos por mes) para los últimos N meses.
     * Usado por el LineChart de evolución en EstadisticasFragment.
     */
    @Query("SELECT CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) AS mes, " +
            "       CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) AS anio, " +
            "       SUM(monto) AS total " +
            "FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "GROUP BY strftime('%Y-%m', fecha / 1000, 'unixepoch') " +
            "ORDER BY anio DESC, mes DESC " +
            "LIMIT :meses")
    LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — usados por StatisticsViewModel (Card #62)
    // ─────────────────────────────────────────────────────────────────────────

    /** Todos los gastos del año indicado (usa índice de fecha). */
    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByAnio(long usuarioId, int anio);

    /** Gastos en un rango de fechas (timestamps ms, inclusive). */
    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByRango(long usuarioId, long inicio, long fin);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — Síncronas para Export (PDF / CSV)
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC")
    List<GastoPersonal> getAllSync(long usuarioId);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "ORDER BY fecha DESC")
    List<GastoPersonal> getByMesSync(long usuarioId, int mes, int anio);

    @Query("SELECT * FROM gastos_personales WHERE id = :id LIMIT 1")
    GastoPersonal getByIdSync(long id);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — Síncronas para Export (rango de fechas y completo)
    // ─────────────────────────────────────────────────────────────────────────

    /** Gastos en un rango de timestamps ms — para ExportFragment CSV/PDF. */
    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "ORDER BY fecha DESC")
    List<GastoPersonal> getGastosParaExportacion(int usuarioId, long inicio, long fin);

    /** Todos los gastos del usuario sin filtro de fecha — para CSV completo. */
    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC")
    List<GastoPersonal> getTodosGastosParaExportacion(int usuarioId);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — usados por DashboardViewModel (rango de timestamps)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gastos con datos de categoría filtrados por rango de timestamps ms.
     * Usado por DashboardViewModel para el selector de mes.
     */
    @Query("SELECT g.id AS id, " +
            "       g.descripcion AS descripcion, " +
            "       g.monto AS importe, " +
            "       g.fecha AS fecha, " +
            "       COALESCE(c.nombre, 'Sin categoría') AS nombreCategoria, " +
            "       COALESCE(c.icono,  'ic_category_default') AS iconoNombre, " +
            "       COALESCE(c.color,  '#9E9E9E') AS colorCategoria " +
            "FROM gastos_personales g " +
            "LEFT JOIN categorias_gasto c ON g.categoria_id = c.id " +
            "WHERE g.usuarioId = :usuarioId " +
            "  AND g.fecha >= :inicio " +
            "  AND g.fecha < :fin " +
            "ORDER BY g.fecha DESC")
    LiveData<List<GastoConCategoria>> getGastosPorCategoria(int usuarioId, long inicio, long fin);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — usados por BusquedaViewModel (Card #58)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Búsqueda de gastos personales con filtros combinados.
     * Devuelve GastoConCategoria con JOIN a categorias_gasto.
     * Todos los parámetros son opcionales: pasar null/empty o -1 para ignorarlos.
     */
    @Query("SELECT g.id AS id, " +
            "       g.descripcion AS descripcion, " +
            "       g.monto AS importe, " +
            "       g.fecha AS fecha, " +
            "       COALESCE(c.nombre, 'Sin categoría') AS nombreCategoria, " +
            "       COALESCE(c.icono,  'ic_category_default') AS iconoNombre, " +
            "       COALESCE(c.color,  '#9E9E9E') AS colorCategoria " +
            "FROM gastos_personales g " +
            "LEFT JOIN categorias_gasto c ON g.categoria_id = c.id " +
            "WHERE g.usuarioId = :usuarioId " +
            "  AND (:query IS NULL OR :query = '' " +
            "       OR LOWER(g.descripcion) LIKE '%' || LOWER(:query) || '%') " +
            "  AND (:categoriaId = -1 OR g.categoria_id = :categoriaId) " +
            "  AND (:montoMin = -1    OR g.monto >= :montoMin) " +
            "  AND (:montoMax = -1    OR g.monto <= :montoMax) " +
            "  AND (:fechaDesde = -1  OR g.fecha >= :fechaDesde) " +
            "  AND (:fechaHasta = -1  OR g.fecha <= :fechaHasta) " +
            "ORDER BY g.fecha DESC")
    LiveData<List<GastoConCategoria>> buscarGastos(
            int usuarioId,
            String query,
            int categoriaId,
            double montoMin,
            double montoMax,
            long fechaDesde,
            long fechaHasta);

    /**
     * Cuenta los resultados del mismo filtro para el contador de BusquedaViewModel.
     */
    @Query("SELECT COUNT(*) FROM gastos_personales g " +
            "WHERE g.usuarioId = :usuarioId " +
            "  AND (:query IS NULL OR :query = '' " +
            "       OR LOWER(g.descripcion) LIKE '%' || LOWER(:query) || '%') " +
            "  AND (:categoriaId = -1 OR g.categoria_id = :categoriaId) " +
            "  AND (:montoMin = -1    OR g.monto >= :montoMin) " +
            "  AND (:montoMax = -1    OR g.monto <= :montoMax) " +
            "  AND (:fechaDesde = -1  OR g.fecha >= :fechaDesde) " +
            "  AND (:fechaHasta = -1  OR g.fecha <= :fechaHasta)")
    LiveData<Integer> contarResultadosPersonales(
            int usuarioId,
            String query,
            int categoriaId,
            double montoMin,
            double montoMax,
            long fechaDesde,
            long fechaHasta);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT / UPDATE — usados por ImageViewerViewModel (Card #35)
    // ─────────────────────────────────────────────────────────────────────────

    /** Busca el gasto personal que tiene vinculada una foto por su ID. */
    @Query("SELECT * FROM gastos_personales WHERE tieneFoto = :fotoId LIMIT 1")
    GastoPersonal getByFotoReciboId(int fotoId);

    /** Desvincula una foto de todos los gastos personales que la referencian. */
    @Query("UPDATE gastos_personales SET tieneFoto = NULL, tieneFoto = 0, fotoRuta = NULL WHERE tieneFoto = :fotoId")
    void desvincularFoto(int fotoId);
}
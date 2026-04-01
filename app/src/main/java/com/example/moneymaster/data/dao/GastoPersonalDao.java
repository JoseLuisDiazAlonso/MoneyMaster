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

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC " +
            "LIMIT 200")
    LiveData<List<GastoPersonal>> getGastosByUsuario(long usuarioId);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByMes(long usuarioId, int mes, int anio);

    @Query("SELECT SUM(monto) FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes)")
    LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC " +
            "LIMIT :limite")
    LiveData<List<GastoPersonal>> getUltimosGastos(long usuarioId, int limite);

    @Query("SELECT * FROM gastos_personales WHERE id = :gastoId LIMIT 1")
    LiveData<GastoPersonal> getGastoById(long gastoId);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — Estadísticas
    // ─────────────────────────────────────────────────────────────────────────

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
    // SELECT — StatisticsViewModel
    // ─────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByAnio(long usuarioId, int anio);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByRango(long usuarioId, long inicio, long fin);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — Síncronas para Export
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

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "ORDER BY fecha DESC")
    List<GastoPersonal> getGastosParaExportacion(int usuarioId, long inicio, long fin);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC")
    List<GastoPersonal> getTodosGastosParaExportacion(int usuarioId);

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT — DashboardViewModel
    // ─────────────────────────────────────────────────────────────────────────

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
    // SELECT — BusquedaViewModel
    // ─────────────────────────────────────────────────────────────────────────

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
}
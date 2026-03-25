package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.GastoConCategoria;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.PuntoLinea;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

@Dao
public interface GastoPersonalDao {

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(GastoPersonal gasto);

    @Update
    void actualizar(GastoPersonal gasto);

    @Delete
    void eliminar(GastoPersonal gasto);

    // ─── Listas ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM gastos_personales WHERE usuario_id = :usuarioId ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByUsuario(long usuarioId);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :mes " +
            "  AND CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :anio " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByMes(long usuarioId, int mes, int anio);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId ORDER BY fecha DESC LIMIT :limite")
    LiveData<List<GastoPersonal>> getUltimosGastos(long usuarioId, int limite);

    @Query("SELECT * FROM gastos_personales WHERE id = :gastoId LIMIT 1")
    LiveData<GastoPersonal> getGastoById(long gastoId);

    // ─── Totales ──────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :mes " +
            "  AND CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :anio")
    LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId AND fecha BETWEEN :desde AND :hasta")
    LiveData<Double> getTotalGastosRango(long usuarioId, long desde, long hasta);

    // ─── Estadísticas: PieChart (long + int) ─────────────────────────────────

    @Query("SELECT " +
            "  c.nombre     AS nombreCategoria, " +
            "  c.icono      AS icono, " +
            "  c.color      AS color, " +
            "  SUM(g.monto) AS total " +
            "FROM gastos_personales g " +
            "INNER JOIN categorias_gasto c ON g.categoria_id = c.id " +
            "WHERE g.usuario_id = :usuarioId " +
            "  AND CAST(strftime('%m', datetime(g.fecha / 1000, 'unixepoch')) AS INTEGER) = :mes " +
            "  AND CAST(strftime('%Y', datetime(g.fecha / 1000, 'unixepoch')) AS INTEGER) = :anio " +
            "GROUP BY c.id " +
            "ORDER BY total DESC")
    LiveData<List<TotalPorCategoria>> getGastosPorCategoria(long usuarioId, int mes, int anio);

    // ─── Estadísticas: PieChart (int + String) ────────────────────────────────

    @Query("SELECT cg.nombre AS nombreCategoria, " +
            "       cg.icono AS icono, " +
            "       cg.color AS color, " +
            "       SUM(gp.monto) AS total " +
            "FROM gastos_personales gp " +
            "LEFT JOIN categorias_gasto cg ON gp.categoria_id = cg.id " +
            "WHERE gp.usuario_id = :usuarioId " +
            "  AND strftime('%m', gp.fecha / 1000, 'unixepoch') = :mes " +
            "  AND strftime('%Y', gp.fecha / 1000, 'unixepoch') = :anio " +
            "GROUP BY gp.categoria_id " +
            "ORDER BY total DESC")
    LiveData<List<TotalPorCategoria>> getGastosPorCategoria(int usuarioId, String mes, String anio);

    @Query("SELECT cg.nombre AS nombreCategoria, " +
            "       cg.icono AS icono, " +
            "       cg.color AS color, " +
            "       SUM(gp.monto) AS total " +
            "FROM gastos_personales gp " +
            "LEFT JOIN categorias_gasto cg ON gp.categoria_id = cg.id " +
            "WHERE gp.usuario_id = :usuarioId " +
            "  AND strftime('%m', gp.fecha / 1000, 'unixepoch') = :mes " +
            "  AND strftime('%Y', gp.fecha / 1000, 'unixepoch') = :anio " +
            "GROUP BY gp.categoria_id " +
            "ORDER BY total DESC")
    List<TotalPorCategoria> getGastosPorCategoriaSync(int usuarioId, String mes, String anio);

    @Query("SELECT COALESCE(SUM(monto), 0) " +
            "FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = :mes " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = :anio")
    LiveData<Double> getTotalGastosMes(int usuarioId, String mes, String anio);

    // ─── Estadísticas: BarChart ───────────────────────────────────────────────

    @Query("SELECT " +
            "  CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS mes, " +
            "  CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS anio, " +
            "  COALESCE(SUM(monto), 0.0) AS total " +
            "FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "GROUP BY anio, mes " +
            "ORDER BY anio DESC, mes DESC " +
            "LIMIT :meses")
    LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses);

    @Query("SELECT " +
            "  CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS mes, " +
            "  CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS anio, " +
            "  COALESCE(SUM(monto), 0.0) AS total " +
            "FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "GROUP BY anio, mes " +
            "ORDER BY anio DESC, mes DESC " +
            "LIMIT :meses")
    List<ResumenMensual> getResumenUltimosMesesSync(long usuarioId, int meses);

    // ─── Estadísticas: LineChart diario (Card #44) ────────────────────────────

    @Query("SELECT " +
            "  CAST(strftime('%d', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS posicion, " +
            "  strftime('%d', datetime(fecha / 1000, 'unixepoch'))                  AS etiqueta, " +
            "  COALESCE(SUM(monto), 0.0)                                            AS total " +
            "FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "GROUP BY posicion " +
            "ORDER BY posicion ASC")
    LiveData<List<PuntoLinea>> getGastosDiarios(long usuarioId, long inicio, long fin);

    // ─── Estadísticas: LineChart semanal (Card #44) ───────────────────────────

    @Query("SELECT " +
            "  MIN(CAST(strftime('%d', datetime(fecha / 1000, 'unixepoch')) AS INTEGER)) AS posicion, " +
            "  'S' || CAST(MIN(((CAST(strftime('%d', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) - 1) / 7) + 1) AS TEXT) AS etiqueta, " +
            "  COALESCE(SUM(monto), 0.0) AS total " +
            "FROM gastos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "GROUP BY ((CAST(strftime('%d', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) - 1) / 7) " +
            "ORDER BY posicion ASC")
    LiveData<List<PuntoLinea>> getGastosSemanales(long usuarioId, long inicio, long fin);

    // ─── Gastos del mes con categoría (Dashboard / RecyclerView) ─────────────

    @Query("SELECT " +
            "  gp.id              AS id, " +
            "  gp.descripcion     AS descripcion, " +
            "  gp.monto           AS importe, " +
            "  gp.fecha           AS fecha, " +
            "  COALESCE(cg.nombre, 'Sin categoría') AS nombreCategoria, " +
            "  COALESCE(cg.icono, 'ic_category')    AS iconoNombre, " +
            "  COALESCE(cg.color, '#6200EE')         AS colorCategoria " +
            "FROM gastos_personales gp " +
            "LEFT JOIN categorias_gasto cg ON gp.categoria_id = cg.id " +
            "WHERE gp.usuario_id = :userId " +
            "  AND gp.fecha >= :inicio " +
            "  AND gp.fecha <  :fin " +
            "ORDER BY gp.fecha DESC")
    LiveData<List<GastoConCategoria>> getGastosConCategoriaDelMes(
            int userId, long inicio, long fin);

    @Query("SELECT COALESCE(SUM(monto), 0.0) " +
            "FROM gastos_personales " +
            "WHERE usuario_id = :userId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <  :fin")
    LiveData<Double> getTotalGastosMesRango(int userId, long inicio, long fin);

    // ─── Consultas por año y rango (StatisticsFragment) ──────────────────────

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuario_id = :userId " +
            "AND CAST(strftime('%m', fecha, 'unixepoch') AS INTEGER) = :mes " +
            "AND CAST(strftime('%Y', fecha, 'unixepoch') AS INTEGER) = :anio " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByMonthYear(int userId, int mes, int anio);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuario_id = :userId " +
            "AND CAST(strftime('%Y', fecha, 'unixepoch') AS INTEGER) = :anio " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByYear(int userId, int anio);

    @Query("SELECT * FROM gastos_personales " +
            "WHERE usuario_id = :userId " +
            "AND fecha >= :startTimestamp AND fecha <= :endTimestamp " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoPersonal>> getGastosByDateRange(int userId, long startTimestamp, long endTimestamp);

    // ─── Foto recibo (Card #35) ───────────────────────────────────────────────

    @Query("SELECT * FROM gastos_personales WHERE foto_recibo_id = :fotoId LIMIT 1")
    GastoPersonal getByFotoReciboId(int fotoId);

    @Query("UPDATE gastos_personales SET foto_recibo_id = NULL WHERE foto_recibo_id = :fotoId")
    void desvincularFoto(int fotoId);

    // ─── Conteo ───────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM gastos_personales WHERE usuario_id = :usuarioId")
    int countGastos(long usuarioId);
}
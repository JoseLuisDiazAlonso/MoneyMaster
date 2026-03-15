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

    /**
     * Gastos de un mes/año concretos derivando mes y año del timestamp Unix
     * mediante strftime, ya que la entidad no tiene columnas 'mes'/'anio'.
     */
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

    // ─── Estadísticas: PieChart ───────────────────────────────────────────────

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

    @Query("SELECT COUNT(*) FROM gastos_personales WHERE usuario_id = :usuarioId")
    int countGastos(long usuarioId);
}
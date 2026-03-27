package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.IngresoConCategoria;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

@Dao
public interface IngresoPersonalDao {

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(IngresoPersonal ingreso);

    @Update
    void actualizar(IngresoPersonal ingreso);

    @Delete
    void eliminar(IngresoPersonal ingreso);

    // ─── Listas ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM ingresos_personales WHERE usuario_id = :usuarioId ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByUsuario(long usuarioId);

    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :mes " +
            "  AND CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :anio " +
            "ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByMes(long usuarioId, int mes, int anio);

    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId ORDER BY fecha DESC LIMIT :limite")
    LiveData<List<IngresoPersonal>> getUltimosIngresos(long usuarioId, int limite);

    @Query("SELECT * FROM ingresos_personales WHERE id = :ingresoId LIMIT 1")
    LiveData<IngresoPersonal> getIngresoById(long ingresoId);

    // ─── Totales ──────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :mes " +
            "  AND CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) = :anio")
    LiveData<Double> getTotalIngresosMes(long usuarioId, int mes, int anio);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId AND fecha BETWEEN :desde AND :hasta")
    LiveData<Double> getTotalIngresosRango(long usuarioId, long desde, long hasta);

    // ─── Estadísticas: PieChart ───────────────────────────────────────────────

    @Query("SELECT " +
            "  c.nombre     AS nombreCategoria, " +
            "  c.icono      AS icono, " +
            "  c.color      AS color, " +
            "  SUM(g.monto) AS total " +
            "FROM ingresos_personales g " +
            "INNER JOIN categorias_ingreso c ON g.categoria_id = c.id " +
            "WHERE g.usuario_id = :usuarioId " +
            "  AND CAST(strftime('%m', datetime(g.fecha / 1000, 'unixepoch')) AS INTEGER) = :mes " +
            "  AND CAST(strftime('%Y', datetime(g.fecha / 1000, 'unixepoch')) AS INTEGER) = :anio " +
            "GROUP BY c.id " +
            "ORDER BY total DESC")
    LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(long usuarioId, int mes, int anio);

    // ─── Estadísticas: BarChart ───────────────────────────────────────────────

    @Query("SELECT " +
            "  CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS mes, " +
            "  CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS anio, " +
            "  COALESCE(SUM(monto), 0.0) AS total " +
            "FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "GROUP BY anio, mes " +
            "ORDER BY anio DESC, mes DESC " +
            "LIMIT :meses")
    LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses);

    @Query("SELECT " +
            "  CAST(strftime('%m', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS mes, " +
            "  CAST(strftime('%Y', datetime(fecha / 1000, 'unixepoch')) AS INTEGER) AS anio, " +
            "  COALESCE(SUM(monto), 0.0) AS total " +
            "FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "GROUP BY anio, mes " +
            "ORDER BY anio DESC, mes DESC " +
            "LIMIT :meses")
    List<ResumenMensual> getResumenUltimosMesesSync(long usuarioId, int meses);

    @Query("SELECT COUNT(*) FROM ingresos_personales WHERE usuario_id = :usuarioId")
    int countIngresos(long usuarioId);

    @Query("SELECT " +
            "  ip.id              AS id, " +
            "  ip.descripcion     AS descripcion, " +
            "  ip.monto           AS importe, " +
            "  ip.fecha           AS fecha, " +
            "  COALESCE(ci.nombre, 'Sin categoría') AS nombreCategoria, " +
            "  COALESCE(ci.icono, 'ic_category')    AS iconoNombre, " +
            "  COALESCE(ci.color, '#4CAF50')         AS colorCategoria " +
            "FROM ingresos_personales ip " +
            "LEFT JOIN categorias_ingreso ci ON ip.categoria_id = ci.id " +
            "WHERE ip.usuario_id = :userId " +
            "  AND ip.fecha >= :inicio " +
            "  AND ip.fecha <  :fin " +
            "ORDER BY ip.fecha DESC")
    LiveData<List<IngresoConCategoria>> getIngresosConCategoriaDelMes(
            int userId, long inicio, long fin);

    @Query("SELECT COALESCE(SUM(monto), 0.0) " +
            "FROM ingresos_personales " +
            "WHERE usuario_id = :userId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <  :fin")
    LiveData<Double> getTotalIngresosMesRango(int userId, long inicio, long fin);

    /**Query para estadísticas**/

    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuario_id = :userId " +
            "AND CAST(strftime('%m', fecha, 'unixepoch') AS INTEGER) = :mes " +
            "AND CAST(strftime('%Y', fecha, 'unixepoch') AS INTEGER) = :anio " +
            "ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByMonthYear(int userId, int mes, int anio);

    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuario_id = :userId " +
            "AND CAST(strftime('%Y', fecha, 'unixepoch') AS INTEGER) = :anio " +
            "ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByYear(int userId, int anio);

    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuario_id = :userId " +
            "AND fecha >= :startTimestamp AND fecha <= :endTimestamp " +
            "ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByDateRange(int userId, long startTimestamp, long endTimestamp);

    /**
     * Ingresos de un usuario en un rango de fechas. Síncrono para exportación.
     *
     * @param usuarioId ID del usuario en sesión.
     * @param inicio    Timestamp Unix (ms) del inicio del período (inclusive).
     * @param fin       Timestamp Unix (ms) del fin del período (exclusive).
     * @return          Lista de IngresoPersonal ordenada por fecha descendente.
     */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha < :fin " +
            "ORDER BY fecha DESC")
    List<IngresoPersonal> getIngresosParaExportacion(int usuarioId, long inicio, long fin);

    /**
     * Todos los ingresos de un usuario. Síncrono para exportación anual o total.
     */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuario_id = :usuarioId " +
            "ORDER BY fecha DESC")
    List<IngresoPersonal> getTodosIngresosParaExportacion(int usuarioId);

    // ─── Card #50: Eliminar datos (PerfilFragment) ────────────────────────────

    /**
     * Elimina todos los ingresos personales de un usuario.
     * Llamar desde databaseWriteExecutor, nunca desde el hilo principal.
     *
     * @param usuarioId ID del usuario en sesión.
     */
    @Query("DELETE FROM ingresos_personales WHERE usuario_id = :usuarioId")
    void deleteAllByUsuario(int usuarioId);

}
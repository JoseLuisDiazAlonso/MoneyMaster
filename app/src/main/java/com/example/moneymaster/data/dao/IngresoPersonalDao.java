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


    // INSERT / UPDATE / DELETE


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(IngresoPersonal ingreso);

    @Update
    void actualizar(IngresoPersonal ingreso);

    @Delete
    void eliminar(IngresoPersonal ingreso);


    // SELECT — usados por IngresoRepository



    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC " +
            "LIMIT 200")
    LiveData<List<IngresoPersonal>> getIngresosByUsuario(long usuarioId);

    /**
     * Ingresos filtrados por mes y año (usa índice de fecha).
     */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByMes(long usuarioId, int mes, int anio);

    /**
     * Total de ingresos del mes para el balance del Dashboard.
     */
    @Query("SELECT SUM(monto) FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes)")
    LiveData<Double> getTotalIngresosMes(long usuarioId, int mes, int anio);

    /**
     * Últimos N ingresos para el widget del Dashboard.
     * MainViewModel lo llama con limite = 3.
     */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC " +
            "LIMIT :limite")
    LiveData<List<IngresoPersonal>> getUltimosIngresos(long usuarioId, int limite);

    /**
     * Ingreso por ID (para edición / detalle).
     */
    @Query("SELECT * FROM ingresos_personales WHERE id = :ingresoId LIMIT 1")
    LiveData<IngresoPersonal> getIngresoById(long ingresoId);


    // SELECT — Estadísticas (usados por IngresoRepository)


    /**
     * Suma de ingresos agrupada por categoría para el mes indicado.
     * Usa índice de categoria_id.
     */
    @Query("SELECT c.nombre AS nombreCategoria, " +
            "       c.color AS colorCategoria, " +
            "       c.icono AS iconoCategoria, " +
            "       SUM(i.monto) AS total " +
            "FROM ingresos_personales i " +
            "LEFT JOIN categorias_ingreso c ON i.categoria_id = c.id " +
            "WHERE i.usuarioId = :usuarioId " +
            "  AND strftime('%Y', i.fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', i.fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "GROUP BY i.categoria_id " +
            "ORDER BY total DESC")
    LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(long usuarioId, int mes, int anio);

    /**
     * Resumen mensual (total ingresos por mes) para los últimos N meses.
     * Usado por el LineChart en EstadisticasFragment.
     */
    @Query("SELECT CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) AS mes, " +
            "       CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) AS anio, " +
            "       SUM(monto) AS total " +
            "FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "GROUP BY strftime('%Y-%m', fecha / 1000, 'unixepoch') " +
            "ORDER BY anio DESC, mes DESC " +
            "LIMIT :meses")
    LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses);


    // SELECT — usados por StatisticsViewModel


    /** Todos los ingresos del año indicado (usa índice de fecha). */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByAnio(long usuarioId, int anio);

    /** Ingresos en un rango de fechas (timestamps ms, inclusive). */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "ORDER BY fecha DESC")
    LiveData<List<IngresoPersonal>> getIngresosByRango(long usuarioId, long inicio, long fin);


    // SELECT — Síncronas para Export (PDF / CSV)


    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC")
    List<IngresoPersonal> getAllSync(long usuarioId);

    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND strftime('%Y', fecha / 1000, 'unixepoch') = printf('%04d', :anio) " +
            "  AND strftime('%m', fecha / 1000, 'unixepoch') = printf('%02d', :mes) " +
            "ORDER BY fecha DESC")
    List<IngresoPersonal> getByMesSync(long usuarioId, int mes, int anio);

    @Query("SELECT * FROM ingresos_personales WHERE id = :id LIMIT 1")
    IngresoPersonal getByIdSync(long id);


    // SELECT — Síncronas para Export (rango de fechas y completo)


    /** Ingresos en un rango de timestamps ms — para ExportFragment CSV/PDF. */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "  AND fecha >= :inicio " +
            "  AND fecha <= :fin " +
            "ORDER BY fecha DESC")
    List<IngresoPersonal> getIngresosParaExportacion(int usuarioId, long inicio, long fin);

    /** Todos los ingresos del usuario sin filtro de fecha — para CSV completo. */
    @Query("SELECT * FROM ingresos_personales " +
            "WHERE usuarioId = :usuarioId " +
            "ORDER BY fecha DESC")
    List<IngresoPersonal> getTodosIngresosParaExportacion(int usuarioId);


    // SELECT — usados por DashboardViewModel (rango de timestamps)


    /**
     * Ingresos con datos de categoría filtrados por rango de timestamps ms.
     * Usado por DashboardViewModel para el selector de mes.
     */
    @Query("SELECT i.id AS id, " +
            "       i.descripcion AS descripcion, " +
            "       i.monto AS importe, " +
            "       i.fecha AS fecha, " +
            "       COALESCE(c.nombre, 'Sin categoría') AS nombreCategoria, " +
            "       COALESCE(c.icono,  'ic_category_default') AS iconoNombre, " +
            "       COALESCE(c.color,  '#9E9E9E') AS colorCategoria " +
            "FROM ingresos_personales i " +
            "LEFT JOIN categorias_ingreso c ON i.categoria_id = c.id " +
            "WHERE i.usuarioId = :usuarioId " +
            "  AND i.fecha >= :inicio " +
            "  AND i.fecha < :fin " +
            "ORDER BY i.fecha DESC")
    LiveData<List<IngresoConCategoria>> getIngresosPorCategoria(int usuarioId, long inicio, long fin);
}
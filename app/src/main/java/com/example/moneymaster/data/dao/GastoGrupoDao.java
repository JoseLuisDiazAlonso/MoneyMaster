package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.GastoGrupo;

import java.util.List;

@Dao
public interface GastoGrupoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(GastoGrupo gasto);

    @Update
    void actualizar(GastoGrupo gasto);

    @Delete
    void eliminar(GastoGrupo gasto);

    @Query("SELECT * FROM gastos_grupo WHERE grupoId = :grupoId ORDER BY fecha DESC")
    LiveData<List<GastoGrupo>> getGastosByGrupo(long grupoId);

    @Query("SELECT * FROM gastos_grupo WHERE grupoId = :grupoId ORDER BY fecha DESC")
    List<GastoGrupo> getGastosByGrupoSync(long grupoId);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_grupo WHERE grupoId = :grupoId")
    LiveData<Double> getTotalGastosGrupo(long grupoId);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_grupo WHERE grupoId = :grupoId")
    double getTotalGastosGrupoSync(long grupoId);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_grupo " +
            "WHERE grupoId = :grupoId AND pagadoPorId = :usuarioId")
    double getTotalPagadoPorUsuarioSync(long grupoId, long usuarioId);

    @Query("SELECT * FROM gastos_grupo WHERE id = :gastoId LIMIT 1")
    LiveData<GastoGrupo> getGastoById(long gastoId);

    @Query("SELECT * FROM gastos_grupo WHERE id = :gastoId LIMIT 1")
    GastoGrupo getById(int gastoId);

    // ─── Card #58: Búsqueda y Filtros ────────────────────────────────────────

    @Query("SELECT * FROM gastos_grupo " +
            "WHERE grupoId = :grupoId " +
            "  AND (:query IS NULL OR :query = '' " +
            "       OR LOWER(descripcion) LIKE '%' || LOWER(:query) || '%') " +
            "  AND (:categoriaId = -1 OR categoria_id = :categoriaId) " +
            "  AND (:montoMin = -1    OR monto >= :montoMin) " +
            "  AND (:montoMax = -1    OR monto <= :montoMax) " +
            "  AND (:fechaDesde = -1  OR fecha >= :fechaDesde) " +
            "  AND (:fechaHasta = -1  OR fecha <= :fechaHasta) " +
            "ORDER BY fecha DESC")
    LiveData<List<GastoGrupo>> buscarGastosGrupo(
            int grupoId,
            String query,
            int categoriaId,
            double montoMin,
            double montoMax,
            long fechaDesde,
            long fechaHasta);

    @Query("SELECT COUNT(*) FROM gastos_grupo " +
            "WHERE grupoId = :grupoId " +
            "  AND (:query IS NULL OR :query = '' " +
            "       OR LOWER(descripcion) LIKE '%' || LOWER(:query) || '%') " +
            "  AND (:categoriaId = -1 OR categoria_id = :categoriaId) " +
            "  AND (:montoMin = -1    OR monto >= :montoMin) " +
            "  AND (:montoMax = -1    OR monto <= :montoMax) " +
            "  AND (:fechaDesde = -1  OR fecha >= :fechaDesde) " +
            "  AND (:fechaHasta = -1  OR fecha <= :fechaHasta)")
    LiveData<Integer> contarResultadosGrupo(
            int grupoId,
            String query,
            int categoriaId,
            double montoMin,
            double montoMax,
            long fechaDesde,
            long fechaHasta);
}
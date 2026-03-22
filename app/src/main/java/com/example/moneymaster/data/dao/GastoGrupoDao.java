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

    @Query("SELECT * FROM gastos_grupo WHERE grupo_id = :grupoId ORDER BY fecha DESC")
    LiveData<List<GastoGrupo>> getGastosByGrupo(long grupoId);

    @Query("SELECT * FROM gastos_grupo WHERE grupo_id = :grupoId ORDER BY fecha DESC")
    List<GastoGrupo> getGastosByGrupoSync(long grupoId);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_grupo WHERE grupo_id = :grupoId")
    LiveData<Double> getTotalGastosGrupo(long grupoId);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_grupo WHERE grupo_id = :grupoId")
    double getTotalGastosGrupoSync(long grupoId);

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_grupo " +
            "WHERE grupo_id = :grupoId AND pagado_por_id = :usuarioId")
    double getTotalPagadoPorUsuarioSync(long grupoId, long usuarioId);

    @Query("SELECT * FROM gastos_grupo WHERE id = :gastoId LIMIT 1")
    LiveData<GastoGrupo> getGastoById(long gastoId);

    @Query("UPDATE gastos_grupo SET foto_recibo_id = NULL WHERE foto_recibo_id = :fotoId")
    void desvincularFoto(int fotoId);

    /**
     * Card #35 — Busca el gasto de grupo asociado a una foto.
     * Síncrono para llamar desde background thread en ImageViewerViewModel.
     */
    @Query("SELECT * FROM gastos_grupo WHERE foto_recibo_id = :fotoId LIMIT 1")
    GastoGrupo getByFotoReciboId(int fotoId);

    /**
     * Card #33/35 — Busca un gasto de grupo por su ID.
     * Síncrono para operaciones en background.
     */
    @Query("SELECT * FROM gastos_grupo WHERE id = :gastoId LIMIT 1")
    GastoGrupo getById(int gastoId);
}
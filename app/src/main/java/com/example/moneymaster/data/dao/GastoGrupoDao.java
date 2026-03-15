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

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos_grupo " +
            "WHERE grupo_id = :grupoId AND pagado_por_id = :usuarioId")
    double getTotalPagadoPorUsuarioSync(long grupoId, long usuarioId);

    @Query("SELECT * FROM gastos_grupo WHERE id = :gastoId LIMIT 1")
    LiveData<GastoGrupo> getGastoById(long gastoId);
}
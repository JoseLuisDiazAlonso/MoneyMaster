package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.moneymaster.data.model.BalanceGrupo;

import java.util.List;

@Dao
public interface BalanceGrupoDao {

    /**
     * Inserta o reemplaza el balance de un miembro en un grupo.
     * Funciona como UPSERT gracias a la clave primaria compuesta (grupoId, usuarioId)
     * definida en la entidad con @Entity(primaryKeys = {"grupoId", "usuarioId"}).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(BalanceGrupo balance);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertVarios(List<BalanceGrupo> balances);

    /** Balances de todos los miembros en un grupo. */
    @Query("SELECT * FROM balance_grupo WHERE grupo_id = :grupoId")
    LiveData<List<BalanceGrupo>> getBalancesByGrupo(long grupoId);

    /** Versión síncrona para el recalculo en hilo de fondo. */
    @Query("SELECT * FROM balance_grupo WHERE grupo_id = :grupoId")
    List<BalanceGrupo> getBalancesByGrupoSync(long grupoId);

    /** Balance individual de un usuario en un grupo. */
    @Query("SELECT * FROM balance_grupo WHERE grupo_id = :grupoId AND usuarioId = :usuarioId LIMIT 1")
    LiveData<BalanceGrupo> getBalanceUsuario(long grupoId, long usuarioId);

    /** Versión síncrona del balance individual. */
    @Query("SELECT * FROM balance_grupo WHERE grupo_id = :grupoId AND usuarioId = :usuarioId LIMIT 1")
    BalanceGrupo getBalanceUsuarioSync(long grupoId, long usuarioId);

    /** Elimina todos los balances de un grupo (usar al eliminar el grupo). */
    @Query("DELETE FROM balance_grupo WHERE grupo_id = :grupoId")
    void eliminarBalancesByGrupo(long grupoId);
}

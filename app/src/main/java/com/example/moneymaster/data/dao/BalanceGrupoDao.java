package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.BalanceGrupo;

import java.util.List;

@Dao
public interface BalanceGrupoDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsertBalance(BalanceGrupo balance);

    /** Inserta o reemplaza una lista completa de balances de una vez. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertVarios(List<BalanceGrupo> balances);

    @Update
    int updateBalance(BalanceGrupo balance);

    /**
     * Acumula un monto al balance existente entre deudor y acreedor.
     * También reactiva la deuda si estaba marcada como liquidada.
     */
    @Query("UPDATE balance_grupo " +
            "SET monto_pendiente = monto_pendiente + :monto, " +
            "    ultima_actualizacion = :timestamp, " +
            "    liquidado = 0 " +
            "WHERE grupo_id = :grupoId " +
            "  AND usuario_deudor_id = :deudorId " +
            "  AND usuario_acreedor_id = :acreedorId")
    int acumularBalance(long grupoId, long deudorId, long acreedorId,
                        double monto, long timestamp);

    /** Marca la deuda como saldada. La fila se conserva como historial. */
    @Query("UPDATE balance_grupo " +
            "SET monto_pendiente = 0, liquidado = 1, " +
            "    ultima_actualizacion = :timestamp " +
            "WHERE grupo_id = :grupoId " +
            "  AND usuario_deudor_id = :deudorId " +
            "  AND usuario_acreedor_id = :acreedorId")
    int liquidarDeuda(long grupoId, long deudorId, long acreedorId, long timestamp);

    /** Elimina todos los balances de un grupo (para recalcular desde cero). */
    @Query("DELETE FROM balance_grupo WHERE grupo_id = :grupoId")
    void eliminarBalancesByGrupo(long grupoId);

    /** Todos los balances de un grupo — LiveData para la UI. */
    @Query("SELECT * FROM balance_grupo WHERE grupo_id = :grupoId " +
            "ORDER BY monto_pendiente DESC")
    LiveData<List<BalanceGrupo>> getBalancesByGrupo(long grupoId);

    /** Solo deudas pendientes (monto > 0 y no liquidadas). */
    @Query("SELECT * FROM balance_grupo " +
            "WHERE grupo_id = :grupoId AND liquidado = 0 AND monto_pendiente > 0")
    LiveData<List<BalanceGrupo>> getBalancesPendientes(long grupoId);

    /** Lo que YO debo a otros miembros del grupo. */
    @Query("SELECT * FROM balance_grupo " +
            "WHERE grupo_id = :grupoId AND usuario_deudor_id = :usuarioId AND liquidado = 0")
    LiveData<List<BalanceGrupo>> getMisDeudas(long grupoId, long usuarioId);

    /** Lo que otros miembros me deben a MÍ en el grupo. */
    @Query("SELECT * FROM balance_grupo " +
            "WHERE grupo_id = :grupoId AND usuario_acreedor_id = :usuarioId AND liquidado = 0")
    LiveData<List<BalanceGrupo>> getLoqueMeDeben(long grupoId, long usuarioId);

    @Query("SELECT * FROM balance_grupo WHERE grupo_id = :grupoId ORDER BY monto_pendiente DESC")
    List<BalanceGrupo> getBalancesByGrupoSync(long grupoId);
}
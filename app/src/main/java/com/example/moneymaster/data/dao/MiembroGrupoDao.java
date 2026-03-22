package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.MiembroGrupo;

import java.util.List;

@Dao
public interface MiembroGrupoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertar(MiembroGrupo miembro);

    @Update
    void actualizar(MiembroGrupo miembro);

    @Delete
    void eliminar(MiembroGrupo miembro);

    /** Miembros activos de un grupo — LiveData para la UI. */
    @Query("SELECT * FROM miembros_grupo WHERE grupoId = :grupoId AND activo = 1")
    LiveData<List<MiembroGrupo>> getMiembrosByGrupo(long grupoId);

    /** Versión síncrona para cálculo de balances en background thread. */
    @Query("SELECT * FROM miembros_grupo WHERE grupoId = :grupoId AND activo = 1")
    List<MiembroGrupo> getMiembrosByGrupoSync(long grupoId);

    /** Comprueba si un usuario ya pertenece al grupo. */
    @Query("SELECT COUNT(*) FROM miembros_grupo " +
            "WHERE grupoId = :grupoId AND usuarioId= :usuarioId AND activo = 1")
    int esMiembro(long grupoId, long usuarioId);

    /** Rol del usuario en el grupo (admin / miembro). */
    @Query("SELECT rol FROM miembros_grupo " +
            "WHERE grupoId = :grupoId AND usuarioId = :usuarioId LIMIT 1")
    String getRolEnGrupo(long grupoId, long usuarioId);
}
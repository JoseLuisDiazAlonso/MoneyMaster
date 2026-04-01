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
    LiveData<List<MiembroGrupo>> getMiembrosByGrupo(int grupoId);

    /** Versión síncrona para background thread. */
    @Query("SELECT * FROM miembros_grupo WHERE grupoId = :grupoId AND activo = 1")
    List<MiembroGrupo> getMiembrosByGrupoSync(int grupoId);

    /** Número de miembros activos de un grupo. */
    @Query("SELECT COUNT(*) FROM miembros_grupo WHERE grupoId = :grupoId AND activo = 1")
    int contarMiembros(int grupoId);
}
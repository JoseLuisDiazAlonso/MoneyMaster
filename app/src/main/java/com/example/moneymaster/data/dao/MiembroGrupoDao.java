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

    /** Todos los miembros de un grupo, ordenados por fecha de unión. */
    @Query("SELECT * FROM miembro_grupo WHERE grupoId = :grupoId ORDER BY fechaUnion ASC")
    LiveData<List<MiembroGrupo>> getMiembrosByGrupo(long grupoId);

    /** Versión síncrona para cálculos de balance en hilo de fondo. */
    @Query("SELECT * FROM miembro_grupo WHERE grupoId = :grupoId ORDER BY fechaUnion ASC")
    List<MiembroGrupo> getMiembrosByGrupoSync(long grupoId);

    /** Número de miembros de un grupo (necesario para dividir gastos). */
    @Query("SELECT COUNT(*) FROM miembro_grupo WHERE grupoId = :grupoId")
    int countMiembros(long grupoId);

    /** Busca si un usuario ya es miembro del grupo. */
    @Query("SELECT * FROM miembro_grupo WHERE grupoId = :grupoId AND usuarioId = :usuarioId LIMIT 1")
    MiembroGrupo getMiembroByUsuario(long grupoId, long usuarioId);
}
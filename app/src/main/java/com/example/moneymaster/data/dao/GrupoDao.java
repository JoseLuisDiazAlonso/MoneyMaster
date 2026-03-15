package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.Grupo;

import java.util.List;

@Dao
public interface GrupoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(Grupo grupo);

    @Update
    void actualizar(Grupo grupo);

    @Delete
    void eliminar(Grupo grupo);

    /**
     * Grupos donde el usuario es creador O es miembro registrado.
     * JOIN con miembros_grupo para incluir grupos en los que fue invitado.
     */
    @Query("SELECT DISTINCT g.* FROM grupos g " +
            "LEFT JOIN miembros_grupo m ON g.id = m.grupo_id " +
            "WHERE g.creador_id = :usuarioId OR m.usuario_id = :usuarioId " +
            "ORDER BY g.fecha_creacion DESC")
    LiveData<List<Grupo>> getGruposByUsuario(long usuarioId);

    @Query("SELECT * FROM grupos WHERE id = :grupoId LIMIT 1")
    LiveData<Grupo> getGrupoById(long grupoId);

    @Query("SELECT COUNT(*) FROM grupos WHERE creador_id = :usuarioId")
    int countGruposCreados(long usuarioId);
}
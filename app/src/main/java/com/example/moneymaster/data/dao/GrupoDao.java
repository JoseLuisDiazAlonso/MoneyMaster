package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.GroupWithDetails;

import java.util.List;

@Dao
public interface GrupoDao {

    // ─── CRUD básico ────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGrupo(Grupo grupo);

    @Update
    void updateGrupo(Grupo grupo);

    @Delete
    void deleteGrupo(Grupo grupo);

    @Query("SELECT * FROM grupos WHERE id = :id")
    LiveData<Grupo> getGrupoById(int id);

    @Query("SELECT * FROM grupos ORDER BY fecha_creacion DESC")
    LiveData<List<Grupo>> getAllGrupos();

    // ─── Consulta enriquecida para el Fragment Lista de Grupos ─────────────

    /**
     * Devuelve todos los grupos con:
     *  - numMiembros : número de filas en miembros_grupo para ese grupo
     *  - balanceTotal: suma de los importes de gastos_grupo para ese grupo
     *                  (NULL si no hay gastos → COALESCE → 0.0)
     *
     * Se usa LEFT JOIN para incluir grupos sin miembros ni gastos.
     */
    @Query("SELECT " +
            "  g.id              AS id, " +
            "  g.nombre          AS nombre, " +
            "  g.descripcion     AS descripcion, " +
            "  g.fecha_creacion  AS fechaCreacion, " +
            "  COUNT(DISTINCT m.id)         AS numMiembros, " +
            "  COALESCE(SUM(gg.monto), 0)  AS balanceTotal " +
            "FROM grupos g " +
            "LEFT JOIN miembros_grupo m  ON m.grupoId = g.id " +
            "LEFT JOIN gastos_grupo gg   ON gg.grupo_id = g.id " +
            "GROUP BY g.id " +
            "ORDER BY g.fecha_creacion DESC")
    LiveData<List<GroupWithDetails>> getAllGroupsWithDetails();

    // ─── Card #50: Eliminar datos (PerfilFragment) ────────────────────────────

    /**
     * Elimina todos los grupos creados por un usuario.
     * Los gastos_grupo asociados se eliminan en cascada por la FK definida en la entidad.
     * Llamar desde databaseWriteExecutor, nunca desde el hilo principal.
     *
     * @param usuarioId ID del usuario en sesión.
     */
    @Query("DELETE FROM grupos WHERE creador_id = :usuarioId")
    void deleteAllByUsuario(int usuarioId);

}
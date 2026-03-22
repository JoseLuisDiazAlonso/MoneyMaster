package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.FotoRecibo;

import java.util.List;

@Dao
public interface FotoReciboDao {

    @Insert
    long insertar(FotoRecibo foto);

    @Update
    void actualizar(FotoRecibo foto);

    @Delete
    void eliminar(FotoRecibo foto);

    @Query("SELECT * FROM fotos_recibo WHERE id = :id")
    FotoRecibo getById(int id);

    @Query("SELECT * FROM fotos_recibo WHERE usuario_id = :usuarioId ORDER BY fecha_captura DESC")
    LiveData<List<FotoRecibo>> getByUsuario(int usuarioId);

    /**
     * Card #34 — Todas las fotos asociadas a gastos de un grupo específico.
     * JOIN con gastos_grupo para filtrar por grupo_id.
     * Ordenadas de más reciente a más antigua.
     */
    @Query("SELECT fr.* FROM fotos_recibo fr " +
            "INNER JOIN gastos_grupo gg ON gg.foto_recibo_id = fr.id " +
            "WHERE gg.grupo_id = :grupoId " +
            "ORDER BY fr.fecha_captura DESC")
    LiveData<List<FotoRecibo>> getFotosByGrupo(int grupoId);

    /**
     * Card #34 — Versión síncrona para operaciones en background.
     */
    @Query("SELECT fr.* FROM fotos_recibo fr " +
            "INNER JOIN gastos_grupo gg ON gg.foto_recibo_id = fr.id " +
            "WHERE gg.grupo_id = :grupoId " +
            "ORDER BY fr.fecha_captura DESC")
    List<FotoRecibo> getFotosByGrupoSync(int grupoId);

    @Query("SELECT * FROM fotos_recibo WHERE id NOT IN " +
            "(SELECT foto_recibo_id FROM gastos_personales WHERE foto_recibo_id IS NOT NULL)")
    List<FotoRecibo> getFotosHuerfanas();
}
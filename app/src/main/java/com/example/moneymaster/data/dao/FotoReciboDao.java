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

    @Query("SELECT fr.* FROM fotos_recibo fr " +
            "INNER JOIN gastos_grupo gg ON gg.foto_recibo_id = fr.id " +
            "WHERE gg.grupoId = :grupoId " +
            "ORDER BY fr.fecha_captura DESC")
    LiveData<List<FotoRecibo>> getFotosByGrupo(int grupoId);

    @Query("SELECT fr.* FROM fotos_recibo fr " +
            "INNER JOIN gastos_grupo gg ON gg.foto_recibo_id = fr.id " +
            "WHERE gg.grupoId = :grupoId " +
            "ORDER BY fr.fecha_captura DESC")
    List<FotoRecibo> getFotosByGrupoSync(int grupoId);

    /**
     * Fotos huérfanas: fotos_recibo que no están referenciadas ni en
     * gastos_personales (via fotoRuta) ni en gastos_grupo (via foto_recibo_id).
     * Se usan para limpieza de archivos en disco.
     */
    @Query("SELECT * FROM fotos_recibo " +
            "WHERE ruta_archivo NOT IN " +
            "    (SELECT fotoRuta FROM gastos_personales " +
            "     WHERE fotoRuta IS NOT NULL AND fotoRuta != '') " +
            "  AND id NOT IN " +
            "    (SELECT foto_recibo_id FROM gastos_grupo " +
            "     WHERE foto_recibo_id IS NOT NULL)")
    List<FotoRecibo> getFotosHuerfanas();
}
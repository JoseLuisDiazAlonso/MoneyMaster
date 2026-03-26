package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.CategoriaIngreso;

import java.util.List;

@Dao
public interface CategoriaIngresoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertar(CategoriaIngreso categoria);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertarVarias(List<CategoriaIngreso> categorias);

    @Update
    void actualizar(CategoriaIngreso categoria);

    @Delete
    void eliminar(CategoriaIngreso categoria);

    @Query("SELECT * FROM categorias_ingreso " +
            "WHERE activo = 1 AND (es_sistema = 1 OR usuario_id = :usuarioId) " +
            "ORDER BY es_sistema DESC, nombre ASC")
    LiveData<List<CategoriaIngreso>> getCategorias(long usuarioId);

    @Query("SELECT * FROM categorias_ingreso " +
            "WHERE activo = 1 AND (es_sistema = 1 OR usuario_id = :usuarioId) " +
            "ORDER BY es_sistema DESC, nombre ASC")
    List<CategoriaIngreso> getCategoriasSync(long usuarioId);

    @Query("SELECT * FROM categorias_ingreso WHERE es_sistema = 1 AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<CategoriaIngreso>> getCategoriasDelSistema();

    @Query("SELECT * FROM categorias_ingreso WHERE usuario_id = :usuarioId AND es_sistema = 0 AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<CategoriaIngreso>> getCategoriasByUsuario(long usuarioId);

    @Query("SELECT * FROM categorias_ingreso WHERE id = :id LIMIT 1")
    LiveData<CategoriaIngreso> getCategoriaById(long id);

    @Query("SELECT COUNT(*) FROM categorias_ingreso WHERE es_sistema = 1")
    int countCategoriasDelSistema();

    /**
     * Devuelve todas las categorías de ingreso activas para el usuario.
     * Síncrono para construir el mapa ID→nombre antes de exportar.
     */
    @Query("SELECT * FROM categorias_ingreso " +
            "WHERE activo = 1 " +
            "  AND (usuario_id IS NULL OR usuario_id = :usuarioId)")
    List<CategoriaIngreso> getCategoriasParaExportacion(int usuarioId);
}
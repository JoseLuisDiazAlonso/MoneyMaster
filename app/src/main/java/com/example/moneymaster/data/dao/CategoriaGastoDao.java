package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.CategoriaGasto;

import java.util.List;

@Dao
public interface CategoriaGastoDao {

    // Inserción / actualización / borrado

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertar(CategoriaGasto categoria);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertarVarias(List<CategoriaGasto> categorias);

    @Update
    void actualizar(CategoriaGasto categoria);

    @Delete
    void eliminar(CategoriaGasto categoria);

    // Consultas


    @Query("SELECT * FROM categorias_gasto " +
            "WHERE activo = 1 AND (es_sistema = 1 OR usuario_id = :usuarioId) " +
            "ORDER BY es_sistema DESC, nombre ASC")
    LiveData<List<CategoriaGasto>> getCategorias(long usuarioId);


    @Query("SELECT * FROM categorias_gasto " +
            "WHERE activo = 1 AND (es_sistema = 1 OR usuario_id = :usuarioId) " +
            "ORDER BY es_sistema DESC, nombre ASC")
    List<CategoriaGasto> getCategoriasSync(long usuarioId);


    @Query("SELECT * FROM categorias_gasto WHERE es_sistema = 1 AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<CategoriaGasto>> getCategoriasDelSistema();


    @Query("SELECT * FROM categorias_gasto WHERE usuario_id = :usuarioId AND es_sistema = 0 AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<CategoriaGasto>> getCategoriasByUsuario(long usuarioId);


    @Query("SELECT * FROM categorias_gasto WHERE id = :id LIMIT 1")
    LiveData<CategoriaGasto> getCategoriaById(long id);


    @Query("SELECT COUNT(*) FROM categorias_gasto WHERE es_sistema = 1")
    int countCategoriasDelSistema();

    @Query("SELECT * FROM categorias_gasto WHERE id = :id LIMIT 1")
    CategoriaGasto getByIdSync(int id);


    @Query("SELECT * FROM categorias_gasto " +
            "WHERE activo = 1 " +
            "  AND (usuario_id IS NULL OR usuario_id = :usuarioId)")
    List<CategoriaGasto> getCategoriasParaExportacion(int usuarioId);
}
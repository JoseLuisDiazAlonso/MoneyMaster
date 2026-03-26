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

    // ─── Inserción / actualización / borrado ──────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertar(CategoriaGasto categoria);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertarVarias(List<CategoriaGasto> categorias);

    @Update
    void actualizar(CategoriaGasto categoria);

    @Delete
    void eliminar(CategoriaGasto categoria);

    // ─── Consultas ────────────────────────────────────────────────────────────

    /**
     * Todas las categorías activas: sistema + las del usuario.
     * Ordenadas: primero sistema, luego personalizadas, ambas por nombre.
     */
    @Query("SELECT * FROM categorias_gasto " +
            "WHERE activo = 1 AND (es_sistema = 1 OR usuario_id = :usuarioId) " +
            "ORDER BY es_sistema DESC, nombre ASC")
    LiveData<List<CategoriaGasto>> getCategorias(long usuarioId);

    /**
     * Versión sin LiveData para seed y operaciones internas.
     */
    @Query("SELECT * FROM categorias_gasto " +
            "WHERE activo = 1 AND (es_sistema = 1 OR usuario_id = :usuarioId) " +
            "ORDER BY es_sistema DESC, nombre ASC")
    List<CategoriaGasto> getCategoriasSync(long usuarioId);

    /** Sólo las categorías predefinidas del sistema. */
    @Query("SELECT * FROM categorias_gasto WHERE es_sistema = 1 AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<CategoriaGasto>> getCategoriasDelSistema();

    /** Sólo las categorías creadas por el usuario. */
    @Query("SELECT * FROM categorias_gasto WHERE usuario_id = :usuarioId AND es_sistema = 0 AND activo = 1 ORDER BY nombre ASC")
    LiveData<List<CategoriaGasto>> getCategoriasByUsuario(long usuarioId);

    /** Una categoría por su ID. */
    @Query("SELECT * FROM categorias_gasto WHERE id = :id LIMIT 1")
    LiveData<CategoriaGasto> getCategoriaById(long id);

    /** Comprueba si ya existe una categoría de sistema (para seed idempotente). */
    @Query("SELECT COUNT(*) FROM categorias_gasto WHERE es_sistema = 1")
    int countCategoriasDelSistema();

    @Query("SELECT * FROM categorias_gasto WHERE id = :id LIMIT 1")
    CategoriaGasto getByIdSync(int id);

    /**
     * Devuelve todas las categorías de gasto activas para el usuario,
     * incluyendo las del sistema. Síncrono para construir el mapa ID→nombre
     * antes de exportar.
     */
    @Query("SELECT * FROM categorias_gasto " +
            "WHERE activo = 1 " +
            "  AND (usuario_id IS NULL OR usuario_id = :usuarioId)")
    List<CategoriaGasto> getCategoriasParaExportacion(int usuarioId);
}
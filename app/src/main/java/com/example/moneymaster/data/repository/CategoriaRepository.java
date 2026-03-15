package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.CategoriaGastoDao;
import com.example.moneymaster.data.dao.CategoriaIngresoDao;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;

import java.util.List;

/**
 * Repositorio de categorías (gastos e ingresos en un solo lugar).
 *
 * Unifica ambos DAOs porque desde la UI casi siempre se necesitan
 * las dos listas juntas (al crear un gasto o un ingreso).
 *
 * Las categorías del sistema ya existen desde el seed de AppDatabase.
 * Este repositorio gestiona principalmente las categorías personalizadas.
 */
public class CategoriaRepository {

    private final CategoriaGastoDao categoriaGastoDao;
    private final CategoriaIngresoDao categoriaIngresoDao;

    public CategoriaRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        categoriaGastoDao   = db.categoriaGastoDao();
        categoriaIngresoDao = db.categoriaIngresoDao();
    }

    // ---- CATEGORÍAS DE GASTO ----

    public void insertCategoriaGasto(CategoriaGasto categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaGastoDao.insertCategoria(categoria));
    }

    public void updateCategoriaGasto(CategoriaGasto categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaGastoDao.updateCategoria(categoria));
    }

    public void desactivarCategoriaGasto(int id) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaGastoDao.desactivarCategoria(id));
    }

    /** LiveData con categorías del sistema + personalizadas del usuario. */
    public LiveData<List<CategoriaGasto>> getCategoriasGastoParaUsuario(int usuarioId) {
        return categoriaGastoDao.getCategoriasParaUsuario(usuarioId);
    }

    public CategoriaGasto getCategoriaGastoById(int id) {
        return categoriaGastoDao.getById(id);
    }

    // ---- CATEGORÍAS DE INGRESO ----

    public void insertCategoriaIngreso(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaIngresoDao.insertCategoria(categoria));
    }

    public void updateCategoriaIngreso(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaIngresoDao.updateCategoria(categoria));
    }

    public void desactivarCategoriaIngreso(int id) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaIngresoDao.desactivarCategoria(id));
    }

    /** LiveData con categorías del sistema + personalizadas del usuario. */
    public LiveData<List<CategoriaIngreso>> getCategoriasIngresoParaUsuario(int usuarioId) {
        return categoriaIngresoDao.getCategoriasParaUsuario(usuarioId);
    }

    public CategoriaIngreso getCategoriaIngresoById(int id) {
        return categoriaIngresoDao.getById(id);
    }
}

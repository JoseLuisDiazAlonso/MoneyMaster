package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.CategoriaGastoDao;
import com.example.moneymaster.data.dao.CategoriaIngresoDao;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;

import java.util.List;

public class CategoriaRepository {

    private final CategoriaGastoDao   categoriaGastoDao;
    private final CategoriaIngresoDao categoriaIngresoDao;

    public CategoriaRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        categoriaGastoDao   = db.categoriaGastoDao();
        categoriaIngresoDao = db.categoriaIngresoDao();
    }

    //GASTOS

    public void insertarCategoriaGasto(CategoriaGasto categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaGastoDao.insertar(categoria));
    }

    public void actualizarCategoriaGasto(CategoriaGasto categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaGastoDao.actualizar(categoria));
    }

    public void eliminarCategoriaGasto(CategoriaGasto categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaGastoDao.eliminar(categoria));
    }

    /** Sistema + personalizadas del usuario, activas. */
    public LiveData<List<CategoriaGasto>> getCategoriasGasto(long usuarioId) {
        return categoriaGastoDao.getCategorias(usuarioId);
    }

    public LiveData<List<CategoriaGasto>> getCategoriasByUsuarioGasto(long usuarioId) {
        return categoriaGastoDao.getCategoriasByUsuario(usuarioId);
    }

    public LiveData<CategoriaGasto> getCategoriaGastoById(long id) {
        return categoriaGastoDao.getCategoriaById(id);
    }

    //INGRESOS

    public void insertarCategoriaIngreso(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaIngresoDao.insertar(categoria));
    }

    public void actualizarCategoriaIngreso(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaIngresoDao.actualizar(categoria));
    }

    public void eliminarCategoriaIngreso(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                categoriaIngresoDao.eliminar(categoria));
    }

    /** Sistema + personalizadas del usuario, activas. */
    public LiveData<List<CategoriaIngreso>> getCategoriasIngreso(long usuarioId) {
        return categoriaIngresoDao.getCategorias(usuarioId);
    }

    public LiveData<List<CategoriaIngreso>> getCategoriasByUsuarioIngreso(long usuarioId) {
        return categoriaIngresoDao.getCategoriasByUsuario(usuarioId);
    }

    public LiveData<CategoriaIngreso> getCategoriaIngresoById(long id) {
        return categoriaIngresoDao.getCategoriaById(id);
    }
}
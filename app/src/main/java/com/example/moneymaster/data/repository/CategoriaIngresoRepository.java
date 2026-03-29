package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.CategoriaIngresoDao;
import com.example.moneymaster.data.model.CategoriaIngreso;

import java.util.List;

public class CategoriaIngresoRepository {

    private final CategoriaIngresoDao dao;

    public CategoriaIngresoRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.categoriaIngresoDao();
    }

    //CONSULTAS

    /** Categorías del sistema + propias del usuario, activas, ordenadas por nombre. */
    public LiveData<List<CategoriaIngreso>> getCategorias(long usuarioId) {
        return dao.getCategorias(usuarioId);
    }

    public List<CategoriaIngreso> getCategoriasSync(long usuarioId) {
        return dao.getCategoriasSync(usuarioId);
    }

    public LiveData<List<CategoriaIngreso>> getCategoriasDelSistema() {
        return dao.getCategoriasDelSistema();
    }

    public LiveData<List<CategoriaIngreso>> getCategoriasByUsuario(long usuarioId) {
        return dao.getCategoriasByUsuario(usuarioId);
    }

    //ESCRITURA

    public void insertar(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() -> dao.insertar(categoria));
    }

    public void insertarVarias(List<CategoriaIngreso> categorias) {
        AppDatabase.databaseWriteExecutor.execute(() -> dao.insertarVarias(categorias));
    }

    public void actualizar(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() -> dao.actualizar(categoria));
    }

    public void eliminar(CategoriaIngreso categoria) {
        AppDatabase.databaseWriteExecutor.execute(() -> dao.eliminar(categoria));
    }
}

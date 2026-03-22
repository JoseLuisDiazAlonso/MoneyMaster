package com.example.moneymaster.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.repository.CategoriaRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoriaRepository repository;

    private final LiveData<List<CategoriaGasto>> categoriasGasto;
    private final LiveData<List<CategoriaIngreso>> categoriasIngreso;

    // TODO: sustituir 1L por el ID real del usuario de la sesión activa
    private static final long USUARIO_ID = 1L;

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoriaRepository(application);
        categoriasGasto  = repository.getCategoriasGasto(USUARIO_ID);
        categoriasIngreso = repository.getCategoriasIngreso(USUARIO_ID);
    }

    public LiveData<List<CategoriaGasto>> getCategoriasGasto() {
        return categoriasGasto;
    }

    public LiveData<List<CategoriaIngreso>> getCategoriasIngreso() {
        return categoriasIngreso;
    }

    public void insertCategoriaGasto(CategoriaGasto categoria) {
        repository.insertarCategoriaGasto(categoria);
    }

    public void deleteCategoriaGasto(CategoriaGasto categoria) {
        repository.eliminarCategoriaGasto(categoria);
    }

    public void insertCategoriaIngreso(CategoriaIngreso categoria) {
        repository.insertarCategoriaIngreso(categoria);
    }

    public void deleteCategoriaIngreso(CategoriaIngreso categoria) {
        repository.eliminarCategoriaIngreso(categoria);
    }
}
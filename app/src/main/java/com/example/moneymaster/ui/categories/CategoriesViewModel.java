package com.example.moneymaster.ui.categories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.repository.CategoriaRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {

    private static final String TAG = "SEED";

    private final CategoriaRepository repository;

    private final LiveData<List<CategoriaGasto>> categoriasGasto;
    private final LiveData<List<CategoriaIngreso>> categoriasIngreso;

    private static final long USUARIO_ID = 1L;

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoriaRepository(application);

        // LOG para depuración
        Log.d(TAG, "CategoriesViewModel creado con USUARIO_ID=" + USUARIO_ID);

        categoriasGasto   = repository.getCategoriasGasto(USUARIO_ID);
        categoriasIngreso = repository.getCategoriasIngreso(USUARIO_ID);

        // Observar el primer valor que llegue para loguearlo
        categoriasGasto.observeForever(lista -> {
            if (lista != null) {
                Log.d(TAG, "Categorías gasto recibidas: " + lista.size());
                for (CategoriaGasto c : lista) {
                    Log.d(TAG, "  gasto nombre='" + c.nombre + "' es_sistema=" + c.esSistema);
                }
            }
        });
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
package com.example.moneymaster.ui.income;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.repository.CategoriaIngresoRepository;
import com.example.moneymaster.data.repository.IngresoPersonalRepository;

import java.util.List;

public class AddIncomeViewModel extends AndroidViewModel {

    private final CategoriaIngresoRepository  categoriaRepo;
    private final IngresoPersonalRepository   ingresoRepo;
    private final LiveData<List<CategoriaIngreso>> categorias;

    public AddIncomeViewModel(@NonNull Application application, long usuarioId) {
        super(application);
        categoriaRepo = new CategoriaIngresoRepository(application);
        ingresoRepo   = new IngresoPersonalRepository(application);
        // Usa getCategorias(usuarioId) que devuelve sistema + propias del usuario
        categorias    = categoriaRepo.getCategorias(usuarioId);
    }

    public LiveData<List<CategoriaIngreso>> getCategorias() {
        return categorias;
    }

    public void insertIngreso(@NonNull IngresoPersonal ingreso,
                              @NonNull IngresoPersonalRepository.SaveCallback<Void> callback) {
        ingresoRepo.insert(ingreso, callback);
    }
}

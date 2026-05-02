package com.example.moneymaster.ui.income;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.repository.IngresoRepository;

import java.util.List;


public class AddIncomeViewModel extends AndroidViewModel {

    public interface SaveCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private final IngresoRepository ingresoRepo;
    private final LiveData<List<CategoriaIngreso>> categorias;

    public AddIncomeViewModel(@NonNull Application application, long usuarioId) {
        super(application);
        ingresoRepo = new IngresoRepository(application);
        categorias  = ingresoRepo.getCategorias(usuarioId);
    }

    public LiveData<List<CategoriaIngreso>> getCategorias() {
        return categorias;
    }

    public void insertIngreso(@NonNull IngresoPersonal ingreso, @NonNull SaveCallback callback) {
        try {
            ingresoRepo.insertar(ingreso);
            callback.onSuccess();
        } catch (Exception e) {
            callback.onError(e);
        }
    }
}
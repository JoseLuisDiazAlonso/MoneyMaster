package com.example.moneymaster.ui.income;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;


public class AddIncomeViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final long        usuarioId;

    public AddIncomeViewModelFactory(Application application, long usuarioId) {
        this.application = application;
        this.usuarioId   = usuarioId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AddIncomeViewModel.class)) {
            //noinspection unchecked
            return (T) new AddIncomeViewModel(application, usuarioId);
        }
        throw new IllegalArgumentException("ViewModel desconocido: " + modelClass.getName());
    }
}

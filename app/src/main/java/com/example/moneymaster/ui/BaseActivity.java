package com.example.moneymaster.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.example.moneymaster.utils.DisposableManager;


public abstract class BaseActivity extends AppCompatActivity {

    /** Gestor centralizado de limpieza de listeners. */
    protected final DisposableManager disposables = new DisposableManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        // 1. Limpiar listeners registrados
        disposables.disposeAll();

        // 2. Cancelar peticiones Glide asociadas a esta Activity
        try {
            Glide.with(this).pauseRequests();
        } catch (Exception ignored) {}

        super.onDestroy();
    }
}


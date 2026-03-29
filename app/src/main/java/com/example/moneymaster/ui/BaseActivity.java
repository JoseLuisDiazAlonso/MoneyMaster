package com.example.moneymaster.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.example.moneymaster.utils.DisposableManager;

/**
 * Card #64 – Manejo de memoria y leaks
 *
 * Activity base que centraliza:
 *  1. Limpieza de listeners mediante {@link DisposableManager}.
 *  2. Cancelación de peticiones Glide al destruirse.
 *
 * Uso:
 * <pre>
 *   public class MiActivity extends BaseActivity {
 *
 *       {@literal @}Override
 *       protected void onCreate(Bundle savedInstanceState) {
 *           super.onCreate(savedInstanceState);
 *           binding = ActivityMiBinding.inflate(getLayoutInflater());
 *           setContentView(binding.getRoot());
 *
 *           // Registrar cualquier listener con limpieza:
 *           SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
 *           sm.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
 *           disposables.add(() -> sm.unregisterListener(sensorListener));
 *       }
 *   }
 * </pre>
 *
 * IMPORTANTE: Las Activities existentes NO necesitan extender BaseActivity si
 * ya gestionan su propio ciclo de vida correctamente. Úsala para Activities
 * nuevas o aquellas que presenten leaks detectados por LeakCanary.
 */
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


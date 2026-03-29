package com.example.moneymaster.utils;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Card #64 – Manejo de memoria y leaks
 *
 * Helper para gestionar el ciclo de vida del ExecutorService compartido
 * en los ViewModels.
 *
 * PROBLEMA DETECTADO:
 * Los ViewModels usan AppDatabase.databaseWriteExecutor para operaciones
 * en segundo plano. Si un ViewModel crea su propio ExecutorService sin
 * cerrarlo en onCleared(), los hilos permanecen vivos reteniendo referencias
 * al ViewModel (y transitivamente al Application context).
 *
 * SOLUCIÓN:
 * Usar ExecutorLifecycleHelper.shutdown() desde onCleared() de cualquier
 * ViewModel que gestione su propio executor.
 *
 * PATRÓN RECOMENDADO EN VIEWMODEL:
 * <pre>
 *   public class MiViewModel extends AndroidViewModel {
 *
 *       // Usar siempre el executor compartido de la base de datos:
 *       private final ExecutorService executor =
 *           AppDatabase.databaseWriteExecutor;
 *
 *       // Si necesitas un executor propio (casos excepcionales):
 *       private final ExecutorService localExecutor =
 *           Executors.newSingleThreadExecutor();
 *
 *       {@literal @}Override
 *       protected void onCleared() {
 *           super.onCleared();
 *           // NO cerrar AppDatabase.databaseWriteExecutor (compartido)
 *           // SÍ cerrar executors locales:
 *           ExecutorLifecycleHelper.shutdown(localExecutor, "MiViewModel");
 *       }
 *   }
 * </pre>
 */
public class ExecutorLifecycleHelper {

    private static final String TAG = "ExecutorLifecycle";
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 2;

    /**
     * Cierra ordenadamente un ExecutorService esperando hasta
     * {@value #SHUTDOWN_TIMEOUT_SECONDS} segundos a que terminen
     * las tareas en curso.
     *
     * @param executor  El executor a cerrar (puede ser null).
     * @param ownerTag  Nombre del propietario para logs de diagnóstico.
     */
    public static void shutdown(ExecutorService executor, String ownerTag) {
        if (executor == null || executor.isShutdown()) return;

        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                Log.w(TAG, ownerTag + ": executor forzado a parar (tareas en curso).");
            } else {
                Log.d(TAG, ownerTag + ": executor cerrado correctamente.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
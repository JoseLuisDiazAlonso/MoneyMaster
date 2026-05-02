package com.example.moneymaster.utils;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


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
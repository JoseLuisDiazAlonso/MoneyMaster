package com.example.moneymaster;

import android.app.Application;

import com.example.moneymaster.utils.AppErrorHandler;
import com.example.moneymaster.utils.AppLogger;

/**
 * Card #65 – Control de errores global
 *
 * Clase Application de MoneyMaster.
 * Punto de inicialización único para el sistema de control de errores.
 *
 * Si ya tenías una clase Application en el proyecto, añade únicamente
 * las dos líneas marcadas con ← AÑADIR en tu onCreate() existente.
 *
 * Asegúrate de que en AndroidManifest.xml figure:
 *   android:name=".MoneyMasterApplication"
 */
public class MoneyMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ← AÑADIR: Logging a fichero para WARN y ERROR (opcional)
        AppLogger.enableFileLogging(this);

        // ← AÑADIR: Crash handler global (captura excepciones no controladas)
        AppErrorHandler.install(this);

        AppLogger.i("Application", "MoneyMaster iniciado.");
    }
}

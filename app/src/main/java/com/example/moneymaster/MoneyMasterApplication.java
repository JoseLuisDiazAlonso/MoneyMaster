package com.example.moneymaster;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.moneymaster.utils.AppErrorHandler;
import com.example.moneymaster.utils.AppLogger;


public class MoneyMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Aplicar tema oscuro/claro según preferencia guardada
        SharedPreferences prefs = getSharedPreferences("MoneyMasterPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Logging a fichero para WARN y ERROR
        AppLogger.enableFileLogging(this);

        // Crash handler global (captura excepciones no controladas)
        AppErrorHandler.install(this);

        AppLogger.i("Application", "MoneyMaster iniciado.");
    }
}
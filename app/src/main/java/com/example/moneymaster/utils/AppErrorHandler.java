package com.example.moneymaster.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Card #65 – Control de errores global
 *
 * Crash handler personalizado que:
 *  1. Captura cualquier excepción no controlada (UncaughtExceptionHandler).
 *  2. Escribe el stack trace en un fichero de log rotativo en filesDir/logs/.
 *  3. Delega al handler por defecto de Android para que el sistema pueda
 *     mostrar el diálogo de "App cerrada inesperadamente".
 *
 * Instalación — llamar UNA sola vez desde MoneyMasterApplication.onCreate():
 * <pre>
 *   AppErrorHandler.install(this);
 * </pre>
 */
public class AppErrorHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG          = "AppErrorHandler";
    private static final String LOGS_DIR     = "logs";
    private static final String LOG_FILENAME = "crash_log.txt";
    /** Tamaño máximo del fichero de log antes de rotarlo (200 KB). */
    private static final long   MAX_LOG_SIZE = 200 * 1024L;

    private final Context                        appContext;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    // ─────────────────────────────────────────────────────────────────────────
    // Instalación
    // ─────────────────────────────────────────────────────────────────────────

    private AppErrorHandler(Context context) {
        this.appContext     = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * Instala el handler global. Llamar desde Application.onCreate().
     */
    public static void install(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new AppErrorHandler(context));
        Log.i(TAG, "AppErrorHandler instalado.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UncaughtExceptionHandler
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            writeCrashToFile(thread, throwable);
        } catch (Exception e) {
            Log.e(TAG, "Error al escribir crash log", e);
        } finally {
            // Delegar siempre al handler por defecto
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Escritura de log
    // ─────────────────────────────────────────────────────────────────────────

    private void writeCrashToFile(Thread thread, Throwable throwable) {
        File logsDir = new File(appContext.getFilesDir(), LOGS_DIR);
        if (!logsDir.exists()) logsDir.mkdirs();

        File logFile = new File(logsDir, LOG_FILENAME);
        rotateSiNecesario(logFile, logsDir);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("════════════════════════════════════════");
            pw.println("CRASH: " + timestamp);
            pw.println("Thread: " + thread.getName());
            pw.println("────────────────────────────────────────");
            throwable.printStackTrace(pw);
            pw.println();

        } catch (IOException e) {
            Log.e(TAG, "No se pudo escribir crash log", e);
        }

        Log.e(TAG, "CRASH capturado [" + timestamp + "]", throwable);
    }

    /**
     * Si el log supera MAX_LOG_SIZE, lo renombra a crash_log_old.txt
     * y empieza uno nuevo.
     */
    private void rotateSiNecesario(File logFile, File logsDir) {
        if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
            File oldLog = new File(logsDir, "crash_log_old.txt");
            if (oldLog.exists()) oldLog.delete();
            logFile.renameTo(oldLog);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública para acceder al log
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Devuelve el fichero de crash log actual, o null si no existe.
     * Útil para la pantalla "Acerca de" / soporte técnico.
     */
    public static File getCrashLogFile(Context context) {
        File f = new File(context.getFilesDir(), LOGS_DIR + File.separator + LOG_FILENAME);
        return f.exists() ? f : null;
    }

    /**
     * Elimina ambos ficheros de log (log actual + rotado).
     */
    public static void clearLogs(Context context) {
        File logsDir = new File(context.getFilesDir(), LOGS_DIR);
        File log    = new File(logsDir, LOG_FILENAME);
        File logOld = new File(logsDir, "crash_log_old.txt");
        if (log.exists())    log.delete();
        if (logOld.exists()) logOld.delete();
        Log.i(TAG, "Logs eliminados.");
    }
}
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


public class AppLogger {

    private static final String APP_TAG      = "MoneyMaster";
    private static final String LOGS_DIR     = "logs";
    private static final String APP_LOG_FILE = "app_log.txt";
    private static final long   MAX_SIZE     = 200 * 1024L;

    private static boolean fileLoggingEnabled = false;
    private static Context appContext          = null;


    // Configuración


    /**
     * Activa la escritura de logs WARN y ERROR a fichero.
     * Llamar desde Application.onCreate() si se desea logging persistente.
     */
    public static void enableFileLogging(Context context) {
        appContext         = context.getApplicationContext();
        fileLoggingEnabled = true;
    }


    // Métodos de log


    /** Solo si el nivel VERBOSE está habilitado para el tag. */
    public static void v(String tag, String msg) {
        if (Log.isLoggable(fullTag(tag), Log.VERBOSE)) {
            Log.v(fullTag(tag), msg);
        }
    }

    /** Solo si el nivel DEBUG está habilitado para el tag. */
    public static void d(String tag, String msg) {
        if (Log.isLoggable(fullTag(tag), Log.DEBUG)) {
            Log.d(fullTag(tag), msg);
        }
    }

    /** Siempre visible. */
    public static void i(String tag, String msg) {
        Log.i(fullTag(tag), msg);
    }

    /** Siempre visible. Escribe a fichero si está habilitado. */
    public static void w(String tag, String msg) {
        Log.w(fullTag(tag), msg);
        writeToFile("W", tag, msg, null);
    }

    /** Siempre visible. Escribe a fichero si está habilitado. */
    public static void w(String tag, String msg, Throwable t) {
        Log.w(fullTag(tag), msg, t);
        writeToFile("W", tag, msg, t);
    }

    /** Siempre visible. Escribe a fichero si está habilitado. */
    public static void e(String tag, String msg) {
        Log.e(fullTag(tag), msg);
        writeToFile("E", tag, msg, null);
    }

    /** Siempre visible. Escribe a fichero si está habilitado. */
    public static void e(String tag, String msg, Throwable t) {
        Log.e(fullTag(tag), msg, t);
        writeToFile("E", tag, msg, t);
    }


    // Escritura a fichero

    private static void writeToFile(String level, String tag, String msg, Throwable t) {
        if (!fileLoggingEnabled || appContext == null) return;

        File logsDir = new File(appContext.getFilesDir(), LOGS_DIR);
        if (!logsDir.exists()) logsDir.mkdirs();

        File logFile = new File(logsDir, APP_LOG_FILE);
        rotateSiNecesario(logFile, logsDir);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println(timestamp + " [" + level + "] " + tag + ": " + msg);
            if (t != null) {
                t.printStackTrace(pw);
            }

        } catch (IOException e) {
            Log.e(APP_TAG, "AppLogger: error escribiendo a fichero", e);
        }
    }

    private static void rotateSiNecesario(File logFile, File logsDir) {
        if (logFile.exists() && logFile.length() > MAX_SIZE) {
            File old = new File(logsDir, "app_log_old.txt");
            if (old.exists()) old.delete();
            logFile.renameTo(old);
        }
    }

    private static String fullTag(String tag) {
        return APP_TAG + "/" + tag;
    }


    // Utilidades


    /**
     * Devuelve el fichero de log de aplicación, o null si no existe.
     */
    public static File getLogFile(Context context) {
        File f = new File(context.getFilesDir(), LOGS_DIR + File.separator + APP_LOG_FILE);
        return f.exists() ? f : null;
    }

    /**
     * Elimina los ficheros de log de aplicación.
     */
    public static void clearLogs(Context context) {
        File logsDir = new File(context.getFilesDir(), LOGS_DIR);
        for (String name : new String[]{APP_LOG_FILE, "app_log_old.txt"}) {
            File f = new File(logsDir, name);
            if (f.exists()) f.delete();
        }
    }
}

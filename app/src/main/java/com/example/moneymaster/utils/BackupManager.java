package com.example.moneymaster.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.example.moneymaster.data.database.AppDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class BackupManager {

    private static final String DB_NAME        = "moneymaster_db";
    private static final String BACKUP_PREFIX  = "MoneyMaster_backup_";
    private static final String BACKUP_EXT     = ".db";

    //Backup

    /**
     * Crea un backup de la base de datos en la carpeta Downloads del dispositivo.
     *
     * @param context Contexto de la aplicación.
     * @return Ruta absoluta del archivo creado, o null si hubo error.
     */
    public static String createBackup(Context context) {
        // 1. Cerrar WAL y asegurar que Room ha volcado todos los datos
        AppDatabase.getDatabase(context).close();

        File dbFile = context.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) return null;

        // 2. Construir nombre con timestamp
        String timestamp  = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String backupName = BACKUP_PREFIX + timestamp + BACKUP_EXT;

        // 3. Destino: carpeta Downloads pública
        File downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) downloadsDir.mkdirs();

        File backupFile = new File(downloadsDir, backupName);

        // 4. Copiar
        try {
            copyFile(dbFile, backupFile);
            return backupFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Restore

    /**
     * Restaura la base de datos desde un Uri elegido por el usuario.
     * Sobrescribe el archivo .db actual con el contenido del Uri.
     *
     * Después de llamar a este método es OBLIGATORIO reiniciar la app.
     *
     * @param context Contexto de la aplicación.
     * @param sourceUri Uri del archivo .db seleccionado (desde ActivityResult / SAF).
     * @return true si la restauración fue exitosa, false si hubo error.
     */
    public static boolean restoreBackup(Context context, Uri sourceUri) {
        // 1. Cerrar la instancia de Room antes de sobrescribir el archivo
        AppDatabase.getDatabase(context).close();
        AppDatabase.resetInstance();   // limpia la referencia estática

        File dbFile = context.getDatabasePath(DB_NAME);

        // 2. Asegurarse de que el directorio padre existe
        if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        // 3. Copiar desde el Uri al archivo de BD
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(dbFile)) {

            if (in == null) return false;
            copyStream(in, out);

            // 4. Borrar archivos WAL y SHM que podrían quedar obsoletos
            deleteIfExists(new File(dbFile.getPath() + "-wal"));
            deleteIfExists(new File(dbFile.getPath() + "-shm"));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Utilidades privadas

    private static void copyFile(File src, File dst) throws IOException {
        try (FileInputStream in  = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            copyStream(in, out);
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }

    private static void deleteIfExists(File file) {
        if (file.exists()) file.delete();
    }

    // Constructor privado: clase de utilidades
    private BackupManager() {}
}

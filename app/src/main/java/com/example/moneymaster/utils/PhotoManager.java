package com.example.moneymaster.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Card #63 – Optimización de imágenes
 *
 * Centraliza el ciclo de vida completo de una foto:
 *  - Copiar desde URI temporal a almacenamiento permanente (receipts/)
 *  - Comprimir automáticamente al guardar (≤ 1 MB)
 *  - Generar thumbnail 150×150
 *  - Eliminar foto + thumbnail
 *
 * Toda la E/S se realiza en el executor inyectado o en uno interno para
 * no bloquear el hilo principal.
 */
public class PhotoManager {

    private static final String TAG = "PhotoManager";
    private static final String RECEIPTS_DIR = "receipts";

    /** Callback que devuelve la ruta final tras guardar/procesar. */
    public interface PhotoSaveCallback {
        /** @param finalPath Ruta permanente comprimida, o {@code null} si hubo error. */
        void onComplete(String finalPath);
    }

    private final Context context;
    private final ExecutorService executor;

    public PhotoManager(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /** Constructor con executor inyectado (útil para tests). */
    public PhotoManager(Context context, ExecutorService executor) {
        this.context = context.getApplicationContext();
        this.executor = executor;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Guardar foto (copia + compresión + thumbnail)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Copia {@code sourcePath} al directorio permanente, lo comprime y genera
     * el thumbnail. El callback se invoca en el hilo del executor (NO en UI).
     *
     * <p>Uso típico: llamar desde el ViewModel en un {@code databaseWriteExecutor}.
     *
     * @param sourcePath Ruta de la foto temporal (capturada por cámara o galería).
     * @param callback   Invocado al finalizar con la ruta permanente o {@code null}.
     */
    public void savePhoto(String sourcePath, PhotoSaveCallback callback) {
        executor.execute(() -> {
            String result = savePhotoSync(sourcePath);
            if (callback != null) callback.onComplete(result);
        });
    }

    /**
     * Versión síncrona de {@link #savePhoto} para llamar desde hilos ya existentes.
     *
     * @return Ruta permanente comprimida, o {@code null} si hubo error.
     */
    public String savePhotoSync(String sourcePath) {
        if (sourcePath == null || sourcePath.isEmpty()) return null;

        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            Log.e(TAG, "Fichero fuente no encontrado: " + sourcePath);
            return null;
        }

        // Directorio permanente: /data/data/<pkg>/files/receipts/
        File receiptsDir = getReceiptsDir();
        if (receiptsDir == null) return null;

        // Nombre único para evitar colisiones
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
                .format(new Date());
        File destFile = new File(receiptsDir, "IMG_" + timestamp + ".jpg");

        // 1. Copiar al directorio permanente
        if (!copyFile(sourceFile, destFile)) return null;

        // 2. Comprimir (modifica el fichero en sitio)
        String compressedPath = ImageOptimizer.compressImage(destFile.getAbsolutePath());
        if (compressedPath == null) {
            destFile.delete();
            return null;
        }

        // 3. Generar thumbnail
        ImageOptimizer.generateThumbnail(compressedPath, context);

        Log.d(TAG, "Foto guardada y optimizada: " + compressedPath
                + " (" + new File(compressedPath).length() / 1024 + " KB)");

        return compressedPath;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Eliminar foto
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Elimina la foto en {@code imagePath} y su thumbnail asociado.
     * Ejecuta en el executor interno.
     */
    public void deletePhoto(String imagePath) {
        if (imagePath == null) return;
        executor.execute(() -> deletePhotoSync(imagePath));
    }

    /** Versión síncrona de {@link #deletePhoto}. */
    public void deletePhotoSync(String imagePath) {
        if (imagePath == null) return;

        File file = new File(imagePath);
        if (file.exists()) file.delete();

        // Eliminar thumbnail
        ImageOptimizer.deleteThumbnail(imagePath);

        Log.d(TAG, "Foto eliminada: " + imagePath);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Regenerar thumbnail si no existe
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Si el thumbnail de {@code imagePath} no existe, lo genera en segundo plano.
     * Útil para fotos guardadas antes de esta optimización.
     */
    public void ensureThumbnailExists(String imagePath) {
        if (imagePath == null) return;
        String thumbPath = ImageOptimizer.buildThumbnailPath(imagePath);
        if (!new File(thumbPath).exists()) {
            executor.execute(() ->
                    ImageOptimizer.generateThumbnail(imagePath, context));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    private File getReceiptsDir() {
        File dir = new File(context.getFilesDir(), RECEIPTS_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "No se pudo crear directorio receipts");
            return null;
        }
        return dir;
    }

    private boolean copyFile(File src, File dst) {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error copiando fichero", e);
            return false;
        }
    }
}

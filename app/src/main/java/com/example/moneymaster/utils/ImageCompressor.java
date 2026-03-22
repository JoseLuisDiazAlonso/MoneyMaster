package com.example.moneymaster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Card #30 — Compresión de imágenes
 *
 * Utilidad para comprimir imágenes JPEG y copiar URIs de galería
 * al directorio interno de la app.
 *
 * Estrategia de compresión:
 *   1. Decode con inSampleSize para bajar la memoria necesaria
 *   2. Scale al maxSidePx si algún lado supera ese límite
 *   3. Corregir rotación EXIF
 *   4. Re-encode como JPEG con calidad configurable
 */
public class ImageCompressor {

    private static final String TAG = "ImageCompressor";

    /**
     * Comprime una imagen existente en disco.
     * Sobreescribe el archivo original con la versión comprimida.
     *
     * @param filePath   Ruta absoluta del archivo JPEG
     * @param maxSidePx  Máximo píxeles en el lado mayor (ej. 1024)
     * @param quality    Calidad JPEG 0-100 (ej. 80)
     * @return           La misma filePath si tuvo éxito, null si falló
     */
    public static String compress(String filePath, int maxSidePx, int quality) {
        if (filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            Log.w(TAG, "Archivo vacío o inexistente: " + filePath);
            return null;
        }
        try {
            // 1. Leer dimensiones sin decodificar pixeles
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            int originalWidth  = options.outWidth;
            int originalHeight = options.outHeight;
            if (originalWidth <= 0 || originalHeight <= 0) {
                Log.w(TAG, "No se pudieron leer dimensiones de: " + filePath);
                return null;
            }

            // 2. Calcular inSampleSize (siempre potencia de 2)
            options.inSampleSize    = calculateInSampleSize(originalWidth, originalHeight, maxSidePx);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig  = Bitmap.Config.ARGB_8888;

            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            if (bitmap == null) {
                Log.e(TAG, "BitmapFactory.decodeFile devolvió null");
                return null;
            }

            // 3. Scale fino si todavía supera maxSidePx
            bitmap = scaleDown(bitmap, maxSidePx);

            // 4. Corregir rotación EXIF
            bitmap = fixExifRotation(bitmap, filePath);

            // 5. Guardar (sobreescribir el original)
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            } finally {
                bitmap.recycle();
            }

            Log.d(TAG, "Comprimido: " + filePath
                    + " (" + file.length() / 1024 + " KB)");
            return filePath;

        } catch (IOException e) {
            Log.e(TAG, "Error comprimiendo imagen", e);
            return null;
        }
    }

    /**
     * Copia el contenido de una URI (galería) a un File de destino.
     * No comprime — llama a compress() después si es necesario.
     */
    public static void copyUriToFile(Context context, Uri sourceUri, File destFile)
            throws IOException {
        try (InputStream in  = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {
            if (in == null) throw new IOException("InputStream null para URI: " + sourceUri);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    /**
     * Calcula el inSampleSize óptimo (potencia de 2) para que la imagen decodificada
     * no supere maxSidePx en ninguno de sus lados.
     */
    private static int calculateInSampleSize(int width, int height, int maxSidePx) {
        int inSampleSize = 1;
        if (width > maxSidePx || height > maxSidePx) {
            final int halfWidth  = width  / 2;
            final int halfHeight = height / 2;
            while ((halfWidth / inSampleSize) >= maxSidePx
                    || (halfHeight / inSampleSize) >= maxSidePx) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Reduce proporcionalmente si algún lado supera maxSidePx.
     */
    private static Bitmap scaleDown(Bitmap bitmap, int maxSidePx) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= maxSidePx && h <= maxSidePx) return bitmap;

        float scale = (float) maxSidePx / Math.max(w, h);
        int newW    = Math.round(w * scale);
        int newH    = Math.round(h * scale);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true);
        bitmap.recycle();
        return scaled;
    }

    /**
     * Lee el tag EXIF Orientation y rota el bitmap si es necesario.
     * Muchos teléfonos guardan las fotos en horizontal y usan EXIF para indicar la rotación.
     */
    private static Bitmap fixExifRotation(Bitmap bitmap, String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int degrees = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:  degrees =  90; break;
                case ExifInterface.ORIENTATION_ROTATE_180: degrees = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: degrees = 270; break;
            }
            if (degrees == 0) return bitmap;

            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            Bitmap rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotated;

        } catch (IOException e) {
            Log.w(TAG, "No se pudo leer EXIF de " + filePath + ": " + e.getMessage());
            return bitmap; // devolver sin rotar si falla
        }
    }
}
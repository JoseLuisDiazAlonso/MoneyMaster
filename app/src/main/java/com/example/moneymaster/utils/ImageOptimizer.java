package com.example.moneymaster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Card #63 – Optimización de imágenes
 *
 * Responsabilidades:
 *  - Comprimir automáticamente al guardar (≤ 1 MB)
 *  - Generar thumbnails 150×150 independientes
 *  - Liberar bitmaps no usados
 */
public class ImageOptimizer {

    private static final String TAG = "ImageOptimizer";

    /** Tamaño máximo permitido para la foto original (1 MB). */
    public static final long MAX_FILE_SIZE_BYTES = 1_048_576L; // 1 MB

    /** Dimensión máxima (ancho o alto) para fotos originales guardadas. */
    private static final int MAX_DIMENSION_PX = 1280;

    /** Lado del thumbnail cuadrado. */
    public static final int THUMBNAIL_SIZE_PX = 150;

    /** Calidad JPEG inicial para la compresión iterativa. */
    private static final int INITIAL_QUALITY = 85;

    /** Calidad JPEG mínima que se permite antes de desistir. */
    private static final int MIN_QUALITY = 40;

    /** Paso de reducción de calidad en cada iteración. */
    private static final int QUALITY_STEP = 10;

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Comprime la imagen en {@code sourcePath} y la sobreescribe garantizando
     * que el fichero resultante no supere {@link #MAX_FILE_SIZE_BYTES}.
     *
     * @param sourcePath Ruta absoluta de la imagen original.
     * @return La misma ruta si todo fue bien, {@code null} si hubo error.
     */
    public static String compressImage(String sourcePath) {
        if (sourcePath == null || sourcePath.isEmpty()) return null;

        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) return null;

        // Si ya cumple el límite, nada que hacer
        if (sourceFile.length() <= MAX_FILE_SIZE_BYTES) {
            return sourcePath;
        }

        Bitmap bitmap = null;
        try {
            bitmap = decodeSampledBitmap(sourcePath, MAX_DIMENSION_PX, MAX_DIMENSION_PX);
            if (bitmap == null) return null;

            bitmap = correctOrientation(bitmap, sourcePath);

            // Compresión iterativa hasta cumplir el límite
            int quality = INITIAL_QUALITY;
            File tempFile = new File(sourceFile.getParent(), "tmp_" + sourceFile.getName());

            boolean success = false;
            while (quality >= MIN_QUALITY) {
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                }
                if (tempFile.length() <= MAX_FILE_SIZE_BYTES) {
                    success = true;
                    break;
                }
                quality -= QUALITY_STEP;
            }

            if (!success) {
                // Último intento al mínimo de calidad
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, MIN_QUALITY, fos);
                }
            }

            // Reemplazar original con el comprimido
            if (tempFile.exists()) {
                sourceFile.delete();
                tempFile.renameTo(sourceFile);
            }

            return sourcePath;

        } catch (IOException e) {
            Log.e(TAG, "Error comprimiendo imagen: " + sourcePath, e);
            return null;
        } finally {
            recycleBitmap(bitmap);
        }
    }

    /**
     * Genera un thumbnail cuadrado de {@link #THUMBNAIL_SIZE_PX} px y lo
     * guarda en la misma carpeta que la imagen original con prefijo "thumb_".
     *
     * @param sourcePath Ruta de la imagen original.
     * @param context    Contexto de aplicación (usado para obtener filesDir si
     *                   fuera necesario).
     * @return Ruta del thumbnail generado, o {@code null} si hubo error.
     */
    public static String generateThumbnail(String sourcePath, Context context) {
        if (sourcePath == null || sourcePath.isEmpty()) return null;

        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) return null;

        String thumbPath = buildThumbnailPath(sourcePath);
        File thumbFile = new File(thumbPath);

        // Si ya existe y es reciente, reutilizarlo
        if (thumbFile.exists() && thumbFile.length() > 0) {
            return thumbPath;
        }

        Bitmap original = null;
        Bitmap thumb = null;
        try {
            original = decodeSampledBitmap(sourcePath, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX);
            if (original == null) return null;

            original = correctOrientation(original, sourcePath);
            thumb = createCenterCroppedThumbnail(original, THUMBNAIL_SIZE_PX);

            try (FileOutputStream fos = new FileOutputStream(thumbFile)) {
                thumb.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            }

            return thumbPath;

        } catch (IOException e) {
            Log.e(TAG, "Error generando thumbnail: " + sourcePath, e);
            return null;
        } finally {
            recycleBitmap(original);
            recycleBitmap(thumb);
        }
    }

    /**
     * Comprime la imagen y genera su thumbnail en una sola operación.
     *
     * @return La ruta original comprimida, o {@code null} si hubo error.
     */
    public static String compressAndGenerateThumbnail(String sourcePath, Context context) {
        String compressed = compressImage(sourcePath);
        if (compressed != null) {
            generateThumbnail(compressed, context);
        }
        return compressed;
    }

    /**
     * Elimina el thumbnail asociado a {@code sourcePath} si existe.
     */
    public static void deleteThumbnail(String sourcePath) {
        if (sourcePath == null) return;
        File thumb = new File(buildThumbnailPath(sourcePath));
        if (thumb.exists()) thumb.delete();
    }

    /**
     * Devuelve la ruta del thumbnail para una imagen, exista o no.
     */
    public static String buildThumbnailPath(String sourcePath) {
        if (sourcePath == null) return null;
        File f = new File(sourcePath);
        return new File(f.getParent(), "thumb_" + f.getName()).getAbsolutePath();
    }

    /**
     * Libera un bitmap de forma segura.
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Decodifica la imagen sub-muestreada para no cargar el bitmap completo
     * en memoria cuando sólo se necesita una versión reducida.
     */
    private static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // Menor uso de memoria

        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Calcula el inSampleSize óptimo para no cargar más píxeles de los necesarios.
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Corrige la orientación del bitmap según los metadatos EXIF.
     */
    private static Bitmap correctOrientation(Bitmap bitmap, String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.preScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.preScale(1, -1);
                    break;
                default:
                    return bitmap;
            }

            Bitmap rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            recycleBitmap(bitmap);
            return rotated;

        } catch (IOException e) {
            Log.w(TAG, "No se pudo leer EXIF: " + path);
            return bitmap;
        }
    }

    /**
     * Crea un thumbnail cuadrado con recorte centrado (center-crop).
     */
    private static Bitmap createCenterCroppedThumbnail(Bitmap source, int size) {
        int srcW = source.getWidth();
        int srcH = source.getHeight();

        float scale = Math.max((float) size / srcW, (float) size / srcH);
        int scaledW = Math.round(srcW * scale);
        int scaledH = Math.round(srcH * scale);

        Bitmap scaled = Bitmap.createScaledBitmap(source, scaledW, scaledH, true);

        int x = (scaledW - size) / 2;
        int y = (scaledH - size) / 2;

        Bitmap cropped = Bitmap.createBitmap(scaled, x, y, size, size);

        if (scaled != source && scaled != cropped) {
            recycleBitmap(scaled);
        }

        return cropped;
    }
}
package com.example.moneymaster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Card #31 — Clase ImageUtils: Utilidades para manejo de imágenes
 *
 * Métodos helper de alto nivel para las pantallas de MoneyMaster.
 * Esta clase NO duplica lógica de ImageCompressor — la delega.
 *
 * Responsabilidades:
 *   - Comprimir imagen         → delega en ImageCompressor.compress()
 *   - Rotar según EXIF         → delega en ImageCompressor (se aplica dentro de compress())
 *   - Redimensionar            → redimensionarBitmap()
 *   - Convertir a Bitmap       → uriToBitmap() / pathToBitmap()
 *   - Guardar en interno       → guardarEnDirectorioInterno()
 *   - Eliminar foto            → eliminarFoto()
 *   - Obtener URI de foto      → obtenerUri()
 *
 * Uso típico desde una Activity o ViewModel:
 *   String ruta = ImageUtils.guardarEnDirectorioInterno(context, uriFoto);
 *   ImageUtils.comprimirFoto(ruta, 1024, 80);
 *   Uri uri = ImageUtils.obtenerUri(context, ruta);
 */
public class ImageUtils {

    private static final String TAG          = "ImageUtils";
    private static final String RECEIPTS_DIR = "receipts";
    private static final String AUTHORITY    = "com.example.moneymaster.fileprovider";

    // Constructor privado — clase de utilidades estáticas, no se instancia
    private ImageUtils() {}

    // ── 1. Comprimir imagen ───────────────────────────────────────────────────

    /**
     * Comprime una imagen JPEG en disco.
     * Delega en ImageCompressor que además corrige la rotación EXIF.
     *
     * @param filePath  Ruta absoluta del archivo a comprimir
     * @param maxSidePx Máximo píxeles en el lado mayor (recomendado: 1024)
     * @param quality   Calidad JPEG 0-100 (recomendado: 80)
     * @return          La misma ruta si tuvo éxito, null si falló
     */
    public static String comprimirFoto(String filePath, int maxSidePx, int quality) {
        return ImageCompressor.compress(filePath, maxSidePx, quality);
    }

    /**
     * Sobrecarga con valores por defecto: 1024px y calidad 80.
     */
    public static String comprimirFoto(String filePath) {
        return ImageCompressor.compress(filePath, 1024, 80);
    }

    // ── 2. Rotar imagen según EXIF ────────────────────────────────────────────

    /**
     * La corrección EXIF se aplica automáticamente dentro de comprimirFoto().
     * Este método existe para casos donde necesitas solo la rotación sin recomprimir,
     * por ejemplo para mostrar un Bitmap en memoria sin guardarlo.
     *
     * @param filePath Ruta absoluta del archivo JPEG
     * @return         Bitmap con la orientación corregida, o null si falla
     */
    public static Bitmap rotarSegunExif(String filePath) {
        if (filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists()) {
            Log.w(TAG, "rotarSegunExif: archivo no encontrado — " + filePath);
            return null;
        }
        try {
            androidx.exifinterface.media.ExifInterface exif =
                    new androidx.exifinterface.media.ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);

            int degrees = 0;
            switch (orientation) {
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                    degrees = 90; break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                    degrees = 180; break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                    degrees = 270; break;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if (bitmap == null) return null;
            if (degrees == 0)   return bitmap;

            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(degrees);
            Bitmap rotado = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotado;

        } catch (IOException e) {
            Log.e(TAG, "rotarSegunExif: error leyendo EXIF — " + e.getMessage());
            return null;
        }
    }

    // ── 3. Redimensionar ──────────────────────────────────────────────────────

    /**
     * Redimensiona un Bitmap a unas dimensiones exactas.
     * Si necesitas redimensionar manteniendo proporción, usa comprimirFoto().
     *
     * @param bitmap    Bitmap de origen
     * @param nuevoAncho Ancho deseado en píxeles
     * @param nuevoAlto  Alto deseado en píxeles
     * @return          Nuevo Bitmap redimensionado (el original NO se recicla)
     */
    public static Bitmap redimensionarBitmap(Bitmap bitmap, int nuevoAncho, int nuevoAlto) {
        if (bitmap == null || nuevoAncho <= 0 || nuevoAlto <= 0) return bitmap;
        return Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true);
    }

    /**
     * Redimensiona desde una ruta de archivo manteniendo la proporción original,
     * ajustando el lado mayor al valor indicado.
     *
     * @param filePath  Ruta absoluta del archivo JPEG
     * @param maxSidePx Máximo píxeles en el lado mayor
     * @return          Bitmap redimensionado, o null si falla
     */
    public static Bitmap redimensionarDesdeRuta(String filePath, int maxSidePx) {
        if (filePath == null) return null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        int w = opts.outWidth;
        int h = opts.outHeight;
        if (w <= 0 || h <= 0) return null;

        opts.inSampleSize      = calcularInSampleSize(w, h, maxSidePx);
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts);
        if (bitmap == null) return null;

        // Scale proporcional fino
        if (w > maxSidePx || h > maxSidePx) {
            float scale = (float) maxSidePx / Math.max(bitmap.getWidth(), bitmap.getHeight());
            int nw = Math.round(bitmap.getWidth()  * scale);
            int nh = Math.round(bitmap.getHeight() * scale);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, nw, nh, true);
            bitmap.recycle();
            return scaled;
        }
        return bitmap;
    }

    // ── 4. Convertir a Bitmap ─────────────────────────────────────────────────

    /**
     * Convierte una ruta absoluta a Bitmap, con muestreo eficiente en memoria.
     *
     * @param filePath  Ruta absoluta del archivo JPEG
     * @param maxSidePx Límite del lado mayor para el muestreo (0 = sin límite)
     * @return          Bitmap decodificado, o null si falla
     */
    public static Bitmap pathToBitmap(String filePath, int maxSidePx) {
        if (filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists()) {
            Log.w(TAG, "pathToBitmap: archivo no encontrado — " + filePath);
            return null;
        }
        if (maxSidePx <= 0) {
            return BitmapFactory.decodeFile(filePath);
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        opts.inSampleSize       = calcularInSampleSize(opts.outWidth, opts.outHeight, maxSidePx);
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, opts);
    }

    /**
     * Convierte una URI de contenido (galería o FileProvider) a Bitmap.
     *
     * @param context   Contexto de la app
     * @param uri       URI de la imagen
     * @param maxSidePx Límite del lado mayor (0 = sin límite)
     * @return          Bitmap decodificado, o null si falla
     */
    public static Bitmap uriToBitmap(Context context, Uri uri, int maxSidePx) {
        if (context == null || uri == null) return null;
        try {
            if (maxSidePx <= 0) {
                return android.provider.MediaStore.Images.Media
                        .getBitmap(context.getContentResolver(), uri);
            }
            // Decodificar con muestreo para no agotar la memoria
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            try (java.io.InputStream is =
                         context.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(is, null, opts);
            }
            opts.inSampleSize       = calcularInSampleSize(opts.outWidth, opts.outHeight, maxSidePx);
            opts.inJustDecodeBounds = false;
            try (java.io.InputStream is =
                         context.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(is, null, opts);
            }
        } catch (IOException e) {
            Log.e(TAG, "uriToBitmap: error — " + e.getMessage());
            return null;
        }
    }

    // ── 5. Guardar en almacenamiento interno ──────────────────────────────────

    /**
     * Copia una imagen desde una URI (galería) al directorio interno de la app,
     * genera un nombre único y la comprime.
     *
     * Flujo: URI → copia a /files/receipts/ → compresión → ruta absoluta final
     *
     * @param context   Contexto de la app
     * @param sourceUri URI de origen (galería o cámara)
     * @return          Ruta absoluta del archivo guardado y comprimido, null si falla
     */
    public static String guardarEnDirectorioInterno(Context context, Uri sourceUri) {
        if (context == null || sourceUri == null) return null;
        try {
            File destFile = crearArchivoUnico(context);
            ImageCompressor.copyUriToFile(context, sourceUri, destFile);
            return ImageCompressor.compress(destFile.getAbsolutePath(), 1024, 80);
        } catch (IOException e) {
            Log.e(TAG, "guardarEnDirectorioInterno: error — " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea el directorio /files/receipts/ si no existe y devuelve un File
     * con nombre único listo para ser escrito.
     */
    public static File crearArchivoUnico(Context context) throws IOException {
        File dir = new File(context.getFilesDir(), RECEIPTS_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio: " + dir.getAbsolutePath());
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String random    = Integer.toHexString((int)(Math.random() * 0xFFFF));
        String fileName  = "receipt_" + timestamp + "_" + random + ".jpg";
        File   file      = new File(dir, fileName);
        if (!file.createNewFile()) {
            throw new IOException("El archivo ya existe: " + file.getAbsolutePath());
        }
        return file;
    }

    // ── 6. Eliminar foto ──────────────────────────────────────────────────────

    /**
     * Elimina un archivo de foto del almacenamiento interno.
     *
     * @param filePath Ruta absoluta del archivo a eliminar
     * @return         true si se eliminó correctamente, false si no existía o falló
     */
    public static boolean eliminarFoto(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Log.w(TAG, "eliminarFoto: ruta nula o vacía");
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Log.w(TAG, "eliminarFoto: archivo no encontrado — " + filePath);
            return false;
        }
        boolean eliminado = file.delete();
        Log.d(TAG, "eliminarFoto: " + (eliminado ? "OK" : "FALLO") + " — " + filePath);
        return eliminado;
    }

    // ── 7. Obtener URI de foto ────────────────────────────────────────────────

    /**
     * Genera una content:// URI para un archivo del directorio interno usando FileProvider.
     * Necesaria para compartir la foto con otras apps (email, WhatsApp, etc.)
     * o para pasarla a la cámara del sistema via EXTRA_OUTPUT.
     *
     * @param context  Contexto de la app
     * @param filePath Ruta absoluta del archivo en /files/receipts/
     * @return         URI content:// apta para Intent, null si falla
     */
    public static Uri obtenerUri(Context context, String filePath) {
        if (context == null || filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists()) {
            Log.w(TAG, "obtenerUri: archivo no encontrado — " + filePath);
            return null;
        }
        try {
            return FileProvider.getUriForFile(context, AUTHORITY, file);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "obtenerUri: FileProvider rechazó el archivo — " + e.getMessage());
            return null;
        }
    }

    // ── Interno ───────────────────────────────────────────────────────────────

    private static int calcularInSampleSize(int width, int height, int maxSidePx) {
        int inSampleSize = 1;
        if (width > maxSidePx || height > maxSidePx) {
            final int halfWidth  = width  / 2;
            final int halfHeight = height / 2;
            while ((halfWidth  / inSampleSize) >= maxSidePx
                    || (halfHeight / inSampleSize) >= maxSidePx) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}

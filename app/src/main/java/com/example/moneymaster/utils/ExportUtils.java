package com.example.moneymaster.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ExportUtils — Utilidades base para exportación de datos (Card #46, EXPORT-001).
 *
 * Clase estática de utilidad. No instanciar.
 *
 * Responsabilidades:
 *   1. Crear y garantizar el directorio de exportación interno.
 *   2. Generar nombres de archivo únicos con timestamp y extensión.
 *   3. Obtener la URI compatible con FileProvider para compartir archivos.
 *   4. Limpiar archivos temporales de exportaciones antiguas.
 *
 * Directorio de trabajo:
 *   getFilesDir()/exports/
 *   → Interno a la app, invisible al usuario sin root.
 *   → Incluido en file_provider_paths.xml como <files-path name="exports" path="exports/"/>
 *   → Se borra al desinstalar la app.
 *
 * Uso típico:
 * <pre>
 *   // 1. Obtener el directorio
 *   File dir = ExportUtils.getDirectorioExportacion(context);
 *
 *   // 2. Crear un archivo con nombre único
 *   String nombre = ExportUtils.generarNombreArchivo("gastos_enero", "xlsx");
 *   File archivo = new File(dir, nombre);
 *
 *   // 3. Escribir el contenido del archivo (Apache POI, iText, etc.)
 *   //    workbook.write(new FileOutputStream(archivo));
 *
 *   // 4. Obtener la URI para compartir
 *   Uri uri = ExportUtils.getUriParaCompartir(context, archivo);
 *
 *   // 5. Limpiar exportaciones antiguas (>7 días)
 *   ExportUtils.limpiarArchivosAntiguos(context, 7);
 * </pre>
 */
public final class ExportUtils {

    private static final String TAG = "ExportUtils";

    /** Nombre del subdirectorio de exportación dentro de getFilesDir(). */
    private static final String DIRECTORIO_EXPORTS = "exports";

    /**
     * Authority del FileProvider declarado en AndroidManifest.xml.
     * Debe coincidir exactamente con android:authorities del <provider>.
     */
    private static final String FILE_PROVIDER_AUTHORITY =
            "com.example.moneymaster.fileprovider";

    /** Formato del timestamp incluido en cada nombre de archivo. */
    private static final String FORMATO_TIMESTAMP = "yyyyMMdd_HHmmss";

    /**
     * Número de días que se conservan los archivos de exportación
     * antes de que limpiarArchivosAntiguos() los elimine.
     */
    public static final int DIAS_RETENCION_DEFAULT = 7;

    // ─── Constructor privado: clase de utilidad, no instanciar ───────────────

    private ExportUtils() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar.");
    }

    // =========================================================================
    // 1. Directorio de exportación
    // =========================================================================

    /**
     * Devuelve el directorio interno de exportación, creándolo si no existe.
     *
     * Ruta: context.getFilesDir() + "/exports/"
     *
     * Por qué getFilesDir() y no getExternalFilesDir():
     *   · No requiere permiso WRITE_EXTERNAL_STORAGE (eliminado en API 29+).
     *   · Compatible con FileProvider sin configuración adicional.
     *   · Los archivos son privados a la app → privacidad garantizada.
     *
     * @param context Contexto de la aplicación o Activity.
     * @return        El directorio de exportación (garantizado que existe).
     * @throws IllegalStateException si no se puede crear el directorio.
     */
    public static File getDirectorioExportacion(Context context) {
        File directorio = new File(context.getFilesDir(), DIRECTORIO_EXPORTS);

        if (!directorio.exists()) {
            boolean creado = directorio.mkdirs();
            if (!creado) {
                Log.e(TAG, "No se pudo crear el directorio de exportación: "
                        + directorio.getAbsolutePath());
                throw new IllegalStateException(
                        "No se pudo crear el directorio de exportación.");
            }
            Log.d(TAG, "Directorio de exportación creado: "
                    + directorio.getAbsolutePath());
        }

        return directorio;
    }

    // =========================================================================
    // 2. Generador de nombres de archivo únicos
    // =========================================================================

    /**
     * Genera un nombre de archivo único combinando un prefijo legible
     * y un timestamp de alta resolución.
     *
     * Formato resultante: {prefijo}_{yyyyMMdd_HHmmss}.{extension}
     * Ejemplo: gastos_enero_20250315_142305.xlsx
     *
     * Por qué timestamp y no UUID:
     *   · El nombre es legible por el usuario si lo ve en un gestor de archivos.
     *   · El timestamp garantiza unicidad sin colisiones en uso normal.
     *   · Permite ordenar por nombre y obtener el más reciente.
     *
     * @param prefijo    Texto descriptivo sin espacios ni caracteres especiales.
     *                   Ejemplos: "gastos_enero", "informe_anual", "grupos"
     * @param extension  Extensión sin punto. Ejemplos: "xlsx", "pdf", "csv"
     * @return           Nombre de archivo completo con timestamp.
     */
    public static String generarNombreArchivo(String prefijo, String extension) {
        String timestamp = new SimpleDateFormat(FORMATO_TIMESTAMP, Locale.getDefault())
                .format(new Date());

        // Sanitizar el prefijo: reemplazar espacios y caracteres no válidos
        String prefijoLimpio = prefijo
                .trim()
                .replaceAll("[^a-zA-Z0-9_\\-]", "_")
                .replaceAll("_{2,}", "_"); // eliminar guiones bajos dobles

        return prefijoLimpio + "_" + timestamp + "." + extension.toLowerCase(Locale.getDefault());
    }

    /**
     * Versión simplificada: genera el nombre y crea el File en el directorio
     * de exportación en un solo paso.
     *
     * Equivalente a:
     *   new File(getDirectorioExportacion(context), generarNombreArchivo(prefijo, extension))
     *
     * @param context   Contexto de la aplicación o Activity.
     * @param prefijo   Texto descriptivo. Ejemplo: "gastos_marzo"
     * @param extension Extensión sin punto. Ejemplo: "pdf"
     * @return          File listo para escribir (el archivo en sí no existe aún).
     */
    public static File crearArchivoExportacion(Context context,
                                               String prefijo,
                                               String extension) {
        File directorio = getDirectorioExportacion(context);
        String nombre   = generarNombreArchivo(prefijo, extension);
        return new File(directorio, nombre);
    }

    // =========================================================================
    // 3. URI compatible con FileProvider
    // =========================================================================

    /**
     * Convierte un File interno en una content:// URI que puede enviarse
     * a otras aplicaciones sin exponer la ruta real del archivo.
     *
     * Por qué FileProvider y no Uri.fromFile():
     *   · Uri.fromFile() genera una file:// URI que está bloqueada desde API 24
     *     (Android 7) y lanza FileUriExposedException.
     *   · FileProvider genera una content:// URI con permisos temporales,
     *     compatible con todas las versiones desde API 24.
     *
     * Prerequisito: el archivo debe estar bajo uno de los directorios declarados
     * en res/xml/file_provider_paths.xml. Para el directorio "exports":
     *   <files-path name="exports" path="exports/" />
     *
     * @param context Contexto de la aplicación o Activity.
     * @param archivo Archivo a compartir. Debe existir y estar en getFilesDir().
     * @return        content:// URI lista para pasarse a un Intent de compartir,
     *                o null si el archivo no existe o hay un error.
     */
    public static Uri getUriParaCompartir(Context context, File archivo) {
        if (archivo == null || !archivo.exists()) {
            Log.e(TAG, "getUriParaCompartir: el archivo no existe o es null → "
                    + (archivo != null ? archivo.getAbsolutePath() : "null"));
            return null;
        }

        try {
            return FileProvider.getUriForFile(
                    context,
                    FILE_PROVIDER_AUTHORITY,
                    archivo
            );
        } catch (IllegalArgumentException e) {
            // Se lanza si el archivo está fuera de los directorios declarados
            // en file_provider_paths.xml.
            Log.e(TAG, "El archivo no está cubierto por FileProvider. "
                    + "Verifica file_provider_paths.xml. Ruta: "
                    + archivo.getAbsolutePath(), e);
            return null;
        }
    }

    // =========================================================================
    // 4. Limpieza de archivos temporales antiguos
    // =========================================================================

    /**
     * Elimina los archivos del directorio de exportación que tengan más
     * de {@code diasRetencion} días de antigüedad.
     *
     * Cuándo llamarlo:
     *   · Al inicio de cada exportación (antes de generar el nuevo archivo).
     *   · En el onCreate() de MainActivity, en segundo plano.
     *   · Nunca en el hilo principal si el directorio puede tener muchos archivos.
     *
     * @param context        Contexto de la aplicación o Activity.
     * @param diasRetencion  Archivos más antiguos que este número de días serán
     *                       eliminados. Usa DIAS_RETENCION_DEFAULT (7) si no
     *                       tienes un criterio específico.
     * @return               Número de archivos eliminados (útil para logging).
     */
    public static int limpiarArchivosAntiguos(Context context, int diasRetencion) {
        File directorio = getDirectorioExportacion(context);
        File[] archivos = directorio.listFiles();

        if (archivos == null || archivos.length == 0) {
            Log.d(TAG, "limpiarArchivosAntiguos: directorio vacío, nada que limpiar.");
            return 0;
        }

        long limiteMs = System.currentTimeMillis()
                - ((long) diasRetencion * 24 * 60 * 60 * 1000);

        int eliminados = 0;
        for (File archivo : archivos) {
            if (archivo.isFile() && archivo.lastModified() < limiteMs) {
                boolean borrado = archivo.delete();
                if (borrado) {
                    eliminados++;
                    Log.d(TAG, "Archivo eliminado: " + archivo.getName());
                } else {
                    Log.w(TAG, "No se pudo eliminar: " + archivo.getName());
                }
            }
        }

        Log.d(TAG, "limpiarArchivosAntiguos: " + eliminados
                + " archivo(s) eliminado(s) de " + archivos.length + " total.");
        return eliminados;
    }

    /**
     * Sobrecarga con retención por defecto de DIAS_RETENCION_DEFAULT (7 días).
     *
     * @param context Contexto de la aplicación o Activity.
     * @return        Número de archivos eliminados.
     */
    public static int limpiarArchivosAntiguos(Context context) {
        return limpiarArchivosAntiguos(context, DIAS_RETENCION_DEFAULT);
    }

    // =========================================================================
    // Helpers de información (útiles para logs y UI)
    // =========================================================================

    /**
     * Devuelve el tamaño total en bytes de todos los archivos en el directorio
     * de exportación. Útil para mostrar al usuario cuánto espacio ocupa.
     *
     * @param context Contexto de la aplicación o Activity.
     * @return        Tamaño total en bytes, o 0 si el directorio está vacío.
     */
    public static long getTamanoTotalBytes(Context context) {
        File directorio = getDirectorioExportacion(context);
        File[] archivos = directorio.listFiles();
        if (archivos == null) return 0;

        long total = 0;
        for (File archivo : archivos) {
            if (archivo.isFile()) {
                total += archivo.length();
            }
        }
        return total;
    }

    /**
     * Devuelve el número de archivos actualmente en el directorio de exportación.
     *
     * @param context Contexto de la aplicación o Activity.
     * @return        Número de archivos, o 0 si el directorio está vacío.
     */
    public static int getNumeroArchivos(Context context) {
        File directorio = getDirectorioExportacion(context);
        File[] archivos = directorio.listFiles();
        if (archivos == null) return 0;

        int count = 0;
        for (File archivo : archivos) {
            if (archivo.isFile()) count++;
        }
        return count;
    }
}
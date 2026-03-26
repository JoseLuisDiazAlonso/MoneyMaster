package com.example.moneymaster.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CsvExporter — Exportación de gastos e ingresos a formato CSV (Card #47).
 *
 * Clase estática de utilidad. No instanciar.
 * Depende de ExportUtils (EXPORT-001) para la gestión del directorio y las URIs.
 *
 * Formato del CSV generado:
 *   · Codificación: UTF-8 con BOM (para compatibilidad con Excel en Windows)
 *   · Separador: coma ","
 *   · Cabecera: Fecha,Tipo,Categoría,Descripción,Cantidad
 *   · Fechas: dd/MM/yyyy  (legible en español)
 *   · Números: sin separador de miles, punto decimal (ej: 1250.50)
 *   · Campos con comas o comillas: envueltos en comillas dobles y escapados
 *
 * Uso típico (en un hilo de fondo — NUNCA en el hilo principal):
 * <pre>
 *   // Exportar solo gastos del mes
 *   Uri uri = CsvExporter.exportarGastos(context, gastos, categorias, "gastos_marzo");
 *
 *   // Exportar solo ingresos
 *   Uri uri = CsvExporter.exportarIngresos(context, ingresos, categorias, "ingresos_marzo");
 *
 *   // Exportar ambos en un solo archivo
 *   Uri uri = CsvExporter.exportarMovimientos(context, gastos, ingresos, categorias, "marzo_2025");
 *
 *   // Compartir
 *   Intent intent = new Intent(Intent.ACTION_SEND);
 *   intent.setType("text/csv");
 *   intent.putExtra(Intent.EXTRA_STREAM, uri);
 *   intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 *   startActivity(Intent.createChooser(intent, "Exportar CSV"));
 * </pre>
 *
 * IMPORTANTE: Todos los métodos son síncronos y hacen I/O de disco.
 * Llamar siempre desde un hilo de fondo:
 *   executorService.execute(() -> { Uri uri = CsvExporter.exportarGastos(...); });
 */
public final class CsvExporter {

    private static final String TAG = "CsvExporter";

    /** BOM UTF-8: hace que Excel en Windows abra el CSV con caracteres especiales correctos. */
    private static final String UTF8_BOM = "\uFEFF";

    /** Cabecera de las columnas — coincide con los requisitos del card. */
    private static final String CABECERA = "Fecha,Tipo,Categoría,Descripción,Cantidad";

    /** Formato de fecha legible en español. */
    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    // ─── Constructor privado ──────────────────────────────────────────────────

    private CsvExporter() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar.");
    }

    // =========================================================================
    // API pública
    // =========================================================================

    /**
     * Exporta una lista de gastos personales a un archivo CSV.
     *
     * @param context    Contexto de la aplicación o Activity.
     * @param gastos     Lista de GastoPersonal a exportar (puede ser vacía, no null).
     * @param categorias Mapa de ID de categoría → nombre de categoría.
     *                   Se construye desde CategoriaGastoDao antes de llamar este método.
     *                   Si un ID no está en el mapa, se usa "Sin categoría".
     * @param prefijo    Nombre descriptivo del archivo sin extensión (ej: "gastos_marzo").
     * @return           URI content:// del archivo generado lista para compartir,
     *                   o null si hubo un error de I/O.
     */
    public static Uri exportarGastos(Context context,
                                     List<GastoPersonal> gastos,
                                     Map<Integer, String> categorias,
                                     String prefijo) {
        File archivo = ExportUtils.crearArchivoExportacion(context, prefijo, "csv");

        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(UTF8_BOM);
            writer.write(CABECERA + "\n");

            if (gastos != null) {
                for (GastoPersonal gasto : gastos) {
                    String nombreCategoria = categorias != null
                            ? categorias.getOrDefault(gasto.categoriaId, "Sin categoría")
                            : "Sin categoría";
                    writer.write(construirFila(
                            gasto.fecha,
                            "Gasto",
                            nombreCategoria,
                            gasto.descripcion,
                            gasto.monto
                    ));
                }
            }

            Log.d(TAG, "CSV de gastos generado: " + archivo.getAbsolutePath()
                    + " (" + (gastos != null ? gastos.size() : 0) + " filas)");

        } catch (IOException e) {
            Log.e(TAG, "Error escribiendo CSV de gastos", e);
            return null;
        }

        return ExportUtils.getUriParaCompartir(context, archivo);
    }

    /**
     * Exporta una lista de ingresos personales a un archivo CSV.
     *
     * @param context    Contexto de la aplicación o Activity.
     * @param ingresos   Lista de IngresoPersonal a exportar (puede ser vacía, no null).
     * @param categorias Mapa de ID de categoría → nombre de categoría.
     *                   Se construye desde CategoriaIngresoDao antes de llamar este método.
     * @param prefijo    Nombre descriptivo del archivo sin extensión (ej: "ingresos_marzo").
     * @return           URI content:// del archivo generado lista para compartir,
     *                   o null si hubo un error de I/O.
     */
    public static Uri exportarIngresos(Context context,
                                       List<IngresoPersonal> ingresos,
                                       Map<Integer, String> categorias,
                                       String prefijo) {
        File archivo = ExportUtils.crearArchivoExportacion(context, prefijo, "csv");

        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(UTF8_BOM);
            writer.write(CABECERA + "\n");

            if (ingresos != null) {
                for (IngresoPersonal ingreso : ingresos) {
                    String nombreCategoria = categorias != null
                            ? categorias.getOrDefault(ingreso.categoriaId, "Sin categoría")
                            : "Sin categoría";
                    writer.write(construirFila(
                            ingreso.fecha,
                            "Ingreso",
                            nombreCategoria,
                            ingreso.descripcion,
                            ingreso.monto
                    ));
                }
            }

            Log.d(TAG, "CSV de ingresos generado: " + archivo.getAbsolutePath()
                    + " (" + (ingresos != null ? ingresos.size() : 0) + " filas)");

        } catch (IOException e) {
            Log.e(TAG, "Error escribiendo CSV de ingresos", e);
            return null;
        }

        return ExportUtils.getUriParaCompartir(context, archivo);
    }

    /**
     * Exporta gastos e ingresos juntos en un único archivo CSV.
     * Las filas se ordenan por fecha ascendente dentro de cada bloque
     * (primero todos los gastos, luego todos los ingresos).
     * Si necesitas ordenación mixta por fecha, ordena las listas antes de llamar.
     *
     * @param context          Contexto de la aplicación o Activity.
     * @param gastos           Lista de GastoPersonal (puede ser vacía, no null).
     * @param ingresos         Lista de IngresoPersonal (puede ser vacía, no null).
     * @param categoriasGasto  Mapa ID → nombre para gastos.
     * @param categoriasIngreso Mapa ID → nombre para ingresos.
     * @param prefijo          Nombre descriptivo del archivo (ej: "movimientos_marzo").
     * @return                 URI content:// del archivo generado, o null si hubo error.
     */
    public static Uri exportarMovimientos(Context context,
                                          List<GastoPersonal> gastos,
                                          List<IngresoPersonal> ingresos,
                                          Map<Integer, String> categoriasGasto,
                                          Map<Integer, String> categoriasIngreso,
                                          String prefijo) {
        File archivo = ExportUtils.crearArchivoExportacion(context, prefijo, "csv");
        int totalFilas = 0;

        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(UTF8_BOM);
            writer.write(CABECERA + "\n");

            // ── Bloque de gastos ──────────────────────────────────────────────
            if (gastos != null) {
                for (GastoPersonal gasto : gastos) {
                    String cat = categoriasGasto != null
                            ? categoriasGasto.getOrDefault(gasto.categoriaId, "Sin categoría")
                            : "Sin categoría";
                    writer.write(construirFila(gasto.fecha, "Gasto", cat,
                            gasto.descripcion, gasto.monto));
                    totalFilas++;
                }
            }

            // ── Bloque de ingresos ────────────────────────────────────────────
            if (ingresos != null) {
                for (IngresoPersonal ingreso : ingresos) {
                    String cat = categoriasIngreso != null
                            ? categoriasIngreso.getOrDefault(ingreso.categoriaId, "Sin categoría")
                            : "Sin categoría";
                    writer.write(construirFila(ingreso.fecha, "Ingreso", cat,
                            ingreso.descripcion, ingreso.monto));
                    totalFilas++;
                }
            }

            Log.d(TAG, "CSV mixto generado: " + archivo.getAbsolutePath()
                    + " (" + totalFilas + " filas)");

        } catch (IOException e) {
            Log.e(TAG, "Error escribiendo CSV mixto", e);
            return null;
        }

        return ExportUtils.getUriParaCompartir(context, archivo);
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    /**
     * Construye una línea CSV con los 5 campos requeridos por el card:
     * Fecha, Tipo, Categoría, Descripción, Cantidad
     *
     * Aplica escape RFC 4180:
     *   · Si un campo contiene comas, saltos de línea o comillas dobles,
     *     se envuelve en comillas dobles.
     *   · Las comillas dobles dentro del campo se duplican ("" → ").
     *
     * @param fechaMs     Timestamp Unix en milisegundos.
     * @param tipo        "Gasto" o "Ingreso".
     * @param categoria   Nombre de la categoría.
     * @param descripcion Descripción libre (puede ser null).
     * @param monto       Importe en la moneda del usuario.
     * @return            Línea CSV terminada en "\n".
     */
    private static String construirFila(long fechaMs,
                                        String tipo,
                                        String categoria,
                                        String descripcion,
                                        double monto) {
        String fecha    = formatearFecha(fechaMs);
        String cantidad = formatearMonto(monto);
        String desc     = escaparCsv(descripcion != null ? descripcion : "");
        String cat      = escaparCsv(categoria  != null ? categoria   : "Sin categoría");

        return fecha + "," + tipo + "," + cat + "," + desc + "," + cantidad + "\n";
    }

    /**
     * Formatea un timestamp Unix (ms) a "dd/MM/yyyy".
     * Ejemplo: 1741305600000 → "07/03/2025"
     */
    private static String formatearFecha(long timestampMs) {
        return new SimpleDateFormat(FORMATO_FECHA, new Locale("es", "ES"))
                .format(new Date(timestampMs));
    }

    /**
     * Formatea un double como número con dos decimales y punto decimal.
     * Se usa punto (no coma) porque la coma es el separador de columnas del CSV.
     * Ejemplo: 1250.5 → "1250.50"
     */
    private static String formatearMonto(double monto) {
        return String.format(Locale.US, "%.2f", monto);
    }

    /**
     * Aplica escape RFC 4180 a un campo de texto.
     * Si el campo contiene coma, comilla doble o salto de línea,
     * lo envuelve en comillas dobles y duplica las comillas internas.
     *
     * Ejemplos:
     *   "Cena, restaurante"  → "\"Cena, restaurante\""
     *   "Dijo \"hola\""      → "\"Dijo \"\"hola\"\"\""
     *   "Supermercado"       → "Supermercado"   (sin cambios)
     */
    private static String escaparCsv(String campo) {
        if (campo == null) return "";
        if (campo.contains(",") || campo.contains("\"") || campo.contains("\n")) {
            return "\"" + campo.replace("\"", "\"\"") + "\"";
        }
        return campo;
    }
}

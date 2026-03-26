package com.example.moneymaster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PdfExporter — Generador de informes PDF con iText7 (Card #48, EXPORT-004).
 *
 * Clase estática de utilidad. No instanciar.
 * Depende de ExportUtils (EXPORT-001) para gestión del directorio y URIs.
 *
 * Estructura del PDF generado:
 *   1. Cabecera:  título "MoneyMaster" + periodo + línea divisoria
 *   2. Resumen:   tabla 3 columnas (Total Gastos | Total Ingresos | Balance)
 *   3. Gráfico:   imagen del PieChart convertida a Bitmap (opcional)
 *   4. Tabla:     gastos e ingresos con Fecha | Tipo | Categoría | Descripción | Cantidad
 *   5. Footer:    fecha de generación centrada al pie de cada página
 *
 * Uso típico (en hilo de fondo):
 * <pre>
 *   Uri uri = PdfExporter.exportar(
 *       context,
 *       gastos, ingresos,
 *       categoriasGasto, categoriasIngreso,
 *       pieChartBitmap,   // null si no hay gráfico disponible
 *       "Marzo 2025",
 *       "informe_marzo"
 *   );
 *   // Compartir uri con Intent.ACTION_SEND, type = "application/pdf"
 * </pre>
 *
 * IMPORTANTE: Todos los métodos hacen I/O de disco.
 * Llamar siempre desde un hilo de fondo (ExecutorService).
 */
public final class PdfExporter {

    private static final String TAG = "PdfExporter";

    // ── Paleta de colores corporativa ─────────────────────────────────────────
    /** Verde primario de MoneyMaster (#006C4C) */
    private static final DeviceRgb COLOR_PRIMARIO  = new DeviceRgb(0x00, 0x6C, 0x4C);
    /** Verde claro para filas alternas */
    private static final DeviceRgb COLOR_FILA_PAR  = new DeviceRgb(0xE8, 0xF5, 0xE9);
    /** Rojo para gastos (#C62828) */
    private static final DeviceRgb COLOR_GASTO     = new DeviceRgb(0xC6, 0x28, 0x28);
    /** Verde para ingresos (#2E7D32) */
    private static final DeviceRgb COLOR_INGRESO   = new DeviceRgb(0x2E, 0x7D, 0x32);
    /** Gris claro para cabecera de tabla */
    private static final DeviceRgb COLOR_CABECERA  = new DeviceRgb(0x37, 0x47, 0x4F);
    /** Gris muy claro para footer */
    private static final DeviceRgb COLOR_FOOTER    = new DeviceRgb(0x9E, 0x9E, 0x9E);

    // ── Formatos ──────────────────────────────────────────────────────────────
    private static final String FORMATO_FECHA      = "dd/MM/yyyy";
    private static final String FORMATO_FECHA_HORA = "dd/MM/yyyy HH:mm";

    // ─── Constructor privado ──────────────────────────────────────────────────
    private PdfExporter() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar.");
    }

    // =========================================================================
    // API pública principal
    // =========================================================================

    /**
     * Genera un informe PDF completo con gastos, ingresos, resumen y gráfico opcional.
     *
     * @param context           Contexto de la aplicación.
     * @param gastos            Lista de GastoPersonal del período.
     * @param ingresos          Lista de IngresoPersonal del período.
     * @param categoriasGasto   Mapa ID → nombre para categorías de gasto.
     * @param categoriasIngreso Mapa ID → nombre para categorías de ingreso.
     * @param pieChartBitmap    Bitmap del PieChart (puede ser null — sección omitida).
     * @param periodoTexto      Texto legible del período (ej: "Marzo 2025", "Todo el historial").
     * @param prefijo           Nombre base del archivo sin extensión (ej: "informe_marzo").
     * @return                  URI content:// lista para compartir, o null si hubo error.
     */
    public static Uri exportar(Context context,
                               List<GastoPersonal> gastos,
                               List<IngresoPersonal> ingresos,
                               Map<Integer, String> categoriasGasto,
                               Map<Integer, String> categoriasIngreso,
                               Bitmap pieChartBitmap,
                               String periodoTexto,
                               String prefijo) {

        File archivo = ExportUtils.crearArchivoExportacion(context, prefijo, "pdf");

        try (FileOutputStream fos = new FileOutputStream(archivo)) {

            PdfWriter   writer   = new PdfWriter(fos);
            PdfDocument pdfDoc   = new PdfDocument(writer);
            Document    document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(36, 36, 54, 36); // top, right, bottom, left (en puntos)

            // ── Añadir footer a cada página ───────────────────────────────────
            agregarFooterCadaPagina(pdfDoc, document);

            // ── 1. Cabecera ───────────────────────────────────────────────────
            agregarCabecera(document, periodoTexto);

            // ── 2. Resumen numérico ───────────────────────────────────────────
            double totalGastos   = calcularTotal(gastos);
            double totalIngresos = calcularTotal(ingresos);
            agregarResumen(document, totalGastos, totalIngresos);

            // ── 3. Gráfico embebido (opcional) ────────────────────────────────
            if (pieChartBitmap != null) {
                agregarGrafico(document, pieChartBitmap);
            }

            // ── 4. Tabla de movimientos ───────────────────────────────────────
            agregarTablaMovimientos(document, gastos, ingresos,
                    categoriasGasto, categoriasIngreso);

            document.close();

            Log.d(TAG, "PDF generado: " + archivo.getAbsolutePath()
                    + " — gastos: " + (gastos != null ? gastos.size() : 0)
                    + ", ingresos: " + (ingresos != null ? ingresos.size() : 0));

        } catch (IOException e) {
            Log.e(TAG, "Error generando PDF", e);
            return null;
        }

        return ExportUtils.getUriParaCompartir(context, archivo);
    }

    // =========================================================================
    // Secciones del documento
    // =========================================================================

    /**
     * Sección 1 — Cabecera con título, subtítulo de período y línea divisoria.
     */
    private static void agregarCabecera(Document doc, String periodoTexto) {
        // Título principal
        Paragraph titulo = new Paragraph("MoneyMaster")
                .setFontSize(28)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4);
        doc.add(titulo);

        // Subtítulo: tipo de informe
        Paragraph subtitulo = new Paragraph("Informe de gastos e ingresos")
                .setFontSize(13)
                .setFontColor(COLOR_CABECERA)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4);
        doc.add(subtitulo);

        // Período
        Paragraph periodo = new Paragraph("Período: " + periodoTexto)
                .setFontSize(11)
                .setFontColor(COLOR_FOOTER)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16);
        doc.add(periodo);

        // Línea divisoria
        doc.add(new Paragraph()
                .setBorderBottom(new SolidBorder(COLOR_PRIMARIO, 1.5f))
                .setMarginBottom(16));
    }

    /**
     * Sección 2 — Tabla de resumen con 3 columnas: gastos, ingresos, balance.
     */
    private static void agregarResumen(Document doc,
                                       double totalGastos,
                                       double totalIngresos) {
        doc.add(new Paragraph("Resumen del período")
                .setFontSize(13)
                .setBold()
                .setFontColor(COLOR_CABECERA)
                .setMarginBottom(6));

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // Fila de etiquetas
        tabla.addCell(celdaResumenEtiqueta("Total Gastos"));
        tabla.addCell(celdaResumenEtiqueta("Total Ingresos"));
        tabla.addCell(celdaResumenEtiqueta("Balance"));

        // Fila de valores
        double balance = totalIngresos - totalGastos;
        tabla.addCell(celdaResumenValor(formatearEuros(totalGastos), COLOR_GASTO));
        tabla.addCell(celdaResumenValor(formatearEuros(totalIngresos), COLOR_INGRESO));
        tabla.addCell(celdaResumenValor(
                formatearEuros(balance),
                balance >= 0 ? COLOR_INGRESO : COLOR_GASTO));

        doc.add(tabla);
    }

    /**
     * Sección 3 — Imagen del PieChart embebida, escalada al ancho disponible.
     *
     * @param doc             Documento iText7.
     * @param pieChartBitmap  Bitmap del PieChart capturado desde la vista.
     */
    private static void agregarGrafico(Document doc, Bitmap pieChartBitmap) {
        try {
            doc.add(new Paragraph("Distribución por categorías")
                    .setFontSize(13)
                    .setBold()
                    .setFontColor(COLOR_CABECERA)
                    .setMarginBottom(6));

            // Convertir Bitmap a byte[] PNG para iText7
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pieChartBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            Image imagen = new Image(ImageDataFactory.create(imageBytes))
                    .setWidth(UnitValue.createPercentValue(80))
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                    .setMarginBottom(20);

            doc.add(imagen);
        } catch (Exception e) {
            Log.w(TAG, "No se pudo embeber el gráfico en el PDF", e);
            // Si falla el gráfico, el PDF se genera igualmente sin él
        }
    }

    /**
     * Sección 4 — Tabla de movimientos con cabecera coloreada y filas alternas.
     * Columnas: Fecha | Tipo | Categoría | Descripción | Cantidad
     */
    private static void agregarTablaMovimientos(Document doc,
                                                List<GastoPersonal> gastos,
                                                List<IngresoPersonal> ingresos,
                                                Map<Integer, String> catGasto,
                                                Map<Integer, String> catIngreso) {
        int totalFilas = (gastos   != null ? gastos.size()   : 0)
                + (ingresos != null ? ingresos.size() : 0);

        doc.add(new Paragraph("Detalle de movimientos (" + totalFilas + " registros)")
                .setFontSize(13)
                .setBold()
                .setFontColor(COLOR_CABECERA)
                .setMarginBottom(6));

        // Anchos relativos de columnas: Fecha(12) Tipo(10) Categoría(20) Descripción(38) Cantidad(20)
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{12, 10, 20, 38, 20}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // ── Cabecera de la tabla ──────────────────────────────────────────────
        String[] encabezados = {"Fecha", "Tipo", "Categoría", "Descripción", "Cantidad"};
        for (String enc : encabezados) {
            tabla.addHeaderCell(celdaCabecera(enc));
        }

        // ── Filas de gastos ───────────────────────────────────────────────────
        if (gastos != null) {
            for (int i = 0; i < gastos.size(); i++) {
                GastoPersonal g   = gastos.get(i);
                boolean filaPar   = (i % 2 == 0);
                String  categoria = catGasto != null
                        ? catGasto.getOrDefault(g.categoriaId, "Sin categoría")
                        : "Sin categoría";

                tabla.addCell(celdaDato(formatearFecha(g.fecha), filaPar, TextAlignment.CENTER));
                tabla.addCell(celdaDatoColor("Gasto", filaPar, COLOR_GASTO));
                tabla.addCell(celdaDato(categoria, filaPar, TextAlignment.LEFT));
                tabla.addCell(celdaDato(g.descripcion != null ? g.descripcion : "", filaPar, TextAlignment.LEFT));
                tabla.addCell(celdaDato(formatearEuros(g.monto), filaPar, TextAlignment.RIGHT));
            }
        }

        // ── Filas de ingresos ─────────────────────────────────────────────────
        int offsetPar = gastos != null ? gastos.size() : 0;
        if (ingresos != null) {
            for (int i = 0; i < ingresos.size(); i++) {
                IngresoPersonal ing = ingresos.get(i);
                boolean filaPar     = ((i + offsetPar) % 2 == 0);
                String  categoria   = catIngreso != null
                        ? catIngreso.getOrDefault(ing.categoriaId, "Sin categoría")
                        : "Sin categoría";

                tabla.addCell(celdaDato(formatearFecha(ing.fecha), filaPar, TextAlignment.CENTER));
                tabla.addCell(celdaDatoColor("Ingreso", filaPar, COLOR_INGRESO));
                tabla.addCell(celdaDato(categoria, filaPar, TextAlignment.LEFT));
                tabla.addCell(celdaDato(ing.descripcion != null ? ing.descripcion : "", filaPar, TextAlignment.LEFT));
                tabla.addCell(celdaDato(formatearEuros(ing.monto), filaPar, TextAlignment.RIGHT));
            }
        }

        // ── Fila de totales ───────────────────────────────────────────────────
        double totalGastos   = calcularTotal(gastos);
        double totalIngresos = calcularTotal(ingresos);
        tabla.addCell(celdaTotales("TOTALES", 3));
        tabla.addCell(celdaTotales(formatearEuros(totalGastos + totalIngresos), 1)
                .setTextAlignment(TextAlignment.RIGHT));

        doc.add(tabla);
    }

    /**
     * Footer — añade la fecha de generación al pie de cada página.
     * Se hace a nivel de PdfDocument para que aparezca en todas las páginas.
     */
    private static void agregarFooterCadaPagina(PdfDocument pdfDoc, Document document) {
        String textoFooter = "Generado por MoneyMaster el "
                + new SimpleDateFormat(FORMATO_FECHA_HORA, new Locale("es", "ES"))
                .format(new Date());

        pdfDoc.addEventHandler(
                com.itextpdf.kernel.events.PdfDocumentEvent.END_PAGE,
                event -> {
                    com.itextpdf.kernel.events.PdfDocumentEvent docEvent =
                            (com.itextpdf.kernel.events.PdfDocumentEvent) event;
                    com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas =
                            new com.itextpdf.kernel.pdf.canvas.PdfCanvas(
                                    docEvent.getPage());
                    PageSize pageSize = pdfDoc.getDefaultPageSize();

                    // Texto centrado a 20pt desde el borde inferior
                    try {
                        canvas.beginText()
                                .setFontAndSize(
                                        PdfFontFactory.createFont(),
                                        8)
                                .moveText(pageSize.getWidth() / 2 - 80, 20)
                                .showText(textoFooter)
                                .endText()
                                .release();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    // =========================================================================
    // Helpers de celdas
    // =========================================================================

    private static Cell celdaCabecera(String texto) {
        return new Cell()
                .add(new Paragraph(texto)
                        .setBold()
                        .setFontSize(9)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_CABECERA)
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER);
    }

    private static Cell celdaDato(String texto, boolean filaPar, TextAlignment alineacion) {
        return new Cell()
                .add(new Paragraph(texto != null ? texto : "").setFontSize(8))
                .setBackgroundColor(filaPar ? COLOR_FILA_PAR : ColorConstants.WHITE)
                .setPadding(4)
                .setTextAlignment(alineacion)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(0xE0, 0xE0, 0xE0), 0.3f));
    }

    private static Cell celdaDatoColor(String texto, boolean filaPar, DeviceRgb colorTexto) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(8).setBold().setFontColor(colorTexto))
                .setBackgroundColor(filaPar ? COLOR_FILA_PAR : ColorConstants.WHITE)
                .setPadding(4)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(0xE0, 0xE0, 0xE0), 0.3f));
    }

    private static Cell celdaTotales(String texto, int colspan) {
        return new Cell(1, colspan)
                .add(new Paragraph(texto).setFontSize(9).setBold()
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_PRIMARIO)
                .setPadding(5)
                .setBorder(Border.NO_BORDER);
    }

    private static Cell celdaResumenEtiqueta(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(9).setBold()
                        .setFontColor(COLOR_CABECERA)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COLOR_FILA_PAR)
                .setPadding(8)
                .setBorder(new SolidBorder(new DeviceRgb(0xC8, 0xE6, 0xC9), 1));
    }

    private static Cell celdaResumenValor(String texto, DeviceRgb colorTexto) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(14).setBold()
                        .setFontColor(colorTexto)
                        .setTextAlignment(TextAlignment.CENTER))
                .setPadding(10)
                .setBorder(new SolidBorder(new DeviceRgb(0xC8, 0xE6, 0xC9), 1));
    }

    // =========================================================================
    // Helpers de formato
    // =========================================================================

    private static String formatearFecha(long timestampMs) {
        return new SimpleDateFormat(FORMATO_FECHA, new Locale("es", "ES"))
                .format(new Date(timestampMs));
    }

    private static String formatearEuros(double cantidad) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return nf.format(cantidad);
    }

    private static double calcularTotal(List<? extends Object> lista) {
        if (lista == null) return 0.0;
        double total = 0.0;
        for (Object obj : lista) {
            if (obj instanceof GastoPersonal) {
                total += ((GastoPersonal) obj).monto;
            } else if (obj instanceof IngresoPersonal) {
                total += ((IngresoPersonal) obj).monto;
            }
        }
        return total;
    }
}
package com.example.moneymaster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.CategoriaGastoDao;
import com.example.moneymaster.data.dao.CategoriaIngresoDao;
import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.utils.CsvExporter;
import com.example.moneymaster.utils.ExportUtils;
import com.example.moneymaster.utils.PdfExporter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExportFragment — Pantalla de exportación de datos (Sprint 7).
 *
 * Gestiona la exportación a CSV (Card #47) y PDF (Card #48).
 * Toda la I/O de disco se ejecuta en un ExecutorService de hilo único
 * para no bloquear el hilo principal durante la escritura de archivos.
 */
public class ExportFragment extends Fragment {

    // ─── Vistas ───────────────────────────────────────────────────────────────
    private Button      btnExportarCsvMes;
    private Button      btnExportarCsvTotal;
    private Button      btnExportarPdfMes;
    private ProgressBar progressExport;

    // ─── Sesión ───────────────────────────────────────────────────────────────
    private int usuarioId;

    // ─── Hilo de fondo para I/O de disco ─────────────────────────────────────
    // Single thread: garantiza que solo una exportación corre a la vez
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ─── Nombres de meses en español ─────────────────────────────────────────
    private static final String[] MESES_ES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    // =========================================================================
    // Ciclo de vida
    // =========================================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_export, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        leerSesion();
        enlazarVistas(view);
        configurarBotones();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // =========================================================================
    // Inicialización
    // =========================================================================

    private void leerSesion() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MoneyMasterPrefs", Context.MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);
    }

    private void enlazarVistas(View view) {
        btnExportarCsvMes   = view.findViewById(R.id.btn_exportar_csv_mes);
        btnExportarCsvTotal = view.findViewById(R.id.btn_exportar_csv_total);
        btnExportarPdfMes   = view.findViewById(R.id.btn_exportar_pdf_mes);
        progressExport      = view.findViewById(R.id.progress_export);
    }

    private void configurarBotones() {
        btnExportarCsvMes.setOnClickListener(v   -> exportarCsvMesActual());
        btnExportarCsvTotal.setOnClickListener(v -> exportarCsvTodosLosDatos());
        btnExportarPdfMes.setOnClickListener(v   -> exportarPdfMesActual());
    }

    // =========================================================================
    // Exportación CSV — mes actual
    // =========================================================================

    private void exportarCsvMesActual() {
        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        long[] rango    = getRangoMesActual();
        int    mesNum   = Calendar.getInstance().get(Calendar.MONTH);
        int    anioNum  = Calendar.getInstance().get(Calendar.YEAR);
        String prefijo  = "movimientos_"
                + String.format(Locale.US, "%02d", mesNum + 1) + "_" + anioNum;

        mostrarProgreso(true);
        executor.execute(() -> {
            Uri uri = realizarExportacionCsv(rango[0], rango[1], prefijo);
            requireActivity().runOnUiThread(() -> {
                mostrarProgreso(false);
                if (uri != null) compartirArchivo(uri, "text/csv");
                else Toast.makeText(requireContext(),
                        "Error al generar el CSV", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // =========================================================================
    // Exportación CSV — todos los datos
    // =========================================================================

    private void exportarCsvTodosLosDatos() {
        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarProgreso(true);
        executor.execute(() -> {
            Uri uri = realizarExportacionCsvTotal();
            requireActivity().runOnUiThread(() -> {
                mostrarProgreso(false);
                if (uri != null) compartirArchivo(uri, "text/csv");
                else Toast.makeText(requireContext(),
                        "Error al generar el CSV", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // =========================================================================
    // Exportación PDF — mes actual
    // =========================================================================

    private void exportarPdfMesActual() {
        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        long[]  rango       = getRangoMesActual();
        int     mesNum      = Calendar.getInstance().get(Calendar.MONTH);
        int     anioNum     = Calendar.getInstance().get(Calendar.YEAR);
        String  periodoTexto = MESES_ES[mesNum] + " " + anioNum;
        String  prefijo     = "informe_"
                + String.format(Locale.US, "%02d", mesNum + 1) + "_" + anioNum;

        // Capturar el PieChart si está visible (puede devolver null — se omite la sección)
        android.graphics.Bitmap pieChartBitmap = capturarPieChart();

        mostrarProgreso(true);
        executor.execute(() -> {
            Uri uri = realizarExportacionPdf(
                    rango[0], rango[1], periodoTexto, prefijo, pieChartBitmap);
            requireActivity().runOnUiThread(() -> {
                mostrarProgreso(false);
                if (uri != null) compartirArchivo(uri, "application/pdf");
                else Toast.makeText(requireContext(),
                        "Error al generar el PDF", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // =========================================================================
    // Lógica de exportación (hilo de fondo)
    // =========================================================================

    /**
     * Exporta CSV de un rango de fechas.
     * SIEMPRE llamar desde executor — hace I/O de disco.
     */
    private Uri realizarExportacionCsv(long inicio, long fin, String prefijo) {
        Context ctx = requireContext().getApplicationContext();
        ExportUtils.limpiarArchivosAntiguos(ctx);

        AppDatabase db = AppDatabase.getDatabase(ctx);

        List<GastoPersonal>   gastos   = db.gastoPersonalDao()
                .getGastosParaExportacion(usuarioId, inicio, fin);
        List<IngresoPersonal> ingresos = db.ingresoPersonalDao()
                .getIngresosParaExportacion(usuarioId, inicio, fin);

        Map<Integer, String> mapCatGasto   = construirMapaCategorias(db, usuarioId);
        Map<Integer, String> mapCatIngreso = construirMapaCategoriasIngreso(db, usuarioId);

        return CsvExporter.exportarMovimientos(
                ctx, gastos, ingresos, mapCatGasto, mapCatIngreso, prefijo);
    }

    /**
     * Exporta CSV con todos los movimientos del usuario, sin filtro de fecha.
     * SIEMPRE llamar desde executor — hace I/O de disco.
     */
    private Uri realizarExportacionCsvTotal() {
        Context ctx = requireContext().getApplicationContext();
        ExportUtils.limpiarArchivosAntiguos(ctx);

        AppDatabase db = AppDatabase.getDatabase(ctx);

        List<GastoPersonal>   gastos   = db.gastoPersonalDao()
                .getTodosGastosParaExportacion(usuarioId);
        List<IngresoPersonal> ingresos = db.ingresoPersonalDao()
                .getTodosIngresosParaExportacion(usuarioId);

        Map<Integer, String> mapCatGasto   = construirMapaCategorias(db, usuarioId);
        Map<Integer, String> mapCatIngreso = construirMapaCategoriasIngreso(db, usuarioId);

        return CsvExporter.exportarMovimientos(
                ctx, gastos, ingresos, mapCatGasto, mapCatIngreso, "movimientos_completo");
    }

    /**
     * Exporta PDF de un rango de fechas.
     * SIEMPRE llamar desde executor — hace I/O de disco.
     */
    private Uri realizarExportacionPdf(long inicio, long fin,
                                       String periodoTexto, String prefijo,
                                       android.graphics.Bitmap pieChartBitmap) {
        Context ctx = requireContext().getApplicationContext();
        ExportUtils.limpiarArchivosAntiguos(ctx);

        AppDatabase db = AppDatabase.getDatabase(ctx);

        List<GastoPersonal>   gastos   = db.gastoPersonalDao()
                .getGastosParaExportacion(usuarioId, inicio, fin);
        List<IngresoPersonal> ingresos = db.ingresoPersonalDao()
                .getIngresosParaExportacion(usuarioId, inicio, fin);

        Map<Integer, String> mapCatGasto   = construirMapaCategorias(db, usuarioId);
        Map<Integer, String> mapCatIngreso = construirMapaCategoriasIngreso(db, usuarioId);

        return PdfExporter.exportar(ctx, gastos, ingresos,
                mapCatGasto, mapCatIngreso,
                pieChartBitmap, periodoTexto, prefijo);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Devuelve {inicioMes, finMes} como timestamps Unix en ms para el mes actual.
     */
    private long[] getRangoMesActual() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicio = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long fin = cal.getTimeInMillis();
        return new long[]{inicio, fin};
    }

    /**
     * Construye mapa ID → nombre para categorías de gasto.
     * Se carga una sola vez antes de iterar los movimientos.
     */
    private Map<Integer, String> construirMapaCategorias(AppDatabase db, int uid) {
        Map<Integer, String> mapa = new HashMap<>();
        List<CategoriaGasto> lista = db.categoriaGastoDao().getCategoriasParaExportacion(uid);
        if (lista != null) {
            for (CategoriaGasto cat : lista) {
                mapa.put(cat.id, cat.nombre);
            }
        }
        return mapa;
    }

    /**
     * Construye mapa ID → nombre para categorías de ingreso.
     */
    private Map<Integer, String> construirMapaCategoriasIngreso(AppDatabase db, int uid) {
        Map<Integer, String> mapa = new HashMap<>();
        List<CategoriaIngreso> lista = db.categoriaIngresoDao().getCategoriasParaExportacion(uid);
        if (lista != null) {
            for (CategoriaIngreso cat : lista) {
                mapa.put(cat.id, cat.nombre);
            }
        }
        return mapa;
    }

    /**
     * Intenta capturar el PieChart del fragment de estadísticas como Bitmap.
     * Devuelve null si no está visible — PdfExporter omite la sección del gráfico.
     */
    private android.graphics.Bitmap capturarPieChart() {
        try {
            com.github.mikephil.charting.charts.PieChart pieChart =
                    requireActivity().findViewById(R.id.pie_chart);
            if (pieChart != null && pieChart.getVisibility() == View.VISIBLE) {
                pieChart.setDrawingCacheEnabled(true);
                android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(
                        pieChart.getDrawingCache());
                pieChart.setDrawingCacheEnabled(false);
                return bmp;
            }
        } catch (Exception e) {
            // PieChart no accesible — normal si el usuario está en otra pantalla
        }
        return null;
    }

    /**
     * Abre el selector del sistema para compartir el archivo exportado.
     * FLAG_GRANT_READ_URI_PERMISSION es obligatorio para content:// URIs.
     */
    private void compartirArchivo(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Exportar datos"));
    }

    /**
     * Muestra u oculta el spinner y habilita/deshabilita todos los botones.
     * Evita que el usuario lance dos exportaciones simultáneas.
     */
    private void mostrarProgreso(boolean cargando) {
        progressExport.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnExportarCsvMes.setEnabled(!cargando);
        btnExportarCsvTotal.setEnabled(!cargando);
        btnExportarPdfMes.setEnabled(!cargando);
    }
}
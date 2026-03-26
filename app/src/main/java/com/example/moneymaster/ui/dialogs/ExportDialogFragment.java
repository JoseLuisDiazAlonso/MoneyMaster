package com.example.moneymaster.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.utils.CsvExporter;
import com.example.moneymaster.utils.ExportUtils;
import com.example.moneymaster.utils.PdfExporter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExportDialogFragment — Dialog selector de exportación (Card #49, EXPORT-005).
 *
 * Implementado como BottomSheetDialogFragment para seguir las guías de Material Design 3.
 * El BottomSheet sube desde la parte inferior de la pantalla — más natural en móvil
 * que un AlertDialog centrado para selecciones de múltiples opciones.
 *
 * Flujo de usuario:
 *   1. Usuario pulsa "Exportar" en el menú de la Toolbar.
 *   2. Aparece este BottomSheet con dos secciones: Formato y Período.
 *   3. Selecciona formato (CSV / PDF) y período (Mes actual / Año actual / Todo).
 *   4. Pulsa "Exportar" → aparece ProgressBar, botón se deshabilita.
 *   5. La exportación corre en el ExecutorService.
 *   6. Al terminar: Toast de confirmación + selector del sistema para abrir/compartir.
 *
 * Uso desde MainActivity o cualquier Fragment:
 * <pre>
 *   ExportDialogFragment.newInstance()
 *       .show(getSupportFragmentManager(), ExportDialogFragment.TAG);
 * </pre>
 */
public class ExportDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ExportDialogFragment";

    // ─── IDs de los RadioButtons de formato ──────────────────────────────────
    private static final int FORMATO_CSV = 0;
    private static final int FORMATO_PDF = 1;

    // ─── IDs de los RadioButtons de período ──────────────────────────────────
    private static final int PERIODO_MES  = 0;
    private static final int PERIODO_ANIO = 1;
    private static final int PERIODO_TODO = 2;

    // ─── Vistas ───────────────────────────────────────────────────────────────
    private RadioGroup   rgFormato;
    private RadioButton  rbCsv;
    private RadioButton  rbPdf;
    private RadioGroup   rgPeriodo;
    private RadioButton  rbMes;
    private RadioButton  rbAnio;
    private RadioButton  rbTodo;
    private MaterialButton btnExportar;
    private MaterialButton btnCancelar;
    private ProgressBar  progressBar;
    private LinearLayout layoutProgreso;
    private TextView     tvProgresoTexto;

    // ─── Sesión ───────────────────────────────────────────────────────────────
    private int usuarioId;

    // ─── Hilo de fondo ────────────────────────────────────────────────────────
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ─── Nombres de meses ─────────────────────────────────────────────────────
    private static final String[] MESES_ES = {
            "Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    // =========================================================================
    // Factory
    // =========================================================================

    public static ExportDialogFragment newInstance() {
        return new ExportDialogFragment();
    }

    // =========================================================================
    // Ciclo de vida
    // =========================================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_export, container, false);
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
        rgFormato      = view.findViewById(R.id.rg_formato);
        rbCsv          = view.findViewById(R.id.rb_csv);
        rbPdf          = view.findViewById(R.id.rb_pdf);
        rgPeriodo      = view.findViewById(R.id.rg_periodo);
        rbMes          = view.findViewById(R.id.rb_mes_actual);
        rbAnio         = view.findViewById(R.id.rb_anio_actual);
        rbTodo         = view.findViewById(R.id.rb_todo);
        btnExportar    = view.findViewById(R.id.btn_exportar);
        btnCancelar    = view.findViewById(R.id.btn_cancelar);
        progressBar    = view.findViewById(R.id.progress_export_dialog);
        layoutProgreso = view.findViewById(R.id.layout_progreso);
        tvProgresoTexto = view.findViewById(R.id.tv_progreso_texto);
    }

    private void configurarBotones() {
        btnCancelar.setOnClickListener(v -> dismiss());

        btnExportar.setOnClickListener(v -> {
            if (usuarioId == -1) {
                Toast.makeText(requireContext(),
                        "Usuario no identificado", Toast.LENGTH_SHORT).show();
                return;
            }
            iniciarExportacion();
        });
    }

    // =========================================================================
    // Lógica de exportación
    // =========================================================================

    /**
     * Lee las selecciones del usuario, calcula el rango de fechas
     * y lanza la exportación en el executor.
     */
    private void iniciarExportacion() {
        int formato = rgFormato.indexOfChild(
                rgFormato.findViewById(rgFormato.getCheckedRadioButtonId()));
        int periodo = rgPeriodo.indexOfChild(
                rgPeriodo.findViewById(rgPeriodo.getCheckedRadioButtonId()));

        // Calcular rango de timestamps según el período seleccionado
        long[] rango         = calcularRango(periodo);
        String periodoTexto  = obtenerTextoPeriodo(periodo);
        String prefijoBase   = obtenerPrefijo(formato, periodo);

        mostrarProgreso(true, "Generando archivo...");

        executor.execute(() -> {
            Uri uri = null;
            String mimeType;

            // Obtener datos de la BD (en hilo de fondo)
            Context ctx = requireContext().getApplicationContext();
            ExportUtils.limpiarArchivosAntiguos(ctx);
            AppDatabase db = AppDatabase.getDatabase(ctx);

            List<GastoPersonal>   gastos   = obtenerGastos(db, rango);
            List<IngresoPersonal> ingresos = obtenerIngresos(db, rango);
            Map<Integer, String>  mapCatG  = construirMapaGasto(db);
            Map<Integer, String>  mapCatI  = construirMapaIngreso(db);

            if (formato == FORMATO_CSV) {
                uri = CsvExporter.exportarMovimientos(
                        ctx, gastos, ingresos, mapCatG, mapCatI, prefijoBase);
                mimeType = "text/csv";
            } else {
                android.graphics.Bitmap pieChart = capturarPieChart();
                uri = PdfExporter.exportar(
                        ctx, gastos, ingresos, mapCatG, mapCatI,
                        pieChart, periodoTexto, prefijoBase);
                mimeType = "application/pdf";
            }

            final Uri    uriFinal    = uri;
            final String mimeFinal   = mimeType;
            final int    numRegistros = (gastos != null ? gastos.size() : 0)
                    + (ingresos != null ? ingresos.size() : 0);

            requireActivity().runOnUiThread(() -> {
                mostrarProgreso(false, "");
                if (uriFinal != null) {
                    mostrarToastExito(formato, numRegistros);
                    compartirArchivo(uriFinal, mimeFinal);
                    dismiss();
                } else {
                    Toast.makeText(requireContext(),
                            "Error al generar el archivo. Inténtalo de nuevo.",
                            Toast.LENGTH_LONG).show();
                    // No cerramos el dialog para que el usuario pueda reintentar
                }
            });
        });
    }

    // =========================================================================
    // Cálculo de rangos y textos
    // =========================================================================

    /**
     * Devuelve {inicio, fin} en ms según el período seleccionado.
     * Para PERIODO_TODO devuelve {0, Long.MAX_VALUE} — todos los registros.
     */
    private long[] calcularRango(int periodo) {
        Calendar cal = Calendar.getInstance();

        if (periodo == PERIODO_MES) {
            // Primer día del mes actual → primer día del mes siguiente
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long inicio = cal.getTimeInMillis();
            cal.add(Calendar.MONTH, 1);
            return new long[]{inicio, cal.getTimeInMillis()};

        } else if (periodo == PERIODO_ANIO) {
            // Primer día del año actual → primer día del año siguiente
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long inicio = cal.getTimeInMillis();
            cal.add(Calendar.YEAR, 1);
            return new long[]{inicio, cal.getTimeInMillis()};

        } else {
            // Todo: sin filtro de fecha
            return new long[]{0L, Long.MAX_VALUE};
        }
    }

    private String obtenerTextoPeriodo(int periodo) {
        Calendar cal = Calendar.getInstance();
        if (periodo == PERIODO_MES) {
            return MESES_ES[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
        } else if (periodo == PERIODO_ANIO) {
            return "Año " + cal.get(Calendar.YEAR);
        } else {
            return "Historial completo";
        }
    }

    private String obtenerPrefijo(int formato, int periodo) {
        Calendar cal  = Calendar.getInstance();
        String   tipo = (formato == FORMATO_CSV) ? "movimientos" : "informe";

        if (periodo == PERIODO_MES) {
            return tipo + "_" + String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
                    + "_" + cal.get(Calendar.YEAR);
        } else if (periodo == PERIODO_ANIO) {
            return tipo + "_" + cal.get(Calendar.YEAR);
        } else {
            return tipo + "_completo";
        }
    }

    // =========================================================================
    // Acceso a datos (hilo de fondo)
    // =========================================================================

    private List<GastoPersonal> obtenerGastos(AppDatabase db, long[] rango) {
        if (rango[0] == 0L && rango[1] == Long.MAX_VALUE) {
            return db.gastoPersonalDao().getTodosGastosParaExportacion(usuarioId);
        }
        return db.gastoPersonalDao().getGastosParaExportacion(usuarioId, rango[0], rango[1]);
    }

    private List<IngresoPersonal> obtenerIngresos(AppDatabase db, long[] rango) {
        if (rango[0] == 0L && rango[1] == Long.MAX_VALUE) {
            return db.ingresoPersonalDao().getTodosIngresosParaExportacion(usuarioId);
        }
        return db.ingresoPersonalDao().getIngresosParaExportacion(usuarioId, rango[0], rango[1]);
    }

    private Map<Integer, String> construirMapaGasto(AppDatabase db) {
        Map<Integer, String> mapa = new HashMap<>();
        List<CategoriaGasto> lista = db.categoriaGastoDao().getCategoriasParaExportacion(usuarioId);
        if (lista != null) {
            for (CategoriaGasto c : lista) mapa.put(c.id, c.nombre);
        }
        return mapa;
    }

    private Map<Integer, String> construirMapaIngreso(AppDatabase db) {
        Map<Integer, String> mapa = new HashMap<>();
        List<CategoriaIngreso> lista = db.categoriaIngresoDao().getCategoriasParaExportacion(usuarioId);
        if (lista != null) {
            for (CategoriaIngreso c : lista) mapa.put(c.id, c.nombre);
        }
        return mapa;
    }

    // =========================================================================
    // Helpers UI
    // =========================================================================

    /**
     * Muestra/oculta el estado de progreso y bloquea los controles.
     */
    private void mostrarProgreso(boolean cargando, String mensaje) {
        layoutProgreso.setVisibility(cargando ? View.VISIBLE : View.GONE);
        tvProgresoTexto.setText(mensaje);
        btnExportar.setEnabled(!cargando);
        btnCancelar.setEnabled(!cargando);
        rgFormato.setEnabled(!cargando);
        rbCsv.setEnabled(!cargando);
        rbPdf.setEnabled(!cargando);
        rgPeriodo.setEnabled(!cargando);
        rbMes.setEnabled(!cargando);
        rbAnio.setEnabled(!cargando);
        rbTodo.setEnabled(!cargando);
        // Evitar que el usuario cierre el dialog deslizando mientras exporta
        setCancelable(!cargando);
    }

    private void mostrarToastExito(int formato, int numRegistros) {
        String tipoArchivo = (formato == FORMATO_CSV) ? "CSV" : "PDF";
        String mensaje = "✓ " + tipoArchivo + " generado con "
                + numRegistros + " registro" + (numRegistros != 1 ? "s" : "");
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private void compartirArchivo(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Abrir o compartir"));
    }

    /**
     * Intenta capturar el PieChart si está visible.
     * Devuelve null si no está accesible — PdfExporter omite la sección.
     */
    private android.graphics.Bitmap capturarPieChart() {
        try {
            com.github.mikephil.charting.charts.PieChart pieChart =
                    requireActivity().findViewById(R.id.pie_chart);
            if (pieChart != null && pieChart.getVisibility() == View.VISIBLE) {
                pieChart.setDrawingCacheEnabled(true);
                android.graphics.Bitmap bmp =
                        android.graphics.Bitmap.createBitmap(pieChart.getDrawingCache());
                pieChart.setDrawingCacheEnabled(false);
                return bmp;
            }
        } catch (Exception ignored) { }
        return null;
    }
}
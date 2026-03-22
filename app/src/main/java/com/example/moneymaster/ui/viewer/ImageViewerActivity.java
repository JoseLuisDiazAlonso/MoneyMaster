package com.example.moneymaster.ui.viewer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.moneymaster.R;
import com.example.moneymaster.data.model.InfoGastoFoto;
import com.example.moneymaster.databinding.ActivityImageViewerBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Card #35 — Activity visor de imagen completa.
 *
 * Características:
 *   - PhotoView con pinch-to-zoom y double-tap
 *   - Swipe vertical para cerrar (con animación fade-out)
 *   - Botones compartir y eliminar en toolbar
 *   - Panel inferior con info del gasto asociado
 *   - Transición fade/slide de entrada y salida
 *
 * Uso:
 *   ImageViewerActivity.start(context, fotoId, rutaFoto);
 */
public class ImageViewerActivity extends AppCompatActivity {

    // ─── Extras ──────────────────────────────────────────────────────────────
    public static final String EXTRA_FOTO_ID   = "fotoId";
    public static final String EXTRA_RUTA_FOTO = "rutaFoto";

    // Resultado para que el Fragment/Activity llamante sepa si se eliminó
    public static final String EXTRA_FOTO_ELIMINADA = "fotoEliminada";
    public static final int    REQUEST_CODE          = 601;

    private ActivityImageViewerBinding binding;
    private ImageViewerViewModel       viewModel;

    private int    fotoId;
    private String rutaFoto;

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

    // ─── Factory ─────────────────────────────────────────────────────────────

    /**
     * @param fotoId   ID de la FotoRecibo en Room
     * @param rutaFoto Ruta absoluta del archivo — para carga inmediata sin esperar al ViewModel
     */
    public static void start(Context context, int fotoId, String rutaFoto) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(EXTRA_FOTO_ID, fotoId);
        intent.putExtra(EXTRA_RUTA_FOTO, rutaFoto);
        context.startActivity(intent);
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fotoId  = getIntent().getIntExtra(EXTRA_FOTO_ID, -1);
        rutaFoto = getIntent().getStringExtra(EXTRA_RUTA_FOTO);

        if (fotoId == -1 || rutaFoto == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ImageViewerViewModel.class);
        viewModel.init(fotoId);

        configurarToolbar();
        configurarFoto();
        configurarSwipeParaCerrar();
        configurarBotones();
        observarInfo();

        // Animación de entrada
        overridePendingTransition(R.anim.fade_in_slide_up, R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out_slide_down);
    }

    // ─── Toolbar ─────────────────────────────────────────────────────────────

    private void configurarToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── Foto con PhotoView ───────────────────────────────────────────────────

    private void configurarFoto() {
        Glide.with(this)
                .load(rutaFoto)
                .placeholder(R.drawable.ic_receipt_long)
                .error(R.drawable.ic_receipt_long)
                .into(binding.photoView);

        // Doble tap para zoom 2x
        binding.photoView.setMaximumScale(5f);
        binding.photoView.setMediumScale(2.5f);
        binding.photoView.setMinimumScale(1f);
    }

    // ─── Swipe vertical para cerrar ───────────────────────────────────────────

    private void configurarSwipeParaCerrar() {
        binding.photoView.setOnSingleFlingListener((e1, e2, vX, vY) -> {
            // Swipe hacia abajo: velocidad vertical positiva y mayor que horizontal
            if (vY > 1500 && Math.abs(vY) > Math.abs(vX)) {
                finish();
                return true;
            }
            return false;
        });
    }

    // ─── Botones compartir y eliminar ────────────────────────────────────────

    private void configurarBotones() {
        binding.btnCompartir.setOnClickListener(v -> compartirFoto());
        binding.btnEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    private void compartirFoto() {
        File file = new File(rutaFoto);
        if (!file.exists()) return;

        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartir recibo"));
        } catch (IllegalArgumentException e) {
            android.widget.Toast.makeText(this,
                    "No se puede compartir este archivo",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar foto")
                .setMessage("¿Eliminar la foto del recibo?\nEl gasto se mantendrá.")
                .setPositiveButton("Eliminar", (d, w) -> {
                    viewModel.eliminarFoto(fotoId, rutaFoto);
                    // Notificar al llamante que se eliminó
                    Intent result = new Intent();
                    result.putExtra(EXTRA_FOTO_ELIMINADA, true);
                    setResult(RESULT_OK, result);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ─── Info del gasto asociado ──────────────────────────────────────────────

    private void observarInfo() {
        viewModel.getInfoGasto().observe(this, info -> {
            if (info == null) {
                binding.cardInfoGasto.setVisibility(View.GONE);
                return;
            }
            binding.cardInfoGasto.setVisibility(View.VISIBLE);
            mostrarInfoGasto(info);

            // Animación de entrada del panel
            binding.cardInfoGasto.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_up));
        });
    }

    private void mostrarInfoGasto(InfoGastoFoto info) {
        // Monto
        binding.tvMonto.setText(
                String.format(new Locale("es", "ES"), "%.2f €", info.monto));

        // Descripción
        if (info.descripcion != null && !info.descripcion.isEmpty()) {
            binding.tvDescripcion.setText(info.descripcion);
            binding.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            binding.tvDescripcion.setVisibility(View.GONE);
        }

        // Categoría
        binding.tvCategoria.setText(
                info.nombreCategoria != null ? info.nombreCategoria : "Sin categoría");

        // Fecha
        binding.tvFecha.setText(SDF.format(new Date(info.fecha)));

        // Pagado por (solo gastos de grupo)
        if (info.pagadoPor != null) {
            binding.tvPagadoPor.setVisibility(View.VISIBLE);
            binding.tvPagadoPor.setText("Pagó: " + info.pagadoPor);
        } else {
            binding.tvPagadoPor.setVisibility(View.GONE);
        }

        // Tipo badge
        binding.tvTipoGasto.setText(
                info.tipo == InfoGastoFoto.TipoGasto.GRUPO ? "Gasto de grupo" : "Gasto personal");
    }
}
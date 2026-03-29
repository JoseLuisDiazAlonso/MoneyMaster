package com.example.moneymaster.ui.viewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.moneymaster.databinding.ActivityImageViewerBinding;
import com.example.moneymaster.utils.GlideImageLoader;

/**
 * Card #63 – Optimización de imágenes (carga con GlideImageLoader)
 * Card #64 – Manejo de memoria y leaks (cleanup en onDestroy)
 */
public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final String EXTRA_FOTO_ID    = "foto_id";

    private ActivityImageViewerBinding binding;

    // ─────────────────────────────────────────────────────────────────────────
    // Método estático de lanzamiento (usado por MovimientoAdapter y otros)
    // ─────────────────────────────────────────────────────────────────────────

    public static void start(Context context, int fotoId, String imagePath) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(EXTRA_FOTO_ID, fotoId);
        intent.putExtra(EXTRA_IMAGE_PATH, imagePath);
        context.startActivity(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ciclo de vida
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);

        // Card #63: carga completa con thumbnail como placeholder progresivo
        GlideImageLoader.loadFullImage(this, imagePath, binding.photoView);

        setupBotones(imagePath);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // Card #64 FIX LEAK 1: cancelar petición Glide activa
        if (binding != null) {
            Glide.with(this).clear(binding.photoView);
        }

        // Card #64 FIX LEAK 2: anular listeners de PhotoView
        if (binding != null) {
            binding.photoView.setOnMatrixChangeListener(null);
            binding.photoView.setOnPhotoTapListener(null);
            binding.photoView.setOnLongClickListener(null);
            binding.btnCompartir.setOnClickListener(null);
            binding.btnEliminar.setOnClickListener(null);
        }

        // Card #64 FIX LEAK 3: liberar referencia al binding
        binding = null;

        super.onDestroy();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────────────────────

    private void setupBotones(String imagePath) {
        binding.btnCompartir.setOnClickListener(v -> compartirFoto(imagePath));
        binding.btnEliminar.setOnClickListener(v -> confirmarEliminar(imagePath));
    }

    private void compartirFoto(String imagePath) {
        // Lógica de compartir existente en tu proyecto
        // (mantener la que tenías antes sin cambios)
    }

    private void confirmarEliminar(String imagePath) {
        // Lógica de eliminar existente en tu proyecto
        // (mantener la que tenías antes sin cambios)
    }
}
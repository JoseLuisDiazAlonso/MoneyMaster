package com.example.moneymaster.ui.expenses;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.moneymaster.R;
import com.example.moneymaster.data.dao.FotoReciboDao;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.repository.FotoReciboRepository;
import com.example.moneymaster.databinding.ActivityAddExpenseBinding;
import com.example.moneymaster.ui.ViewModel.GastoViewModel;
import com.example.moneymaster.utils.CameraGalleryManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    // ── ViewBinding & ViewModel ──────────────────────────────────────────────
    private ActivityAddExpenseBinding binding;
    private GastoViewModel gastoViewModel;

    // ── Estado del formulario ────────────────────────────────────────────────
    private List<CategoriaGasto> categoriasList        = new ArrayList<>();
    private CategoriaGasto       categoriaSeleccionada = null;
    private final Calendar       fechaSeleccionada     = Calendar.getInstance();
    private int                  usuarioId             = -1;

    // ── Card #30: ruta absoluta de la foto comprimida (null si no hay foto) ─
    private String               rutaFotoActual        = null;
    private CameraGalleryManager cameraGalleryManager;

    // ── ActivityResultLaunchers ──────────────────────────────────────────────
    // IMPORTANTE: deben declararse como campos (antes de onCreate) para que
    // Android los registre en el ciclo de vida correctamente.

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> cameraGalleryManager.handleCameraResult(
                            result.getResultCode() == RESULT_OK));

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> cameraGalleryManager.handleGalleryResult(
                            result.getResultCode() == RESULT_OK,
                            result.getData()));

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usuarioId = getSharedPreferences("MoneyMasterPrefs", MODE_PRIVATE)
                .getInt("userId", -1);

        configurarCameraGalleryManager(); // Card #30 — debe ir antes de los botones
        configurarToolbar();
        configurarViewModels();
        configurarDatePicker();
        configurarBotonFoto();
        configurarBotonEliminarFoto();
        configurarBotonGuardar();

        actualizarCampoFecha();
    }

    // ── Card #30: Configuración del manager ───────────────────────────────────

    private void configurarCameraGalleryManager() {
        cameraGalleryManager = new CameraGalleryManager(
                this, CameraGalleryManager.FILE_PROVIDER_AUTHORITY);

        cameraGalleryManager.setCameraLauncher(cameraLauncher);
        cameraGalleryManager.setGalleryLauncher(galleryLauncher);

        cameraGalleryManager.setCallback(new CameraGalleryManager.Callback() {
            @Override
            public void onImageReady(String absolutePath) {
                // La foto está comprimida y guardada en /files/receipts/
                rutaFotoActual = absolutePath;
                mostrarPreviewFoto(absolutePath);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AddExpenseActivity.this,
                        errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Card #30: Permisos runtime ────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraGalleryManager.handlePermissionResult(requestCode, permissions, grantResults);
    }

    // ── Configuración UI ─────────────────────────────────────────────────────

    private void configurarToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_add_expense);
        }
    }

    private void configurarViewModels() {
        gastoViewModel = new ViewModelProvider(this).get(GastoViewModel.class);

        if (usuarioId != -1) {
            gastoViewModel.setUsuarioId(usuarioId);
        }

        gastoViewModel.categorias.observe(this, categorias -> {
            if (categorias != null) {
                categoriasList = categorias;
                configurarDropdownCategorias(categorias);
            }
        });
    }

    private void configurarDropdownCategorias(List<CategoriaGasto> categorias) {
        List<String> nombres = new ArrayList<>();
        for (CategoriaGasto c : categorias) {
            nombres.add(c.nombre);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_dropdown_item_1line, nombres) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = nombres;
                        results.count  = nombres.size();
                        return results;
                    }
                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };

        binding.actvCategoria.setAdapter(adapter);
        binding.actvCategoria.setOnClickListener(v -> binding.actvCategoria.showDropDown());
        binding.actvCategoria.setOnItemClickListener((parent, view, position, id) -> {
            categoriaSeleccionada = categoriasList.get(position);
            binding.tilCategoria.setError(null);
        });
    }

    private void configurarDatePicker() {
        View.OnClickListener abrirPicker = v -> new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    fechaSeleccionada.set(year, month, day);
                    actualizarCampoFecha();
                },
                fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH),
                fechaSeleccionada.get(Calendar.DAY_OF_MONTH)
        ).show();

        binding.etFecha.setOnClickListener(abrirPicker);
        binding.tilFecha.setEndIconOnClickListener(abrirPicker);
    }

    private void actualizarCampoFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        binding.etFecha.setText(sdf.format(fechaSeleccionada.getTime()));
    }

    private void configurarBotonFoto() {
        // Delega toda la lógica al manager (diálogo + permisos + cámara/galería)
        binding.btnFoto.setOnClickListener(v ->
                cameraGalleryManager.showSelectionDialog());
    }

    private void configurarBotonEliminarFoto() {
        binding.btnEliminarFoto.setOnClickListener(v -> {
            // Opcional: borrar el archivo físico si quieres liberar espacio inmediatamente
            // CameraGalleryManager.deletePhoto(rutaFotoActual);
            rutaFotoActual = null;
            binding.cardFotoPreview.setVisibility(View.GONE);
            binding.btnFoto.setText(R.string.btn_anadir_foto);
        });
    }

    // ── Foto ─────────────────────────────────────────────────────────────────

    private void mostrarPreviewFoto(String absolutePath) {
        binding.cardFotoPreview.setVisibility(View.VISIBLE);
        // Glide acepta String de ruta absoluta directamente
        Glide.with(this).load(absolutePath).centerCrop().into(binding.ivFotoPreview);
        binding.btnFoto.setText(R.string.btn_cambiar_foto);
    }

    // ── Validación y guardado ────────────────────────────────────────────────

    private void configurarBotonGuardar() {
        binding.btnGuardar.setOnClickListener(v -> {
            if (validarFormulario()) guardarGasto();
        });
    }

    private boolean validarFormulario() {
        boolean valido = true;

        String montoStr = binding.etCantidad.getText() != null
                ? binding.etCantidad.getText().toString().trim() : "";

        if (montoStr.isEmpty()) {
            binding.tilCantidad.setError(getString(R.string.error_cantidad_requerida));
            valido = false;
        } else {
            try {
                double monto = Double.parseDouble(montoStr.replace(",", "."));
                if (monto <= 0) {
                    binding.tilCantidad.setError(getString(R.string.error_cantidad_positiva));
                    valido = false;
                } else {
                    binding.tilCantidad.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.tilCantidad.setError(getString(R.string.error_cantidad_invalida));
                valido = false;
            }
        }

        if (categoriaSeleccionada == null) {
            binding.tilCategoria.setError(getString(R.string.error_categoria_requerida));
            valido = false;
        } else {
            binding.tilCategoria.setError(null);
        }

        return valido;
    }

    private void guardarGasto() {
        double monto = Double.parseDouble(
                binding.etCantidad.getText().toString().trim().replace(",", "."));

        String descripcion = binding.etDescripcion.getText() != null
                ? binding.etDescripcion.getText().toString().trim() : "";

        if (rutaFotoActual != null) {
            // Hay foto: insertar FotoRecibo primero → obtener ID → luego insertar gasto
            FotoRecibo foto = new FotoRecibo();
            foto.usuarioId     = usuarioId;
            foto.rutaArchivo   = rutaFotoActual;
            foto.nombreArchivo = new File(rutaFotoActual).getName();
            foto.tamanioBytes  = new File(rutaFotoActual).length();
            foto.fechaCaptura  = System.currentTimeMillis();

            binding.btnGuardar.setEnabled(false);

            FotoReciboRepository fotoReciboRepository = null;
            fotoReciboRepository.insertar(foto, fotoId -> {
                GastoPersonal gasto = buildGasto(monto, descripcion, fotoId);
                gastoViewModel.insertar(gasto);
                finalizarGuardado();
            });

        } else {
            // Sin foto: insertar gasto directamente
            GastoPersonal gasto = buildGasto(monto, descripcion, 0);
            binding.btnGuardar.setEnabled(false);
            gastoViewModel.insertar(gasto);
            finalizarGuardado();
        }
    }

    private GastoPersonal buildGasto(double monto, String descripcion, int fotoId) {
        GastoPersonal gasto = new GastoPersonal();
        gasto.usuarioId     = usuarioId;
        gasto.categoriaId   = categoriaSeleccionada.id;
        gasto.monto         = monto;
        gasto.descripcion   = descripcion.isEmpty() ? null : descripcion;
        gasto.fecha         = fechaSeleccionada.getTimeInMillis();
        gasto.fechaCreacion = System.currentTimeMillis();
        gasto.fotoReciboId  = fotoId > 0 ? fotoId : null;
        return gasto;
    }

    private void finalizarGuardado() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(this, R.string.msg_gasto_guardado, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }, 300);
    }

    // ── Navegación ───────────────────────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
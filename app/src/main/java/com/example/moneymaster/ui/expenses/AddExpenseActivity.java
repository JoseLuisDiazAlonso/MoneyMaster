package com.example.moneymaster.ui.expenses;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.repository.FotoReciboRepository;
import com.example.moneymaster.databinding.ActivityAddExpenseBinding;
import com.example.moneymaster.ui.ViewModel.GastoViewModel;
import com.example.moneymaster.utils.CameraGalleryManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
    private List<CategoriaGasto> categoriasList  = new ArrayList<>();
    private CategoriaGasto categoriaSeleccionada = null;
    private final Calendar fechaSeleccionada     = Calendar.getInstance();
    private int usuarioId                        = -1;

    // ── Foto ─────────────────────────────────────────────────────────────────
    private String rutaFotoActual                = null;
    private CameraGalleryManager cameraGalleryManager;

    // ── ActivityResultLaunchers ──────────────────────────────────────────────
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

        usuarioId = new com.example.moneymaster.utils.SessionManager(this).getUserId();

        configurarCameraGalleryManager();
        configurarToolbar();
        configurarViewModels();
        configurarDatePicker();
        configurarBotonFoto();
        configurarBotonEliminarFoto();
        configurarBotonGuardar();
        actualizarCampoFecha();
    }

    // ── CameraGalleryManager ──────────────────────────────────────────────────

    private void configurarCameraGalleryManager() {
        cameraGalleryManager = new CameraGalleryManager(
                this, CameraGalleryManager.FILE_PROVIDER_AUTHORITY);
        cameraGalleryManager.setCameraLauncher(cameraLauncher);
        cameraGalleryManager.setGalleryLauncher(galleryLauncher);
        cameraGalleryManager.setCallback(new CameraGalleryManager.Callback() {
            @Override
            public void onImageReady(String absolutePath) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraGalleryManager.handlePermissionResult(requestCode, permissions, grantResults);
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private void configurarToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_add_expense);
        }
    }

    // ── ViewModel + categorías ────────────────────────────────────────────────

    private void configurarViewModels() {
        gastoViewModel = new ViewModelProvider(this).get(GastoViewModel.class);
        if (usuarioId != -1) {
            gastoViewModel.setUsuarioId((long) usuarioId);
        }

        // Cargar categorías directamente desde BD en hilo de fondo
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CategoriaGasto> lista = AppDatabase.getDatabase(this)
                    .categoriaGastoDao()
                    .getCategoriasSync((long) usuarioId);
            runOnUiThread(() -> {
                if (lista != null && !lista.isEmpty()) {
                    categoriasList = lista;
                } else {
                    Toast.makeText(this,
                            "No hay categorías disponibles", Toast.LENGTH_LONG).show();
                }
            });
        });

        configurarSelectorCategoria();
    }

    private void configurarSelectorCategoria() {
        binding.etCategoria.setOnClickListener(v -> mostrarDialogCategoria());
        binding.tilCategoria.setEndIconOnClickListener(v -> mostrarDialogCategoria());
    }

    private void mostrarDialogCategoria() {
        if (categoriasList.isEmpty()) {
            Toast.makeText(this, "Cargando categorías...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nombres = new String[categoriasList.size()];
        for (int i = 0; i < categoriasList.size(); i++) {
            nombres[i] = categoriasList.get(i).nombre;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Seleccionar categoría")
                .setItems(nombres, (dialog, which) -> {
                    categoriaSeleccionada = categoriasList.get(which);
                    binding.etCategoria.setText(categoriaSeleccionada.nombre);
                    binding.tilCategoria.setError(null);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── DatePicker ────────────────────────────────────────────────────────────

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

    // ── Foto ──────────────────────────────────────────────────────────────────

    private void configurarBotonFoto() {
        binding.btnFoto.setOnClickListener(v ->
                cameraGalleryManager.showSelectionDialog());
    }

    private void configurarBotonEliminarFoto() {
        binding.btnEliminarFoto.setOnClickListener(v -> {
            rutaFotoActual = null;
            binding.cardFotoPreview.setVisibility(View.GONE);
            binding.btnFoto.setText(R.string.btn_anadir_foto);
        });
    }

    private void mostrarPreviewFoto(String absolutePath) {
        binding.cardFotoPreview.setVisibility(View.VISIBLE);
        Glide.with(this).load(absolutePath).centerCrop().into(binding.ivFotoPreview);
        binding.btnFoto.setText(R.string.btn_cambiar_foto);
    }

    // ── Validación y guardado ─────────────────────────────────────────────────

    private void configurarBotonGuardar() {
        binding.btnGuardar.setOnClickListener(v -> {
            android.util.Log.d("DEBUG_GASTO", "Botón guardar pulsado");
            android.util.Log.d("DEBUG_GASTO", "categoriaSeleccionada=" + categoriaSeleccionada);
            android.util.Log.d("DEBUG_GASTO", "usuarioId=" + usuarioId);
            String montoStr = binding.etCantidad.getText() != null
                    ? binding.etCantidad.getText().toString().trim() : "null";
            android.util.Log.d("DEBUG_GASTO", "monto=" + montoStr);
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
            FotoRecibo foto = new FotoRecibo();
            foto.usuarioId     = usuarioId;
            foto.rutaArchivo   = rutaFotoActual;
            foto.nombreArchivo = new File(rutaFotoActual).getName();
            foto.tamanioBytes  = new File(rutaFotoActual).length();
            foto.fechaCaptura  = System.currentTimeMillis();

            binding.btnGuardar.setEnabled(false);

            FotoReciboRepository fotoReciboRepository =
                    new FotoReciboRepository(getApplication());
            fotoReciboRepository.insertar(foto, fotoId -> {
                GastoPersonal gasto = buildGasto(monto, descripcion, fotoId);
                android.util.Log.d("DEBUG_GASTO", "Insertando gasto: usuarioId=" + gasto.usuarioId
                        + " categoria=" + gasto.categoria_id
                        + " monto=" + gasto.monto
                        + " fecha=" + gasto.fecha);
                gastoViewModel.insertar(gasto);
                finalizarGuardado();
            });

        } else {
            GastoPersonal gasto = buildGasto(monto, descripcion, 0);
            binding.btnGuardar.setEnabled(false);
            gastoViewModel.insertar(gasto);
            finalizarGuardado();
        }
    }

    private GastoPersonal buildGasto(double monto, String descripcion, int fotoId) {
        GastoPersonal gasto = new GastoPersonal();
        gasto.usuarioId    = usuarioId;
        gasto.categoria_id = categoriaSeleccionada.id;
        gasto.monto        = monto;
        gasto.descripcion  = descripcion.isEmpty() ? null : descripcion;
        gasto.fecha        = fechaSeleccionada.getTimeInMillis();
        if (fotoId > 0) {
            gasto.tieneFoto = fotoId;
        }
        return gasto;
    }

    private void finalizarGuardado() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(this, R.string.msg_gasto_guardado, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }, 300);
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
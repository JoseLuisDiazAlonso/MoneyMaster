package com.example.moneymaster.ui.groups;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.data.repository.FotoReciboRepository;
import com.example.moneymaster.data.repository.GrupoRepository;
import com.example.moneymaster.databinding.DialogAddGroupExpenseBinding;
import com.example.moneymaster.utils.CameraGalleryManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BottomSheetDialogFragment para añadir un gasto a un grupo.
 * Card #33: añade soporte de foto de recibo reutilizando CameraGalleryManager.
 */
public class AddGroupExpenseDialog extends BottomSheetDialogFragment {

    public static final String TAG = "add_group_expense";
    private static final String ARG_GRUPO_ID = "grupoId";

    private DialogAddGroupExpenseBinding binding;
    private GrupoRepository              repository;
    private FotoReciboRepository         fotoReciboRepository; // Card #33

    private int  grupoId;
    private long fechaSeleccionada;

    private List<MiembroGrupo>   miembros   = new ArrayList<>();
    private List<CategoriaGasto> categorias = new ArrayList<>();

    private MiembroGrupo   miembroSeleccionado;
    private CategoriaGasto categoriaSeleccionada;

    // Card #33 — ruta de la foto seleccionada (null si no hay foto)
    private String rutaFotoActual = null;
    private CameraGalleryManager cameraGalleryManager;

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

    // ─── Launchers — deben declararse como campos del Fragment ────────────────

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> cameraGalleryManager.handleCameraResult(
                            result.getResultCode() == android.app.Activity.RESULT_OK));

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> cameraGalleryManager.handleGalleryResult(
                            result.getResultCode() == android.app.Activity.RESULT_OK,
                            result.getData()));

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static AddGroupExpenseDialog newInstance(int grupoId) {
        AddGroupExpenseDialog d = new AddGroupExpenseDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_GRUPO_ID, grupoId);
        d.setArguments(args);
        return d;
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAddGroupExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        grupoId    = getArguments() != null ? getArguments().getInt(ARG_GRUPO_ID, -1) : -1;
        repository = new GrupoRepository(requireActivity().getApplication());
        fotoReciboRepository = new FotoReciboRepository(requireActivity().getApplication());

        fechaSeleccionada = System.currentTimeMillis();
        binding.editTextExpenseDate.setText(SDF.format(new Date(fechaSeleccionada)));

        configurarCameraGalleryManager(); // Card #33
        setupDatePicker();
        loadData();
        setupButtons();
    }

    // ─── Card #33: CameraGalleryManager ──────────────────────────────────────

    private void configurarCameraGalleryManager() {
        cameraGalleryManager = new CameraGalleryManager(
                requireActivity(),
                CameraGalleryManager.FILE_PROVIDER_AUTHORITY);

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
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Permisos runtime ─────────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraGalleryManager.handlePermissionResult(requestCode, permissions, grantResults);
    }

    // ─── Date picker ──────────────────────────────────────────────────────────

    private void setupDatePicker() {
        binding.editTextExpenseDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(fechaSeleccionada);
            new DatePickerDialog(requireContext(),
                    (dp, year, month, day) -> {
                        cal.set(year, month, day);
                        fechaSeleccionada = cal.getTimeInMillis();
                        binding.editTextExpenseDate.setText(
                                SDF.format(new Date(fechaSeleccionada)));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    // ─── Carga de datos ───────────────────────────────────────────────────────

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<MiembroGrupo>   listaMiembros  = db.miembroGrupoDao().getMiembrosByGrupoSync(grupoId);
            List<CategoriaGasto> listaCategorias = db.categoriaGastoDao().getCategoriasSync(0);
            requireActivity().runOnUiThread(() -> {
                miembros   = listaMiembros;
                categorias = listaCategorias;
                setupDropdowns();
            });
        });
    }

    // ─── Dropdowns ────────────────────────────────────────────────────────────

    private void setupDropdowns() {
        List<String> nombresMiembros = new ArrayList<>();
        for (MiembroGrupo m : miembros)
            nombresMiembros.add(m.nombre != null ? m.nombre : "Miembro");

        binding.autoCompletePagador.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombresMiembros));
        binding.autoCompletePagador.setOnItemClickListener((p, v, pos, id) -> {
            if (pos < miembros.size()) miembroSeleccionado = miembros.get(pos);
        });

        List<String> nombresCategorias = new ArrayList<>();
        for (CategoriaGasto c : categorias) nombresCategorias.add(c.nombre);

        binding.autoCompleteCategoria.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombresCategorias));
        binding.autoCompleteCategoria.setOnItemClickListener((p, v, pos, id) -> {
            if (pos < categorias.size()) categoriaSeleccionada = categorias.get(pos);
        });
    }

    // ─── Botones ──────────────────────────────────────────────────────────────

    private void setupButtons() {
        binding.buttonCancelar.setOnClickListener(v -> dismiss());
        binding.buttonGuardarGasto.setOnClickListener(v -> validateAndSave());

        // Card #33 — botón foto
        binding.btnFotoGrupo.setOnClickListener(v ->
                cameraGalleryManager.showSelectionDialog());

        // Card #33 — botón eliminar foto
        binding.btnEliminarFotoGrupo.setOnClickListener(v -> {
            rutaFotoActual = null;
            binding.cardFotoPreviewGrupo.setVisibility(View.GONE);
            binding.btnFotoGrupo.setText("Añadir foto del recibo");
        });
    }

    // ─── Preview foto ─────────────────────────────────────────────────────────

    private void mostrarPreviewFoto(String absolutePath) {
        binding.cardFotoPreviewGrupo.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(absolutePath)
                .centerCrop()
                .into(binding.ivFotoPreviewGrupo);
        binding.btnFotoGrupo.setText("Cambiar foto");
    }

    // ─── Validación y guardado ────────────────────────────────────────────────

    private void validateAndSave() {
        String montoStr = binding.editTextMonto.getText() != null
                ? binding.editTextMonto.getText().toString().trim() : "";
        if (TextUtils.isEmpty(montoStr)) {
            binding.inputLayoutMonto.setError("Introduce el monto");
            return;
        }
        double monto;
        try {
            monto = Double.parseDouble(montoStr.replace(",", "."));
        } catch (NumberFormatException e) {
            binding.inputLayoutMonto.setError("Monto no válido");
            return;
        }
        if (monto <= 0) {
            binding.inputLayoutMonto.setError("El monto debe ser mayor que 0");
            return;
        }
        binding.inputLayoutMonto.setError(null);

        if (miembroSeleccionado == null) {
            Snackbar.make(binding.getRoot(), "Selecciona quién pagó",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        String descripcion = binding.editTextDescripcion.getText() != null
                ? binding.editTextDescripcion.getText().toString().trim() : null;

        binding.buttonGuardarGasto.setEnabled(false);

        if (rutaFotoActual != null) {
            // Card #33: insertar FotoRecibo primero → obtener ID → luego insertar gasto
            FotoRecibo foto = new FotoRecibo();
            foto.usuarioId     = 0; // grupos no tienen usuario propietario directo
            foto.rutaArchivo   = rutaFotoActual;
            foto.nombreArchivo = new File(rutaFotoActual).getName();
            foto.tamanioBytes  = new File(rutaFotoActual).length();
            foto.fechaCaptura  = System.currentTimeMillis();

            fotoReciboRepository.insertar(foto, fotoId -> {
                GastoGrupo gasto = buildGasto(monto, descripcion, fotoId);
                repository.insertarGasto(gasto, id -> dismiss());
            });
        } else {
            GastoGrupo gasto = buildGasto(monto, descripcion, 0);
            repository.insertarGasto(gasto, id -> dismiss());
        }
    }

    private GastoGrupo buildGasto(double monto, String descripcion, int fotoId) {
        GastoGrupo gasto      = new GastoGrupo();
        gasto.grupoId         = grupoId;
        gasto.monto           = monto;
        gasto.descripcion     = TextUtils.isEmpty(descripcion) ? null : descripcion;
        gasto.pagadoPorId     = miembroSeleccionado.id;
        gasto.pagadoPorNombre = miembroSeleccionado.nombre;
        gasto.categoriaId     = categoriaSeleccionada != null ? categoriaSeleccionada.id : 1;
        gasto.fecha           = fechaSeleccionada;
        gasto.fechaCreacion   = System.currentTimeMillis();
        gasto.dividirIgual    = 1;
        gasto.fotoReciboId    = fotoId > 0 ? fotoId : null;
        return gasto;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
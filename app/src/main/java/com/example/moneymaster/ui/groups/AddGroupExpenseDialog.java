package com.example.moneymaster.ui.groups;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.repository.GrupoRepository;
import com.example.moneymaster.databinding.DialogAddGroupExpenseBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddGroupExpenseDialog extends BottomSheetDialogFragment {

    public static final String TAG = "add_group_expense";
    private static final String ARG_GRUPO_ID = "grupoId";

    private DialogAddGroupExpenseBinding binding;
    private GrupoRepository              repository;

    private int  grupoId;
    private long fechaSeleccionada;

    private List<CategoriaGasto> categorias = new ArrayList<>();
    private CategoriaGasto categoriaSeleccionada;

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

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

        fechaSeleccionada = System.currentTimeMillis();
        binding.editTextExpenseDate.setText(SDF.format(new Date(fechaSeleccionada)));

        setupDatePicker();
        loadData();
        setupButtons();
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
            List<CategoriaGasto> listaCategorias = db.categoriaGastoDao().getCategoriasSync(0);
            requireActivity().runOnUiThread(() -> {
                categorias = listaCategorias;
                setupDropdownCategoria();
            });
        });
    }

    // ─── Dropdown categoría ───────────────────────────────────────────────────

    private void setupDropdownCategoria() {
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

        String pagadorNombre = binding.editTextPagador.getText() != null
                ? binding.editTextPagador.getText().toString().trim() : "";
        if (TextUtils.isEmpty(pagadorNombre)) {
            binding.inputLayoutPagador.setError("Introduce el nombre de quien pagó");
            return;
        }
        binding.inputLayoutPagador.setError(null);

        String descripcion = binding.editTextDescripcion.getText() != null
                ? binding.editTextDescripcion.getText().toString().trim() : null;

        binding.buttonGuardarGasto.setEnabled(false);

        GastoGrupo gasto = buildGasto(monto, descripcion, pagadorNombre);
        repository.insertarGasto(gasto, id -> dismiss());
    }

    private GastoGrupo buildGasto(double monto, String descripcion, String pagadorNombre) {
        GastoGrupo gasto      = new GastoGrupo();
        gasto.grupoId         = grupoId;
        gasto.monto           = monto;
        gasto.descripcion     = TextUtils.isEmpty(descripcion) ? null : descripcion;
        gasto.pagadoPorId     = 0;
        gasto.pagadoPorNombre = pagadorNombre;
        gasto.categoria_id    = categoriaSeleccionada != null ? categoriaSeleccionada.id : 1;
        gasto.fecha           = fechaSeleccionada;
        gasto.dividirIgual    = 1;
        return gasto;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
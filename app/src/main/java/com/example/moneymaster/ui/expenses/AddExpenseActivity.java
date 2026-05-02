package com.example.moneymaster.ui.expenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.databinding.ActivityAddExpenseBinding;
import com.example.moneymaster.ui.ViewModel.GastoViewModel;
import com.example.moneymaster.ui.categories.CategoryAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private ActivityAddExpenseBinding binding;
    private GastoViewModel gastoViewModel;

    private List<CategoriaGasto> categoriasList  = new ArrayList<>();
    private CategoriaGasto categoriaSeleccionada = null;
    private final Calendar fechaSeleccionada     = Calendar.getInstance();
    private int usuarioId                        = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usuarioId = new com.example.moneymaster.utils.SessionManager(this).getUserId();

        configurarToolbar();
        configurarViewModels();
        configurarDatePicker();
        configurarBotonGuardar();
        actualizarCampoFecha();
    }

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
            gastoViewModel.setUsuarioId((long) usuarioId);
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CategoriaGasto> lista = AppDatabase.getDatabase(this)
                    .categoriaGastoDao()
                    .getCategoriasSync((long) usuarioId);
            runOnUiThread(() -> {
                if (lista != null && !lista.isEmpty()) {
                    categoriasList = lista;
                } else {
                    Toast.makeText(this,
                            getString(R.string.sin_categorias), Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, getString(R.string.cargando), Toast.LENGTH_SHORT).show();
            return;
        }

        // FIX: resolver claves cat_* al nombre traducido antes de mostrar
        String[] nombres = new String[categoriasList.size()];
        for (int i = 0; i < categoriasList.size(); i++) {
            nombres[i] = CategoryAdapter.resolverNombre(this, categoriasList.get(i).nombre);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.hint_categoria)
                .setItems(nombres, (dialog, which) -> {
                    categoriaSeleccionada = categoriasList.get(which);
                    // FIX: mostrar nombre traducido en el campo
                    binding.etCategoria.setText(
                            CategoryAdapter.resolverNombre(this, categoriaSeleccionada.nombre));
                    binding.tilCategoria.setError(null);
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        binding.etFecha.setText(sdf.format(fechaSeleccionada.getTime()));
    }

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

        GastoPersonal gasto = buildGasto(monto, descripcion);
        binding.btnGuardar.setEnabled(false);
        gastoViewModel.insertar(gasto);
        finalizarGuardado();
    }

    private GastoPersonal buildGasto(double monto, String descripcion) {
        GastoPersonal gasto = new GastoPersonal();
        gasto.usuarioId    = usuarioId;
        gasto.categoria_id = categoriaSeleccionada.id;
        gasto.monto        = monto;
        gasto.descripcion  = descripcion.isEmpty() ? null : descripcion;
        gasto.fecha        = fechaSeleccionada.getTimeInMillis();
        return gasto;
    }

    private void finalizarGuardado() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(this, R.string.msg_gasto_guardado, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }, 300);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
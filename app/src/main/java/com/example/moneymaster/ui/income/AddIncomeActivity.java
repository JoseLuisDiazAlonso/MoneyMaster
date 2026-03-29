package com.example.moneymaster.ui.income;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.databinding.ActivityAddIncomeBinding;
import com.example.moneymaster.ui.adapters.IncomeDropdownAdapter;
import com.example.moneymaster.utils.SessionManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {

    private ActivityAddIncomeBinding binding;
    private AddIncomeViewModel       viewModel;
    private IncomeDropdownAdapter    categoryAdapter;

    private CategoriaIngreso selectedCategory     = null;
    private long             selectedDateTimestamp = 0;
    private long             usuarioId             = -1;

    private static final String DATE_DISPLAY_FORMAT = "dd MMM yyyy";

    // ═════════════════════════════════════════════════════════════════════════
    //  CICLO DE VIDA
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddIncomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_income_title);
        }

        usuarioId = new SessionManager(this).getUserId();

        AddIncomeViewModelFactory factory =
                new AddIncomeViewModelFactory(getApplication(), usuarioId);
        viewModel = new ViewModelProvider(this, factory).get(AddIncomeViewModel.class);

        setupCategoryDropdown();
        setupDatePicker();
        setupSaveButton();
        observeCategories();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CONFIGURACIÓN DE COMPONENTES
    // ═════════════════════════════════════════════════════════════════════════

    private void setupCategoryDropdown() {
        categoryAdapter = new IncomeDropdownAdapter(this);
        binding.autoCompleteCategory.setAdapter(categoryAdapter);
        binding.autoCompleteCategory.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectedCategory = categoryAdapter.getItem(position);
                    binding.tilCategory.setError(null);
                });
    }

    private void setupDatePicker() {
        binding.etFecha.setFocusable(false);
        binding.etFecha.setClickable(true);

        View.OnClickListener showPicker = v -> {
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build();

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.select_date)
                    .setCalendarConstraints(constraints)
                    .setSelection(selectedDateTimestamp > 0
                            ? selectedDateTimestamp * 1000L
                            : MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            picker.addOnPositiveButtonClickListener(selectionMs -> {
                selectedDateTimestamp = selectionMs / 1000L;
                String formatted = new SimpleDateFormat(DATE_DISPLAY_FORMAT,
                        new Locale("es", "ES")).format(new Date(selectionMs));
                binding.etFecha.setText(formatted);
                binding.tilFecha.setError(null);
            });

            picker.show(getSupportFragmentManager(), "DATE_PICKER_INCOME");
        };

        binding.etFecha.setOnClickListener(showPicker);
        binding.tilFecha.setEndIconOnClickListener(showPicker);
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            if (validateForm()) saveIncome();
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LIVEDATA
    // ═════════════════════════════════════════════════════════════════════════

    private void observeCategories() {
        viewModel.getCategorias().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.updateCategories(categories);
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  VALIDACIÓN
    // ═════════════════════════════════════════════════════════════════════════

    private boolean validateForm() {
        boolean valid = true;

        String importeStr = binding.etImporte.getText() != null
                ? binding.etImporte.getText().toString().trim() : "";
        if (TextUtils.isEmpty(importeStr)) {
            binding.tilImporte.setError(getString(R.string.error_amount_required));
            valid = false;
        } else {
            try {
                double importe = Double.parseDouble(importeStr.replace(",", "."));
                if (importe <= 0) {
                    binding.tilImporte.setError(getString(R.string.error_amount_positive));
                    valid = false;
                } else {
                    binding.tilImporte.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.tilImporte.setError(getString(R.string.error_amount_invalid));
                valid = false;
            }
        }

        if (selectedCategory == null) {
            binding.tilCategory.setError(getString(R.string.error_category_required));
            valid = false;
        } else {
            binding.tilCategory.setError(null);
        }

        if (selectedDateTimestamp == 0) {
            binding.tilFecha.setError(getString(R.string.error_date_required));
            valid = false;
        } else {
            binding.tilFecha.setError(null);
        }

        return valid;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  GUARDADO
    // ═════════════════════════════════════════════════════════════════════════

    private void saveIncome() {
        IngresoPersonal ingreso = new IngresoPersonal();
        ingreso.usuarioId    = (int) usuarioId;
        ingreso.categoria_id = selectedCategory.id;
        ingreso.monto        = Double.parseDouble(
                binding.etImporte.getText().toString().trim().replace(",", "."));
        ingreso.descripcion  = binding.etDescripcion.getText() != null
                ? binding.etDescripcion.getText().toString().trim() : "";
        ingreso.fecha        = selectedDateTimestamp * 1000L;

        binding.btnSave.setEnabled(false);

        // Corrección Card #62: usa AddIncomeViewModel.SaveCallback
        // en lugar del eliminado IngresoPersonalRepository.SaveCallback
        viewModel.insertIngreso(ingreso, new AddIncomeViewModel.SaveCallback() {
            @Override
            public void onSuccess() {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                binding.btnSave.setEnabled(true);
                Snackbar.make(binding.getRoot(),
                        R.string.error_save_income, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
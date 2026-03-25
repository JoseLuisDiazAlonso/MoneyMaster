package com.example.moneymaster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.databinding.FragmentStatisticsCustomBinding;
import com.example.moneymaster.ui.ViewModel.StatisticsViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Pestaña "Personalizado" del fragmento de Estadísticas.
 *
 * CONTENIDO:
 *  1. Selector de rango de fechas con MaterialDateRangePicker
 *  2. Botón "Aplicar" para cargar los datos del rango seleccionado
 *  3. Tarjetas de resumen: Total Gastado · Total Ingresado · Balance
 *  4. Lista textual de los movimientos del período (sin gráfico complejo,
 *     ya que el rango puede abarcar semanas o años)
 *
 * El selector devuelve timestamps en milisegundos (UTC).
 * El ViewModel espera timestamps en segundos; hacemos la conversión aquí.
 */
public class StatisticsCustomFragment extends Fragment {

    private FragmentStatisticsCustomBinding binding;
    private StatisticsViewModel viewModel;

    // Timestamps seleccionados en MILISEGUNDOS (formato MaterialDatePicker)
    private long startMs = 0L;
    private long endMs   = 0L;

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsCustomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        setupDateRangePicker();
        setupApplyButton();
        observeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Setup
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Abre el MaterialDateRangePicker al pulsar el campo de fechas.
     * El picker restringe la selección al pasado (no se pueden seleccionar
     * fechas futuras) para evitar rangos sin datos.
     */
    private void setupDateRangePicker() {
        binding.btnSelectRange.setOnClickListener(v -> {

            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build();

            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Selecciona un rango de fechas")
                            .setCalendarConstraints(constraints);

            // Restaurar selección previa si existe
            if (startMs > 0 && endMs > 0) {
                builder.setSelection(new androidx.core.util.Pair<>(startMs, endMs));
            }

            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();

            picker.addOnPositiveButtonClickListener(selection -> {
                startMs = selection.first  != null ? selection.first  : 0L;
                endMs   = selection.second != null ? selection.second : 0L;
                updateDateLabel();
                binding.btnApply.setEnabled(startMs > 0 && endMs > 0);
            });

            picker.show(getParentFragmentManager(), "date_range_picker");
        });
    }

    private void setupApplyButton() {
        binding.btnApply.setEnabled(false);
        binding.btnApply.setOnClickListener(v -> {
            if (startMs > 0 && endMs > 0) {
                // Convertir ms → segundos para el ViewModel
                long startSec = startMs / 1000L;
                // endMs apunta al inicio del día; sumar 86399 s para incluir todo el día final
                long endSec   = (endMs / 1000L) + 86399L;
                viewModel.loadCustomRangeData(startSec, endSec);
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Observadores
    // ──────────────────────────────────────────────────────────────────────────

    private void observeData() {
        boolean b = viewModel.getGastosCustom() != null && false;// guard null inicial

        // El ViewModel puede tener null si aún no se ha aplicado ningún rango
        // Observamos desde que se cargan por primera vez
        if (viewModel.getGastosCustom() != null) {
            viewModel.getGastosCustom().observe(getViewLifecycleOwner(), this::updateSummary);
        }
        if (viewModel.getIngresosCustom() != null) {
            viewModel.getIngresosCustom().observe(getViewLifecycleOwner(),
                    ingresos -> recalcularResumen(
                            viewModel.getGastosCustom() != null
                                    ? viewModel.getGastosCustom().getValue()
                                    : null,
                            ingresos));
        }
    }

    /**
     * Se llama cuando cambian los gastos del rango personalizado.
     * Recalcula también el resumen total con los ingresos disponibles.
     */
    private void updateSummary(List<GastoPersonal> gastos) {
        List<IngresoPersonal> ingresos = viewModel.getIngresosCustom() != null
                ? viewModel.getIngresosCustom().getValue()
                : null;
        recalcularResumen(gastos, ingresos);
    }

    private void recalcularResumen(@Nullable List<GastoPersonal> gastos,
                                   @Nullable List<IngresoPersonal> ingresos) {
        double totalGastos = 0;
        double totalIngresos = 0;
        int numMovimientos = 0;

        if (gastos != null) {
            for (GastoPersonal g : gastos) { totalGastos += g.monto; numMovimientos++; }
        }
        if (ingresos != null) {
            for (IngresoPersonal i : ingresos) { totalIngresos += i.monto; numMovimientos++; }
        }

        binding.tvTotalGastadoCustom.setText(formatCurrency(totalGastos));
        binding.tvTotalIngresadoCustom.setText(formatCurrency(totalIngresos));

        double balance = totalIngresos - totalGastos;
        binding.tvBalanceCustom.setText(formatCurrency(balance));
        binding.tvBalanceCustom.setTextColor(balance >= 0
                ? android.graphics.Color.parseColor("#2E7D32")
                : android.graphics.Color.parseColor("#B3261E"));

        binding.tvNumMovimientos.setText(numMovimientos + " movimientos");

        // Mostrar estado vacío si no hay datos
        binding.groupResultados.setVisibility(numMovimientos > 0 ? View.VISIBLE : View.GONE);
        binding.tvEmpty.setVisibility(numMovimientos == 0 ? View.VISIBLE : View.GONE);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void updateDateLabel() {
        String inicio = SDF.format(new Date(startMs));
        String fin    = SDF.format(new Date(endMs));
        binding.tvRangeLabel.setText(inicio + "  →  " + fin);
    }

    private String formatCurrency(double value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return nf.format(value);
    }
}
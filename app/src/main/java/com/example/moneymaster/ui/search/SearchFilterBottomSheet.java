package com.example.moneymaster.ui.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.databinding.FragmentSearchFiltersBinding;

import java.util.Calendar;
import java.util.List;

/**
 * SearchFilterBottomSheet — Card #58
 *
 * Bottom Sheet con filtros avanzados:
 *  - Chips de categoría (generados dinámicamente desde la lista de categorías)
 *  - Rango de precio (min/max)
 *  - Chips de período (Hoy, Esta semana, Este mes, Este año)
 *
 * Uso:
 *   SearchFilterBottomSheet sheet = SearchFilterBottomSheet.newInstance(filtroActual, categorias);
 *   sheet.setOnFiltroAplicadoListener(filtro -> busquedaViewModel.aplicarFiltro(filtro));
 *   sheet.show(getChildFragmentManager(), SearchFilterBottomSheet.TAG);
 */
public class SearchFilterBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "SearchFilterBottomSheet";

    public interface OnFiltroAplicadoListener {
        void onFiltroAplicado(FiltroGasto filtro);
    }

    private FragmentSearchFiltersBinding binding;
    private OnFiltroAplicadoListener     listener;
    private FiltroGasto                  filtroInicial;
    private List<CategoriaGasto>         categorias;

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static SearchFilterBottomSheet newInstance(FiltroGasto filtroActual,
                                                      List<CategoriaGasto> categorias) {
        SearchFilterBottomSheet sheet = new SearchFilterBottomSheet();
        sheet.filtroInicial = filtroActual != null ? filtroActual : FiltroGasto.empty();
        sheet.categorias    = categorias;
        return sheet;
    }

    public void setOnFiltroAplicadoListener(OnFiltroAplicadoListener l) {
        this.listener = l;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchFiltersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        construirChipsCategorias();
        restaurarEstadoInicial();
        setupBotones();
    }

    // ─── Chips de categorías ──────────────────────────────────────────────────

    private void construirChipsCategorias() {
        if (categorias == null || categorias.isEmpty()) return;

        binding.chipGroupCategorias.removeAllViews();

        for (CategoriaGasto cat : categorias) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat.nombre);
            chip.setTag(cat.id);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);

            // Pre-seleccionar si coincide con el filtro actual
            if (filtroInicial.categoriaId == cat.id) {
                chip.setChecked(true);
            }

            binding.chipGroupCategorias.addView(chip);
        }
    }

    // ─── Restaurar estado inicial ─────────────────────────────────────────────

    private void restaurarEstadoInicial() {
        // Monto
        if (filtroInicial.montoMin != -1) {
            binding.etMontoMin.setText(String.valueOf((int) filtroInicial.montoMin));
        }
        if (filtroInicial.montoMax != -1) {
            binding.etMontoMax.setText(String.valueOf((int) filtroInicial.montoMax));
        }

        // Período — detectar qué chip corresponde al rango actual
        // (no se restaura automáticamente porque el rango exacto puede no
        //  coincidir con ningún chip predefinido)
    }

    // ─── Botones ──────────────────────────────────────────────────────────────

    private void setupBotones() {
        // Chips de período → calcular rango de fechas
        binding.chipPeriodoHoy.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) desmarcarOtrosPeriodos(btn.getId());
        });
        binding.chipPeriodoSemana.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) desmarcarOtrosPeriodos(btn.getId());
        });
        binding.chipPeriodoMes.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) desmarcarOtrosPeriodos(btn.getId());
        });
        binding.chipPeriodoAnio.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) desmarcarOtrosPeriodos(btn.getId());
        });

        // Limpiar
        binding.btnLimpiarFiltros.setOnClickListener(v -> {
            binding.chipGroupCategorias.clearCheck();
            binding.chipGroupPeriodo.clearCheck();
            binding.etMontoMin.setText("");
            binding.etMontoMax.setText("");
        });

        // Aplicar
        binding.btnAplicarFiltros.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFiltroAplicado(construirFiltro());
            }
            dismiss();
        });
    }

    private void desmarcarOtrosPeriodos(int checkedId) {
        int[] ids = {
                R.id.chip_periodo_hoy,
                R.id.chip_periodo_semana,
                R.id.chip_periodo_mes,
                R.id.chip_periodo_anio
        };
        for (int id : ids) {
            if (id != checkedId) {
                Chip c = binding.getRoot().findViewById(id);
                if (c != null) c.setChecked(false);
            }
        }
    }

    // ─── Construir FiltroGasto desde el estado de la UI ──────────────────────

    private FiltroGasto construirFiltro() {
        FiltroGasto.Builder builder = new FiltroGasto.Builder()
                .query(filtroInicial.query); // conservar el texto de búsqueda

        // Categoría seleccionada
        int checkedCatId = binding.chipGroupCategorias.getCheckedChipId();
        if (checkedCatId != View.NO_ID) {
            Chip chip = binding.chipGroupCategorias.findViewById(checkedCatId);
            if (chip != null && chip.getTag() instanceof Integer) {
                builder.categoriaId((Integer) chip.getTag());
            }
        }

        // Rango de monto
        String minStr = binding.etMontoMin.getText() != null
                ? binding.etMontoMin.getText().toString().trim() : "";
        String maxStr = binding.etMontoMax.getText() != null
                ? binding.etMontoMax.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(minStr)) {
            try { builder.montoMin(Double.parseDouble(minStr)); } catch (NumberFormatException ignored) {}
        }
        if (!TextUtils.isEmpty(maxStr)) {
            try { builder.montoMax(Double.parseDouble(maxStr)); } catch (NumberFormatException ignored) {}
        }

        // Período
        long[] rango = calcularRangoPeriodo();
        if (rango != null) {
            builder.fechaDesde(rango[0]).fechaHasta(rango[1]);
        }

        return builder.build();
    }

    /** Calcula el rango de timestamps según el chip de período seleccionado. */
    @Nullable
    private long[] calcularRangoPeriodo() {
        Calendar cal = Calendar.getInstance();

        if (binding.chipPeriodoHoy.isChecked()) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long inicio = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            return new long[]{ inicio, cal.getTimeInMillis() };

        } else if (binding.chipPeriodoSemana.isChecked()) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long inicio = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            return new long[]{ inicio, cal.getTimeInMillis() };

        } else if (binding.chipPeriodoMes.isChecked()) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long inicio = cal.getTimeInMillis();
            cal.add(Calendar.MONTH, 1);
            return new long[]{ inicio, cal.getTimeInMillis() };

        } else if (binding.chipPeriodoAnio.isChecked()) {
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long inicio = cal.getTimeInMillis();
            cal.add(Calendar.YEAR, 1);
            return new long[]{ inicio, cal.getTimeInMillis() };
        }

        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

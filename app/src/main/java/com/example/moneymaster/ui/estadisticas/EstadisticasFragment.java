package com.example.moneymaster.ui.estadisticas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.R;
import com.example.moneymaster.ui.ViewModel.EstadisticasViewModel;

import java.util.Calendar;

public class EstadisticasFragment extends Fragment {

    // ─── ViewModel ────────────────────────────────────────────────────────────
    private EstadisticasViewModel viewModel;

    // ─── Estado interno del selector de mes ──────────────────────────────────
    private Calendar calendarActual;

    // ─── Vistas (solo IDs que existen en tu fragment_estadisticas.xml) ────────
    private TextView    tvMesAnio;       // R.id.tv_mes_anio
    private ImageButton btnMesAnterior;  // R.id.btn_mes_anterior
    private ImageButton btnMesSiguiente; // R.id.btn_mes_siguiente

    // ─── Sesión ───────────────────────────────────────────────────────────────
    private int usuarioId;

    // ─── Nombres de meses en español (Calendar.MONTH es 0-indexed) ───────────
    private static final String[] MESES_ES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    // =========================================================================
    // Ciclo de vida
    // =========================================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estadisticas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        leerSesion();
        inicializarCalendario();
        enlazarVistas(view);
        inicializarViewModel();
        configurarSelectorMes();
        observarDatos();
    }

    // =========================================================================
    // Inicialización
    // =========================================================================

    private void leerSesion() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MoneyMasterPrefs", Context.MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);
    }

    private void inicializarCalendario() {
        calendarActual = Calendar.getInstance();
    }

    /**
     * Solo se enlazan las vistas que existen en el XML actual.
     * tv_total_gastos, tv_total_ingresos, tv_balance, rv_top_categorias
     * se añadirán al XML cuando se implemente la UI completa.
     */
    private void enlazarVistas(View view) {
        tvMesAnio       = view.findViewById(R.id.tv_mes_anio);
        btnMesAnterior  = view.findViewById(R.id.btn_mes_anterior);
        btnMesSiguiente = view.findViewById(R.id.btn_mes_siguiente);
    }

    private void inicializarViewModel() {
        viewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);
        if (usuarioId != -1) {
            viewModel.setUsuarioId((long) usuarioId);
        }
    }

    private void configurarSelectorMes() {
        actualizarTextMes();

        btnMesAnterior.setOnClickListener(v -> {
            calendarActual.add(Calendar.MONTH, -1);
            actualizarTextMes();
            empujarFiltroMes();
        });

        btnMesSiguiente.setOnClickListener(v -> {
            Calendar ahora = Calendar.getInstance();
            boolean esAnioAnterior = calendarActual.get(Calendar.YEAR) < ahora.get(Calendar.YEAR);
            boolean esMesAnterior  = calendarActual.get(Calendar.YEAR) == ahora.get(Calendar.YEAR)
                    && calendarActual.get(Calendar.MONTH) < ahora.get(Calendar.MONTH);
            if (esAnioAnterior || esMesAnterior) {
                calendarActual.add(Calendar.MONTH, 1);
                actualizarTextMes();
                empujarFiltroMes();
            }
        });
    }

    // =========================================================================
    // Observers LiveData
    // =========================================================================

    /**
     * Los observers están preparados para cuando añadas las vistas al XML.
     * Ahora mismo el ViewModel carga los datos en memoria — cuando conectes
     * tv_total_gastos, tv_total_ingresos, tv_balance y rv_top_categorias
     * al XML, solo tendrás que descomentar el código de cada observer.
     */
    private void observarDatos() {

        // ── Totales del mes ───────────────────────────────────────────────────
        // Descomentar cuando añadas tv_total_gastos al XML:
        /*
        viewModel.totalGastosMes.observe(getViewLifecycleOwner(), total -> {
            double valor = (total != null) ? total : 0.0;
            tvTotalGastos.setText(formatearEuros(valor));
            tvTotalGastos.setTextColor(requireContext().getColor(R.color.expense_red));
        });
        */

        // Descomentar cuando añadas tv_total_ingresos al XML:
        /*
        viewModel.totalIngresosMes.observe(getViewLifecycleOwner(), total -> {
            double valor = (total != null) ? total : 0.0;
            tvTotalIngresos.setText(formatearEuros(valor));
            tvTotalIngresos.setTextColor(requireContext().getColor(R.color.income_green));
        });
        */

        // Descomentar cuando añadas tv_balance al XML:
        /*
        viewModel.balanceMes.observe(getViewLifecycleOwner(), balance -> {
            double valor = (balance != null) ? balance : 0.0;
            tvBalance.setText(formatearEuros(valor));
            if (valor > 0) {
                tvBalance.setTextColor(requireContext().getColor(R.color.income_green));
            } else if (valor < 0) {
                tvBalance.setTextColor(requireContext().getColor(R.color.expense_red));
            } else {
                tvBalance.setTextColor(requireContext().getColor(R.color.md_theme_onSurface));
            }
        });
        */

        // ── Top 5 categorías (STATS-005) ──────────────────────────────────────
        // Descomentar cuando añadas section_top_categorias y rv_top_categorias al XML:
        /*
        viewModel.top5CategoriasMes.observe(getViewLifecycleOwner(), items -> {
            topAdapter.submitList(items);
            boolean hayDatos = items != null && !items.isEmpty();
            sectionTopCategorias.setVisibility(hayDatos ? View.VISIBLE : View.GONE);
        });
        */
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void actualizarTextMes() {
        int mes  = calendarActual.get(Calendar.MONTH);
        int anio = calendarActual.get(Calendar.YEAR);
        tvMesAnio.setText(MESES_ES[mes] + " " + anio);
    }

    private void empujarFiltroMes() {
        int mes  = calendarActual.get(Calendar.MONTH) + 1; // DAOs usan 1-indexed
        int anio = calendarActual.get(Calendar.YEAR);
        viewModel.setFiltroMes(mes, anio);
    }
}
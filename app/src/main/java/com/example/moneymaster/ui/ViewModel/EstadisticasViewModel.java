package com.example.moneymaster.ui.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;
import com.example.moneymaster.data.repository.EstadisticasRepository;

import java.util.Calendar;
import java.util.List;

/**
 * ViewModel de la pantalla de Estadísticas.
 *
 * Proporciona datos agregados, listos para pasarlos directamente
 * a MPAndroidChart sin transformaciones adicionales en la UI.
 *
 * Gráficas que alimenta:
 *   · PieChart  → gastos o ingresos por categoría en un mes concreto
 *   · BarChart  → evolución de gastos vs ingresos en los últimos N meses
 *
 * Uso desde la Activity/Fragment:
 *   viewModel.setUsuarioId(id);
 *   viewModel.gastosPorCategoria.observe(this, lista -> pintarPieChart(lista));
 *   viewModel.resumenGastosMeses.observe(this, lista -> pintarBarChart(lista));
 */
public class EstadisticasViewModel extends AndroidViewModel {

    private final EstadisticasRepository repository;

    private final MutableLiveData<Long>    usuarioIdLive    = new MutableLiveData<>();
    private final MutableLiveData<int[]>   filtroMes        = new MutableLiveData<>();
    private final MutableLiveData<Integer> mesesHistorial   = new MutableLiveData<>(6);

    // ─── LiveData para PieChart ───────────────────────────────────────────────

    /**
     * Gastos agrupados por categoría en el mes activo.
     * Cada item tiene: nombreCategoria, icono, color, total.
     */
    public final LiveData<List<TotalPorCategoria>> gastosPorCategoria;

    /**
     * Ingresos agrupados por categoría en el mes activo.
     */
    public final LiveData<List<TotalPorCategoria>> ingresosPorCategoria;

    // ─── LiveData para BarChart ───────────────────────────────────────────────

    /**
     * Totales de gasto, mes a mes, para los últimos N meses con datos.
     * Ordenados cronológicamente (más antiguo primero) para el eje X.
     */
    public final LiveData<List<ResumenMensual>> resumenGastosMeses;

    /**
     * Totales de ingreso mes a mes, misma estructura que resumenGastosMeses.
     */
    public final LiveData<List<ResumenMensual>> resumenIngresosMeses;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public EstadisticasViewModel(@NonNull Application application) {
        super(application);
        repository = new EstadisticasRepository(application);

        // Mes por defecto: el actual
        Calendar cal = Calendar.getInstance();
        filtroMes.setValue(new int[]{cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)});

        // PieChart: se actualiza al cambiar el mes seleccionado
        gastosPorCategoria = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(null);
            return repository.getGastosPorCategoria(uid, filtro[0], filtro[1]);
        });

        ingresosPorCategoria = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(null);
            return repository.getIngresosPorCategoria(uid, filtro[0], filtro[1]);
        });

        // BarChart: se actualiza al cambiar el rango de meses
        resumenGastosMeses = Transformations.switchMap(mesesHistorial, meses -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null) return new MutableLiveData<>(null);
            return repository.getResumenGastosMeses(uid, meses);
        });

        resumenIngresosMeses = Transformations.switchMap(mesesHistorial, meses -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null) return new MutableLiveData<>(null);
            return repository.getResumenIngresosMeses(uid, meses);
        });
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setUsuarioId(long id) {
        if (!Long.valueOf(id).equals(usuarioIdLive.getValue())) {
            usuarioIdLive.setValue(id);
            // Forzar actualización de los switchMap que dependen del usuario
            filtroMes.setValue(filtroMes.getValue());
            mesesHistorial.setValue(mesesHistorial.getValue());
        }
    }

    /** Cambia el mes del PieChart. Llamar al tocar el selector de mes. */
    public void setFiltroMes(int mes, int anio) {
        filtroMes.setValue(new int[]{mes, anio});
    }

    /**
     * Cambia el rango del BarChart.
     * Valores sugeridos: 3 (trimestral), 6 (semestral), 12 (anual).
     */
    public void setMesesHistorial(int meses) {
        mesesHistorial.setValue(meses);
    }

    // ─── Getters de estado ────────────────────────────────────────────────────

    public int getMesActual() {
        int[] f = filtroMes.getValue();
        return f != null ? f[0] : Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public int getAnioActual() {
        int[] f = filtroMes.getValue();
        return f != null ? f[1] : Calendar.getInstance().get(Calendar.YEAR);
    }

    public int getMesesHistorial() {
        Integer m = mesesHistorial.getValue();
        return m != null ? m : 6;
    }
}
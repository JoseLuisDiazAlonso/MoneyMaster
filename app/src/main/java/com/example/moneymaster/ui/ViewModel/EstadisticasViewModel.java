package com.example.moneymaster.ui.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TopCategoriasItem;
import com.example.moneymaster.data.model.TotalPorCategoria;
import com.example.moneymaster.data.repository.EstadisticasRepository;

import java.util.Calendar;
import java.util.List;

/**
 * ViewModel de la pantalla de Estadísticas.
 *
 * LiveData que expone:
 *
 *   Resumen numérico del mes activo:
 *     · totalGastosMes   → Double  (para tv_total_gastos)
 *     · totalIngresosMes → Double  (para tv_total_ingresos)
 *     · balanceMes       → Double  (ingresos - gastos, para tv_balance)
 *
 *   STATS-005 Top 5 categorías:
 *     · top5CategoriasMes → List<TopCategoriasItem>
 *
 *   STATS-002 PieChart (se usa en cards futuros):
 *     · gastosPorCategoria   → List<TotalPorCategoria>
 *     · ingresosPorCategoria → List<TotalPorCategoria>
 *
 *   STATS-003 BarChart (se usa en cards futuros):
 *     · resumenGastosMeses   → List<ResumenMensual>
 *     · resumenIngresosMeses → List<ResumenMensual>
 *
 * Patrón de reactividad:
 *   filtroMes (MutableLiveData<int[]>) actúa como disparador central.
 *   Todos los LiveData dependientes del mes usan Transformations.switchMap
 *   sobre filtroMes, por lo que se re-ejecutan automáticamente al cambiar de mes.
 */
public class EstadisticasViewModel extends AndroidViewModel {

    private final EstadisticasRepository repository;

    // ─── Disparadores internos ────────────────────────────────────────────────

    /** [mes (1-12), anio (e.g. 2025)] — cambia al pulsar < o > en el Fragment */
    private final MutableLiveData<int[]>   filtroMes      = new MutableLiveData<>();

    /** ID del usuario en sesión; se establece en onViewCreated() del Fragment */
    private final MutableLiveData<Long>    usuarioIdLive  = new MutableLiveData<>();

    /** Número de meses hacia atrás para el BarChart (3, 6 o 12) */
    private final MutableLiveData<Integer> mesesHistorial = new MutableLiveData<>(6);

    // ─── LiveData públicos: resumen numérico ──────────────────────────────────

    public final LiveData<Double> totalGastosMes;
    public final LiveData<Double> totalIngresosMes;

    /**
     * Balance = ingresos - gastos del mes activo.
     * Calculado con MediatorLiveData para reaccionar a cualquiera de los dos.
     */
    public final MediatorLiveData<Double> balanceMes = new MediatorLiveData<>();

    // ─── LiveData públicos: STATS-005 Top 5 ──────────────────────────────────

    public final LiveData<List<TopCategoriasItem>> top5CategoriasMes;

    // ─── LiveData públicos: STATS-002 PieChart ────────────────────────────────

    public final LiveData<List<TotalPorCategoria>> gastosPorCategoria;
    public final LiveData<List<TotalPorCategoria>> ingresosPorCategoria;

    // ─── LiveData públicos: STATS-003 BarChart ────────────────────────────────

    public final LiveData<List<ResumenMensual>> resumenGastosMeses;
    public final LiveData<List<ResumenMensual>> resumenIngresosMeses;

    // =========================================================================
    // Constructor
    // =========================================================================

    public EstadisticasViewModel(@NonNull Application application) {
        super(application);
        repository = new EstadisticasRepository(application);

        // Mes por defecto: el mes actual
        Calendar cal = Calendar.getInstance();
        filtroMes.setValue(new int[]{
                cal.get(Calendar.MONTH) + 1,  // 1-indexed
                cal.get(Calendar.YEAR)
        });

        // ── Totales del mes ───────────────────────────────────────────────────
        totalGastosMes = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(0.0);
            return repository.getTotalGastosMes(uid.intValue(), filtro[0], filtro[1]);
        });

        totalIngresosMes = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(0.0);
            return repository.getTotalIngresosMes(uid.intValue(), filtro[0], filtro[1]);
        });

        // ── Balance = ingresos - gastos ───────────────────────────────────────
        // MediatorLiveData se actualiza cuando cambia cualquiera de los dos
        balanceMes.addSource(totalGastosMes,   g -> recalcularBalance());
        balanceMes.addSource(totalIngresosMes, i -> recalcularBalance());

        // ── STATS-005: Top 5 categorías ───────────────────────────────────────
        top5CategoriasMes = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(null);
            return repository.getTop5CategoriasMes(uid.intValue(), filtro[0], filtro[1]);
        });

        // ── STATS-002: PieChart ───────────────────────────────────────────────
        gastosPorCategoria = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(null);
            return repository.getGastosPorCategoria(uid.intValue(), filtro[0], filtro[1]);
        });

        ingresosPorCategoria = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(null);
            return repository.getIngresosPorCategoria(uid.intValue(), filtro[0], filtro[1]);
        });

        // ── STATS-003: BarChart ───────────────────────────────────────────────
        resumenGastosMeses = Transformations.switchMap(mesesHistorial, meses -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null) return new MutableLiveData<>(null);
            return repository.getResumenGastosMeses(uid.intValue(), meses);
        });

        resumenIngresosMeses = Transformations.switchMap(mesesHistorial, meses -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null) return new MutableLiveData<>(null);
            return repository.getResumenIngresosMeses(uid.intValue(), meses);
        });
    }

    // =========================================================================
    // Setters públicos
    // =========================================================================

    /**
     * Establece el ID del usuario en sesión.
     * Llamar desde onViewCreated() del Fragment justo después de crear el ViewModel.
     * Re-dispara todos los switchMap para que carguen datos del usuario correcto.
     */
    public void setUsuarioId(long id) {
        if (!Long.valueOf(id).equals(usuarioIdLive.getValue())) {
            usuarioIdLive.setValue(id);
            // Forzar re-disparo de todos los switchMap que dependen de usuarioId
            filtroMes.setValue(filtroMes.getValue());
            mesesHistorial.setValue(mesesHistorial.getValue());
        }
    }

    /**
     * Cambia el mes del filtro activo.
     * Llamar cuando el usuario pulsa < o > en el selector de mes del Fragment.
     *
     * @param mes  Mes en formato 1-indexed (1 = enero, 12 = diciembre)
     * @param anio Año completo (e.g. 2025)
     */
    public void setFiltroMes(int mes, int anio) {
        filtroMes.setValue(new int[]{mes, anio});
    }

    /**
     * Cambia el número de meses del historial para el BarChart.
     *
     * @param meses Número de meses hacia atrás (3, 6 o 12)
     */
    public void setMesesHistorial(int meses) {
        mesesHistorial.setValue(meses);
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    /**
     * Recalcula el balance neto del mes: ingresos - gastos.
     * Lo llama balanceMes como MediatorLiveData cada vez que cambia
     * totalGastosMes o totalIngresosMes.
     */
    private void recalcularBalance() {
        Double gastos   = totalGastosMes.getValue();
        Double ingresos = totalIngresosMes.getValue();
        double g = (gastos   != null) ? gastos   : 0.0;
        double i = (ingresos != null) ? ingresos : 0.0;
        balanceMes.setValue(i - g);
    }
}
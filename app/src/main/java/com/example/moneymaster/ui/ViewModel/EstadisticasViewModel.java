package com.example.moneymaster.ui.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.model.PuntoLinea;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;
import com.example.moneymaster.data.repository.EstadisticasRepository;

import java.util.Calendar;
import java.util.List;

public class EstadisticasViewModel extends AndroidViewModel {

    private final EstadisticasRepository repository;

    private final MutableLiveData<Long>    usuarioIdLive  = new MutableLiveData<>();
    private final MutableLiveData<int[]>   filtroMes      = new MutableLiveData<>();
    private final MutableLiveData<Integer> mesesHistorial = new MutableLiveData<>(6);
    private final MutableLiveData<long[]>  rangoLineChart = new MutableLiveData<>();

    // ─── LiveData para PieChart (Card #42) ───────────────────────────────────

    public final LiveData<List<TotalPorCategoria>> gastosPorCategoria;
    public final LiveData<List<TotalPorCategoria>> ingresosPorCategoria;
    public final LiveData<Double>                  totalGastosMes;

    // ─── LiveData para BarChart (Card #43) ───────────────────────────────────

    public final LiveData<List<ResumenMensual>> resumenGastosMeses;
    public final LiveData<List<ResumenMensual>> resumenIngresosMeses;

    // ─── LiveData para LineChart (Card #44) ──────────────────────────────────

    public final LiveData<List<PuntoLinea>> gastosDiarios;
    public final LiveData<List<PuntoLinea>> gastosSemanales;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public EstadisticasViewModel(@NonNull Application application) {
        super(application);
        repository = new EstadisticasRepository(application);

        // Mes por defecto: el actual
        Calendar cal = Calendar.getInstance();
        int mesActual  = cal.get(Calendar.MONTH) + 1;
        int anioActual = cal.get(Calendar.YEAR);
        filtroMes.setValue(new int[]{mesActual, anioActual});
        rangoLineChart.setValue(calcularRangoMes(mesActual, anioActual));

        // ── PieChart ──────────────────────────────────────────────────────────

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

        totalGastosMes = Transformations.switchMap(filtroMes, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(0.0);
            return repository.getTotalGastosMes(uid, filtro[0], filtro[1]);
        });

        // ── BarChart ──────────────────────────────────────────────────────────

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

        // ── LineChart ─────────────────────────────────────────────────────────

        gastosDiarios = Transformations.switchMap(rangoLineChart, rango -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || rango == null) return new MutableLiveData<>(null);
            return repository.getGastosDiarios(uid, rango[0], rango[1]);
        });

        gastosSemanales = Transformations.switchMap(rangoLineChart, rango -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || rango == null) return new MutableLiveData<>(null);
            return repository.getGastosSemanales(uid, rango[0], rango[1]);
        });
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    /**
     * Inyectar el ID del usuario desde SharedPreferences.
     * Fuerza re-emisión de todos los switchMap.
     */
    public void setUsuarioId(long id) {
        if (!Long.valueOf(id).equals(usuarioIdLive.getValue())) {
            usuarioIdLive.setValue(id);
            filtroMes.setValue(filtroMes.getValue());
            mesesHistorial.setValue(mesesHistorial.getValue());
            rangoLineChart.setValue(rangoLineChart.getValue());
        }
    }

    /** Cambia el mes activo del PieChart. */
    public void setFiltroMes(int mes, int anio) {
        filtroMes.setValue(new int[]{mes, anio});
    }

    /** Cambia el rango del BarChart (3, 6 o 12 meses). */
    public void setMesesHistorial(int meses) {
        mesesHistorial.setValue(meses);
    }

    /**
     * Actualiza el rango del LineChart al mes indicado.
     * Llamar junto a setFiltroMes() cuando el usuario cambia de mes.
     */
    public void setRangoLineChart(int mes, int anio) {
        rangoLineChart.setValue(calcularRangoMes(mes, anio));
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

    // ─── Helpers privados ─────────────────────────────────────────────────────

    /**
     * Calcula los timestamps de inicio (00:00:00 del día 1) y fin
     * (23:59:59 del último día) del mes dado, en milisegundos.
     */
    private long[] calcularRangoMes(int mes, int anio) {
        Calendar inicio = Calendar.getInstance();
        inicio.set(anio, mes - 1, 1, 0, 0, 0);
        inicio.set(Calendar.MILLISECOND, 0);

        Calendar fin = Calendar.getInstance();
        fin.set(anio, mes - 1, inicio.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        fin.set(Calendar.MILLISECOND, 999);

        return new long[]{ inicio.getTimeInMillis(), fin.getTimeInMillis() };
    }
}
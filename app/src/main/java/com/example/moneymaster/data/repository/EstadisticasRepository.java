package com.example.moneymaster.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

/**
 * Repository de solo lectura.
 * Agrega datos de gastos e ingresos para las gráficas de MPAndroidChart.
 * No expone métodos de escritura — modificar datos va por GastoRepository/IngresoRepository.
 */
public class EstadisticasRepository {

    private final GastoPersonalDao gastoDao;
    private final IngresoPersonalDao ingresoDao;

    public EstadisticasRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        gastoDao   = db.gastoPersonalDao();
        ingresoDao = db.ingresoPersonalDao();
    }

    // ─── Para PieChart ────────────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getGastosPorCategoria(long usuarioId, int mes, int anio) {
        return gastoDao.getGastosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(long usuarioId, int mes, int anio) {
        return ingresoDao.getIngresosPorCategoria(usuarioId, mes, anio);
    }

    // ─── Para BarChart ────────────────────────────────────────────────────────

    public LiveData<List<ResumenMensual>> getResumenGastosMeses(long usuarioId, int meses) {
        return gastoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    public LiveData<List<ResumenMensual>> getResumenIngresosMeses(long usuarioId, int meses) {
        return ingresoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    // ─── Totales rápidos (también usados por MainViewModel) ───────────────────

    public LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio) {
        return gastoDao.getTotalGastosMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalIngresosMes(long usuarioId, int mes, int anio) {
        return ingresoDao.getTotalIngresosMes(usuarioId, mes, anio);
    }
}

package com.example.moneymaster.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TopCategoriasItem;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

/**
 * Repositorio de datos de estadísticas.
 *
 * Centraliza los accesos de solo lectura a GastoPersonalDao e IngresoPersonalDao
 * para el Fragment de Estadísticas y su ViewModel.
 * No tiene métodos de escritura (eso va por GastoRepository / IngresoRepository).
 */
public class EstadisticasRepository {

    private final GastoPersonalDao   gastoDao;
    private final IngresoPersonalDao ingresoDao;

    public EstadisticasRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        gastoDao   = db.gastoPersonalDao();
        ingresoDao = db.ingresoPersonalDao();
    }

    // ─── Totales del mes ──────────────────────────────────────────────────────

    public LiveData<Double> getTotalGastosMes(int usuarioId, int mes, int anio) {
        return gastoDao.getTotalGastosMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalIngresosMes(int usuarioId, int mes, int anio) {
        return ingresoDao.getTotalIngresosMes(usuarioId, mes, anio);
    }

    // ─── STATS-005: Top 5 categorías ─────────────────────────────────────────

    public LiveData<List<TopCategoriasItem>> getTop5CategoriasMes(int usuarioId, int mes, int anio) {
        return gastoDao.getTop5CategoriasMes(usuarioId, mes, anio);
    }

    // ─── STATS-002: PieChart ──────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getGastosPorCategoria(int usuarioId, int mes, int anio) {
        return gastoDao.getGastosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(int usuarioId, int mes, int anio) {
        return ingresoDao.getIngresosPorCategoria(usuarioId, mes, anio);
    }

    // ─── STATS-003: BarChart ──────────────────────────────────────────────────

    public LiveData<List<ResumenMensual>> getResumenGastosMeses(int usuarioId, int meses) {
        return gastoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    public LiveData<List<ResumenMensual>> getResumenIngresosMeses(int usuarioId, int meses) {
        return ingresoDao.getResumenUltimosMeses(usuarioId, meses);
    }
}


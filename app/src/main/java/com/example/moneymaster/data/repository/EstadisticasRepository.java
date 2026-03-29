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
 * Corrección Card #62:
 *  - usuarioId cambiado de int a long (coherente con los DAOs).
 *  - getTop5CategoriasMes() ahora existe en GastoPersonalDao.
 */
public class EstadisticasRepository {

    private final GastoPersonalDao   gastoDao;
    private final IngresoPersonalDao ingresoDao;

    public EstadisticasRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        gastoDao   = db.gastoPersonalDao();
        ingresoDao = db.ingresoPersonalDao();
    }

    // ── Totales del mes ───────────────────────────────────────────────────────

    public LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio) {
        return gastoDao.getTotalGastosMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalIngresosMes(long usuarioId, int mes, int anio) {
        return ingresoDao.getTotalIngresosMes(usuarioId, mes, anio);
    }

    // ── STATS-005: Top 5 categorías ───────────────────────────────────────────

    public LiveData<List<TopCategoriasItem>> getTop5CategoriasMes(long usuarioId, int mes, int anio) {
        return gastoDao.getTop5CategoriasMes(usuarioId, mes, anio);
    }

    // ── STATS-002: PieChart ───────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getGastosPorCategoria(long usuarioId, int mes, int anio) {
        return gastoDao.getGastosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(long usuarioId, int mes, int anio) {
        return ingresoDao.getIngresosPorCategoria(usuarioId, mes, anio);
    }

    // ── STATS-003: BarChart ───────────────────────────────────────────────────

    public LiveData<List<ResumenMensual>> getResumenGastosMeses(long usuarioId, int meses) {
        return gastoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    public LiveData<List<ResumenMensual>> getResumenIngresosMeses(long usuarioId, int meses) {
        return ingresoDao.getResumenUltimosMeses(usuarioId, meses);
    }
}
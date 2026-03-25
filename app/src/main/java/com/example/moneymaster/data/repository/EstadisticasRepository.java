package com.example.moneymaster.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.PuntoLinea;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

/**
 * Repository de solo lectura.
 * Agrega datos de gastos e ingresos para las gráficas de MPAndroidChart.
 * No expone métodos de escritura — modificar datos va por GastoRepository/IngresoRepository.
 */
public class EstadisticasRepository {

    private final GastoPersonalDao   gastoDao;
    private final IngresoPersonalDao ingresoDao;

    public EstadisticasRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        gastoDao   = db.gastoPersonalDao();
        ingresoDao = db.ingresoPersonalDao();
    }

    // ─── PieChart (Card #42) ──────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getGastosPorCategoria(long usuarioId, int mes, int anio) {
        return gastoDao.getGastosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(long usuarioId, int mes, int anio) {
        return ingresoDao.getIngresosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<TotalPorCategoria>> getGastosPorCategoria(int usuarioId, String mes, String anio) {
        return gastoDao.getGastosPorCategoria(usuarioId, mes, anio);
    }

    // ─── Totales del mes ──────────────────────────────────────────────────────

    public LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio) {
        return gastoDao.getTotalGastosMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalIngresosMes(long usuarioId, int mes, int anio) {
        return ingresoDao.getTotalIngresosMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalGastosMes(int usuarioId, String mes, String anio) {
        return gastoDao.getTotalGastosMes(usuarioId, mes, anio);
    }

    // ─── BarChart (Card #43) ──────────────────────────────────────────────────

    public LiveData<List<ResumenMensual>> getResumenGastosMeses(long usuarioId, int meses) {
        return gastoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    public LiveData<List<ResumenMensual>> getResumenIngresosMeses(long usuarioId, int meses) {
        return ingresoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    // ─── LineChart (Card #44) ─────────────────────────────────────────────────

    /**
     * Gastos agrupados por día para el rango dado (inicio y fin en ms).
     * Room devuelve sólo los días con gastos; LineChartHelper rellena los huecos con 0.
     */
    public LiveData<List<PuntoLinea>> getGastosDiarios(long usuarioId, long inicio, long fin) {
        return gastoDao.getGastosDiarios(usuarioId, inicio, fin);
    }

    /**
     * Gastos agrupados por semana para el rango dado (inicio y fin en ms).
     * Semana 1 = días 1–7, semana 2 = 8–14, semana 3 = 15–21, semana 4 = 22–fin.
     */
    public LiveData<List<PuntoLinea>> getGastosSemanales(long usuarioId, long inicio, long fin) {
        return gastoDao.getGastosSemanales(usuarioId, inicio, fin);
    }
}


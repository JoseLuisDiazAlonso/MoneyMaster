package com.example.moneymaster.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.dao.CategoriaGastoDao;
import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class GastoRepository {

    private final GastoPersonalDao gastoDao;
    private final CategoriaGastoDao categoriaDao;
    private final ExecutorService executor;

    public GastoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        gastoDao     = db.gastoPersonalDao();
        categoriaDao = db.categoriaGastoDao();
        executor     = AppDatabase.databaseWriteExecutor;
    }

    // ── Gastos ────────────────────────────────────────────────────────────────

    public LiveData<List<GastoPersonal>> getGastosByUsuario(long usuarioId) {
        return gastoDao.getGastosByUsuario(usuarioId);
    }

    public LiveData<List<GastoPersonal>> getGastosByMes(long usuarioId, int mes, int anio) {
        return gastoDao.getGastosByMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio) {
        return gastoDao.getTotalGastosMes(usuarioId, mes, anio);
    }

    public LiveData<List<GastoPersonal>> getUltimosGastos(long usuarioId, int limite) {
        return gastoDao.getUltimosGastos(usuarioId, limite);
    }

    public LiveData<GastoPersonal> getGastoById(long gastoId) {
        return gastoDao.getGastoById(gastoId);
    }

    /** Card #62 — StatisticsViewModel: gastos de todo un año. */
    public LiveData<List<GastoPersonal>> getGastosByAnio(long usuarioId, int anio) {
        return gastoDao.getGastosByAnio(usuarioId, anio);
    }

    /** Card #62 — StatisticsViewModel: gastos en rango de fechas. */
    public LiveData<List<GastoPersonal>> getGastosByRango(long usuarioId, long inicio, long fin) {
        return gastoDao.getGastosByRango(usuarioId, inicio, fin);
    }

    public void insertar(GastoPersonal gasto) {
        executor.execute(() -> gastoDao.insertar(gasto));
    }

    public void actualizar(GastoPersonal gasto) {
        executor.execute(() -> gastoDao.actualizar(gasto));
    }

    public void eliminar(GastoPersonal gasto) {
        executor.execute(() -> gastoDao.eliminar(gasto));
    }

    // ── Estadísticas ──────────────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getGastosPorCategoria(long usuarioId, int mes, int anio) {
        return gastoDao.getGastosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses) {
        return gastoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    // ── Categorías ────────────────────────────────────────────────────────────

    public LiveData<List<CategoriaGasto>> getCategorias(long usuarioId) {
        return categoriaDao.getCategorias(usuarioId);
    }

    public LiveData<List<CategoriaGasto>> getCategoriasDelSistema() {
        return categoriaDao.getCategoriasDelSistema();
    }

    public LiveData<List<CategoriaGasto>> getCategoriasByUsuario(long usuarioId) {
        return categoriaDao.getCategoriasByUsuario(usuarioId);
    }

    public void insertarCategoria(CategoriaGasto categoria) {
        executor.execute(() -> categoriaDao.insertar(categoria));
    }

    public void actualizarCategoria(CategoriaGasto categoria) {
        executor.execute(() -> categoriaDao.actualizar(categoria));
    }

    public void eliminarCategoria(CategoriaGasto categoria) {
        executor.execute(() -> categoriaDao.eliminar(categoria));
    }
}

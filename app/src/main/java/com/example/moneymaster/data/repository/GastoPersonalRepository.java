package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

public class GastoPersonalRepository {

    private final GastoPersonalDao gastoPersonalDao;

    public GastoPersonalRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        gastoPersonalDao = db.gastoPersonalDao();
    }

    // ── ESCRITURAS ────────────────────────────────────────────────────────────

    public void insertarGasto(GastoPersonal gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoPersonalDao.insertar(gasto));
    }

    public void insertarGastoYObtenerId(GastoPersonal gasto, SaveCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = gastoPersonalDao.insertar(gasto);
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onSaved(id));
        });
    }

    public void actualizarGasto(GastoPersonal gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoPersonalDao.actualizar(gasto));
    }

    public void eliminarGasto(GastoPersonal gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoPersonalDao.eliminar(gasto));
    }

    // ── LISTAS ────────────────────────────────────────────────────────────────

    public LiveData<List<GastoPersonal>> getGastosByUsuario(long usuarioId) {
        return gastoPersonalDao.getGastosByUsuario(usuarioId);
    }

    public LiveData<List<GastoPersonal>> getGastosByMes(long usuarioId, int mes, int anio) {
        return gastoPersonalDao.getGastosByMes(usuarioId, mes, anio);
    }

    public LiveData<List<GastoPersonal>> getUltimosGastos(long usuarioId, int limite) {
        return gastoPersonalDao.getUltimosGastos(usuarioId, limite);
    }

    public LiveData<GastoPersonal> getGastoById(long gastoId) {
        return gastoPersonalDao.getGastoById(gastoId);
    }

    // ── TOTALES ───────────────────────────────────────────────────────────────

    public LiveData<Double> getTotalGastosMes(long usuarioId, int mes, int anio) {
        return gastoPersonalDao.getTotalGastosMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalGastosRango(long usuarioId, long desde, long hasta) {
        return gastoPersonalDao.getTotalGastosRango(usuarioId, desde, hasta);
    }

    // ── ESTADÍSTICAS ──────────────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getGastosPorCategoria(long usuarioId,
                                                                   int mes, int anio) {
        return gastoPersonalDao.getGastosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses) {
        return gastoPersonalDao.getResumenUltimosMeses(usuarioId, meses);
    }

    public List<ResumenMensual> getResumenUltimosMesesSync(long usuarioId, int meses) {
        return gastoPersonalDao.getResumenUltimosMesesSync(usuarioId, meses);
    }

    public int countGastos(long usuarioId) {
        return gastoPersonalDao.countGastos(usuarioId);
    }

    // ── CALLBACK ──────────────────────────────────────────────────────────────

    public interface SaveCallback<T> {
        void onSaved(T result);
    }
}
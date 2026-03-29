package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.model.GastoGrupo;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * GastoGrupoRepository — refactorizado para Card #60 (Unit Tests)
 *
 * Mismo patrón que GastoPersonalRepository: el Executor se inyecta
 * en el constructor de tests para hacer las operaciones síncronas.
 */
public class GastoGrupoRepository {

    private final GastoGrupoDao gastoGrupoDao;
    private final Executor      executor;

    // ─── Constructor de producción ────────────────────────────────────────────
    public GastoGrupoRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.gastoGrupoDao = db.gastoGrupoDao();
        this.executor      = AppDatabase.databaseWriteExecutor;
    }

    // ─── Constructor para tests ───────────────────────────────────────────────
    public GastoGrupoRepository(GastoGrupoDao dao, Executor executor) {
        this.gastoGrupoDao = dao;
        this.executor      = executor;
    }

    // ─── ESCRITURAS ───────────────────────────────────────────────────────────

    public void insertarGasto(GastoGrupo gasto) {
        executor.execute(() -> gastoGrupoDao.insertar(gasto));
    }

    public void insertarGastoYObtenerId(GastoGrupo gasto, SaveCallback<Long> callback) {
        executor.execute(() -> {
            long id = gastoGrupoDao.insertar(gasto);
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onSaved(id));
        });
    }

    public void actualizarGasto(GastoGrupo gasto) {
        executor.execute(() -> gastoGrupoDao.actualizar(gasto));
    }

    public void eliminarGasto(GastoGrupo gasto) {
        executor.execute(() -> gastoGrupoDao.eliminar(gasto));
    }

    // ─── LECTURAS REACTIVAS ───────────────────────────────────────────────────

    public LiveData<List<GastoGrupo>> getGastosByGrupo(long grupoId) {
        return gastoGrupoDao.getGastosByGrupo(grupoId);
    }

    public LiveData<Double> getTotalGastosGrupo(long grupoId) {
        return gastoGrupoDao.getTotalGastosGrupo(grupoId);
    }

    public LiveData<GastoGrupo> getGastoById(long gastoId) {
        return gastoGrupoDao.getGastoById(gastoId);
    }

    // ─── LECTURAS SÍNCRONAS ───────────────────────────────────────────────────

    public List<GastoGrupo> getGastosByGrupoSync(long grupoId) {
        return gastoGrupoDao.getGastosByGrupoSync(grupoId);
    }

    public double getTotalPagadoPorUsuarioSync(long grupoId, long usuarioId) {
        return gastoGrupoDao.getTotalPagadoPorUsuarioSync(grupoId, usuarioId);
    }

    // ─── CALLBACK ─────────────────────────────────────────────────────────────

    public interface SaveCallback<T> {
        void onSaved(T result);
    }
}
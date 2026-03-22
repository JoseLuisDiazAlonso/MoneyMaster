package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.model.GastoGrupo;

import java.util.List;

public class GastoGrupoRepository {

    private final GastoGrupoDao gastoGrupoDao;

    public GastoGrupoRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        gastoGrupoDao = db.gastoGrupoDao();
    }

    // ── ESCRITURAS ────────────────────────────────────────────────────────────

    public void insertarGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoGrupoDao.insertar(gasto));
    }

    /** Inserta y devuelve el ID generado vía callback (necesario para asignar balances). */
    public void insertarGastoYObtenerId(GastoGrupo gasto, SaveCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = gastoGrupoDao.insertar(gasto);
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onSaved(id));
        });
    }

    public void actualizarGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoGrupoDao.actualizar(gasto));
    }

    public void eliminarGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoGrupoDao.eliminar(gasto));
    }

    // ── LECTURAS REACTIVAS (LiveData) ─────────────────────────────────────────

    /** Todos los gastos del grupo, ordenados por fecha descendente. */
    public LiveData<List<GastoGrupo>> getGastosByGrupo(long grupoId) {
        return gastoGrupoDao.getGastosByGrupo(grupoId);
    }

    /** Total acumulado de gastos del grupo como LiveData. */
    public LiveData<Double> getTotalGastosGrupo(long grupoId) {
        return gastoGrupoDao.getTotalGastosGrupo(grupoId);
    }

    /** Un gasto concreto por ID como LiveData. */
    public LiveData<GastoGrupo> getGastoById(long gastoId) {
        return gastoGrupoDao.getGastoById(gastoId);
    }

    // ── LECTURAS SÍNCRONAS (llamar desde background thread) ───────────────────

    /** Lista de gastos del grupo sin LiveData. Para cálculos de balance. */
    public List<GastoGrupo> getGastosByGrupoSync(long grupoId) {
        return gastoGrupoDao.getGastosByGrupoSync(grupoId);
    }

    /** Total pagado por un usuario concreto en el grupo. Para cálculos de balance. */
    public double getTotalPagadoPorUsuarioSync(long grupoId, long usuarioId) {
        return gastoGrupoDao.getTotalPagadoPorUsuarioSync(grupoId, usuarioId);
    }

    // ── CALLBACK ──────────────────────────────────────────────────────────────

    public interface SaveCallback<T> {
        void onSaved(T result);
    }
}
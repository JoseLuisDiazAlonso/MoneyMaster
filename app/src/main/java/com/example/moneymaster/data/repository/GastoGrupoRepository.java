package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.model.GastoGrupo;

import java.util.List;

/**
 * Repositorio de gastos grupales.
 *
 * Gestiona el CRUD de GastoGrupo.
 *
 * Importante: al insertar un gasto grupal, el ViewModel o la capa de negocio
 * debe encargarse de actualizar BalanceGrupo usando BalanceGrupoRepository.
 * Este repositorio solo gestiona los gastos, no los balances derivados.
 */
public class GastoGrupoRepository {

    private final GastoGrupoDao gastoGrupoDao;

    public GastoGrupoRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        gastoGrupoDao = db.gastoGrupoDao();
    }

    // ---- ESCRITURAS (background thread) ----

    public void insertGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoGrupoDao.insertGasto(gasto));
    }

    public void updateGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoGrupoDao.updateGasto(gasto));
    }

    public void deleteGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoGrupoDao.deleteGasto(gasto));
    }

    // ---- LECTURAS REACTIVAS (LiveData) ----

    /** Todos los gastos de un grupo, más recientes primero. */
    public LiveData<List<GastoGrupo>> getGastosDeGrupo(int grupoId) {
        return gastoGrupoDao.getGastosDeGrupo(grupoId);
    }

    /** Gastos de un grupo en un rango de fechas. */
    public LiveData<List<GastoGrupo>> getGastosDeGrupoPorFecha(int grupoId, long inicio, long fin) {
        return gastoGrupoDao.getGastosDeGrupoPorFecha(grupoId, inicio, fin);
    }

    // ---- LECTURAS SINCRÓNICAS (llamar desde background thread) ----

    /** Suma total de gastos del grupo. */
    public double getTotalGrupo(int grupoId) {
        return gastoGrupoDao.getTotalGrupo(grupoId);
    }

    /** Suma de lo que ha pagado un miembro específico en el grupo. */
    public double getTotalPagadoPor(int grupoId, int usuarioId) {
        return gastoGrupoDao.getTotalPagadoPor(grupoId, usuarioId);
    }

    public GastoGrupo getById(int id) {
        return gastoGrupoDao.getById(id);
    }
}

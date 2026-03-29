package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.BalanceGrupoDao;
import com.example.moneymaster.data.model.BalanceGrupo;

import java.util.List;

/**
 * Repositorio de balances grupales.
 *
 * Corrección Card #62:
 *  - getInstance() → getDatabase()
 *  - balanceGrupoDao() añadido a AppDatabase
 *
 * Flujo típico al agregar un GastoGrupo con dividirIgual = true:
 *   1. Obtener miembros activos del grupo.
 *   2. Calcular split = gasto.monto / numMiembros.
 *   3. Para cada miembro (excepto quien pagó):
 *      acumularBalance(grupoId, miembro.usuarioId, pagadoPorId, split)
 */
public class BalanceGrupoRepository {

    private final BalanceGrupoDao balanceGrupoDao;

    public BalanceGrupoRepository(Context context) {
        // Corrección: getDatabase() en lugar de getInstance()
        AppDatabase db = AppDatabase.getDatabase(context);
        balanceGrupoDao = db.balanceGrupoDao();
    }

    // ── Escrituras (background thread) ───────────────────────────────────────

    /** Inserta o reemplaza un balance completo. Usar para crear balances nuevos. */
    public void upsertBalance(BalanceGrupo balance) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                balanceGrupoDao.upsertBalance(balance));
    }

    public void updateBalance(BalanceGrupo balance) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                balanceGrupoDao.updateBalance(balance));
    }

    /**
     * Acumula un monto a un balance existente.
     * Si no existe la fila, no hace nada — usar upsertBalance primero para crearla.
     */
    public void acumularBalance(int grupoId, int deudorId, int acreedorId, double monto) {
        long timestamp = System.currentTimeMillis();
        AppDatabase.databaseWriteExecutor.execute(() ->
                balanceGrupoDao.acumularBalance(grupoId, deudorId, acreedorId, monto, timestamp));
    }

    /** Marca una deuda como liquidada (monto_pendiente = 0, liquidado = 1). */
    public void liquidarDeuda(int grupoId, int deudorId, int acreedorId) {
        long timestamp = System.currentTimeMillis();
        AppDatabase.databaseWriteExecutor.execute(() ->
                balanceGrupoDao.liquidarDeuda(grupoId, deudorId, acreedorId, timestamp));
    }

    // ── Lecturas reactivas (LiveData) ─────────────────────────────────────────

    /** Deudas pendientes dentro de un grupo (para la pantalla de balances). */
    public LiveData<List<BalanceGrupo>> getBalancesPendientes(int grupoId) {
        return balanceGrupoDao.getBalancesPendientes(grupoId);
    }

    /** Lo que YO debo a otros miembros del grupo. */
    public LiveData<List<BalanceGrupo>> getMisDeudas(int grupoId, int usuarioId) {
        return balanceGrupoDao.getMisDeudas(grupoId, usuarioId);
    }

    /** Lo que otros miembros me deben a MÍ en el grupo. */
    public LiveData<List<BalanceGrupo>> getLoqueMeDeben(int grupoId, int usuarioId) {
        return balanceGrupoDao.getLoqueMeDeben(grupoId, usuarioId);
    }
}

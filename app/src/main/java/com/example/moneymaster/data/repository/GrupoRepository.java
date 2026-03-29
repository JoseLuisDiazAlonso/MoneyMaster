package com.example.moneymaster.data.repository;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.BalanceGrupoDao;
import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.dao.GrupoDao;
import com.example.moneymaster.data.dao.MiembroGrupoDao;
import com.example.moneymaster.data.model.BalanceGrupo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.GroupWithDetails;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.data.service.BalancePersistenceService;

import java.util.List;

/**
 * Repositorio unificado para toda la gestión de grupos.
 *
 * Responsabilidades:
 *   - CRUD de Grupo, MiembroGrupo y GastoGrupo
 *   - Exposición de LiveData para la UI
 *   - Disparo automático de BalancePersistenceService tras cada
 *     inserción o eliminación de gasto
 *
 * El recálculo de balances se encadena automáticamente:
 *   insertarGasto() → Room actualiza gastos_grupo
 *                   → BalancePersistenceService recalcula
 *                   → Room actualiza balance_grupo
 *                   → LiveData notifica a la UI
 */
public class GrupoRepository {

    private final AppDatabase db;
    private final Context context;

    private final GrupoDao         grupoDao;
    private final MiembroGrupoDao  miembroDao;
    private final GastoGrupoDao    gastoDao;
    private final BalanceGrupoDao  balanceDao;

    public GrupoRepository(Application application) {
        this.db      = AppDatabase.getDatabase(application);
        this.context = application.getApplicationContext();

        grupoDao   = db.grupoDao();
        miembroDao = db.miembroGrupoDao();
        gastoDao   = db.gastoGrupoDao();
        balanceDao = db.balanceGrupoDao();
    }


    // GRUPOS


    public LiveData<List<GroupWithDetails>> getAllGroupsWithDetails() {
        return grupoDao.getAllGroupsWithDetails();
    }

    public LiveData<Grupo> getGrupoById(int id) {
        return grupoDao.getGrupoById(id);
    }

    public void insertarGrupo(Grupo grupo, SaveCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = grupoDao.insertGrupo(grupo);
            if (callback != null) {
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onSaved(id));
            }
        });
    }

    public void actualizarGrupo(Grupo grupo) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                grupoDao.updateGrupo(grupo));
    }

    public void eliminarGrupo(Grupo grupo) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                grupoDao.deleteGrupo(grupo));
    }


    // MIEMBROS


    public LiveData<List<MiembroGrupo>> getMiembrosByGrupo(int grupoId) {
        return miembroDao.getMiembrosByGrupo(grupoId);
    }

    public void insertarMiembro(MiembroGrupo miembro) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                miembroDao.insertar(miembro));
    }

    public void eliminarMiembro(MiembroGrupo miembro) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                miembroDao.eliminar(miembro));
    }


    // GASTOS — con recálculo automático de balances


    public LiveData<List<GastoGrupo>> getGastosByGrupo(int grupoId) {
        return gastoDao.getGastosByGrupo(grupoId);
    }

    public LiveData<Double> getTotalGastosGrupo(int grupoId) {
        return gastoDao.getTotalGastosGrupo(grupoId);
    }

    /**
     * Inserta un gasto y recalcula los balances del grupo automáticamente.
     * El recálculo se encadena en el mismo hilo de background.
     */
    public void insertarGasto(GastoGrupo gasto, SaveCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = gastoDao.insertar(gasto);

            // Recalcular y persistir balances en el mismo hilo
            BalancePersistenceService.recalcularYPersistir(context, gasto.grupoId);

            if (callback != null) {
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onSaved(id));
            }
        });
    }

    /**
     * Elimina un gasto y recalcula los balances del grupo automáticamente.
     */
    public void eliminarGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            gastoDao.eliminar(gasto);
            BalancePersistenceService.recalcularYPersistir(context, gasto.grupoId);
        });
    }

    public void actualizarGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            gastoDao.actualizar(gasto);
            BalancePersistenceService.recalcularYPersistir(context, gasto.grupoId);
        });
    }


    // BALANCES — solo lectura desde la UI


    public LiveData<List<BalanceGrupo>> getBalancesByGrupo(int grupoId) {
        return balanceDao.getBalancesByGrupo(grupoId);
    }

    public LiveData<List<BalanceGrupo>> getBalancesPendientes(int grupoId) {
        return balanceDao.getBalancesPendientes(grupoId);
    }


    // CALLBACK


    public interface SaveCallback {
        void onSaved(long newId);
    }
}
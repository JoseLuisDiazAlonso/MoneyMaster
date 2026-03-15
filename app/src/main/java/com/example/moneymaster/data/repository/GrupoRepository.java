package com.example.moneymaster.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.dao.BalanceGrupoDao;
import com.example.moneymaster.data.dao.FotoReciboDao;
import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.dao.GrupoDao;
import com.example.moneymaster.data.dao.MiembroGrupoDao;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.BalanceGrupo;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.MiembroGrupo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class GrupoRepository {

    private final GrupoDao grupoDao;
    private final MiembroGrupoDao miembroDao;
    private final GastoGrupoDao gastoGrupoDao;
    private final BalanceGrupoDao balanceDao;
    private final FotoReciboDao fotoDao;
    private final ExecutorService executor;

    public GrupoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        grupoDao      = db.grupoDao();
        miembroDao    = db.miembroGrupoDao();
        gastoGrupoDao = db.gastoGrupoDao();
        balanceDao    = db.balanceGrupoDao();
        fotoDao       = db.fotoReciboDao();
        executor      = AppDatabase.databaseWriteExecutor;
    }

    // ─── Grupos ───────────────────────────────────────────────────────────────

    public LiveData<List<Grupo>> getGruposByUsuario(long usuarioId) {
        return grupoDao.getGruposByUsuario(usuarioId);
    }

    public LiveData<Grupo> getGrupoById(long grupoId) {
        return grupoDao.getGrupoById(grupoId);
    }

    public Future<Long> insertarGrupo(Grupo grupo) {
        return executor.submit(() -> grupoDao.insertar(grupo));
    }

    public void actualizarGrupo(Grupo grupo) {
        executor.execute(() -> grupoDao.actualizar(grupo));
    }

    public void eliminarGrupo(Grupo grupo) {
        executor.execute(() -> {
            balanceDao.eliminarBalancesByGrupo(grupo.id);
            grupoDao.eliminar(grupo);
        });
    }

    // ─── Miembros ─────────────────────────────────────────────────────────────

    public LiveData<List<MiembroGrupo>> getMiembrosByGrupo(long grupoId) {
        return miembroDao.getMiembrosByGrupo(grupoId);
    }

    public void agregarMiembro(MiembroGrupo miembro) {
        executor.execute(() -> miembroDao.insertar(miembro));
    }

    public void eliminarMiembro(MiembroGrupo miembro) {
        executor.execute(() -> miembroDao.eliminar(miembro));
    }

    // ─── Gastos de grupo ──────────────────────────────────────────────────────

    public LiveData<List<GastoGrupo>> getGastosByGrupo(long grupoId) {
        return gastoGrupoDao.getGastosByGrupo(grupoId);
    }

    public LiveData<Double> getTotalGastosGrupo(long grupoId) {
        return gastoGrupoDao.getTotalGastosGrupo(grupoId);
    }

    /** Inserta el gasto y recalcula balances en el mismo bloque de BD. */
    public Future<Long> insertarGastoYRecalcular(GastoGrupo gasto) {
        return executor.submit(() -> {
            long id = gastoGrupoDao.insertar(gasto);
            if (id > 0) recalcularBalances(gasto.grupoId);
            return id;
        });
    }

    public void actualizarGastoGrupo(GastoGrupo gasto) {
        executor.execute(() -> {
            gastoGrupoDao.actualizar(gasto);
            recalcularBalances(gasto.grupoId);
        });
    }

    public void eliminarGastoGrupo(GastoGrupo gasto) {
        executor.execute(() -> {
            fotoDao.eliminarFotosByGasto(gasto.id);
            gastoGrupoDao.eliminar(gasto);
            recalcularBalances(gasto.grupoId);
        });
    }

    // ─── Balances ─────────────────────────────────────────────────────────────

    public LiveData<List<BalanceGrupo>> getBalancesByGrupo(long grupoId) {
        return balanceDao.getBalancesByGrupo(grupoId);
    }

    public LiveData<BalanceGrupo> getBalanceUsuario(long grupoId, long usuarioId) {
        return balanceDao.getBalanceUsuario(grupoId, usuarioId);
    }

    /**
     * Recalcula los balances de todos los miembros del grupo.
     *
     * balance(i) = totalPagadoPor(i) − cuotaEquitativa
     *   > 0: los demás te deben dinero
     *   < 0: tú debes al grupo
     */
    private void recalcularBalances(long grupoId) {
        List<MiembroGrupo> miembros = miembroDao.getMiembrosByGrupoSync(grupoId);
        if (miembros == null || miembros.isEmpty()) return;

        int n = miembros.size();

        double totalGastos = 0;
        List<GastoGrupo> gastos = gastoGrupoDao.getGastosByGrupoSync(grupoId);
        if (gastos != null) {
            for (GastoGrupo g : gastos) totalGastos += g.monto;
        }

        double cuota = totalGastos / n;

        List<BalanceGrupo> nuevos = new ArrayList<>();
        for (MiembroGrupo miembro : miembros) {
            // miembro.usuarioId es el campo Java de MiembroGrupo (int/long según tu entidad)
            double pagado = gastoGrupoDao.getTotalPagadoPorUsuarioSync(grupoId, miembro.usuarioId);

            BalanceGrupo balance = new BalanceGrupo();
            balance.grupoId             = (int) grupoId;
            balance.usuarioId           = miembro.usuarioId;
            balance.montoPendiente      = pagado - cuota;
            balance.ultimaActualizacion = System.currentTimeMillis();

            nuevos.add(balance);
        }

        balanceDao.upsertVarios(nuevos);
    }

}
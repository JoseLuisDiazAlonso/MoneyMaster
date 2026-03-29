package com.example.moneymaster.data.service;

import android.content.Context;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.BalanceGrupoDao;
import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.dao.MiembroGrupoDao;
import com.example.moneymaster.data.model.BalanceGrupo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.ui.groups.model.DeudaItem;
import com.example.moneymaster.ui.groups.model.MiembroBalanceItem;
import com.example.moneymaster.ui.groups.util.BalanceCalculator;

import java.util.ArrayList;
import java.util.List;

public class BalancePersistenceService {

    /**
     * Recalcula todos los balances de un grupo y los persiste en Room.
     * Debe ejecutarse en un hilo de background.
     *
     * @param context contexto de aplicación
     * @param grupoId ID del grupo a recalcular
     */
    public static void recalcularYPersistir(Context context, int grupoId) {
        AppDatabase db = AppDatabase.getDatabase(context);

        GastoGrupoDao   gastoDao   = db.gastoGrupoDao();
        MiembroGrupoDao miembroDao = db.miembroGrupoDao();
        BalanceGrupoDao balanceDao = db.balanceGrupoDao();

        // 1. Obtener datos actuales de forma síncrona
        List<GastoGrupo>   gastos   = gastoDao.getGastosByGrupoSync(grupoId);
        List<MiembroGrupo> miembros = miembroDao.getMiembrosByGrupoSync(grupoId);

        if (miembros == null || miembros.isEmpty()) return;
        if (gastos == null) gastos = new ArrayList<>();

        // 2. Calcular balances netos y deudas con BalanceCalculator (Card #28)
        List<MiembroBalanceItem> balancesNetos =
                BalanceCalculator.calcularBalancesMiembros(miembros, gastos);
        List<DeudaItem> deudas =
                BalanceCalculator.calcularDeudas(balancesNetos);

        // 3. Construir entidades BalanceGrupo para persistir
        List<BalanceGrupo> entidades = new ArrayList<>();
        long ahora = System.currentTimeMillis();

        // Mapa nombre → MiembroGrupo para buscar IDs
        java.util.Map<String, MiembroGrupo> mapaMiembros = new java.util.HashMap<>();
        for (MiembroGrupo m : miembros) {
            if (m.nombre != null) mapaMiembros.put(m.nombre, m);
        }

        for (DeudaItem deuda : deudas) {
            MiembroGrupo deudor   = mapaMiembros.get(deuda.nombreDeudor);
            MiembroGrupo acreedor = mapaMiembros.get(deuda.nombreAcreedor);

            if (deudor == null || acreedor == null) continue;

            BalanceGrupo balance = new BalanceGrupo();
            balance.grupoId             = grupoId;
            balance.usuarioDeudorId     = deudor.id;
            balance.usuarioAcreedorId   = acreedor.id;
            balance.montoPendiente      = deuda.monto;
            balance.ultimaActualizacion = ahora;
            balance.liquidado           = 0;

            entidades.add(balance);
        }

        // 4. Borra los balances anteriores del grupo y persiste los nuevos
        balanceDao.eliminarBalancesByGrupo(grupoId);
        if (!entidades.isEmpty()) {
            balanceDao.upsertVarios(entidades);
        }
    }
}

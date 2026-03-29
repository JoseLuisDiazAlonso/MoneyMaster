package com.example.moneymaster.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.ui.groups.model.MemberBalanceItem;
import com.example.moneymaster.utils.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupExpensesViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private int grupoId = -1;

    // LiveData expuestos a la UI
    private LiveData<Grupo>             grupo;
    private LiveData<List<GastoGrupo>>  gastos;
    private LiveData<List<MiembroGrupo>> miembros;

    // Balance calculado: total pagado por cada miembro
    private final MediatorLiveData<List<MemberBalanceItem>> balancesPorMiembro
            = new MediatorLiveData<>();

    // Fuentes actuales para el MediatorLiveData
    private LiveData<List<GastoGrupo>>  gastosSource;
    private LiveData<List<MiembroGrupo>> miembrosSource;

    public GroupExpensesViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    /**
     * Inicializa el ViewModel con el ID del grupo.
     * Debe llamarse desde onViewCreated() antes de observar cualquier LiveData.
     */
    public void init(int grupoId) {
        if (this.grupoId == grupoId) return;
        this.grupoId = grupoId;

        grupo    = db.grupoDao().getGrupoById(grupoId);
        gastos   = db.gastoGrupoDao().getGastosByGrupo(grupoId);
        miembros = db.miembroGrupoDao().getMiembrosByGrupo(grupoId);

        // Eliminar fuentes anteriores si existían
        if (gastosSource != null)    balancesPorMiembro.removeSource(gastosSource);
        if (miembrosSource != null)  balancesPorMiembro.removeSource(miembrosSource);

        gastosSource   = gastos;
        miembrosSource = miembros;

        balancesPorMiembro.addSource(gastosSource,   g -> recalcularBalances());
        balancesPorMiembro.addSource(miembrosSource, m -> recalcularBalances());
    }

    // ─── LiveData públicos ────────────────────────────────────────────────────

    public LiveData<Grupo>              getGrupo()             { return grupo; }
    public LiveData<List<GastoGrupo>>   getGastos()            { return gastos; }
    public LiveData<List<MiembroGrupo>> getMiembros()          { return miembros; }
    public LiveData<List<MemberBalanceItem>> getBalancesPorMiembro() {
        return balancesPorMiembro;
    }
    public LiveData<Double> getTotalGrupo() {
        return db.gastoGrupoDao().getTotalGastosGrupo(grupoId);
    }

    // ─── Cálculo de balances ──────────────────────────────────────────────────

    private void recalcularBalances() {
        List<GastoGrupo>   listaGastos   = gastosSource   != null ? gastosSource.getValue()   : null;
        List<MiembroGrupo> listaMiembros = miembrosSource != null ? miembrosSource.getValue() : null;

        if (listaMiembros == null) {
            balancesPorMiembro.setValue(new ArrayList<>());
            return;
        }

        Map<String, Double> totalesPorNombre = new HashMap<>();
        if (listaGastos != null) {
            for (GastoGrupo g : listaGastos) {
                if (g.pagadoPorNombre != null) {
                    double actual = totalesPorNombre.containsKey(g.pagadoPorNombre)
                            ? totalesPorNombre.get(g.pagadoPorNombre) : 0.0;
                    totalesPorNombre.put(g.pagadoPorNombre, actual + g.monto);
                }
            }
        }

        List<MemberBalanceItem> resultado = new ArrayList<>();
        for (MiembroGrupo m : listaMiembros) {
            if (m.nombre == null) continue;
            double total = totalesPorNombre.containsKey(m.nombre)
                    ? totalesPorNombre.get(m.nombre) : 0.0;
            resultado.add(new MemberBalanceItem(m.nombre, m.color, total));
        }

        balancesPorMiembro.setValue(resultado);
    }

    // ─── Escrituras ───────────────────────────────────────────────────────────

    public void eliminarGasto(GastoGrupo gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                db.gastoGrupoDao().eliminar(gasto));
    }

    // ─── Card #33: eliminar foto manteniendo el gasto ─────────────────────────

    /**
     * Desvincula la foto de un gasto de grupo y elimina el archivo físico.
     * El gasto permanece en la base de datos con fotoReciboId = null.
     *
     * Flujo:
     *   1. Busca GastoGrupo por ID
     *   2. Guarda fotoReciboId antes de nullearlo
     *   3. Actualiza GastoGrupo con fotoReciboId = null
     *   4. Elimina FotoRecibo de Room + archivo físico del disco
     */
    public void eliminarFotoDeGasto(int gastoId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            GastoGrupo gasto = db.gastoGrupoDao().getGastoById(gastoId).getValue();
            if (gasto == null || gasto.foto_recibo_id == null) return;

            int fotoId = gasto.foto_recibo_id;

            // 1. Desvincular foto del gasto
            gasto.foto_recibo_id = null;
            db.gastoGrupoDao().actualizar(gasto);

            // 2. Eliminar FotoRecibo + archivo físico
            FotoRecibo foto = db.fotoReciboDao().getById(fotoId);
            if (foto != null) {
                ImageUtils.eliminarFoto(foto.rutaArchivo);
                if (foto.miniaturaRuta != null) {
                    ImageUtils.eliminarFoto(foto.miniaturaRuta);
                }
                db.fotoReciboDao().eliminar(foto);
            }
        });
    }
}

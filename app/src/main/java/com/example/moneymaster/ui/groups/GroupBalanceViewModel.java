package com.example.moneymaster.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.ui.groups.model.DeudaItem;
import com.example.moneymaster.ui.groups.model.MiembroBalanceItem;
import com.example.moneymaster.ui.groups.util.BalanceCalculator;

import java.util.ArrayList;
import java.util.List;

public class GroupBalanceViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private int grupoId = -1;

    private LiveData<List<GastoGrupo>>   gastosSource;
    private LiveData<List<MiembroGrupo>> miembrosSource;

    // Balance neto por miembro
    private final MediatorLiveData<List<MiembroBalanceItem>> balancesMiembros
            = new MediatorLiveData<>();

    // Deudas calculadas (transacciones sugeridas)
    private final MediatorLiveData<List<DeudaItem>> deudas
            = new MediatorLiveData<>();

    public GroupBalanceViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    /**
     * Inicializa el ViewModel con el ID del grupo.
     * Llamar desde onViewCreated() antes de observar LiveData.
     */
    public void init(int grupoId) {
        if (this.grupoId == grupoId) return;
        this.grupoId = grupoId;

        gastosSource   = db.gastoGrupoDao().getGastosByGrupo(grupoId);
        miembrosSource = db.miembroGrupoDao().getMiembrosByGrupo(grupoId);

        // Recalcular cuando cambia cualquier fuente
        balancesMiembros.addSource(gastosSource,   g -> recalcular());
        balancesMiembros.addSource(miembrosSource, m -> recalcular());
        deudas.addSource(gastosSource,   g -> recalcular());
        deudas.addSource(miembrosSource, m -> recalcular());
    }

    // ─── LiveData públicos ────────────────────────────────────────────────────

    public LiveData<List<MiembroBalanceItem>> getBalancesMiembros() {
        return balancesMiembros;
    }

    public LiveData<List<DeudaItem>> getDeudas() {
        return deudas;
    }

    // ─── Recálculo reactivo ───────────────────────────────────────────────────

    private void recalcular() {
        List<GastoGrupo>   listaGastos   = gastosSource   != null ? gastosSource.getValue()   : null;
        List<MiembroGrupo> listaMiembros = miembrosSource != null ? miembrosSource.getValue() : null;

        if (listaMiembros == null) {
            balancesMiembros.setValue(new ArrayList<>());
            deudas.setValue(new ArrayList<>());
            return;
        }
        if (listaGastos == null) listaGastos = new ArrayList<>();

        // Paso 1: balances netos
        List<MiembroBalanceItem> balances =
                BalanceCalculator.calcularBalancesMiembros(listaMiembros, listaGastos);
        balancesMiembros.setValue(balances);

        // Paso 2: deudas mínimas
        deudas.setValue(BalanceCalculator.calcularDeudas(balances));
    }

    // ─── Marcar deuda como pagada (en memoria) ────────────────────────────────

    /**
     * Marca una deuda como pagada en la lista actual.
     * La lista se actualiza reactivamente mediante setValue.
     */
    public void marcarPagada(int posicion) {
        List<DeudaItem> lista = deudas.getValue();
        if (lista == null || posicion < 0 || posicion >= lista.size()) return;
        lista.get(posicion).pagado = true;
        deudas.setValue(new ArrayList<>(lista)); // copia para forzar observer
    }
}

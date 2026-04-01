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

    private final MediatorLiveData<List<MiembroBalanceItem>> balancesMiembros
            = new MediatorLiveData<>();
    private final MediatorLiveData<List<DeudaItem>> deudas
            = new MediatorLiveData<>();

    public GroupBalanceViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public void init(int grupoId) {
        if (this.grupoId == grupoId) return;
        this.grupoId = grupoId;

        gastosSource   = db.gastoGrupoDao().getGastosByGrupo(grupoId);
        miembrosSource = db.miembroGrupoDao().getMiembrosByGrupo(grupoId);

        balancesMiembros.addSource(gastosSource,   g -> recalcular());
        balancesMiembros.addSource(miembrosSource, m -> recalcular());
        deudas.addSource(gastosSource,   g -> recalcular());
        deudas.addSource(miembrosSource, m -> recalcular());
    }

    public LiveData<List<MiembroBalanceItem>> getBalancesMiembros() {
        return balancesMiembros;
    }

    public LiveData<List<DeudaItem>> getDeudas() {
        return deudas;
    }

    private void recalcular() {
        List<GastoGrupo>   listaGastos   = gastosSource   != null ? gastosSource.getValue()   : null;
        List<MiembroGrupo> listaMiembros = miembrosSource != null ? miembrosSource.getValue() : null;

        if (listaMiembros == null) {
            balancesMiembros.setValue(new ArrayList<>());
            deudas.setValue(new ArrayList<>());
            return;
        }
        if (listaGastos == null) listaGastos = new ArrayList<>();

        List<MiembroBalanceItem> balances =
                BalanceCalculator.calcularBalancesMiembros(listaMiembros, listaGastos);
        balancesMiembros.setValue(balances);
        deudas.setValue(BalanceCalculator.calcularDeudas(balances));
    }

    public void marcarPagada(int posicion) {
        List<DeudaItem> listaActual = deudas.getValue();
        if (listaActual == null || posicion < 0 || posicion >= listaActual.size()) return;

        List<DeudaItem> nuevaLista = new ArrayList<>();
        for (int i = 0; i < listaActual.size(); i++) {
            DeudaItem original = listaActual.get(i);
            boolean esPagado = (i == posicion) || original.pagado;
            nuevaLista.add(new DeudaItem(
                    original.nombreDeudor,
                    original.colorDeudor,
                    original.nombreAcreedor,
                    original.monto,
                    esPagado
            ));
        }
        deudas.setValue(nuevaLista);
    }
}
package com.example.moneymaster.ui.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.repository.GastoRepository;
import com.example.moneymaster.data.repository.IngresoRepository;

import java.util.Calendar;
import java.util.List;


public class MainViewModel extends AndroidViewModel {

    private final GastoRepository gastoRepository;
    private final IngresoRepository ingresoRepository;

    private final int mesActual;
    private final int anioActual;

    // Disparador: cuando cambia el usuarioId todos los switchMap reaccionan
    private final MutableLiveData<Long> usuarioIdLive = new MutableLiveData<>();

    //LiveData públicos

    public final LiveData<Double> totalGastosMes;
    public final LiveData<Double> totalIngresosMes;

    /**
     * Balance neto = ingresos − gastos.
     * MediatorLiveData combina dos fuentes LiveData en un solo observable.
     */
    public final MediatorLiveData<Double> balanceMes = new MediatorLiveData<>();

    /** Últimas 5 transacciones de gasto para el widget del dashboard. */
    public final LiveData<List<GastoPersonal>> ultimosGastos;

    /** Últimas 3 transacciones de ingreso. */
    public final LiveData<List<IngresoPersonal>> ultimosIngresos;

    //Constructor

    public MainViewModel(@NonNull Application application) {
        super(application);
        gastoRepository   = new GastoRepository(application);
        ingresoRepository = new IngresoRepository(application);

        Calendar cal = Calendar.getInstance();
        mesActual  = cal.get(Calendar.MONTH) + 1; // MONTH es 0-based
        anioActual = cal.get(Calendar.YEAR);

        totalGastosMes = Transformations.switchMap(usuarioIdLive, id ->
                gastoRepository.getTotalGastosMes(id, mesActual, anioActual));

        totalIngresosMes = Transformations.switchMap(usuarioIdLive, id ->
                ingresoRepository.getTotalIngresosMes(id, mesActual, anioActual));

        ultimosGastos = Transformations.switchMap(usuarioIdLive, id ->
                gastoRepository.getUltimosGastos(id, 5));

        ultimosIngresos = Transformations.switchMap(usuarioIdLive, id ->
                ingresoRepository.getUltimosIngresos(id, 3));

        // Recalcula el balance cuando cambia cualquiera de las dos fuentes
        balanceMes.addSource(totalIngresosMes, v -> recalcularBalance());
        balanceMes.addSource(totalGastosMes,   v -> recalcularBalance());
    }

    //API pública

    public void setUsuarioId(long id) {
        if (!Long.valueOf(id).equals(usuarioIdLive.getValue())) {
            usuarioIdLive.setValue(id);
        }
    }

    public int getMesActual()  { return mesActual; }
    public int getAnioActual() { return anioActual; }

    //Privado

    private void recalcularBalance() {
        double i = totalIngresosMes.getValue() != null ? totalIngresosMes.getValue() : 0.0;
        double g = totalGastosMes.getValue()   != null ? totalGastosMes.getValue()   : 0.0;
        balanceMes.setValue(i - g);
    }
}

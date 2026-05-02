package com.example.moneymaster.ui.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.GastoConCategoria;
import com.example.moneymaster.data.model.IngresoConCategoria;
import com.example.moneymaster.data.model.MovimientoReciente;
import com.example.moneymaster.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;


public class DashboardViewModel extends AndroidViewModel {

    // Dependencias
    private final AppDatabase db;
    private final int         userId;

    // Estado del mes seleccionado
    private final Calendar calendarActual;

    private final MutableLiveData<long[]> rangoMes = new MutableLiveData<>();

    // LiveData de Room (reactivos al rangoMes)
    private final LiveData<List<GastoConCategoria>>   gastosMes;
    private final LiveData<List<IngresoConCategoria>> ingresosMes;

    // LiveData expuestos al Fragment
    public final MediatorLiveData<List<MovimientoReciente>> movimientos =
            new MediatorLiveData<>();

    private final MutableLiveData<Double> _balance       = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> _totalIngresos = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> _totalGastos   = new MutableLiveData<>(0.0);

    public final LiveData<Double> balance       = _balance;
    public final LiveData<Double> totalIngresos = _totalIngresos;
    public final LiveData<Double> totalGastos   = _totalGastos;

    public final MutableLiveData<String> etiquetaMes = new MutableLiveData<>();

    private static final int MAX_MOVIMIENTOS = 10;

    // FIX: Locale.getDefault() en lugar de Locale("es","ES") fijo
    // Así el nombre del mes cambia automáticamente con el idioma del dispositivo
    private static final SimpleDateFormat FORMAT_MES =
            new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    // Constructor

    public DashboardViewModel(@NonNull Application application) {
        super(application);

        db     = AppDatabase.getDatabase(application);
        userId = new SessionManager(application).getUserId();

        calendarActual = Calendar.getInstance();
        actualizarRangoMes();

        gastosMes = Transformations.switchMap(rangoMes, rango ->
                db.gastoPersonalDao().getGastosPorCategoria(
                        userId, rango[0], rango[1]));

        ingresosMes = Transformations.switchMap(rangoMes, rango ->
                db.ingresoPersonalDao().getIngresosPorCategoria(
                        userId, rango[0], rango[1]));

        movimientos.addSource(gastosMes,   gastos   -> recalcular());
        movimientos.addSource(ingresosMes, ingresos -> recalcular());
    }

    // Navegación de meses

    public void mesAnterior() {
        calendarActual.add(Calendar.MONTH, -1);
        actualizarRangoMes();
    }

    public void mesSiguiente() {
        calendarActual.add(Calendar.MONTH, 1);
        actualizarRangoMes();
    }

    private void actualizarRangoMes() {
        Calendar inicio = (Calendar) calendarActual.clone();
        inicio.set(Calendar.DAY_OF_MONTH, 1);
        inicio.set(Calendar.HOUR_OF_DAY,  0);
        inicio.set(Calendar.MINUTE,       0);
        inicio.set(Calendar.SECOND,       0);
        inicio.set(Calendar.MILLISECOND,  0);

        Calendar fin = (Calendar) inicio.clone();
        fin.add(Calendar.MONTH, 1);

        rangoMes.setValue(new long[]{ inicio.getTimeInMillis(), fin.getTimeInMillis() });

        // FIX: usar Locale.getDefault() para que la primera letra en mayúscula
        // funcione correctamente en cualquier idioma
        Locale locale = Locale.getDefault();
        SimpleDateFormat formatMes = new SimpleDateFormat("MMMM yyyy", locale);
        String label = formatMes.format(calendarActual.getTime());
        etiquetaMes.setValue(
                label.substring(0, 1).toUpperCase(locale) + label.substring(1)
        );
    }

    // Recálculo combinado

    private void recalcular() {
        List<GastoConCategoria>   gastos   = gastosMes.getValue();
        List<IngresoConCategoria> ingresos = ingresosMes.getValue();

        if (gastos   == null) gastos   = Collections.emptyList();
        if (ingresos == null) ingresos = Collections.emptyList();

        List<MovimientoReciente> combinada = new ArrayList<>();

        double sumGastos = 0.0;
        for (GastoConCategoria g : gastos) {
            sumGastos += g.importe;
            combinada.add(new MovimientoReciente(
                    g.id,
                    MovimientoReciente.Tipo.GASTO,
                    g.descripcion,
                    g.nombreCategoria,
                    g.iconoNombre,
                    g.colorCategoria,
                    g.importe,
                    g.fecha
            ));
        }

        double sumIngresos = 0.0;
        for (IngresoConCategoria i : ingresos) {
            sumIngresos += i.importe;
            combinada.add(new MovimientoReciente(
                    i.id,
                    MovimientoReciente.Tipo.INGRESO,
                    i.descripcion,
                    i.nombreCategoria,
                    i.iconoNombre,
                    i.colorCategoria,
                    i.importe,
                    i.fecha
            ));
        }

        Collections.sort(combinada,
                (a, b) -> Long.compare(b.getFecha(), a.getFecha()));

        List<MovimientoReciente> recortada = combinada.size() > MAX_MOVIMIENTOS
                ? combinada.subList(0, MAX_MOVIMIENTOS)
                : combinada;

        movimientos.setValue(new ArrayList<>(recortada));
        _totalGastos.setValue(sumGastos);
        _totalIngresos.setValue(sumIngresos);
        _balance.setValue(sumIngresos - sumGastos);
    }

    public void eliminarMovimiento(MovimientoReciente movimiento) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (movimiento.getTipo() == MovimientoReciente.Tipo.GASTO) {
                GastoPersonal gasto = new GastoPersonal();
                gasto.id = movimiento.getId();
                db.gastoPersonalDao().eliminar(gasto);
            } else {
                IngresoPersonal ingreso = new IngresoPersonal();
                ingreso.id = movimiento.getId();
                db.ingresoPersonalDao().eliminar(ingreso);
            }
        });
    }
}

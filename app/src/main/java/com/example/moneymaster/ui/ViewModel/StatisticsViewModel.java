package com.example.moneymaster.ui.ViewModel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.repository.GastoRepository;
import com.example.moneymaster.data.repository.IngresoRepository;

import java.util.Calendar;
import java.util.List;


public class StatisticsViewModel extends AndroidViewModel {

    //Repositorios
    private final GastoRepository   gastoRepo;
    private final IngresoRepository ingresoRepo;

    //ID de usuario (obtenido de SharedPreferences)
    private final long usuarioId;

    //Estado de navegación y filtros
    private final MutableLiveData<Integer> selectedTab   = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedMonth;
    private final MutableLiveData<Integer> selectedYear;
    private final MutableLiveData<Long> customStartTimestamp = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> customEndTimestamp   = new MutableLiveData<>(0L);

    //Datos de movimientos
    private LiveData<List<GastoPersonal>>   gastosMes;
    private LiveData<List<IngresoPersonal>> ingresosMes;
    private LiveData<List<GastoPersonal>>   gastosAnio;
    private LiveData<List<IngresoPersonal>> ingresosAnio;
    private LiveData<List<GastoPersonal>>   gastosCustom;
    private LiveData<List<IngresoPersonal>> ingresosCustom;

    //Resumen numérico
    private final MediatorLiveData<Double> summaryTotalGastos   = new MediatorLiveData<>();
    private final MediatorLiveData<Double> summaryTotalIngresos = new MediatorLiveData<>();
    private final MediatorLiveData<Double> summaryBalance       = new MediatorLiveData<>();

    //Constructor

    public StatisticsViewModel(@NonNull Application application) {
        super(application);

        gastoRepo   = new GastoRepository(application);
        ingresoRepo = new IngresoRepository(application);

        // Obtener usuarioId de SharedPreferences (mismo patrón que el resto del proyecto)
        SharedPreferences prefs = application.getSharedPreferences(
                "MoneyMasterPrefs", Context.MODE_PRIVATE);
        usuarioId = prefs.getInt("userId", -1);

        // Inicializar filtros con el mes/año actual
        Calendar now = Calendar.getInstance();
        selectedMonth = new MutableLiveData<>(now.get(Calendar.MONTH) + 1); // 1–12
        selectedYear  = new MutableLiveData<>(now.get(Calendar.YEAR));

        loadCurrentMonthData();
        loadCurrentYearData();
    }

    //Carga de datos
    public void loadCurrentMonthData() {
        int mes  = selectedMonth.getValue() != null ? selectedMonth.getValue() : 1;
        int anio = selectedYear.getValue()  != null ? selectedYear.getValue()  : 2024;

        // Eliminar fuentes anteriores para evitar duplicados en MediatorLiveData
        if (gastosMes   != null) summaryTotalGastos.removeSource(gastosMes);
        if (ingresosMes != null) summaryTotalIngresos.removeSource(ingresosMes);

        gastosMes   = gastoRepo.getGastosByMes(usuarioId, mes, anio);
        ingresosMes = ingresoRepo.getIngresosByMes(usuarioId, mes, anio);

        summaryTotalGastos.addSource(gastosMes,     g -> recalcularResumen());
        summaryTotalIngresos.addSource(ingresosMes, i -> recalcularResumen());
    }

    public void loadCurrentYearData() {
        int anio = selectedYear.getValue() != null ? selectedYear.getValue() : 2024;
        gastosAnio   = gastoRepo.getGastosByAnio(usuarioId, anio);
        ingresosAnio = ingresoRepo.getIngresosByAnio(usuarioId, anio);
    }

    public void loadCustomRangeData(long startTimestamp, long endTimestamp) {
        customStartTimestamp.setValue(startTimestamp);
        customEndTimestamp.setValue(endTimestamp);
        gastosCustom   = gastoRepo.getGastosByRango(usuarioId, startTimestamp, endTimestamp);
        ingresosCustom = ingresoRepo.getIngresosByRango(usuarioId, startTimestamp, endTimestamp);
    }

    //Resumen numérico
    private void recalcularResumen() {
        double totalGastos   = 0;
        double totalIngresos = 0;

        List<GastoPersonal> gastos = gastosMes != null ? gastosMes.getValue() : null;
        if (gastos != null) {
            for (GastoPersonal g : gastos) totalGastos += g.monto;
        }

        List<IngresoPersonal> ingresos = ingresosMes != null ? ingresosMes.getValue() : null;
        if (ingresos != null) {
            for (IngresoPersonal i : ingresos) totalIngresos += i.monto;
        }

        summaryTotalGastos.setValue(totalGastos);
        summaryTotalIngresos.setValue(totalIngresos);
        summaryBalance.setValue(totalIngresos - totalGastos);
    }

    //Setters

    public void setSelectedTab(int tab) { selectedTab.setValue(tab); }

    public void setMonthYear(int month, int year) {
        selectedMonth.setValue(month);
        selectedYear.setValue(year);
        loadCurrentMonthData();
        loadCurrentYearData();
    }

    //Getters

    public LiveData<Integer> getSelectedTab()    { return selectedTab; }
    public LiveData<Integer> getSelectedMonth()  { return selectedMonth; }
    public LiveData<Integer> getSelectedYear()   { return selectedYear; }

    public LiveData<List<GastoPersonal>>   getGastosMes()      { return gastosMes; }
    public LiveData<List<IngresoPersonal>> getIngresosMes()    { return ingresosMes; }
    public LiveData<List<GastoPersonal>>   getGastosAnio()     { return gastosAnio; }
    public LiveData<List<IngresoPersonal>> getIngresosAnio()   { return ingresosAnio; }
    public LiveData<List<GastoPersonal>>   getGastosCustom()   { return gastosCustom; }
    public LiveData<List<IngresoPersonal>> getIngresosCustom() { return ingresosCustom; }

    public LiveData<Double> getSummaryTotalGastos()   { return summaryTotalGastos; }
    public LiveData<Double> getSummaryTotalIngresos() { return summaryTotalIngresos; }
    public LiveData<Double> getSummaryBalance()       { return summaryBalance; }

    public LiveData<Long> getCustomStartTimestamp() { return customStartTimestamp; }
    public LiveData<Long> getCustomEndTimestamp()   { return customEndTimestamp; }
}
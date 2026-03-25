package com.example.moneymaster.ui.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.repository.GastoPersonalRepository;
import com.example.moneymaster.data.repository.IngresoPersonalRepository;

import java.util.Calendar;
import java.util.List;

/**
 * ViewModel compartido para todos los fragmentos de Estadísticas.
 *
 * ÁMBITO: Se instancia con requireActivity() desde cualquier fragmento hijo,
 * lo que garantiza una única instancia mientras la Activity viva.
 *
 * DATOS QUE EXPONE:
 *  - selectedTab          → pestaña activa (0 Mes / 1 Año / 2 Personalizado)
 *  - selectedMonth/Year   → filtro de mes y año activo
 *  - customStartTimestamp / customEndTimestamp → rango personalizado
 *  - gastosMes / ingresosMes         → movimientos del mes seleccionado
 *  - gastosAnio / ingresosAnio       → movimientos del año seleccionado
 *  - gastosCustom / ingresosCustom   → movimientos del rango personalizado
 *  - summaryTotalGastos / summaryTotalIngresos / summaryBalance → resumen numérico
 */
public class StatisticsViewModel extends AndroidViewModel {

    // ──────────────────────────────────────────────────────────────────────────
    // Repositorios
    // ──────────────────────────────────────────────────────────────────────────
    private final GastoPersonalRepository gastoRepo;
    private final IngresoPersonalRepository ingresoRepo;

    // ──────────────────────────────────────────────────────────────────────────
    // Estado de navegación y filtros
    // ──────────────────────────────────────────────────────────────────────────
    private final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedMonth;
    private final MutableLiveData<Integer> selectedYear;
    private final MutableLiveData<Long> customStartTimestamp = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> customEndTimestamp   = new MutableLiveData<>(0L);

    // ──────────────────────────────────────────────────────────────────────────
    // Datos de movimientos
    // ──────────────────────────────────────────────────────────────────────────
    private LiveData<List<GastoPersonal>>   gastosMes;
    private LiveData<List<IngresoPersonal>> ingresosMes;
    private LiveData<List<GastoPersonal>>   gastosAnio;
    private LiveData<List<IngresoPersonal>> ingresosAnio;
    private LiveData<List<GastoPersonal>>   gastosCustom;
    private LiveData<List<IngresoPersonal>> ingresosCustom;

    // ──────────────────────────────────────────────────────────────────────────
    // Resumen numérico calculado como MediatorLiveData
    // ──────────────────────────────────────────────────────────────────────────
    private final MediatorLiveData<Double> summaryTotalGastos   = new MediatorLiveData<>();
    private final MediatorLiveData<Double> summaryTotalIngresos = new MediatorLiveData<>();
    private final MediatorLiveData<Double> summaryBalance       = new MediatorLiveData<>();

    // ──────────────────────────────────────────────────────────────────────────
    // Constructor
    // ──────────────────────────────────────────────────────────────────────────
    public StatisticsViewModel(@NonNull Application application) {
        super(application);

        gastoRepo   = new GastoPersonalRepository(application);
        ingresoRepo = new IngresoPersonalRepository(application);

        // Inicializar filtros con el mes/año actual
        Calendar now = Calendar.getInstance();
        selectedMonth = new MutableLiveData<>(now.get(Calendar.MONTH) + 1); // 1–12
        selectedYear  = new MutableLiveData<>(now.get(Calendar.YEAR));

        loadCurrentMonthData();
        loadCurrentYearData();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Carga de datos
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Carga gastos e ingresos del mes/año actualmente seleccionados y
     * recalcula el resumen numérico.
     */
    public void loadCurrentMonthData() {
        int mes  = selectedMonth.getValue() != null ? selectedMonth.getValue() : 1;
        int anio = selectedYear.getValue()  != null ? selectedYear.getValue()  : 2024;

        gastosMes   = gastoRepo.getGastosByMonthYear(mes, anio);
        ingresosMes = ingresoRepo.getIngresosByMonthYear(mes, anio);

        // Recalcular resumen cada vez que cambien los datos del mes
        summaryTotalGastos.addSource(gastosMes, gastos -> recalcularResumen());
        summaryTotalIngresos.addSource(ingresosMes, ingresos -> recalcularResumen());
    }

    /**
     * Carga gastos e ingresos de todo el año seleccionado.
     */
    public void loadCurrentYearData() {
        int anio = selectedYear.getValue() != null ? selectedYear.getValue() : 2024;
        gastosAnio   = gastoRepo.getGastosByYear(anio);
        ingresosAnio = ingresoRepo.getIngresosByYear(anio);
    }

    /**
     * Carga datos para el rango personalizado de fechas.
     * @param startTimestamp Unix timestamp de inicio (inclusive)
     * @param endTimestamp   Unix timestamp de fin   (inclusive)
     */
    public void loadCustomRangeData(long startTimestamp, long endTimestamp) {
        customStartTimestamp.setValue(startTimestamp);
        customEndTimestamp.setValue(endTimestamp);
        gastosCustom   = gastoRepo.getGastosByDateRange(startTimestamp, endTimestamp);
        ingresosCustom = ingresoRepo.getIngresosByDateRange(startTimestamp, endTimestamp);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lógica de resumen numérico
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Suma todos los importes de la lista activa y publica los resultados
     * en los tres MediatorLiveData de resumen.
     */
    private void recalcularResumen() {
        double totalGastos = 0;
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

    // ──────────────────────────────────────────────────────────────────────────
    // Setters de estado
    // ──────────────────────────────────────────────────────────────────────────

    public void setSelectedTab(int tab) { selectedTab.setValue(tab); }

    /**
     * Cambia el mes/año del filtro y recarga los datos correspondientes.
     */
    public void setMonthYear(int month, int year) {
        selectedMonth.setValue(month);
        selectedYear.setValue(year);
        loadCurrentMonthData();
        loadCurrentYearData();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Getters de LiveData
    // ──────────────────────────────────────────────────────────────────────────

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
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

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  DashboardViewModel.java                                         ║
 * ║  Ruta: ui/viewmodel/DashboardViewModel.java                      ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  ViewModel del Fragment de Inicio (Dashboard).                   ║
 * ║                                                                  ║
 * ║  Responsabilidades:                                              ║
 * ║  1. Gestionar el mes seleccionado (prev/next).                   ║
 * ║  2. Lanzar queries reactivas a Room a través de switchMap.       ║
 * ║  3. Combinar gastos + ingresos con MediatorLiveData.             ║
 * ║  4. Exponer balance, totales y lista de movimientos al Fragment. ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class DashboardViewModel extends AndroidViewModel {

    // ─── Dependencias ────────────────────────────────────────────────────────
    private final AppDatabase db;
    private final int         userId;

    // ─── Estado del mes seleccionado ─────────────────────────────────────────
    /**
     * Guarda el mes que está viendo el usuario.
     * Se modifica con mesAnterior() y mesSiguiente().
     */
    private final Calendar calendarActual;

    /**
     * Trigger interno: array [inicioMes, finMes] en ms Unix.
     * Cada cambio aquí dispara nuevas queries en Room vía switchMap.
     */
    private final MutableLiveData<long[]> rangoMes = new MutableLiveData<>();

    // ─── LiveData de Room (reactivos al rangoMes) ─────────────────────────────
    private final LiveData<List<GastoConCategoria>>   gastosMes;
    private final LiveData<List<IngresoConCategoria>> ingresosMes;

    // ─── LiveData expuestos al Fragment ──────────────────────────────────────

    /**
     * Hasta 10 movimientos recientes (gastos + ingresos mezclados,
     * ordenados por fecha DESC). Usa MediatorLiveData porque combina
     * dos fuentes independientes.
     */
    public final MediatorLiveData<List<MovimientoReciente>> movimientos =
            new MediatorLiveData<>();

    /** Balance del mes = totalIngresos - totalGastos (puede ser negativo). */
    private final MutableLiveData<Double> _balance       = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> _totalIngresos = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> _totalGastos   = new MutableLiveData<>(0.0);

    public final LiveData<Double> balance       = _balance;
    public final LiveData<Double> totalIngresos = _totalIngresos;
    public final LiveData<Double> totalGastos   = _totalGastos;

    /** Etiqueta del mes, p.ej. "Marzo 2026". Observada por el selector. */
    public final MutableLiveData<String> etiquetaMes = new MutableLiveData<>();

    /** Número máximo de movimientos a mostrar en el dashboard. */
    private static final int MAX_MOVIMIENTOS = 10;

    // ─── Formato de fecha para la etiqueta del mes ───────────────────────────
    private static final SimpleDateFormat FORMAT_MES =
            new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));

    // ─── Constructor ─────────────────────────────────────────────────────────

    public DashboardViewModel(@NonNull Application application) {
        super(application);

        db     = AppDatabase.getInstance(application);
        userId = new SessionManager(application).getUserId();

        calendarActual = Calendar.getInstance();
        actualizarRangoMes(); // emite el primer valor → lanza las queries iniciales

        // ── SwitchMap: cada vez que rangoMes cambia, Room lanza nuevas queries ──
        // switchMap cancela automáticamente las observaciones anteriores,
        // evitando fugas de memoria y resultados de meses anteriores.
        gastosMes = Transformations.switchMap(rangoMes, rango ->
                db.gastoPersonalDao().getGastosConCategoriaDelMes(
                        userId, rango[0], rango[1]));

        ingresosMes = Transformations.switchMap(rangoMes, rango ->
                db.ingresoPersonalDao().getIngresosConCategoriaDelMes(
                        userId, rango[0], rango[1]));

        // ── MediatorLiveData: recalcula cuando llegan datos de cualquier fuente ──
        // Se llama a recalcular() aunque solo llegue una de las dos fuentes,
        // usando getValue() para leer el valor más reciente de la otra.
        movimientos.addSource(gastosMes,   gastos   -> recalcular());
        movimientos.addSource(ingresosMes, ingresos -> recalcular());
    }

    // ─── Navegación de meses ─────────────────────────────────────────────────

    /** Retrocede al mes anterior y dispara nuevas queries. */
    public void mesAnterior() {
        calendarActual.add(Calendar.MONTH, -1);
        actualizarRangoMes();
    }

    /** Avanza al mes siguiente y dispara nuevas queries. */
    public void mesSiguiente() {
        calendarActual.add(Calendar.MONTH, 1);
        actualizarRangoMes();
    }

    /**
     * Calcula el rango [primerMilisegundoDelMes, primerMilisegundoDelMesSiguiente)
     * y actualiza rangoMes y etiquetaMes.
     *
     * Por qué < y no <=:
     * Las fechas son timestamps en ms. Usando < finMes no hay ambigüedad
     * en el último instante del mes (23:59:59.999 < 00:00:00.000 del mes siguiente).
     */
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

        // Etiqueta con la primera letra en mayúscula: "marzo 2026" → "Marzo 2026"
        String label = FORMAT_MES.format(calendarActual.getTime());
        etiquetaMes.setValue(
                label.substring(0, 1).toUpperCase(new Locale("es", "ES")) + label.substring(1)
        );
    }

    // ─── Recálculo combinado ─────────────────────────────────────────────────

    /**
     * Combina la lista de gastos y la de ingresos en una sola lista de
     * MovimientoReciente, ordena por fecha DESC y recorta a MAX_MOVIMIENTOS.
     * También actualiza balance, totalIngresos y totalGastos.
     *
     * Se llama cada vez que gastosMes o ingresosMes emiten un nuevo valor.
     * Si alguna fuente todavía no ha llegado, getValue() devuelve null:
     * en ese caso se usa una lista vacía para no bloquear el recálculo.
     */
    private void recalcular() {
        List<GastoConCategoria>   gastos   = gastosMes.getValue();
        List<IngresoConCategoria> ingresos = ingresosMes.getValue();

        if (gastos   == null) gastos   = Collections.emptyList();
        if (ingresos == null) ingresos = Collections.emptyList();

        List<MovimientoReciente> combinada = new ArrayList<>();

        // ── Convertir gastos ──────────────────────────────────────────────────
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

        // ── Convertir ingresos ────────────────────────────────────────────────
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

        // ── Ordenar por fecha DESC ────────────────────────────────────────────
        Collections.sort(combinada,
                (a, b) -> Long.compare(b.getFecha(), a.getFecha()));

        // ── Recortar a MAX_MOVIMIENTOS ────────────────────────────────────────
        List<MovimientoReciente> recortada = combinada.size() > MAX_MOVIMIENTOS
                ? combinada.subList(0, MAX_MOVIMIENTOS)
                : combinada;

        // ── Publicar resultados ───────────────────────────────────────────────
        movimientos.setValue(new ArrayList<>(recortada));
        _totalGastos.setValue(sumGastos);
        _totalIngresos.setValue(sumIngresos);
        _balance.setValue(sumIngresos - sumGastos);
    }
}

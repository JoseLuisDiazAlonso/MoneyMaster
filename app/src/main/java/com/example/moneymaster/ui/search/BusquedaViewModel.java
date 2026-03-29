package com.example.moneymaster.ui.search;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.GastoConCategoria;
import com.example.moneymaster.data.model.GastoGrupo;

import java.util.List;

/**
 * BusquedaViewModel — Card #58
 *
 * ViewModel compartido que gestiona el estado de búsqueda y filtros.
 * Puede usarse tanto para gastos personales (HomeFragment) como para
 * gastos de grupo (GroupExpensesFragment).
 *
 * Uso:
 *   BusquedaViewModel vm = new ViewModelProvider(this).get(BusquedaViewModel.class);
 *
 *   // Para gastos personales:
 *   vm.initPersonal();
 *   vm.getResultadosPersonales().observe(...)
 *
 *   // Para gastos de grupo:
 *   vm.initGrupo(grupoId);
 *   vm.getResultadosGrupo().observe(...)
 *
 *   // Aplicar filtro:
 *   vm.aplicarFiltro(new FiltroGasto.Builder().query("gasolina").build());
 */
public class BusquedaViewModel extends AndroidViewModel {

    private static final String PREFS_NAME  = "moneymaster_session";
    private static final String KEY_USER_ID = "usuario_id";

    private final AppDatabase db;
    private final int         usuarioId;
    private int               grupoId = -1;

    // Estado actual del filtro — cualquier cambio relanza las queries
    private final MutableLiveData<FiltroGasto> filtroActual =
            new MutableLiveData<>(FiltroGasto.empty());

    // Resultados para gastos personales
    private LiveData<List<GastoConCategoria>> resultadosPersonales;
    private LiveData<Integer>                 contadorPersonales;

    // Resultados para gastos de grupo
    private LiveData<List<GastoGrupo>>        resultadosGrupo;
    private LiveData<Integer>                 contadorGrupo;

    public BusquedaViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, 0);
        usuarioId = (int) prefs.getLong(KEY_USER_ID, -1);
    }

    // ─── Inicialización ───────────────────────────────────────────────────────

    /** Inicializa las queries para gastos personales. Llamar en HomeFragment. */
    public void initPersonal() {
        resultadosPersonales = Transformations.switchMap(filtroActual, f ->
                db.gastoPersonalDao().buscarGastos(
                        usuarioId,
                        f.query,
                        f.categoriaId,
                        f.montoMin,
                        f.montoMax,
                        f.fechaDesde,
                        f.fechaHasta));

        contadorPersonales = Transformations.switchMap(filtroActual, f ->
                db.gastoPersonalDao().contarResultadosPersonales(
                        usuarioId,
                        f.query,
                        f.categoriaId,
                        f.montoMin,
                        f.montoMax,
                        f.fechaDesde,
                        f.fechaHasta));
    }

    /** Inicializa las queries para gastos de grupo. Llamar en GroupExpensesFragment. */
    public void initGrupo(int grupoId) {
        this.grupoId = grupoId;

        resultadosGrupo = Transformations.switchMap(filtroActual, f ->
                db.gastoGrupoDao().buscarGastosGrupo(
                        grupoId,
                        f.query,
                        f.categoriaId,
                        f.montoMin,
                        f.montoMax,
                        f.fechaDesde,
                        f.fechaHasta));

        contadorGrupo = Transformations.switchMap(filtroActual, f ->
                db.gastoGrupoDao().contarResultadosGrupo(
                        grupoId,
                        f.query,
                        f.categoriaId,
                        f.montoMin,
                        f.montoMax,
                        f.fechaDesde,
                        f.fechaHasta));
    }

    // ─── LiveData públicos ────────────────────────────────────────────────────

    public LiveData<List<GastoConCategoria>> getResultadosPersonales() {
        return resultadosPersonales;
    }

    public LiveData<Integer> getContadorPersonales() {
        return contadorPersonales;
    }

    public LiveData<List<GastoGrupo>> getResultadosGrupo() {
        return resultadosGrupo;
    }

    public LiveData<Integer> getContadorGrupo() {
        return contadorGrupo;
    }

    public LiveData<FiltroGasto> getFiltroActual() {
        return filtroActual;
    }

    // ─── Actualización de filtros ─────────────────────────────────────────────

    /** Aplica un nuevo filtro completo. */
    public void aplicarFiltro(FiltroGasto filtro) {
        filtroActual.setValue(filtro);
    }

    /** Actualiza solo el texto de búsqueda, manteniendo el resto de filtros. */
    public void setQuery(String query) {
        FiltroGasto actual = filtroActual.getValue();
        FiltroGasto.Builder builder = actual != null
                ? actual.toBuilder() : new FiltroGasto.Builder();
        filtroActual.setValue(builder.query(query).build());
    }

    /** Limpia todos los filtros volviendo al estado vacío. */
    public void limpiarFiltros() {
        filtroActual.setValue(FiltroGasto.empty());
    }

    /** Devuelve el filtro actual (no reactivo, para leer valor inmediato). */
    public FiltroGasto getFiltroSnapshot() {
        FiltroGasto f = filtroActual.getValue();
        return f != null ? f : FiltroGasto.empty();
    }
}

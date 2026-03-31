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

public class BusquedaViewModel extends AndroidViewModel {

    // FIX: claves corregidas para coincidir con SessionManager
    private static final String PREFS_NAME  = "MoneyMasterSession";
    private static final String KEY_USER_ID = "userId";

    private final AppDatabase db;
    private final int         usuarioId;
    private int               grupoId = -1;

    private final MutableLiveData<FiltroGasto> filtroActual =
            new MutableLiveData<>(FiltroGasto.empty());

    private LiveData<List<GastoConCategoria>> resultadosPersonales;
    private LiveData<Integer>                 contadorPersonales;

    private LiveData<List<GastoGrupo>>        resultadosGrupo;
    private LiveData<Integer>                 contadorGrupo;

    public BusquedaViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, 0);
        // FIX: getInt en lugar de getLong
        usuarioId = prefs.getInt(KEY_USER_ID, -1);
    }

    // ─── Inicialización ───────────────────────────────────────────────────────

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

    public void aplicarFiltro(FiltroGasto filtro) {
        filtroActual.setValue(filtro);
    }

    public void setQuery(String query) {
        FiltroGasto actual = filtroActual.getValue();
        FiltroGasto.Builder builder = actual != null
                ? actual.toBuilder() : new FiltroGasto.Builder();
        filtroActual.setValue(builder.query(query).build());
    }

    public void limpiarFiltros() {
        filtroActual.setValue(FiltroGasto.empty());
    }

    public FiltroGasto getFiltroSnapshot() {
        FiltroGasto f = filtroActual.getValue();
        return f != null ? f : FiltroGasto.empty();
    }
}
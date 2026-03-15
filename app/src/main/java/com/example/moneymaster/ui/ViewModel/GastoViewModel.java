package com.example.moneymaster.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.repository.GastoRepository;

import java.util.Calendar;
import java.util.List;

/**
 * ViewModel de la pantalla de Gastos.
 *
 * Maneja:
 * - CRUD de gastos personales
 * - Filtro por mes/año (selector en la toolbar)
 * - Lista de categorías para el spinner del formulario
 * - Gasto seleccionado para el formulario de edición
 */
public class GastoViewModel extends AndroidViewModel {

    private final GastoRepository repository;

    private final MutableLiveData<Long>  usuarioIdLive  = new MutableLiveData<>();
    private final MutableLiveData<int[]> filtroMesAnio  = new MutableLiveData<>();

    // ─── LiveData públicos ────────────────────────────────────────────────────

    /** Lista completa sin filtro de mes. */
    public final LiveData<List<GastoPersonal>> todosLosGastos;

    /** Lista filtrada por el mes/año seleccionado en la toolbar. */
    public final LiveData<List<GastoPersonal>> gastosFiltrados;

    /** Suma total del filtro activo. */
    public final LiveData<Double> totalGastosFiltro;

    /** Categorías para el spinner del formulario (sistema + del usuario). */
    public final LiveData<List<CategoriaGasto>> categorias;

    /** Gasto en edición. Null cuando se está creando uno nuevo. */
    private final MutableLiveData<GastoPersonal> gastoSeleccionado = new MutableLiveData<>();
    public LiveData<GastoPersonal> getGastoSeleccionado() { return gastoSeleccionado; }

    // ─── Constructor ──────────────────────────────────────────────────────────

    public GastoViewModel(@NonNull Application application) {
        super(application);
        repository = new GastoRepository(application);

        // Mes por defecto: el actual
        Calendar cal = Calendar.getInstance();
        filtroMesAnio.setValue(new int[]{cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)});

        todosLosGastos = Transformations.switchMap(usuarioIdLive,
                repository::getGastosByUsuario);

        gastosFiltrados = Transformations.switchMap(filtroMesAnio, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(null);
            return repository.getGastosByMes(uid, filtro[0], filtro[1]);
        });

        totalGastosFiltro = Transformations.switchMap(filtroMesAnio, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(0.0);
            return repository.getTotalGastosMes(uid, filtro[0], filtro[1]);
        });

        categorias = Transformations.switchMap(usuarioIdLive,
                repository::getCategorias);
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setUsuarioId(long id) {
        if (!Long.valueOf(id).equals(usuarioIdLive.getValue())) {
            usuarioIdLive.setValue(id);
        }
    }

    /** Llamar cuando el usuario cambia el mes en el selector de la toolbar. */
    public void setFiltroMesAnio(int mes, int anio) {
        filtroMesAnio.setValue(new int[]{mes, anio});
    }

    public void seleccionarGasto(GastoPersonal gasto) {
        gastoSeleccionado.setValue(gasto);
    }

    public void limpiarSeleccion() {
        gastoSeleccionado.setValue(null);
    }

    // ─── CRUD Gastos ──────────────────────────────────────────────────────────

    public void insertar(GastoPersonal gasto) {
        repository.insertar(gasto);
    }

    public void actualizar(GastoPersonal gasto) {
        repository.actualizar(gasto);
    }

    public void eliminar(GastoPersonal gasto) {
        repository.eliminar(gasto);
    }

    // ─── CRUD Categorías ──────────────────────────────────────────────────────

    public void insertarCategoria(CategoriaGasto categoria) {
        repository.insertarCategoria(categoria);
    }

    public void actualizarCategoria(CategoriaGasto categoria) {
        repository.actualizarCategoria(categoria);
    }

    public void eliminarCategoria(CategoriaGasto categoria) {
        repository.eliminarCategoria(categoria);
    }
}

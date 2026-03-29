package com.example.moneymaster.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.dao.CategoriaIngresoDao;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class IngresoRepository {

    private final IngresoPersonalDao ingresoDao;
    private final CategoriaIngresoDao categoriaDao;
    private final ExecutorService executor;

    public IngresoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        ingresoDao   = db.ingresoPersonalDao();
        categoriaDao = db.categoriaIngresoDao();
        executor     = AppDatabase.databaseWriteExecutor;
    }

    // ── Ingresos ──────────────────────────────────────────────────────────────

    public LiveData<List<IngresoPersonal>> getIngresosByUsuario(long usuarioId) {
        return ingresoDao.getIngresosByUsuario(usuarioId);
    }

    public LiveData<List<IngresoPersonal>> getIngresosByMes(long usuarioId, int mes, int anio) {
        return ingresoDao.getIngresosByMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalIngresosMes(long usuarioId, int mes, int anio) {
        return ingresoDao.getTotalIngresosMes(usuarioId, mes, anio);
    }

    public LiveData<List<IngresoPersonal>> getUltimosIngresos(long usuarioId, int limite) {
        return ingresoDao.getUltimosIngresos(usuarioId, limite);
    }

    public LiveData<IngresoPersonal> getIngresoById(long ingresoId) {
        return ingresoDao.getIngresoById(ingresoId);
    }

    /** Card #62 — StatisticsViewModel: ingresos de todo un año. */
    public LiveData<List<IngresoPersonal>> getIngresosByAnio(long usuarioId, int anio) {
        return ingresoDao.getIngresosByAnio(usuarioId, anio);
    }

    /** Card #62 — StatisticsViewModel: ingresos en rango de fechas. */
    public LiveData<List<IngresoPersonal>> getIngresosByRango(long usuarioId, long inicio, long fin) {
        return ingresoDao.getIngresosByRango(usuarioId, inicio, fin);
    }

    public void insertar(IngresoPersonal ingreso) {
        executor.execute(() -> ingresoDao.insertar(ingreso));
    }

    public void actualizar(IngresoPersonal ingreso) {
        executor.execute(() -> ingresoDao.actualizar(ingreso));
    }

    public void eliminar(IngresoPersonal ingreso) {
        executor.execute(() -> ingresoDao.eliminar(ingreso));
    }

    // ── Estadísticas ──────────────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(long usuarioId, int mes, int anio) {
        return ingresoDao.getIngresosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses) {
        return ingresoDao.getResumenUltimosMeses(usuarioId, meses);
    }

    // ── Categorías ────────────────────────────────────────────────────────────

    public LiveData<List<CategoriaIngreso>> getCategorias(long usuarioId) {
        return categoriaDao.getCategorias(usuarioId);
    }

    public LiveData<List<CategoriaIngreso>> getCategoriasDelSistema() {
        return categoriaDao.getCategoriasDelSistema();
    }

    public LiveData<List<CategoriaIngreso>> getCategoriasByUsuario(long usuarioId) {
        return categoriaDao.getCategoriasByUsuario(usuarioId);
    }

    public void insertarCategoria(CategoriaIngreso categoria) {
        executor.execute(() -> categoriaDao.insertar(categoria));
    }

    public void actualizarCategoria(CategoriaIngreso categoria) {
        executor.execute(() -> categoriaDao.actualizar(categoria));
    }

    public void eliminarCategoria(CategoriaIngreso categoria) {
        executor.execute(() -> categoriaDao.eliminar(categoria));
    }
}

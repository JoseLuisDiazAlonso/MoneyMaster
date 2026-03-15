package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.model.GastoPersonal;

import java.util.List;

/**
 * Repositorio de gastos personales.
 *
 * Gestiona el CRUD de GastoPersonal y expone LiveData para que
 * el ViewModel observe cambios en tiempo real sin acoplarse al DAO.
 *
 * Nota sobre fotos: este repositorio NO gestiona FotoRecibo.
 * Si el gasto lleva foto, usar FotoReciboRepository primero para
 * obtener el fotoReciboId, asignarlo al GastoPersonal y luego insertar.
 */
public class GastoPersonalRepository {

    private final GastoPersonalDao gastoPersonalDao;

    public GastoPersonalRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        gastoPersonalDao = db.gastoPersonalDao();
    }

    // ---- ESCRITURAS (background thread) ----

    public void insertGasto(GastoPersonal gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoPersonalDao.insertGasto(gasto));
    }

    public void updateGasto(GastoPersonal gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoPersonalDao.updateGasto(gasto));
    }

    public void deleteGasto(GastoPersonal gasto) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                gastoPersonalDao.deleteGasto(gasto));
    }

    // ---- LECTURAS REACTIVAS (LiveData) ----

    /** Todos los gastos del usuario, más recientes primero. */
    public LiveData<List<GastoPersonal>> getGastosPorUsuario(int usuarioId) {
        return gastoPersonalDao.getGastosPorUsuario(usuarioId);
    }

    /** Gastos dentro de un rango de fechas (timestamps en milisegundos). */
    public LiveData<List<GastoPersonal>> getGastosPorFecha(int usuarioId, long inicio, long fin) {
        return gastoPersonalDao.getGastosPorFecha(usuarioId, inicio, fin);
    }

    /** Gastos filtrados por categoría. */
    public LiveData<List<GastoPersonal>> getGastosPorCategoria(int usuarioId, int categoriaId) {
        return gastoPersonalDao.getGastosPorCategoria(usuarioId, categoriaId);
    }

    // ---- LECTURAS SINCRÓNICAS (llamar desde background thread) ----

    /** Total gastado en un período. Útil para el dashboard. */
    public double getTotalGastos(int usuarioId, long inicio, long fin) {
        return gastoPersonalDao.getTotalGastos(usuarioId, inicio, fin);
    }

    public GastoPersonal getById(int id) {
        return gastoPersonalDao.getById(id);
    }
}

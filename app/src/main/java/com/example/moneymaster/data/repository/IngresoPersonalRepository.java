package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.model.IngresoPersonal;

import java.util.List;

/**
 * Repositorio de ingresos personales.
 *
 * Estructura análoga a GastoPersonalRepository.
 */
public class IngresoPersonalRepository {

    private final IngresoPersonalDao ingresoPersonalDao;

    public IngresoPersonalRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        ingresoPersonalDao = db.ingresoPersonalDao();
    }

    // ---- ESCRITURAS (background thread) ----

    public void insertIngreso(IngresoPersonal ingreso) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                ingresoPersonalDao.insertIngreso(ingreso));
    }

    public void updateIngreso(IngresoPersonal ingreso) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                ingresoPersonalDao.updateIngreso(ingreso));
    }

    public void deleteIngreso(IngresoPersonal ingreso) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                ingresoPersonalDao.deleteIngreso(ingreso));
    }

    // ---- LECTURAS REACTIVAS (LiveData) ----

    /** Todos los ingresos del usuario, más recientes primero. */
    public LiveData<List<IngresoPersonal>> getIngresosPorUsuario(int usuarioId) {
        return ingresoPersonalDao.getIngresosPorUsuario(usuarioId);
    }

    /** Ingresos dentro de un rango de fechas (timestamps en milisegundos). */
    public LiveData<List<IngresoPersonal>> getIngresosPorFecha(int usuarioId, long inicio, long fin) {
        return ingresoPersonalDao.getIngresosPorFecha(usuarioId, inicio, fin);
    }

    // ---- LECTURAS SINCRÓNICAS (llamar desde background thread) ----

    /** Total ingresado en un período. Útil para el dashboard. */
    public double getTotalIngresos(int usuarioId, long inicio, long fin) {
        return ingresoPersonalDao.getTotalIngresos(usuarioId, inicio, fin);
    }

    public IngresoPersonal getById(int id) {
        return ingresoPersonalDao.getById(id);
    }
}

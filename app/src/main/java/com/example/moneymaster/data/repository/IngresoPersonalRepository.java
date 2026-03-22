package com.example.moneymaster.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.model.ResumenMensual;
import com.example.moneymaster.data.model.TotalPorCategoria;

import java.util.List;

public class IngresoPersonalRepository {

    private final IngresoPersonalDao ingresoPersonalDao;
    private final Handler            mainHandler = new Handler(Looper.getMainLooper());

    public IngresoPersonalRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        ingresoPersonalDao = db.ingresoPersonalDao();
    }

    // ── ESCRITURAS ────────────────────────────────────────────────────────────

    public void insertarIngreso(IngresoPersonal ingreso) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                ingresoPersonalDao.insertar(ingreso));
    }

    public void insertarIngresoYObtenerId(IngresoPersonal ingreso, SaveCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = ingresoPersonalDao.insertar(ingreso);
            mainHandler.post(() -> callback.onSaved(id));
        });
    }

    /**
     * Inserta un ingreso y notifica éxito/error mediante SaveCallback simple.
     * Usado por AddIncomeActivity para saber si el guardado fue correcto.
     *
     * @param ingreso  Objeto a persistir.
     * @param callback Notificado en el Main Thread.
     */
    public void insert(IngresoPersonal ingreso, SaveCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long rowId = ingresoPersonalDao.insertar(ingreso);
                mainHandler.post(() -> {
                    if (rowId > 0) {
                        callback.onSaved(null);
                    } else {
                        callback.onError(new Exception("Insert devolvió -1"));
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void actualizarIngreso(IngresoPersonal ingreso) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                ingresoPersonalDao.actualizar(ingreso));
    }

    public void eliminarIngreso(IngresoPersonal ingreso) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                ingresoPersonalDao.eliminar(ingreso));
    }

    // ── LISTAS ────────────────────────────────────────────────────────────────

    public LiveData<List<IngresoPersonal>> getIngresosByUsuario(long usuarioId) {
        return ingresoPersonalDao.getIngresosByUsuario(usuarioId);
    }

    public LiveData<List<IngresoPersonal>> getIngresosByMes(long usuarioId, int mes, int anio) {
        return ingresoPersonalDao.getIngresosByMes(usuarioId, mes, anio);
    }

    public LiveData<List<IngresoPersonal>> getUltimosIngresos(long usuarioId, int limite) {
        return ingresoPersonalDao.getUltimosIngresos(usuarioId, limite);
    }

    public LiveData<IngresoPersonal> getIngresoById(long ingresoId) {
        return ingresoPersonalDao.getIngresoById(ingresoId);
    }

    // ── TOTALES ───────────────────────────────────────────────────────────────

    public LiveData<Double> getTotalIngresosMes(long usuarioId, int mes, int anio) {
        return ingresoPersonalDao.getTotalIngresosMes(usuarioId, mes, anio);
    }

    public LiveData<Double> getTotalIngresosRango(long usuarioId, long desde, long hasta) {
        return ingresoPersonalDao.getTotalIngresosRango(usuarioId, desde, hasta);
    }

    // ── ESTADÍSTICAS ──────────────────────────────────────────────────────────

    public LiveData<List<TotalPorCategoria>> getIngresosPorCategoria(long usuarioId,
                                                                     int mes, int anio) {
        return ingresoPersonalDao.getIngresosPorCategoria(usuarioId, mes, anio);
    }

    public LiveData<List<ResumenMensual>> getResumenUltimosMeses(long usuarioId, int meses) {
        return ingresoPersonalDao.getResumenUltimosMeses(usuarioId, meses);
    }

    public List<ResumenMensual> getResumenUltimosMesesSync(long usuarioId, int meses) {
        return ingresoPersonalDao.getResumenUltimosMesesSync(usuarioId, meses);
    }

    public int countIngresos(long usuarioId) {
        return ingresoPersonalDao.countIngresos(usuarioId);
    }

    // ── CALLBACK ──────────────────────────────────────────────────────────────

    public interface SaveCallback<T> {
        void onSaved(T result);

        default void onError(Exception e) {
            // Implementación por defecto vacía para no romper el uso anterior
            // con SaveCallback<Long> en insertarIngresoYObtenerId()
        }
    }
}
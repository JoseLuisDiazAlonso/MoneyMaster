package com.example.moneymaster.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.FotoReciboDao;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.utils.ImageUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FotoReciboRepository {

    private final FotoReciboDao   dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    public FotoReciboRepository(Application application) {
        dao = AppDatabase.getInstance(application).fotoReciboDao();
    }

    // ─── Interfaz callback ────────────────────────────────────────────────────

    public interface InsertCallback {
        void onInserted(int fotoId);
    }

    // ─── Insertar y obtener ID generado ───────────────────────────────────────

    /**
     * Inserta una FotoRecibo en background y devuelve el ID generado
     * en el hilo principal via callback. Necesario porque GastoPersonal
     * necesita ese ID antes de ser insertado.
     */
    public void insertar(FotoRecibo foto, InsertCallback callback) {
        executor.execute(() -> {
            long id = dao.insertar(foto);
            mainHandler.post(() -> callback.onInserted((int) id));
        });
    }

    // ─── Eliminar foto + archivo físico ──────────────────────────────────────

    /**
     * Elimina la fila de Room Y el archivo físico del disco.
     * Siempre llama este método en lugar de dao.eliminar() directamente.
     */
    public void eliminarConArchivo(FotoRecibo foto) {
        executor.execute(() -> {
            // 1. Borrar archivo físico
            if (foto.rutaArchivo != null) {
                ImageUtils.eliminarFoto(foto.rutaArchivo);
            }
            if (foto.miniaturaRuta != null) {
                ImageUtils.eliminarFoto(foto.miniaturaRuta);
            }
            // 2. Borrar fila de Room
            dao.eliminar(foto);
        });
    }

    // ─── Obtener por ID (síncrono, llamar desde background) ──────────────────

    public FotoRecibo getById(int id) {
        return dao.getById(id);
    }

    // ─── Limpieza de huérfanas ────────────────────────────────────────────────

    public void limpiarHuerfanas() {
        executor.execute(() -> {
            for (FotoRecibo foto : dao.getFotosHuerfanas()) {
                eliminarConArchivo(foto);
            }
        });
    }
}
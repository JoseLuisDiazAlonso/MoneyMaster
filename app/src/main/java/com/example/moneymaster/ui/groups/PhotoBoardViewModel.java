package com.example.moneymaster.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.utils.ImageUtils;

import java.util.List;

/**
 * Card #34 — ViewModel del tablón de fotos.
 * Expone las fotos del grupo y gestiona la eliminación.
 */
public class PhotoBoardViewModel extends AndroidViewModel {

    private final AppDatabase          db;
    private LiveData<List<FotoRecibo>> fotos;
    private int                        grupoId = -1;

    public PhotoBoardViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public void init(int grupoId) {
        if (this.grupoId == grupoId) return;
        this.grupoId = grupoId;
        fotos = db.fotoReciboDao().getFotosByGrupo(grupoId);
    }

    public LiveData<List<FotoRecibo>> getFotos() {
        return fotos;
    }

    /**
     * Elimina una lista de fotos:
     *   1. Desvincula cada foto de su gasto (fotoReciboId = null)
     *   2. Elimina el archivo físico del disco
     *   3. Elimina la fila de fotos_recibo
     *
     * Todo en background para no bloquear el UI thread.
     */
    public void eliminarFotos(List<FotoRecibo> fotos) {
        if (fotos == null || fotos.isEmpty()) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            for (FotoRecibo foto : fotos) {
                // 1. Desvincular de gastos_grupo
                db.gastoGrupoDao().desvincularFoto(foto.id);

                // 2. Borrar archivo físico
                ImageUtils.eliminarFoto(foto.rutaArchivo);
                if (foto.miniaturaRuta != null) {
                    ImageUtils.eliminarFoto(foto.miniaturaRuta);
                }

                // 3. Borrar fila de Room
                db.fotoReciboDao().eliminar(foto);
            }
        });
    }
}
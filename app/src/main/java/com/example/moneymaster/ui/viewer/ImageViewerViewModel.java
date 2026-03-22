package com.example.moneymaster.ui.viewer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.InfoGastoFoto;
import com.example.moneymaster.utils.ImageUtils;

/**
 * Card #35 — ViewModel del visor de imagen completa.
 */
public class ImageViewerViewModel extends AndroidViewModel {

    private final AppDatabase                    db;
    private final MutableLiveData<InfoGastoFoto> infoGasto = new MutableLiveData<>();

    public ImageViewerViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public void init(int fotoId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            InfoGastoFoto info = cargarInfoGasto(fotoId);
            infoGasto.postValue(info);
        });
    }

    public LiveData<InfoGastoFoto> getInfoGasto() {
        return infoGasto;
    }

    // ─── Carga de info del gasto ──────────────────────────────────────────────

    private InfoGastoFoto cargarInfoGasto(int fotoId) {

        // 1. Buscar en gastos personales — variable tipada explícitamente
        GastoPersonal gastoPersonal =
                db.gastoPersonalDao().getByFotoReciboId(fotoId);

        if (gastoPersonal != null) {
            InfoGastoFoto info = new InfoGastoFoto();
            info.tipo        = InfoGastoFoto.TipoGasto.PERSONAL;
            info.monto       = gastoPersonal.monto;
            info.descripcion = gastoPersonal.descripcion;
            info.fecha       = gastoPersonal.fecha;
            info.pagadoPor   = null; // gastos personales no tienen pagador

            CategoriaGasto cat =
                    db.categoriaGastoDao().getByIdSync(gastoPersonal.categoriaId);
            info.nombreCategoria = cat != null ? cat.nombre : null;
            return info;
        }

        // 2. Buscar en gastos de grupo — variable tipada explícitamente
        GastoGrupo gastoGrupo =
                db.gastoGrupoDao().getByFotoReciboId(fotoId);

        if (gastoGrupo != null) {
            InfoGastoFoto info = new InfoGastoFoto();
            info.tipo        = InfoGastoFoto.TipoGasto.GRUPO;
            info.monto       = gastoGrupo.monto;
            info.descripcion = gastoGrupo.descripcion;
            info.fecha       = gastoGrupo.fecha;
            info.pagadoPor   = gastoGrupo.pagadoPorNombre;

            CategoriaGasto cat =
                    db.categoriaGastoDao().getByIdSync(gastoGrupo.categoriaId);
            info.nombreCategoria = cat != null ? cat.nombre : null;
            return info;
        }

        return null;
    }

    // ─── Eliminación ─────────────────────────────────────────────────────────

    public void eliminarFoto(int fotoId, String rutaArchivo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {

            // 1. Desvincular de gastos personales
            db.gastoPersonalDao().desvincularFoto(fotoId);

            // 2. Desvincular de gastos de grupo
            db.gastoGrupoDao().desvincularFoto(fotoId);

            // 3. Borrar archivo físico
            ImageUtils.eliminarFoto(rutaArchivo);

            // 4. Borrar miniatura si existe
            FotoRecibo foto = db.fotoReciboDao().getById(fotoId);
            if (foto != null) {
                if (foto.miniaturaRuta != null) {
                    ImageUtils.eliminarFoto(foto.miniaturaRuta);
                }
                db.fotoReciboDao().eliminar(foto);
            }
        });
    }
}

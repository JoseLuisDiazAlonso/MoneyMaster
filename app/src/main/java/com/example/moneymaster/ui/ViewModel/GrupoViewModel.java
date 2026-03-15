package com.example.moneymaster.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.model.BalanceGrupo;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.data.repository.GrupoRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * ViewModel de la gestión de grupos de gasto compartido.
 *
 * Flujo de navegación:
 *   Lista de grupos → seleccionarGrupo(id)
 *   → miembros, gastos y balances se actualizan solos vía LiveData
 *
 * Uso desde la Activity:
 *   viewModel.setUsuarioId(sessionManager.getUsuarioId());
 *   viewModel.grupos.observe(this, lista -> adapter.submitList(lista));
 *   viewModel.seleccionarGrupo(grupoId);  // al pulsar un item
 */
public class GrupoViewModel extends AndroidViewModel {

    private final GrupoRepository repository;

    private final MutableLiveData<Long> usuarioIdLive       = new MutableLiveData<>();
    private final MutableLiveData<Long> grupoSeleccionadoId = new MutableLiveData<>();
    private final MutableLiveData<Long> gastoAbiertoId      = new MutableLiveData<>();

    // ─── LiveData públicos ────────────────────────────────────────────────────

    /** Lista de grupos del usuario. */
    public final LiveData<List<Grupo>> grupos;

    /** Detalle del grupo seleccionado. */
    public final LiveData<Grupo> grupoDetalle;

    /** Miembros del grupo seleccionado. */
    public final LiveData<List<MiembroGrupo>> miembros;

    /** Gastos del grupo seleccionado, más recientes primero. */
    public final LiveData<List<GastoGrupo>> gastos;

    /** Total acumulado del grupo seleccionado. */
    public final LiveData<Double> totalGastosGrupo;

    /** Balances de todos los miembros del grupo seleccionado. */
    public final LiveData<List<BalanceGrupo>> balances;

    /** Fotos del gasto actualmente abierto. */
    public final LiveData<List<FotoRecibo>> fotosGastoAbierto;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public GrupoViewModel(@NonNull Application application) {
        super(application);
        repository = new GrupoRepository(application);

        grupos = Transformations.switchMap(usuarioIdLive,
                repository::getGruposByUsuario);

        grupoDetalle = Transformations.switchMap(grupoSeleccionadoId,
                repository::getGrupoById);

        miembros = Transformations.switchMap(grupoSeleccionadoId,
                repository::getMiembrosByGrupo);

        gastos = Transformations.switchMap(grupoSeleccionadoId,
                repository::getGastosByGrupo);

        totalGastosGrupo = Transformations.switchMap(grupoSeleccionadoId,
                repository::getTotalGastosGrupo);

        balances = Transformations.switchMap(grupoSeleccionadoId,
                repository::getBalancesByGrupo);

        fotosGastoAbierto = Transformations.switchMap(gastoAbiertoId,
                repository::getFotosByGasto);
    }

    // ─── Setters de navegación ────────────────────────────────────────────────

    public void setUsuarioId(long id) {
        if (!Long.valueOf(id).equals(usuarioIdLive.getValue())) {
            usuarioIdLive.setValue(id);
        }
    }

    /** Abre el detalle de un grupo. Todos los LiveData relacionados reaccionan. */
    public void seleccionarGrupo(long grupoId) {
        grupoSeleccionadoId.setValue(grupoId);
    }

    /** Abre el detalle de un gasto para ver/agregar fotos. */
    public void abrirGasto(long gastoId) {
        gastoAbiertoId.setValue(gastoId);
    }

    // ─── CRUD Grupos ──────────────────────────────────────────────────────────

    /**
     * Crea un grupo y devuelve su ID para navegar al detalle.
     *
     * Llamar desde un hilo de fondo o usar con executor:
     *   AppDatabase.databaseWriteExecutor.execute(() -> {
     *       long id = viewModel.crearGrupo(grupo);
     *       runOnUiThread(() -> viewModel.seleccionarGrupo(id));
     *   });
     */
    public long crearGrupo(Grupo grupo) {
        Future<Long> future = repository.insertarGrupo(grupo);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1L;
        }
    }

    public void actualizarGrupo(Grupo grupo) {
        repository.actualizarGrupo(grupo);
    }

    /** Elimina el grupo y borra también sus balances asociados. */
    public void eliminarGrupo(Grupo grupo) {
        repository.eliminarGrupo(grupo);
    }

    // ─── Miembros ─────────────────────────────────────────────────────────────

    public void agregarMiembro(MiembroGrupo miembro) {
        repository.agregarMiembro(miembro);
    }

    public void eliminarMiembro(MiembroGrupo miembro) {
        repository.eliminarMiembro(miembro);
    }

    // ─── Gastos del grupo ─────────────────────────────────────────────────────

    /**
     * Registra un gasto y recalcula los balances del grupo automáticamente.
     * Devuelve el ID del gasto creado para uso inmediato (ej. adjuntar fotos).
     *
     * Uso:
     *   AppDatabase.databaseWriteExecutor.execute(() -> {
     *       long id = viewModel.registrarGasto(gasto);
     *       if (id > 0 && hayFotos) viewModel.agregarFoto(new FotoRecibo(id, ruta));
     *   });
     */
    public long registrarGasto(GastoGrupo gasto) {
        Future<Long> future = repository.insertarGastoYRecalcular(gasto);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1L;
        }
    }

    public void actualizarGasto(GastoGrupo gasto) {
        repository.actualizarGastoGrupo(gasto);
    }

    /** Elimina el gasto, sus fotos y recalcula los balances. */
    public void eliminarGasto(GastoGrupo gasto) {
        repository.eliminarGastoGrupo(gasto);
    }

    // ─── Fotos ────────────────────────────────────────────────────────────────

    public void agregarFoto(FotoRecibo foto) {
        repository.insertarFoto(foto);
    }

    public void eliminarFoto(FotoRecibo foto) {
        repository.eliminarFoto(foto);
    }
}

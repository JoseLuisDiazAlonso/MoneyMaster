package com.example.moneymaster.ui.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.repository.IngresoRepository;

import java.util.Calendar;
import java.util.List;


public class IngresoViewModel extends AndroidViewModel {

    private final IngresoRepository repository;

    private final MutableLiveData<Long>  usuarioIdLive = new MutableLiveData<>();
    private final MutableLiveData<int[]> filtroMesAnio = new MutableLiveData<>();

    //LiveData públicos

    public final LiveData<List<IngresoPersonal>> todosLosIngresos;
    public final LiveData<List<IngresoPersonal>> ingresosFiltrados;
    public final LiveData<Double>                totalIngresosFiltro;
    public final LiveData<List<CategoriaIngreso>> categorias;

    private final MutableLiveData<IngresoPersonal> ingresoSeleccionado = new MutableLiveData<>();
    public LiveData<IngresoPersonal> getIngresoSeleccionado() { return ingresoSeleccionado; }

    //Constructor

    public IngresoViewModel(@NonNull Application application) {
        super(application);
        repository = new IngresoRepository(application);

        Calendar cal = Calendar.getInstance();
        filtroMesAnio.setValue(new int[]{cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)});

        todosLosIngresos = Transformations.switchMap(usuarioIdLive,
                repository::getIngresosByUsuario);

        ingresosFiltrados = Transformations.switchMap(filtroMesAnio, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(null);
            return repository.getIngresosByMes(uid, filtro[0], filtro[1]);
        });

        totalIngresosFiltro = Transformations.switchMap(filtroMesAnio, filtro -> {
            Long uid = usuarioIdLive.getValue();
            if (uid == null || filtro == null) return new MutableLiveData<>(0.0);
            return repository.getTotalIngresosMes(uid, filtro[0], filtro[1]);
        });

        categorias = Transformations.switchMap(usuarioIdLive,
                repository::getCategorias);
    }

    //Setters

    public void setUsuarioId(long id) {
        if (!Long.valueOf(id).equals(usuarioIdLive.getValue())) {
            usuarioIdLive.setValue(id);
        }
    }

    public void setFiltroMesAnio(int mes, int anio) {
        filtroMesAnio.setValue(new int[]{mes, anio});
    }

    public void seleccionarIngreso(IngresoPersonal ingreso) {
        ingresoSeleccionado.setValue(ingreso);
    }

    public void limpiarSeleccion() {
        ingresoSeleccionado.setValue(null);
    }

    //CRUD Ingresos

    public void insertar(IngresoPersonal ingreso) {
        repository.insertar(ingreso);
    }

    public void actualizar(IngresoPersonal ingreso) {
        repository.actualizar(ingreso);
    }

    public void eliminar(IngresoPersonal ingreso) {
        repository.eliminar(ingreso);
    }

    //CRUD Categorías

    public void insertarCategoria(CategoriaIngreso categoria) {
        repository.insertarCategoria(categoria);
    }

    public void actualizarCategoria(CategoriaIngreso categoria) {
        repository.actualizarCategoria(categoria);
    }

    public void eliminarCategoria(CategoriaIngreso categoria) {
        repository.eliminarCategoria(categoria);
    }
}

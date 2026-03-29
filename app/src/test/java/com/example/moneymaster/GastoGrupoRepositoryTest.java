package com.example.moneymaster;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.repository.GastoGrupoRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * GastoGrupoRepositoryTest — Card #60
 *
 * Tests unitarios del repositorio de gastos de grupo.
 *
 * Cobertura:
 *  - insertar gasto de grupo → DAO correcto
 *  - actualizar gasto de grupo → campos correctos
 *  - eliminar gasto de grupo → DAO correcto, no llama a otros métodos
 *  - getGastosByGrupo → delega al DAO
 *  - getGastosByGrupoSync → delega al DAO
 *  - getTotalPagadoPorUsuarioSync → devuelve valor del DAO
 */
@RunWith(MockitoJUnitRunner.class)
public class GastoGrupoRepositoryTest {

    private static final Executor SYNC_EXECUTOR = Runnable::run;

    @Mock
    private GastoGrupoDao mockDao;

    private GastoGrupoRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new GastoGrupoRepository(mockDao, SYNC_EXECUTOR);
    }

    // ─── insertar ─────────────────────────────────────────────────────────────

    @Test
    public void insertarGasto_llamaAlDaoConElGastoCorrecto() {
        // Arrange
        GastoGrupo gasto = crearGastoGrupo(0, 10, 1, 1, 75.00, "Cena");

        // Act
        repository.insertarGasto(gasto);

        // Assert
        verify(mockDao, times(1)).insertar(gasto);
    }

    @Test
    public void insertarGasto_captura_camposCorrectos() {
        // Arrange
        GastoGrupo gasto = crearGastoGrupo(0, 5, 3, 2, 200.00, "Hotel");

        // Act
        repository.insertarGasto(gasto);

        // Assert
        ArgumentCaptor<GastoGrupo> captor = ArgumentCaptor.forClass(GastoGrupo.class);
        verify(mockDao).insertar(captor.capture());

        GastoGrupo capturado = captor.getValue();
        assertEquals(5,       capturado.grupoId);
        assertEquals(3,       capturado.pagadoPorId);
        assertEquals(Optional.of(2),       capturado.categoria_id);
        assertEquals(200.00,  capturado.monto, 0.001);
        assertEquals("Hotel", capturado.descripcion);
    }

    @Test
    public void insertarGasto_conNombrePagador_conservaElNombre() {
        // Arrange
        GastoGrupo gasto = crearGastoGrupo(0, 1, 2, 1, 50.00, "Taxi");
        gasto.pagadoPorNombre = "Ana";

        // Act
        repository.insertarGasto(gasto);

        // Assert
        ArgumentCaptor<GastoGrupo> captor = ArgumentCaptor.forClass(GastoGrupo.class);
        verify(mockDao).insertar(captor.capture());
        assertEquals("Ana", captor.getValue().pagadoPorNombre);
    }

    // ─── actualizar ───────────────────────────────────────────────────────────

    @Test
    public void actualizarGasto_llamaAlDaoConElGastoModificado() {
        // Arrange
        GastoGrupo gasto = crearGastoGrupo(15, 1, 2, 1, 30.00, "Parking");

        // Act
        repository.actualizarGasto(gasto);

        // Assert
        verify(mockDao, times(1)).actualizar(gasto);
    }

    @Test
    public void actualizarGasto_captura_montoActualizado() {
        // Arrange
        GastoGrupo gasto = crearGastoGrupo(8, 1, 1, 1, 40.00, "Compra");
        gasto.monto = 55.00;

        // Act
        repository.actualizarGasto(gasto);

        // Assert
        ArgumentCaptor<GastoGrupo> captor = ArgumentCaptor.forClass(GastoGrupo.class);
        verify(mockDao).actualizar(captor.capture());
        assertEquals(55.00, captor.getValue().monto, 0.001);
    }

    // ─── eliminar ─────────────────────────────────────────────────────────────

    @Test
    public void eliminarGasto_llamaAlDaoConElGastoCorrecto() {
        // Arrange
        GastoGrupo gasto = crearGastoGrupo(20, 2, 1, 3, 90.00, "Vuelo");

        // Act
        repository.eliminarGasto(gasto);

        // Assert
        verify(mockDao, times(1)).eliminar(gasto);
    }

    @Test
    public void eliminarGasto_noLlamaAInsertar() {
        // Arrange
        GastoGrupo gasto = crearGastoGrupo(4, 1, 1, 1, 10.00, "Cafe");

        // Act
        repository.eliminarGasto(gasto);

        // Assert
        verify(mockDao, never()).insertar(any());
        verify(mockDao, times(1)).eliminar(gasto);
    }

    // ─── lecturas ─────────────────────────────────────────────────────────────

    @Test
    public void getGastosByGrupo_delegaAlDao() {
        // Arrange
        MutableLiveData<List<GastoGrupo>> liveData = new MutableLiveData<>();
        when(mockDao.getGastosByGrupo(3L)).thenReturn(liveData);

        // Act
        LiveData<List<GastoGrupo>> resultado = repository.getGastosByGrupo(3L);

        // Assert
        assertSame(liveData, resultado);
        verify(mockDao, times(1)).getGastosByGrupo(3L);
    }

    @Test
    public void getGastosByGrupoSync_devuelveLaListaDelDao() {
        // Arrange
        GastoGrupo g1 = crearGastoGrupo(1, 1, 1, 1, 30.00, "Pizza");
        GastoGrupo g2 = crearGastoGrupo(2, 1, 2, 1, 20.00, "Refresco");
        when(mockDao.getGastosByGrupoSync(1L)).thenReturn(Arrays.asList(g1, g2));

        // Act
        List<GastoGrupo> lista = repository.getGastosByGrupoSync(1L);

        // Assert
        assertEquals(2, lista.size());
        assertEquals(g1, lista.get(0));
        assertEquals(g2, lista.get(1));
    }

    @Test
    public void getGastosByGrupoSync_grupoVacio_devuelveListaVacia() {
        // Arrange
        when(mockDao.getGastosByGrupoSync(99L)).thenReturn(Collections.emptyList());

        // Act
        List<GastoGrupo> lista = repository.getGastosByGrupoSync(99L);

        // Assert
        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    @Test
    public void getTotalPagadoPorUsuarioSync_devuelveElTotalDelDao() {
        // Arrange
        when(mockDao.getTotalPagadoPorUsuarioSync(1L, 2L)).thenReturn(150.75);

        // Act
        double total = repository.getTotalPagadoPorUsuarioSync(1L, 2L);

        // Assert
        assertEquals(150.75, total, 0.001);
    }

    @Test
    public void getTotalPagadoPorUsuarioSync_sinPagos_devuelveCero() {
        // Arrange
        when(mockDao.getTotalPagadoPorUsuarioSync(1L, 99L)).thenReturn(0.0);

        // Act
        double total = repository.getTotalPagadoPorUsuarioSync(1L, 99L);

        // Assert
        assertEquals(0.0, total, 0.001);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private GastoGrupo crearGastoGrupo(int id, int grupoId, int pagadoPorId,
                                       int categoriaId, double monto, String descripcion) {
        GastoGrupo g = new GastoGrupo();
        g.id          = id;
        g.grupoId     = grupoId;
        g.pagadoPorId = pagadoPorId;
        g.categoria_id = categoriaId;
        g.monto       = monto;
        g.descripcion = descripcion;
        g.fecha       = System.currentTimeMillis();
        g.fecha = System.currentTimeMillis();
        return g;
    }
}
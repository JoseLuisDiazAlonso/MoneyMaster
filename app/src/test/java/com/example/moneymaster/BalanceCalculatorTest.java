package com.example.moneymaster;

import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.ui.groups.model.DeudaItem;
import com.example.moneymaster.ui.groups.model.MiembroBalanceItem;
import com.example.moneymaster.ui.groups.util.BalanceCalculator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * BalanceCalculatorTest — Card #60
 *
 * Tests puros de JUnit para el algoritmo de balance de grupo.
 * No necesita Mockito porque BalanceCalculator es una clase estática
 * sin dependencias Android ni Room.
 *
 * Escenarios cubiertos:
 *
 * calcularBalancesMiembros:
 *  - 3 miembros, gastos iguales → todos en equilibrio
 *  - 3 miembros, un solo pagador → acreedor y deudores correctos
 *  - 2 miembros, gastos desiguales → balances netos correctos
 *  - grupo vacío → lista vacía
 *  - miembro sin gastos → balance negativo (debe su cuota)
 *  - un solo miembro → siempre equilibrado
 *
 * calcularDeudas:
 *  - ejemplo del Javadoc (Ana/Pedro/Luis 90€) → 2 transacciones correctas
 *  - 2 miembros en desequilibrio → 1 transacción
 *  - todos equilibrados → sin deudas
 *  - balances vacíos → lista vacía
 *  - deuda exacta entre dos → se salda en una transacción
 *  - múltiples deudores, un acreedor → transacciones mínimas
 */
public class BalanceCalculatorTest {

    private static final double DELTA = 0.01; // tolerancia para comparaciones de double

    // ═══════════════════════════════════════════════════════════════════════════
    // calcularBalancesMiembros
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    public void calcularBalances_gastosTotalesIguales_todosEnEquilibrio() {
        // Arrange: 3 miembros, cada uno pagó exactamente 30€ (total 90€, cuota 30€)
        List<MiembroGrupo> miembros = Arrays.asList(
                miembro(1, "Ana",   "#FF0000"),
                miembro(2, "Pedro", "#00FF00"),
                miembro(3, "Luis",  "#0000FF")
        );
        List<GastoGrupo> gastos = Arrays.asList(
                gasto(1, 30.00, "Ana"),
                gasto(2, 30.00, "Pedro"),
                gasto(3, 30.00, "Luis")
        );

        // Act
        List<MiembroBalanceItem> balances = BalanceCalculator.calcularBalancesMiembros(miembros, gastos);

        // Assert
        assertEquals(3, balances.size());
        for (MiembroBalanceItem b : balances) {
            assertEquals("Balance neto de " + b.nombreMiembro + " debe ser 0",
                    0.0, b.balanceNeto, DELTA);
        }
    }

    @Test
    public void calcularBalances_unSoloPagador_esAcreedor() {
        // Arrange: Ana pagó todo (90€), Pedro y Luis no pagaron nada
        // Cuota ideal = 30€ → Ana: +60, Pedro: -30, Luis: -30
        List<MiembroGrupo> miembros = Arrays.asList(
                miembro(1, "Ana",   "#FF0000"),
                miembro(2, "Pedro", "#00FF00"),
                miembro(3, "Luis",  "#0000FF")
        );
        List<GastoGrupo> gastos = Arrays.asList(
                gasto(1, 40.00, "Ana"),
                gasto(2, 30.00, "Ana"),
                gasto(3, 20.00, "Ana")
        );

        // Act
        List<MiembroBalanceItem> balances = BalanceCalculator.calcularBalancesMiembros(miembros, gastos);

        // Assert: Ana tiene balance positivo
        MiembroBalanceItem ana   = buscarPorNombre(balances, "Ana");
        MiembroBalanceItem pedro = buscarPorNombre(balances, "Pedro");
        MiembroBalanceItem luis  = buscarPorNombre(balances, "Luis");

        assertNotNull(ana);
        assertNotNull(pedro);
        assertNotNull(luis);

        assertEquals(60.0,  ana.balanceNeto,   DELTA); // pagó 90, cuota 30 → +60
        assertEquals(-30.0, pedro.balanceNeto, DELTA); // pagó 0,  cuota 30 → -30
        assertEquals(-30.0, luis.balanceNeto,  DELTA); // pagó 0,  cuota 30 → -30
    }

    @Test
    public void calcularBalances_dosmiembros_gastosDesiguales_balancesCorrectos() {
        // Arrange: Ana 70€, Pedro 30€ → total 100€, cuota 50€
        // Ana: +20, Pedro: -20
        List<MiembroGrupo> miembros = Arrays.asList(
                miembro(1, "Ana",   "#FF0000"),
                miembro(2, "Pedro", "#00FF00")
        );
        List<GastoGrupo> gastos = Arrays.asList(
                gasto(1, 70.00, "Ana"),
                gasto(2, 30.00, "Pedro")
        );

        // Act
        List<MiembroBalanceItem> balances = BalanceCalculator.calcularBalancesMiembros(miembros, gastos);

        // Assert
        MiembroBalanceItem ana   = buscarPorNombre(balances, "Ana");
        MiembroBalanceItem pedro = buscarPorNombre(balances, "Pedro");

        assertEquals( 20.0, ana.balanceNeto,   DELTA);
        assertEquals(-20.0, pedro.balanceNeto, DELTA);
    }

    @Test
    public void calcularBalances_grupoSinGastos_todosEnCero() {
        // Arrange
        List<MiembroGrupo> miembros = Arrays.asList(
                miembro(1, "Ana",   "#FF0000"),
                miembro(2, "Pedro", "#00FF00")
        );
        List<GastoGrupo> gastos = new ArrayList<>();

        // Act
        List<MiembroBalanceItem> balances = BalanceCalculator.calcularBalancesMiembros(miembros, gastos);

        // Assert: sin gastos, cuota es 0, todos en equilibrio
        assertEquals(2, balances.size());
        for (MiembroBalanceItem b : balances) {
            assertEquals(0.0, b.balanceNeto, DELTA);
        }
    }

    @Test
    public void calcularBalances_miembrosVacios_devuelveListaVacia() {
        // Arrange
        List<MiembroGrupo> miembros = new ArrayList<>();
        List<GastoGrupo> gastos = Collections.singletonList(gasto(1, 50.00, "Ana"));

        // Act
        List<MiembroBalanceItem> balances = BalanceCalculator.calcularBalancesMiembros(miembros, gastos);

        // Assert
        assertNotNull(balances);
        assertTrue(balances.isEmpty());
    }

    @Test
    public void calcularBalances_unSoloMiembro_siempreEnEquilibrio() {
        // Arrange: si hay un solo miembro, cuota = total → balance siempre 0
        List<MiembroGrupo> miembros = Collections.singletonList(
                miembro(1, "Ana", "#FF0000")
        );
        List<GastoGrupo> gastos = Arrays.asList(
                gasto(1, 100.00, "Ana"),
                gasto(2, 50.00,  "Ana")
        );

        // Act
        List<MiembroBalanceItem> balances = BalanceCalculator.calcularBalancesMiembros(miembros, gastos);

        // Assert
        assertEquals(1, balances.size());
        assertEquals(0.0, balances.get(0).balanceNeto, DELTA);
    }

    @Test
    public void calcularBalances_devuelve_totalPagadoCorrecto() {
        // Arrange
        List<MiembroGrupo> miembros = Arrays.asList(
                miembro(1, "Ana",   "#FF0000"),
                miembro(2, "Pedro", "#00FF00")
        );
        List<GastoGrupo> gastos = Arrays.asList(
                gasto(1, 40.00, "Ana"),
                gasto(2, 20.00, "Ana"),
                gasto(3, 60.00, "Pedro")
        );

        // Act
        List<MiembroBalanceItem> balances = BalanceCalculator.calcularBalancesMiembros(miembros, gastos);

        // Assert: Ana pagó 60, Pedro pagó 60 → cuota 60 → todos en equilibrio
        MiembroBalanceItem ana   = buscarPorNombre(balances, "Ana");
        MiembroBalanceItem pedro = buscarPorNombre(balances, "Pedro");

        assertEquals(60.0, ana.totalPagado,   DELTA);
        assertEquals(60.0, pedro.totalPagado, DELTA);
        assertEquals(0.0,  ana.balanceNeto,   DELTA);
        assertEquals(0.0,  pedro.balanceNeto, DELTA);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // calcularDeudas
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    public void calcularDeudas_ejemploDelJavadoc_dosTransacciones() {
        // Arrange: Ana +30, Pedro -10, Luis -20 (del ejemplo del Javadoc de BalanceCalculator)
        List<MiembroBalanceItem> balances = Arrays.asList(
                balanceItem("Ana",   "#FF0000",  30.0),
                balanceItem("Pedro", "#00FF00", -10.0),
                balanceItem("Luis",  "#0000FF", -20.0)
        );

        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(balances);

        // Assert: exactamente 2 transacciones
        assertEquals(2, deudas.size());

        // Verificar que Ana es siempre acreedora
        for (DeudaItem d : deudas) {
            assertEquals("Ana", d.nombreAcreedor);
        }

        // La suma de las transacciones debe saldar el total: 10 + 20 = 30
        double sumaTransacciones = 0;
        for (DeudaItem d : deudas) sumaTransacciones += d.monto;
        assertEquals(30.0, sumaTransacciones, DELTA);
    }

    @Test
    public void calcularDeudas_dosMiembros_unaTransaccion() {
        // Arrange: Ana +20, Pedro -20
        List<MiembroBalanceItem> balances = Arrays.asList(
                balanceItem("Ana",   "#FF0000",  20.0),
                balanceItem("Pedro", "#00FF00", -20.0)
        );

        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(balances);

        // Assert
        assertEquals(1, deudas.size());
        DeudaItem deuda = deudas.get(0);
        assertEquals("Pedro", deuda.nombreDeudor);
        assertEquals("Ana",   deuda.nombreAcreedor);
        assertEquals(20.0,    deuda.monto, DELTA);
    }

    @Test
    public void calcularDeudas_todosEquilibrados_sinDeudas() {
        // Arrange: todos con balance 0
        List<MiembroBalanceItem> balances = Arrays.asList(
                balanceItem("Ana",   "#FF0000", 0.0),
                balanceItem("Pedro", "#00FF00", 0.0),
                balanceItem("Luis",  "#0000FF", 0.0)
        );

        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(balances);

        // Assert
        assertNotNull(deudas);
        assertTrue("Sin desequilibrios no debe haber deudas", deudas.isEmpty());
    }

    @Test
    public void calcularDeudas_listaVacia_devuelveListaVacia() {
        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(new ArrayList<>());

        // Assert
        assertNotNull(deudas);
        assertTrue(deudas.isEmpty());
    }

    @Test
    public void calcularDeudas_listaNull_devuelveListaVacia() {
        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(null);

        // Assert
        assertNotNull(deudas);
        assertTrue(deudas.isEmpty());
    }

    @Test
    public void calcularDeudas_sumaDeudas_igualASumaCreditos() {
        // Arrange: Ana +50, Pedro -30, Luis -20 → suma créditos = suma deudas = 50
        List<MiembroBalanceItem> balances = Arrays.asList(
                balanceItem("Ana",   "#FF0000",  50.0),
                balanceItem("Pedro", "#00FF00", -30.0),
                balanceItem("Luis",  "#0000FF", -20.0)
        );

        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(balances);

        // Assert: suma de todas las transacciones == crédito total de Ana
        double total = 0;
        for (DeudaItem d : deudas) total += d.monto;
        assertEquals(50.0, total, DELTA);
    }

    @Test
    public void calcularDeudas_multiplesAcreedores_transaccionesMinimas() {
        // Arrange: 4 miembros, 2 acreedores y 2 deudores
        // Ana +40, Pedro +10, Luis -30, Maria -20
        List<MiembroBalanceItem> balances = Arrays.asList(
                balanceItem("Ana",   "#FF0000",  40.0),
                balanceItem("Pedro", "#00FF00",  10.0),
                balanceItem("Luis",  "#0000FF", -30.0),
                balanceItem("Maria", "#FFFF00", -20.0)
        );

        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(balances);

        // Assert: al menos 2 transacciones (mínimo para saldar 2 deudores con 2 acreedores)
        assertTrue("Deben generarse transacciones para saldar las deudas",
                deudas.size() >= 2);

        // La suma de transacciones debe saldar todas las deudas: 30 + 20 = 50
        double sumaTotal = 0;
        for (DeudaItem d : deudas) sumaTotal += d.monto;
        assertEquals(50.0, sumaTotal, DELTA);
    }

    @Test
    public void calcularDeudas_montoMuyPequeño_ignorado() {
        // Arrange: diferencia de menos de 1 céntimo debe ignorarse (EPSILON = 0.01)
        List<MiembroBalanceItem> balances = Arrays.asList(
                balanceItem("Ana",   "#FF0000",  0.005),
                balanceItem("Pedro", "#00FF00", -0.005)
        );

        // Act
        List<DeudaItem> deudas = BalanceCalculator.calcularDeudas(balances);

        // Assert: diferencias menores que EPSILON se ignoran
        assertTrue("Diferencias de menos de 1 céntimo deben ignorarse", deudas.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private MiembroGrupo miembro(int id, String nombre, String color) {
        MiembroGrupo m = new MiembroGrupo();
        m.id     = id;
        m.nombre = nombre;
        m.color  = color;
        return m;
    }

    private GastoGrupo gasto(int id, double monto, String pagadoPorNombre) {
        GastoGrupo g = new GastoGrupo();
        g.id              = id;
        g.monto           = monto;
        g.pagadoPorNombre = pagadoPorNombre;
        g.fecha           = System.currentTimeMillis();
        return g;
    }

    private MiembroBalanceItem balanceItem(String nombre, String color, double balanceNeto) {
        // Cuota y totalPagado consistentes con el balanceNeto dado
        double cuota  = 100.0; // valor arbitrario
        double pagado = cuota + balanceNeto;
        return new MiembroBalanceItem(nombre, color, pagado, cuota, balanceNeto);
    }

    private MiembroBalanceItem buscarPorNombre(List<MiembroBalanceItem> lista, String nombre) {
        for (MiembroBalanceItem item : lista) {
            if (nombre.equals(item.nombreMiembro)) return item;
        }
        return null;
    }
}

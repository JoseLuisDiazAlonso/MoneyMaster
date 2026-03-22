package com.example.moneymaster.ui.groups.util;

import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.ui.groups.model.DeudaItem;
import com.example.moneymaster.ui.groups.model.MiembroBalanceItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula quién debe a quién en un grupo de gastos compartidos.
 *
 * Algoritmo en dos pasos:
 *
 * PASO 1 — Balance neto por miembro:
 *   Para cada miembro calculamos su balance neto:
 *     balanceNeto = totalPagado - cuotaIdeal
 *   Donde:
 *     totalPagado  = suma de gastos donde pagadoPorNombre == miembro.nombre
 *     cuotaIdeal   = totalGrupo / numMiembros  (división igualitaria)
 *
 *   Si balanceNeto > 0 → el miembro recupera dinero (acreedor)
 *   Si balanceNeto < 0 → el miembro debe dinero (deudor)
 *   Si balanceNeto == 0 → está equilibrado
 *
 * PASO 2 — Liquidación mínima de transacciones:
 *   Algoritmo greedy de dos punteros sobre listas ordenadas de
 *   acreedores (mayor a menor) y deudores (mayor deuda a menor):
 *     - Tomar el mayor acreedor y el mayor deudor.
 *     - La transacción es min(deuda, crédito).
 *     - Reducir ambos saldos. Si alguno llega a 0, avanzar al siguiente.
 *     - Repetir hasta que todas las deudas sean 0.
 *
 *   Este algoritmo minimiza el número de transacciones necesarias.
 *
 * Ejemplo con 3 miembros y total 90€:
 *   Ana pagó 60€, Pedro 20€, Luis 10€ → cuota ideal 30€ cada uno
 *   Ana:   60 - 30 = +30 (acreedor)
 *   Pedro: 20 - 30 = -10 (deudor)
 *   Luis:  10 - 30 = -20 (deudor)
 *
 *   Transacciones:
 *   Luis  debe 20€ a Ana  → Ana queda con +10
 *   Pedro debe 10€ a Ana  → Ana queda con 0
 *   Resultado: 2 transacciones (mínimo posible)
 */
public class BalanceCalculator {

    private static final double EPSILON = 0.01; // umbral para ignorar diferencias de céntimos

    /**
     * Calcula el balance neto de cada miembro.
     *
     * @param miembros  lista de miembros activos del grupo
     * @param gastos    lista de gastos del grupo
     * @return lista de MiembroBalanceItem con balance neto y color
     */
    public static List<MiembroBalanceItem> calcularBalancesMiembros(
            List<MiembroGrupo> miembros,
            List<GastoGrupo> gastos) {

        if (miembros == null || miembros.isEmpty()) return new ArrayList<>();

        int numMiembros = miembros.size();

        // Total del grupo
        double totalGrupo = 0;
        for (GastoGrupo g : gastos) totalGrupo += g.monto;

        // Cuota ideal por miembro
        double cuotaIdeal = numMiembros > 0 ? totalGrupo / numMiembros : 0;

        // Total pagado por cada miembro (agrupado por nombre)
        Map<String, Double> totalPagadoPorNombre = new HashMap<>();
        for (GastoGrupo g : gastos) {
            if (g.pagadoPorNombre != null) {
                double actual = totalPagadoPorNombre.containsKey(g.pagadoPorNombre)
                        ? totalPagadoPorNombre.get(g.pagadoPorNombre) : 0.0;
                totalPagadoPorNombre.put(g.pagadoPorNombre, actual + g.monto);
            }
        }

        // Construir MiembroBalanceItem para cada miembro
        List<MiembroBalanceItem> resultado = new ArrayList<>();
        for (MiembroGrupo m : miembros) {
            if (m.nombre == null) continue;
            double pagado = totalPagadoPorNombre.containsKey(m.nombre)
                    ? totalPagadoPorNombre.get(m.nombre) : 0.0;
            double balanceNeto = pagado - cuotaIdeal;
            resultado.add(new MiembroBalanceItem(
                    m.nombre,
                    m.color,
                    pagado,
                    cuotaIdeal,
                    balanceNeto
            ));
        }

        return resultado;
    }

    /**
     * Calcula la lista mínima de transacciones para saldar todas las deudas.
     *
     * @param balances lista de MiembroBalanceItem calculados previamente
     * @return lista de DeudaItem: "X debe Y€ a Z"
     */
    public static List<DeudaItem> calcularDeudas(List<MiembroBalanceItem> balances) {
        List<DeudaItem> deudas = new ArrayList<>();
        if (balances == null || balances.isEmpty()) return deudas;

        // Separar en acreedores (balance > 0) y deudores (balance < 0)
        // Usamos arrays mutables para poder modificar los saldos en el algoritmo
        List<double[]> acreedores = new ArrayList<>(); // [0]=monto, [1]=índice en balances
        List<double[]> deudores   = new ArrayList<>();

        for (int i = 0; i < balances.size(); i++) {
            double b = balances.get(i).balanceNeto;
            if (b > EPSILON)  acreedores.add(new double[]{b, i});
            if (b < -EPSILON) deudores.add(new double[]{-b, i}); // guardamos valor positivo
        }

        // Algoritmo greedy de dos punteros
        int ia = 0, id = 0;
        while (ia < acreedores.size() && id < deudores.size()) {
            double credito = acreedores.get(ia)[0];
            double deuda   = deudores.get(id)[0];

            int idxAcreedor = (int) acreedores.get(ia)[1];
            int idxDeudor   = (int) deudores.get(id)[1];

            String nombreAcreedor = balances.get(idxAcreedor).nombreMiembro;
            String nombreDeudor   = balances.get(idxDeudor).nombreMiembro;
            String colorDeudor    = balances.get(idxDeudor).colorMiembro;

            double transaccion = Math.min(credito, deuda);

            deudas.add(new DeudaItem(
                    nombreDeudor,
                    colorDeudor,
                    nombreAcreedor,
                    transaccion,
                    false // no pagado por defecto
            ));

            acreedores.get(ia)[0] -= transaccion;
            deudores.get(id)[0]   -= transaccion;

            if (acreedores.get(ia)[0] < EPSILON) ia++;
            if (deudores.get(id)[0]   < EPSILON) id++;
        }

        return deudas;
    }
}

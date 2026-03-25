package com.example.moneymaster.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import com.example.moneymaster.data.model.BalanceGrupo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.MiembroGrupo;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ResumenTextGenerator — Genera resúmenes de texto formateados del balance de un grupo.
 *
 * Produce texto enriquecido con emojis y separadores, listo para ser compartido
 * por WhatsApp, SMS o cualquier app de mensajería. También incluye utilidad para
 * copiar al portapapeles directamente.
 *
 * Uso básico:
 *   ResumenTextGenerator.DatosGrupo datos = new ResumenTextGenerator.DatosGrupo(
 *       grupo, miembros, gastos, balances, mapaAcreedores
 *   );
 *   String texto = ResumenTextGenerator.generar(datos);
 *   ResumenTextGenerator.copiarAlPortapapeles(context, texto);
 *
 * Card #37 — Sprint 5: Compartir
 * Dependencia: GROUP-007 (BalanceGrupo con deudor/acreedor)
 */
public class ResumenTextGenerator {

    // ── Constantes de formato ─────────────────────────────────────────────────

    private static final String SEPARADOR_GRUESO  = "━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final String SEPARADOR_FINO    = "──────────────────────────";
    private static final String SEPARADOR_PUNTOS  = "··························";

    private static final String EMOJI_GRUPO       = "👥";
    private static final String EMOJI_DINERO      = "💰";
    private static final String EMOJI_PERSONA     = "👤";
    private static final String EMOJI_DEBE        = "📌";
    private static final String EMOJI_COBRAR      = "✅";
    private static final String EMOJI_FECHA       = "📅";
    private static final String EMOJI_TOTAL       = "💵";
    private static final String EMOJI_PAGADO      = "✔️";
    private static final String EMOJI_OK          = "🎉";
    private static final String EMOJI_APP         = "📱";

    // Locale español para formato de moneda (punto de miles, coma decimal)
    private static final Locale LOCALE_ES         = new Locale("es", "ES");
    private static final NumberFormat FORMATO_MONEDA;

    static {
        FORMATO_MONEDA = NumberFormat.getNumberInstance(LOCALE_ES);
        FORMATO_MONEDA.setMinimumFractionDigits(2);
        FORMATO_MONEDA.setMaximumFractionDigits(2);
    }

    // Evitar instanciación
    private ResumenTextGenerator() {}

    // =========================================================================
    // Clase contenedora de datos del grupo
    // =========================================================================

    /**
     * Agrupa todos los datos necesarios para generar el resumen.
     *
     * El llamador (Fragment/ViewModel) es responsable de proporcionar datos
     * ya cargados desde Room. Esta clase no accede a la base de datos.
     */
    public static class DatosGrupo {

        /** Entidad Grupo (nombre, descripción). */
        public final Grupo grupo;

        /** Lista de miembros del grupo. */
        public final List<MiembroGrupo> miembros;

        /** Lista de gastos del grupo (para calcular total pagado por persona). */
        public final List<GastoGrupo> gastos;

        /**
         * Lista de balances calculados por GROUP-007.
         * Cada fila tiene: grupoId, usuarioDeudorId, usuarioAcreedorId, montoPendiente, liquidado.
         */
        public final List<BalanceGrupo> balances;

        /**
         * Mapa de ID de miembro → nombre legible.
         * Necesario porque BalanceGrupo solo tiene IDs numéricos.
         * Key: MiembroGrupo.id  |  Value: MiembroGrupo.nombre
         */
        public final Map<Integer, String> mapaIdNombre;

        public DatosGrupo(Grupo grupo,
                          List<MiembroGrupo> miembros,
                          List<GastoGrupo> gastos,
                          List<BalanceGrupo> balances,
                          Map<Integer, String> mapaIdNombre) {
            this.grupo        = grupo;
            this.miembros     = miembros != null ? miembros : new ArrayList<>();
            this.gastos       = gastos   != null ? gastos   : new ArrayList<>();
            this.balances     = balances != null ? balances : new ArrayList<>();
            this.mapaIdNombre = mapaIdNombre != null ? mapaIdNombre : new HashMap<>();
        }
    }

    // =========================================================================
    // Método principal: generar(DatosGrupo)
    // =========================================================================

    /**
     * Genera el texto completo del resumen del grupo.
     *
     * El texto producido sigue esta estructura:
     *   1. Cabecera con nombre del grupo y fecha de generación
     *   2. Resumen totales (total gastado, núm. miembros, cuota equitativa)
     *   3. Gastos por miembro (cuánto ha pagado cada uno)
     *   4. Balance individual (quién recupera / quién debe)
     *   5. Lista de liquidaciones sugeridas (transacciones mínimas)
     *   6. Pie de página con mención a MoneyMaster
     *
     * @param datos Objeto con todos los datos del grupo ya cargados.
     * @return String formateado listo para compartir.
     */
    public static String generar(DatosGrupo datos) {
        if (datos == null || datos.grupo == null) {
            return "Error: datos del grupo no disponibles.";
        }

        StringBuilder sb = new StringBuilder();

        agregarCabecera(sb, datos);
        agregarTotales(sb, datos);
        agregarGastosPorMiembro(sb, datos);
        agregarBalance(sb, datos);
        agregarLiquidaciones(sb, datos);
        agregarPie(sb);

        return sb.toString().trim();
    }

    // =========================================================================
    // Secciones del resumen
    // =========================================================================

    /**
     * Sección 1 — Cabecera.
     * Muestra el nombre del grupo y la fecha/hora de generación del resumen.
     */
    private static void agregarCabecera(StringBuilder sb, DatosGrupo datos) {
        String fechaHoy = new SimpleDateFormat("d MMM yyyy, HH:mm", LOCALE_ES)
                .format(new Date());

        sb.append(SEPARADOR_GRUESO).append("\n");
        sb.append(EMOJI_GRUPO).append(" *").append(datos.grupo.nombre.toUpperCase()).append("*\n");

        if (datos.grupo.descripcion != null && !datos.grupo.descripcion.isEmpty()) {
            sb.append("   ").append(datos.grupo.descripcion).append("\n");
        }

        sb.append(EMOJI_FECHA).append(" ").append(fechaHoy).append("\n");
        sb.append(SEPARADOR_GRUESO).append("\n\n");
    }

    /**
     * Sección 2 — Totales globales del grupo.
     * Total gastado, número de miembros y cuota equitativa.
     */
    private static void agregarTotales(StringBuilder sb, DatosGrupo datos) {
        double totalGastado = calcularTotalGastado(datos.gastos);
        int numMiembros     = datos.miembros.size();
        double cuota        = numMiembros > 0 ? totalGastado / numMiembros : 0;

        sb.append(EMOJI_TOTAL).append(" *RESUMEN GENERAL*\n");
        sb.append(SEPARADOR_FINO).append("\n");
        sb.append("💶 Total gastado:   ").append(formatearMonto(totalGastado)).append(" €\n");
        sb.append(EMOJI_PERSONA).append("  Miembros:         ").append(numMiembros).append(" personas\n");
        sb.append("⚖️  Cuota equitativa: ").append(formatearMonto(cuota)).append(" €/persona\n");
        sb.append("\n");
    }

    /**
     * Sección 3 — Gastos por miembro.
     * Cuánto ha pagado cada miembro como pagador principal de gastos.
     */
    private static void agregarGastosPorMiembro(StringBuilder sb, DatosGrupo datos) {
        sb.append(EMOJI_PERSONA).append(" *GASTOS POR PERSONA*\n");
        sb.append(SEPARADOR_FINO).append("\n");

        // Acumular total pagado por cada miembro
        Map<Integer, Double> pagadoPor = calcularPagadoPorMiembro(datos.gastos);

        if (datos.miembros.isEmpty()) {
            sb.append("   Sin datos de miembros.\n");
        } else {
            for (MiembroGrupo m : datos.miembros) {
                double pagado = pagadoPor.getOrDefault(m.id, 0.0);
                sb.append(EMOJI_PERSONA).append(" ")
                        .append(padDerecha(m.nombre, 16))
                        .append(formatearMonto(pagado)).append(" €\n");
            }
        }
        sb.append("\n");
    }

    /**
     * Sección 4 — Balance individual.
     * Verde (recupera dinero) → ✅  |  Rojo (debe dinero) → 📌
     *
     * El balance neto de cada persona = totalPagado − cuota
     * Positivo → los demás le deben.  Negativo → él debe al grupo.
     */
    private static void agregarBalance(StringBuilder sb, DatosGrupo datos) {
        sb.append(EMOJI_DINERO).append(" *BALANCE INDIVIDUAL*\n");
        sb.append(SEPARADOR_FINO).append("\n");

        double totalGastado = calcularTotalGastado(datos.gastos);
        int numMiembros     = datos.miembros.size();
        double cuota        = numMiembros > 0 ? totalGastado / numMiembros : 0;

        Map<Integer, Double> pagadoPor = calcularPagadoPorMiembro(datos.gastos);

        boolean todosEnPaz = true;

        for (MiembroGrupo m : datos.miembros) {
            double pagado  = pagadoPor.getOrDefault(m.id, 0.0);
            double balance = pagado - cuota;

            if (Math.abs(balance) < 0.01) {
                // En paz — no añadir línea para no ensuciar
                continue;
            }

            todosEnPaz = false;

            if (balance > 0) {
                sb.append(EMOJI_COBRAR).append(" ")
                        .append(padDerecha(m.nombre, 14))
                        .append("recupera +").append(formatearMonto(balance)).append(" €\n");
            } else {
                sb.append(EMOJI_DEBE).append(" ")
                        .append(padDerecha(m.nombre, 14))
                        .append("debe      ").append(formatearMonto(Math.abs(balance))).append(" €\n");
            }
        }

        if (todosEnPaz) {
            sb.append(EMOJI_OK).append(" ¡Todos están en paz! No hay deudas pendientes.\n");
        }
        sb.append("\n");
    }

    /**
     * Sección 5 — Liquidaciones sugeridas.
     *
     * Usa los balances de la tabla balance_grupo (calculados por GROUP-007)
     * para listar las transacciones necesarias para saldar todas las deudas.
     * Solo muestra las deudas con montoPendiente > 0.01 y liquidado == 0.
     */
    private static void agregarLiquidaciones(StringBuilder sb, DatosGrupo datos) {
        sb.append(EMOJI_DEBE).append(" *LIQUIDACIONES SUGERIDAS*\n");
        sb.append(SEPARADOR_FINO).append("\n");

        // Filtrar solo deudas pendientes (no liquidadas)
        List<BalanceGrupo> pendientes = new ArrayList<>();
        for (BalanceGrupo b : datos.balances) {
            if (b.liquidado == 0 && b.montoPendiente > 0.01) {
                pendientes.add(b);
            }
        }

        if (pendientes.isEmpty()) {
            sb.append(EMOJI_OK).append(" No hay liquidaciones pendientes.\n");
        } else {
            int num = 1;
            for (BalanceGrupo b : pendientes) {
                String nombreDeudor   = datos.mapaIdNombre.getOrDefault(b.usuarioDeudorId,   "Miembro " + b.usuarioDeudorId);
                String nombreAcreedor = datos.mapaIdNombre.getOrDefault(b.usuarioAcreedorId, "Miembro " + b.usuarioAcreedorId);

                sb.append("  ").append(num++).append(". ")
                        .append(nombreDeudor)
                        .append(" → ")
                        .append(nombreAcreedor)
                        .append("  *").append(formatearMonto(b.montoPendiente)).append(" €*\n");
            }
        }

        sb.append("\n");
    }

    /**
     * Sección 6 — Pie de página.
     * Crédito a la app y mensaje motivacional.
     */
    private static void agregarPie(StringBuilder sb) {
        sb.append(SEPARADOR_PUNTOS).append("\n");
        sb.append(EMOJI_APP).append(" _Generado con MoneyMaster_\n");
        sb.append("_¡Compartir es de bien nacidos! 💸_\n");
        sb.append(SEPARADOR_PUNTOS).append("\n");
    }

    // =========================================================================
    // Variante: solo liquidaciones (mensaje corto para WhatsApp)
    // =========================================================================

    /**
     * Genera un resumen muy compacto, ideal para enviar como mensaje rápido.
     * Solo incluye el nombre del grupo, total y las liquidaciones.
     *
     * @param datos Datos del grupo.
     * @return Texto corto listo para copiar/enviar.
     */
    public static String generarCompacto(DatosGrupo datos) {
        if (datos == null || datos.grupo == null) return "";

        StringBuilder sb = new StringBuilder();

        double totalGastado = calcularTotalGastado(datos.gastos);

        sb.append(EMOJI_GRUPO).append(" *").append(datos.grupo.nombre).append("*  ")
                .append(EMOJI_TOTAL).append(" ").append(formatearMonto(totalGastado)).append(" € total\n");
        sb.append(SEPARADOR_FINO).append("\n");

        List<BalanceGrupo> pendientes = new ArrayList<>();
        for (BalanceGrupo b : datos.balances) {
            if (b.liquidado == 0 && b.montoPendiente > 0.01) {
                pendientes.add(b);
            }
        }

        if (pendientes.isEmpty()) {
            sb.append(EMOJI_OK).append(" Sin deudas pendientes.");
        } else {
            for (BalanceGrupo b : pendientes) {
                String deudor   = datos.mapaIdNombre.getOrDefault(b.usuarioDeudorId,   "?");
                String acreedor = datos.mapaIdNombre.getOrDefault(b.usuarioAcreedorId, "?");
                sb.append(EMOJI_DEBE).append(" ").append(deudor)
                        .append(" → ").append(acreedor)
                        .append(": ").append(formatearMonto(b.montoPendiente)).append(" €\n");
            }
        }

        sb.append(EMOJI_APP).append(" _MoneyMaster_");
        return sb.toString().trim();
    }

    // =========================================================================
    // Portapapeles
    // =========================================================================

    /**
     * Copia el texto proporcionado al portapapeles del sistema y muestra un Toast.
     *
     * Llamar desde la UI thread (Activity o Fragment).
     *
     * @param context Contexto actual.
     * @param texto   Texto a copiar.
     */
    public static void copiarAlPortapapeles(Context context, String texto) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard == null) {
            Toast.makeText(context, "No se pudo acceder al portapapeles", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipData clip = ClipData.newPlainText("Resumen MoneyMaster", texto);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "✅ Resumen copiado al portapapeles", Toast.LENGTH_SHORT).show();
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    /**
     * Suma todos los montos de la lista de gastos del grupo.
     */
    private static double calcularTotalGastado(List<GastoGrupo> gastos) {
        double total = 0;
        for (GastoGrupo g : gastos) {
            total += g.monto;
        }
        return total;
    }

    /**
     * Construye un mapa MiembroId → totalPagado acumulando los gastos donde ese miembro
     * fue el pagador principal (pagadoPorId).
     *
     * NOTA: Este cálculo es para mostrar cuánto ha puesto de su bolsillo cada persona,
     * independientemente del reparto. El balance real lo proporciona la tabla balance_grupo.
     */
    private static Map<Integer, Double> calcularPagadoPorMiembro(List<GastoGrupo> gastos) {
        Map<Integer, Double> mapa = new HashMap<>();
        for (GastoGrupo g : gastos) {
            int pagador = g.pagadoPorId;
            mapa.put(pagador, mapa.getOrDefault(pagador, 0.0) + g.monto);
        }
        return mapa;
    }

    /**
     * Formatea un double como moneda española ("1.234,56").
     */
    private static String formatearMonto(double monto) {
        return FORMATO_MONEDA.format(monto);
    }

    /**
     * Rellena un String con espacios a la derecha hasta alcanzar la longitud deseada.
     * Útil para alinear columnas en texto monoespaciado.
     */
    private static String padDerecha(String texto, int longitud) {
        if (texto == null) texto = "";
        if (texto.length() >= longitud) return texto.substring(0, longitud);
        StringBuilder sb = new StringBuilder(texto);
        while (sb.length() < longitud) sb.append(' ');
        return sb.toString();
    }
}

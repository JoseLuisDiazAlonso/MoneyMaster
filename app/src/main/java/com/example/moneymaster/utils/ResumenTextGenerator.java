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

public class ResumenTextGenerator {

    private static final String SEPARADOR_GRUESO = "━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final String SEPARADOR_FINO   = "──────────────────────────";
    private static final String SEPARADOR_PUNTOS = "··························";

    private static final String EMOJI_GRUPO   = "👥";
    private static final String EMOJI_DINERO  = "💰";
    private static final String EMOJI_PERSONA = "👤";
    private static final String EMOJI_DEBE    = "📌";
    private static final String EMOJI_COBRAR  = "✅";
    private static final String EMOJI_FECHA   = "📅";
    private static final String EMOJI_TOTAL   = "💵";
    private static final String EMOJI_OK      = "🎉";
    private static final String EMOJI_APP     = "📱";

    private static final Locale       LOCALE_ES = new Locale("es", "ES");
    private static final NumberFormat FORMATO_MONEDA;

    static {
        FORMATO_MONEDA = NumberFormat.getNumberInstance(LOCALE_ES);
        FORMATO_MONEDA.setMinimumFractionDigits(2);
        FORMATO_MONEDA.setMaximumFractionDigits(2);
    }

    private ResumenTextGenerator() {}


    // DatosGrupo


    public static class DatosGrupo {
        public final Grupo              grupo;
        public final List<MiembroGrupo> miembros;
        public final List<GastoGrupo>   gastos;
        public final List<BalanceGrupo> balances;
        public final Map<Integer, String> mapaIdNombre;

        public DatosGrupo(Grupo grupo,
                          List<MiembroGrupo> miembros,
                          List<GastoGrupo> gastos,
                          List<BalanceGrupo> balances,
                          Map<Integer, String> mapaIdNombre) {
            this.grupo        = grupo;
            this.miembros     = miembros     != null ? miembros     : new ArrayList<>();
            this.gastos       = gastos       != null ? gastos       : new ArrayList<>();
            this.balances     = balances     != null ? balances     : new ArrayList<>();
            this.mapaIdNombre = mapaIdNombre != null ? mapaIdNombre : new HashMap<>();
        }
    }


    // Método principal


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


    // Secciones


    private static void agregarCabecera(StringBuilder sb, DatosGrupo datos) {
        String fechaHoy = new SimpleDateFormat("d MMM yyyy, HH:mm", LOCALE_ES)
                .format(new Date());
        sb.append(SEPARADOR_GRUESO).append("\n");
        sb.append(EMOJI_GRUPO).append(" *")
                .append(datos.grupo.nombre.toUpperCase()).append("*\n");
        if (datos.grupo.descripcion != null && !datos.grupo.descripcion.isEmpty()) {
            sb.append("   ").append(datos.grupo.descripcion).append("\n");
        }
        sb.append(EMOJI_FECHA).append(" ").append(fechaHoy).append("\n");
        sb.append(SEPARADOR_GRUESO).append("\n\n");
    }

    private static void agregarTotales(StringBuilder sb, DatosGrupo datos) {
        double totalGastado = calcularTotalGastado(datos.gastos);
        int    numMiembros  = datos.miembros.size();
        double cuota        = numMiembros > 0 ? totalGastado / numMiembros : 0;

        sb.append(EMOJI_TOTAL).append(" *RESUMEN GENERAL*\n");
        sb.append(SEPARADOR_FINO).append("\n");
        sb.append("💶 Total gastado:   ").append(formatearMonto(totalGastado)).append(" €\n");
        sb.append(EMOJI_PERSONA).append("  Miembros:         ").append(numMiembros).append(" personas\n");
        sb.append("⚖️  Cuota equitativa: ").append(formatearMonto(cuota)).append(" €/persona\n");
        sb.append("\n");
    }

    /**
     * FIX: acumula por pagadoPorNombre (texto) en lugar de pagadoPorId (siempre 0).
     * Cruza con m.nombre para mostrar la fila de cada miembro.
     */
    private static void agregarGastosPorMiembro(StringBuilder sb, DatosGrupo datos) {
        sb.append(EMOJI_PERSONA).append(" *GASTOS POR PERSONA*\n");
        sb.append(SEPARADOR_FINO).append("\n");

        // FIX: mapa nombre → total pagado (insensible a mayúsculas para robustez)
        Map<String, Double> pagadoPorNombre = calcularPagadoPorNombre(datos.gastos);

        if (datos.miembros.isEmpty()) {
            sb.append("   Sin datos de miembros.\n");
        } else {
            for (MiembroGrupo m : datos.miembros) {
                // Buscar por nombre exacto primero, luego insensible a mayúsculas
                double pagado = 0.0;
                if (m.nombre != null) {
                    pagado = pagadoPorNombre.getOrDefault(m.nombre, 0.0);
                    if (pagado == 0.0) {
                        // Fallback: búsqueda insensible a mayúsculas
                        for (Map.Entry<String, Double> entry : pagadoPorNombre.entrySet()) {
                            if (entry.getKey().equalsIgnoreCase(m.nombre)) {
                                pagado = entry.getValue();
                                break;
                            }
                        }
                    }
                }
                sb.append(EMOJI_PERSONA).append(" ")
                        .append(padDerecha(m.nombre != null ? m.nombre : "?", 16))
                        .append(formatearMonto(pagado)).append(" €\n");
            }
        }
        sb.append("\n");
    }

    /**
     * FIX: balance también usa nombre en lugar de ID para ser consistente
     * con cómo se guardan los gastos.
     */
    private static void agregarBalance(StringBuilder sb, DatosGrupo datos) {
        sb.append(EMOJI_DINERO).append(" *BALANCE INDIVIDUAL*\n");
        sb.append(SEPARADOR_FINO).append("\n");

        double totalGastado = calcularTotalGastado(datos.gastos);
        int    numMiembros  = datos.miembros.size();
        double cuota        = numMiembros > 0 ? totalGastado / numMiembros : 0;

        Map<String, Double> pagadoPorNombre = calcularPagadoPorNombre(datos.gastos);

        boolean todosEnPaz = true;

        for (MiembroGrupo m : datos.miembros) {
            double pagado = 0.0;
            if (m.nombre != null) {
                pagado = pagadoPorNombre.getOrDefault(m.nombre, 0.0);
                if (pagado == 0.0) {
                    for (Map.Entry<String, Double> entry : pagadoPorNombre.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(m.nombre)) {
                            pagado = entry.getValue();
                            break;
                        }
                    }
                }
            }
            double balance = pagado - cuota;

            if (Math.abs(balance) < 0.01) continue;

            todosEnPaz = false;

            if (balance > 0) {
                sb.append(EMOJI_COBRAR).append(" ")
                        .append(padDerecha(m.nombre != null ? m.nombre : "?", 14))
                        .append("recupera +").append(formatearMonto(balance)).append(" €\n");
            } else {
                sb.append(EMOJI_DEBE).append(" ")
                        .append(padDerecha(m.nombre != null ? m.nombre : "?", 14))
                        .append("debe      ").append(formatearMonto(Math.abs(balance))).append(" €\n");
            }
        }

        if (todosEnPaz) {
            sb.append(EMOJI_OK).append(" ¡Todos están en paz! No hay deudas pendientes.\n");
        }
        sb.append("\n");
    }

    private static void agregarLiquidaciones(StringBuilder sb, DatosGrupo datos) {
        sb.append(EMOJI_DEBE).append(" *LIQUIDACIONES SUGERIDAS*\n");
        sb.append(SEPARADOR_FINO).append("\n");

        List<BalanceGrupo> pendientes = new ArrayList<>();
        for (BalanceGrupo b : datos.balances) {
            if (b.liquidado == 0 && b.montoPendiente > 0.01) pendientes.add(b);
        }

        if (pendientes.isEmpty()) {
            sb.append(EMOJI_OK).append(" No hay liquidaciones pendientes.\n");
        } else {
            int num = 1;
            for (BalanceGrupo b : pendientes) {
                String nombreDeudor   = datos.mapaIdNombre.getOrDefault(
                        b.usuarioDeudorId,   "Miembro " + b.usuarioDeudorId);
                String nombreAcreedor = datos.mapaIdNombre.getOrDefault(
                        b.usuarioAcreedorId, "Miembro " + b.usuarioAcreedorId);
                sb.append("  ").append(num++).append(". ")
                        .append(nombreDeudor).append(" → ").append(nombreAcreedor)
                        .append("  *").append(formatearMonto(b.montoPendiente)).append(" €*\n");
            }
        }
        sb.append("\n");
    }

    private static void agregarPie(StringBuilder sb) {
        sb.append(SEPARADOR_PUNTOS).append("\n");
        sb.append(EMOJI_APP).append(" _Generado con MoneyMaster_\n");
        sb.append("_¡Compartir es de bien nacidos! 💸_\n");
        sb.append(SEPARADOR_PUNTOS).append("\n");
    }


    // Variante compacta


    public static String generarCompacto(DatosGrupo datos) {
        if (datos == null || datos.grupo == null) return "";

        StringBuilder sb = new StringBuilder();
        double totalGastado = calcularTotalGastado(datos.gastos);

        sb.append(EMOJI_GRUPO).append(" *").append(datos.grupo.nombre).append("*  ")
                .append(EMOJI_TOTAL).append(" ").append(formatearMonto(totalGastado)).append(" € total\n");
        sb.append(SEPARADOR_FINO).append("\n");

        List<BalanceGrupo> pendientes = new ArrayList<>();
        for (BalanceGrupo b : datos.balances) {
            if (b.liquidado == 0 && b.montoPendiente > 0.01) pendientes.add(b);
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


    // Portapapeles


    public static void copiarAlPortapapeles(Context context, String texto) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(context, "No se pudo acceder al portapapeles",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ClipData clip = ClipData.newPlainText("Resumen MoneyMaster", texto);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "✅ Resumen copiado al portapapeles",
                Toast.LENGTH_SHORT).show();
    }


    // Helpers privados


    private static double calcularTotalGastado(List<GastoGrupo> gastos) {
        double total = 0;
        for (GastoGrupo g : gastos) total += g.monto;
        return total;
    }

    /**
     * FIX: acumula por pagadoPorNombre en lugar de pagadoPorId.
     * pagadoPorId siempre vale 0 porque AddGroupExpenseDialog no lo asigna.
     */
    private static Map<String, Double> calcularPagadoPorNombre(List<GastoGrupo> gastos) {
        Map<String, Double> mapa = new HashMap<>();
        for (GastoGrupo g : gastos) {
            if (g.pagadoPorNombre != null && !g.pagadoPorNombre.isEmpty()) {
                String nombre = g.pagadoPorNombre.trim();
                mapa.put(nombre, mapa.getOrDefault(nombre, 0.0) + g.monto);
            }
        }
        return mapa;
    }

    private static String formatearMonto(double monto) {
        return FORMATO_MONEDA.format(monto);
    }

    private static String padDerecha(String texto, int longitud) {
        if (texto == null) texto = "";
        if (texto.length() >= longitud) return texto.substring(0, longitud);
        StringBuilder sb = new StringBuilder(texto);
        while (sb.length() < longitud) sb.append(' ');
        return sb.toString();
    }
}
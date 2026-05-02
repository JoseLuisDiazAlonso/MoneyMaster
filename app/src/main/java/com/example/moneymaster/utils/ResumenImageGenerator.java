package com.example.moneymaster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.moneymaster.data.model.BalanceGrupo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.Grupo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ResumenImageGenerator {

    // ── Dimensiones del canvas ─────────────────────────────────────────────────
    private static final int  IMG_W         = 1080;
    private static final int  IMG_H         = 1920;

    // ── Márgenes y padding ─────────────────────────────────────────────────────
    private static final float MARGIN        = 48f;
    private static final float PADDING       = 36f;
    private static final float CARD_RADIUS   = 28f;
    private static final float ROW_H         = 88f;    // altura de cada fila de tabla

    // ── Paleta Material ────────────────────────────────────────────────────────
    private static final int COLOR_BG           = Color.parseColor("#F4F6FA");
    private static final int COLOR_HEADER_TOP   = Color.parseColor("#1565C0");  // Blue 800
    private static final int COLOR_HEADER_BOT   = Color.parseColor("#1976D2");  // Blue 700
    private static final int COLOR_CARD_BG      = Color.parseColor("#FFFFFF");
    private static final int COLOR_ACCENT       = Color.parseColor("#1976D2");
    private static final int COLOR_TEXT_PRIMARY = Color.parseColor("#1A1A2E");
    private static final int COLOR_TEXT_SEC     = Color.parseColor("#5C6B8A");
    private static final int COLOR_GREEN        = Color.parseColor("#2E7D32");  // Green 800
    private static final int COLOR_GREEN_BG     = Color.parseColor("#E8F5E9");
    private static final int COLOR_RED          = Color.parseColor("#C62828");  // Red 800
    private static final int COLOR_RED_BG       = Color.parseColor("#FFEBEE");
    private static final int COLOR_DIVIDER      = Color.parseColor("#E0E6EF");
    private static final int COLOR_BAR_BG       = Color.parseColor("#E8EFF8");
    private static final int COLOR_GOLD         = Color.parseColor("#F9A825");

    // Paleta de colores para los sectores del pie chart y avatares
    private static final int[] MEMBER_COLORS = {
            Color.parseColor("#1976D2"),  // azul
            Color.parseColor("#388E3C"),  // verde
            Color.parseColor("#F57C00"),  // naranja
            Color.parseColor("#7B1FA2"),  // morado
            Color.parseColor("#D32F2F"),  // rojo
            Color.parseColor("#0097A7"),  // cian
            Color.parseColor("#5D4037"),  // marrón
            Color.parseColor("#455A64"),  // gris azulado
    };

    // FileProvider authority (igual que ShareUtils)
    private static final String FILE_PROVIDER_AUTHORITY =
            "com.example.moneymaster.fileprovider";

    // Locale español
    private static final Locale LOCALE_ES = new Locale("es", "ES");
    private static final NumberFormat FMT_MONEDA;

    static {
        FMT_MONEDA = NumberFormat.getNumberInstance(LOCALE_ES);
        FMT_MONEDA.setMinimumFractionDigits(2);
        FMT_MONEDA.setMaximumFractionDigits(2);
    }

    private ResumenImageGenerator() {}

    // =========================================================================
    // DTO: DatosGrupo
    // =========================================================================

    /**
     * DTO con todos los datos necesarios para generar la imagen.
     * El llamador (Fragment/ViewModel) debe construirlo con datos ya cargados.
     */
    public static class DatosGrupo {

        /** Entidad del grupo. */
        public final Grupo grupo;

        /**
         * Lista de entradas (una por miembro activo) con nombre y total pagado.
         * El llamador debe construir esta lista uniendo MiembroGrupo + User.fullName.
         */
        public final List<EntradaMiembro> miembros;

        /** Balances calculados por GROUP-007 (deudor → acreedor, montoPendiente). */
        public final List<BalanceGrupo> balances;

        /** Mapa ID → nombre legible (MiembroGrupo.usuarioId → User.fullName). */
        public final Map<Integer, String> mapaIdNombre;

        public DatosGrupo(Grupo grupo,
                          List<EntradaMiembro> miembros,
                          List<BalanceGrupo> balances,
                          Map<Integer, String> mapaIdNombre) {
            this.grupo         = grupo;
            this.miembros      = miembros      != null ? miembros      : new ArrayList<>();
            this.balances      = balances      != null ? balances      : new ArrayList<>();
            this.mapaIdNombre  = mapaIdNombre  != null ? mapaIdNombre  : new HashMap<>();
        }
    }

    /**
     * Entrada de un miembro para el generador de imagen.
     * Contiene nombre (ya resuelto desde User.fullName) y cuánto pagó en total.
     */
    public static class EntradaMiembro {
        public final int    usuarioId;
        public final String nombre;
        public final double totalPagado;

        public EntradaMiembro(int usuarioId, String nombre, double totalPagado) {
            this.usuarioId   = usuarioId;
            this.nombre      = nombre;
            this.totalPagado = totalPagado;
        }
    }

    // =========================================================================
    // Método principal: generar()
    // =========================================================================

    /**
     * Genera la imagen PNG del balance del grupo y la guarda en la caché de la app.
     *
     * Se ejecuta en el hilo actual — LLAMAR DESDE UN HILO DE FONDO (ej. executor).
     *
     * @param context Contexto de la app.
     * @param datos   Datos del grupo para dibujar.
     * @return File del PNG generado, o null si hubo error.
     */
    public static File generarFile(Context context, DatosGrupo datos) {
        if (datos == null || datos.grupo == null) return null;

        // Calcular altura real dinámica según número de miembros
        int alturaReal = calcularAlturaTotal(datos);
        Bitmap bitmap = Bitmap.createBitmap(IMG_W, alturaReal, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Fondo general
        canvas.drawColor(COLOR_BG);

        // Dibujar secciones en orden vertical
        float y = 0f;
        y = dibujarCabecera(canvas, datos, y);
        y = dibujarTarjetaTotales(canvas, datos, y);
        y = dibujarTablaGastos(canvas, datos, y);
        y = dibujarPieChart(canvas, datos, y);
        y = dibujarSeccionBalance(canvas, datos, y);
        y = dibujarLiquidaciones(canvas, datos, y);
        dibujarPie(canvas, y, alturaReal);

        // Guardar en caché
        return guardarEnCache(context, bitmap, datos.grupo.nombre);
    }

    /**
     * Genera la imagen y devuelve su Uri de FileProvider para compartir.
     *
     * @param context Contexto de la app.
     * @param datos   Datos del grupo.
     * @return Uri del archivo generado, o null si hubo error.
     */
    public static Uri generarUri(Context context, DatosGrupo datos) {
        File file = generarFile(context, datos);
        if (file == null) return null;
        try {
            return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================================================
    // Helper: construir DatosGrupo desde listas Room
    // =========================================================================

    /**
     * Método de conveniencia para construir el DatosGrupo desde las listas raw de Room.
     * Llámalo en el Fragment o ViewModel antes de invocar generar().
     *
     * @param grupo         Entidad Grupo.
     * @param mapaIdNombre  Map<usuarioId, nombre> (construido del JOIN MiembroGrupo + User).
     * @param gastos        Lista de GastoGrupo del grupo.
     * @param balances      Lista de BalanceGrupo del grupo.
     * @return DatosGrupo listo para usar.
     */
    public static DatosGrupo construirDatos(Grupo grupo,
                                            Map<Integer, String> mapaIdNombre,
                                            List<GastoGrupo> gastos,
                                            List<BalanceGrupo> balances) {
        // Acumular total pagado por cada miembro
        Map<Integer, Double> pagadoPor = new HashMap<>();
        if (gastos != null) {
            for (GastoGrupo g : gastos) {
                // pagadoPorId es int (primitivo) — nunca null
                pagadoPor.put(g.pagadoPorId,
                        pagadoPor.getOrDefault(g.pagadoPorId, 0.0) + g.monto);
            }
        }

        // Construir lista de entradas de miembros
        List<EntradaMiembro> entradas = new ArrayList<>();
        if (mapaIdNombre != null) {
            for (Map.Entry<Integer, String> e : mapaIdNombre.entrySet()) {
                entradas.add(new EntradaMiembro(
                        e.getKey(),
                        e.getValue(),
                        pagadoPor.getOrDefault(e.getKey(), 0.0)
                ));
            }
        }

        return new DatosGrupo(grupo, entradas, balances, mapaIdNombre);
    }

    // =========================================================================
    // Secciones de dibujo
    // =========================================================================

    /** Altura dinámica: base fija + filas por miembro y liquidaciones. */
    private static int calcularAlturaTotal(DatosGrupo datos) {
        int filasMiembros = datos.miembros.size();
        int filasLiquid   = contarLiquidacionesPendientes(datos);
        // Cabecera(320) + Totales(220) + TituloTabla(60) + filas(88 c/u) + PieChart(380)
        // + Balance(60 + 88*miembros) + Liquidaciones(60 + 88*liquid) + Pie(140)
        return 320 + 220 + 80 + (filasMiembros * (int) ROW_H)
                + 380
                + 80 + (filasMiembros * (int) ROW_H)
                + 80 + Math.max(1, filasLiquid) * (int) ROW_H
                + 160;
    }

    // ── CABECERA ──────────────────────────────────────────────────────────────

    /**
     * Dibuja el bloque de cabecera con fondo degradado azul, nombre del grupo,
     * emoji decorativo y fecha de generación.
     */
    private static float dibujarCabecera(Canvas canvas, DatosGrupo datos, float y) {
        float h = 300f;
        RectF rect = new RectF(0, y, IMG_W, y + h);

        // Degradado manual: dos bandas de color
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(COLOR_HEADER_TOP);
        canvas.drawRect(0, y, IMG_W, y + h * 0.55f, p);
        p.setColor(COLOR_HEADER_BOT);
        canvas.drawRect(0, y + h * 0.55f, IMG_W, y + h, p);

        // Círculo decorativo (logo placeholder)
        Paint circulo = new Paint(Paint.ANTI_ALIAS_FLAG);
        circulo.setColor(Color.parseColor("#FFFFFF20"));
        canvas.drawCircle(IMG_W - 120f, y + 80f, 120f, circulo);
        circulo.setColor(Color.parseColor("#FFFFFF10"));
        canvas.drawCircle(IMG_W - 80f, y + 180f, 160f, circulo);

        // Emoji moneda como logotipo
        Paint emoji = textPaint(96f, Color.WHITE, false);
        canvas.drawText("💰", MARGIN, y + 130f, emoji);

        // Nombre del grupo
        Paint titulo = textPaint(68f, Color.WHITE, true);
        String nombre = datos.grupo.nombre.length() > 18
                ? datos.grupo.nombre.substring(0, 17) + "…"
                : datos.grupo.nombre;
        canvas.drawText(nombre, MARGIN, y + 210f, titulo);

        // Fecha
        String fecha = new SimpleDateFormat("d MMMM yyyy", LOCALE_ES).format(new Date());
        Paint fechaPaint = textPaint(36f, Color.parseColor("#B3E5FC"), false);
        canvas.drawText("📅 " + fecha, MARGIN, y + 265f, fechaPaint);

        // Línea separadora
        Paint sep = new Paint();
        sep.setColor(Color.parseColor("#FFFFFF40"));
        sep.setStrokeWidth(2f);
        canvas.drawLine(MARGIN, y + h - 8f, IMG_W - MARGIN, y + h - 8f, sep);

        return y + h;
    }

    // ── TARJETA TOTALES ───────────────────────────────────────────────────────

    /**
     * Dibuja la tarjeta con el total gastado, número de miembros y cuota equitativa.
     */
    private static float dibujarTarjetaTotales(Canvas canvas, DatosGrupo datos, float y) {
        float topMargin = 32f;
        float cardH     = 200f;
        float x0        = MARGIN;
        float x1        = IMG_W - MARGIN;

        float top = y + topMargin;
        drawCard(canvas, x0, top, x1, top + cardH);

        // Total gastado
        double total    = calcularTotal(datos.miembros);
        int numMiembros = datos.miembros.size();
        double cuota    = numMiembros > 0 ? total / numMiembros : 0;

        // Tres columnas dentro de la tarjeta
        float colW  = (x1 - x0) / 3f;
        String[][] cols = {
                { FMT_MONEDA.format(total) + " €", "Total gastado" },
                { String.valueOf(numMiembros),       "Participantes" },
                { FMT_MONEDA.format(cuota) + " €",  "Cuota/persona" },
        };

        for (int i = 0; i < cols.length; i++) {
            float cx = x0 + colW * i + colW / 2f;

            // Separador vertical (excepto primero)
            if (i > 0) {
                Paint div = new Paint();
                div.setColor(COLOR_DIVIDER);
                div.setStrokeWidth(2f);
                canvas.drawLine(x0 + colW * i, top + 24f, x0 + colW * i, top + cardH - 24f, div);
            }

            Paint valorPaint = textPaint(42f, COLOR_ACCENT, true);
            valorPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(cols[i][0], cx, top + 96f, valorPaint);

            Paint labelPaint = textPaint(30f, COLOR_TEXT_SEC, false);
            labelPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(cols[i][1], cx, top + 144f, labelPaint);
        }

        return top + cardH + 24f;
    }

    // ── TABLA GASTOS POR MIEMBRO ──────────────────────────────────────────────

    /**
     * Dibuja la tabla de gastos por miembro con barras de progreso proporcionales.
     */
    private static float dibujarTablaGastos(Canvas canvas, DatosGrupo datos, float y) {
        float x0 = MARGIN;
        float x1 = IMG_W - MARGIN;

        // Título de sección
        y = dibujarTituloSeccion(canvas, "👤  Gastos por persona", y);

        double total = calcularTotal(datos.miembros);
        if (total == 0) total = 1; // evitar división por cero

        float cardTop = y;
        float cardBot = y + datos.miembros.size() * ROW_H + PADDING;
        drawCard(canvas, x0, cardTop, x1, cardBot);

        float rowY = cardTop;
        for (int i = 0; i < datos.miembros.size(); i++) {
            EntradaMiembro m  = datos.miembros.get(i);
            int colorMiembro  = MEMBER_COLORS[i % MEMBER_COLORS.length];
            float fila        = rowY + PADDING / 2f;

            // Separador entre filas
            if (i > 0) {
                Paint sep = new Paint();
                sep.setColor(COLOR_DIVIDER);
                sep.setStrokeWidth(1f);
                canvas.drawLine(x0 + PADDING, rowY, x1 - PADDING, rowY, sep);
            }

            // Avatar circular con inicial
            float avatarX = x0 + PADDING + 28f;
            float avatarY = fila + ROW_H / 2f;
            drawAvatar(canvas, avatarX, avatarY, 28f, colorMiembro, inicialDe(m.nombre));

            // Nombre
            Paint nombreP = textPaint(34f, COLOR_TEXT_PRIMARY, false);
            canvas.drawText(abreviar(m.nombre, 14), avatarX + 44f, avatarY + 12f, nombreP);

            // Barra de progreso
            float barX0    = x0 + PADDING + 240f;
            float barX1    = x1 - PADDING - 200f;
            float barY     = avatarY + 4f;
            float barH     = 16f;
            float ratio    = (float) (m.totalPagado / total);
            float barFill  = (barX1 - barX0) * ratio;

            Paint barFondo = new Paint(Paint.ANTI_ALIAS_FLAG);
            barFondo.setColor(COLOR_BAR_BG);
            canvas.drawRoundRect(new RectF(barX0, barY - barH / 2f, barX1, barY + barH / 2f), 8f, 8f, barFondo);

            if (barFill > 0) {
                Paint barRell = new Paint(Paint.ANTI_ALIAS_FLAG);
                barRell.setColor(colorMiembro);
                canvas.drawRoundRect(new RectF(barX0, barY - barH / 2f, barX0 + barFill, barY + barH / 2f), 8f, 8f, barRell);
            }

            // Importe alineado a la derecha
            Paint montoPaint = textPaint(34f, COLOR_TEXT_PRIMARY, true);
            montoPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(FMT_MONEDA.format(m.totalPagado) + " €",
                    x1 - PADDING, avatarY + 12f, montoPaint);

            rowY += ROW_H;
        }

        return cardBot + 24f;
    }

    // ── PIE CHART ─────────────────────────────────────────────────────────────

    /**
     * Dibuja un gráfico circular simple con los gastos de cada miembro.
     * Cada sector tiene el color asignado al miembro y una leyenda lateral.
     */
    private static float dibujarPieChart(Canvas canvas, DatosGrupo datos, float y) {
        if (datos.miembros.isEmpty()) return y;

        float x0    = MARGIN;
        float x1    = IMG_W - MARGIN;
        float cardH = 340f;

        y = dibujarTituloSeccion(canvas, "📊  Distribución del gasto", y);
        drawCard(canvas, x0, y, x1, y + cardH);

        double total = calcularTotal(datos.miembros);
        if (total == 0) return y + cardH + 24f;

        // Pie chart centrado a la izquierda de la tarjeta
        float cx    = x0 + 220f;
        float cy    = y + cardH / 2f;
        float radio = 120f;

        float startAngle = -90f;
        for (int i = 0; i < datos.miembros.size(); i++) {
            EntradaMiembro m = datos.miembros.get(i);
            float sweep      = (float) (m.totalPagado / total * 360f);

            Paint sector = new Paint(Paint.ANTI_ALIAS_FLAG);
            sector.setColor(MEMBER_COLORS[i % MEMBER_COLORS.length]);
            canvas.drawArc(
                    new RectF(cx - radio, cy - radio, cx + radio, cy + radio),
                    startAngle, sweep, true, sector);

            startAngle += sweep;
        }

        // Hueco central (donut)
        Paint hueco = new Paint(Paint.ANTI_ALIAS_FLAG);
        hueco.setColor(COLOR_CARD_BG);
        canvas.drawCircle(cx, cy, radio * 0.52f, hueco);

        // Porcentaje total en el centro
        Paint pctPaint = textPaint(32f, COLOR_TEXT_SEC, false);
        pctPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Total", cx, cy - 10f, pctPaint);
        Paint totalPaint = textPaint(36f, COLOR_ACCENT, true);
        totalPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(FMT_MONEDA.format(total) + "€", cx, cy + 30f, totalPaint);

        // Leyenda a la derecha del gráfico
        float leyX  = cx + radio + 60f;
        float leyY  = y + 72f;
        for (int i = 0; i < datos.miembros.size(); i++) {
            EntradaMiembro m = datos.miembros.get(i);
            int color        = MEMBER_COLORS[i % MEMBER_COLORS.length];
            double pct       = total > 0 ? m.totalPagado / total * 100.0 : 0;

            // Cuadrado de color
            Paint cuadrado = new Paint(Paint.ANTI_ALIAS_FLAG);
            cuadrado.setColor(color);
            canvas.drawRoundRect(new RectF(leyX, leyY, leyX + 24f, leyY + 24f), 6f, 6f, cuadrado);

            // Nombre y porcentaje
            Paint leyNombre = textPaint(30f, COLOR_TEXT_PRIMARY, false);
            canvas.drawText(abreviar(m.nombre, 12), leyX + 36f, leyY + 20f, leyNombre);

            Paint leyPct = textPaint(28f, COLOR_TEXT_SEC, false);
            canvas.drawText(String.format(LOCALE_ES, "%.1f%%", pct), leyX + 36f, leyY + 50f, leyPct);

            leyY += 72f;
        }

        return y + cardH + 24f;
    }

    // ── BALANCE INDIVIDUAL ────────────────────────────────────────────────────

    /**
     * Dibuja el balance neto de cada miembro (cuánto recupera / cuánto debe).
     * Fondo verde para saldo positivo, rojo para negativo.
     */
    private static float dibujarSeccionBalance(Canvas canvas, DatosGrupo datos, float y) {
        float x0 = MARGIN;
        float x1 = IMG_W - MARGIN;

        y = dibujarTituloSeccion(canvas, "⚖️  Balance individual", y);

        double total    = calcularTotal(datos.miembros);
        int n           = datos.miembros.size();
        double cuota    = n > 0 ? total / n : 0;

        float cardTop = y;
        float cardBot = y + datos.miembros.size() * ROW_H + PADDING;
        drawCard(canvas, x0, cardTop, x1, cardBot);

        float rowY = cardTop;
        for (int i = 0; i < datos.miembros.size(); i++) {
            EntradaMiembro m = datos.miembros.get(i);
            double balance   = m.totalPagado - cuota;
            boolean positivo = balance >= -0.009;

            if (i > 0) {
                Paint sep = new Paint();
                sep.setColor(COLOR_DIVIDER);
                sep.setStrokeWidth(1f);
                canvas.drawLine(x0 + PADDING, rowY, x1 - PADDING, rowY, sep);
            }

            float avatarX = x0 + PADDING + 28f;
            float avatarY = rowY + PADDING / 2f + ROW_H / 2f;

            // Avatar con color según balance
            int color = positivo ? COLOR_GREEN : COLOR_RED;
            drawAvatar(canvas, avatarX, avatarY, 28f, color, inicialDe(m.nombre));

            // Nombre
            Paint nombreP = textPaint(34f, COLOR_TEXT_PRIMARY, false);
            canvas.drawText(abreviar(m.nombre, 14), avatarX + 44f, avatarY + 12f, nombreP);

            // Píldora de estado
            String etiqueta = positivo ? "COBRA" : "DEBE";
            int bgColor     = positivo ? COLOR_GREEN_BG : COLOR_RED_BG;
            int txtColor    = positivo ? COLOR_GREEN    : COLOR_RED;

            float pilX = x1 - PADDING - 260f;
            float pilY = avatarY - 22f;
            drawPildora(canvas, pilX, pilY, 140f, 44f, bgColor, etiqueta, txtColor);

            // Importe
            Paint montoPaint = textPaint(36f, txtColor, true);
            montoPaint.setTextAlign(Paint.Align.RIGHT);
            String signo = positivo ? "+" : "-";
            canvas.drawText(signo + FMT_MONEDA.format(Math.abs(balance)) + " €",
                    x1 - PADDING, avatarY + 12f, montoPaint);

            rowY += ROW_H;
        }

        return cardBot + 24f;
    }

    // ── LIQUIDACIONES ─────────────────────────────────────────────────────────

    /**
     * Dibuja la lista de liquidaciones pendientes de balance_grupo.
     * Cada fila muestra: deudor → acreedor : importe.
     */
    private static float dibujarLiquidaciones(Canvas canvas, DatosGrupo datos, float y) {
        float x0 = MARGIN;
        float x1 = IMG_W - MARGIN;

        y = dibujarTituloSeccion(canvas, "📌  Liquidaciones sugeridas", y);

        List<BalanceGrupo> pendientes = new ArrayList<>();
        for (BalanceGrupo b : datos.balances) {
            if (b.liquidado == 0 && b.montoPendiente > 0.009) pendientes.add(b);
        }

        float cardTop = y;
        int   numFil  = Math.max(1, pendientes.size());
        float cardBot = y + numFil * ROW_H + PADDING;
        drawCard(canvas, x0, cardTop, x1, cardBot);

        if (pendientes.isEmpty()) {
            Paint okPaint = textPaint(38f, COLOR_GREEN, false);
            okPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("🎉 ¡Sin deudas pendientes!", IMG_W / 2f, cardTop + ROW_H / 2f + 16f, okPaint);
        } else {
            float rowY = cardTop;
            for (int i = 0; i < pendientes.size(); i++) {
                BalanceGrupo b    = pendientes.get(i);
                String deudor     = datos.mapaIdNombre.getOrDefault(b.usuarioDeudorId,   "Miembro " + b.usuarioDeudorId);
                String acreedor   = datos.mapaIdNombre.getOrDefault(b.usuarioAcreedorId, "Miembro " + b.usuarioAcreedorId);

                if (i > 0) {
                    Paint sep = new Paint();
                    sep.setColor(COLOR_DIVIDER);
                    sep.setStrokeWidth(1f);
                    canvas.drawLine(x0 + PADDING, rowY, x1 - PADDING, rowY, sep);
                }

                float cy = rowY + ROW_H / 2f + 12f;

                // Número de orden
                Paint numP = textPaint(30f, COLOR_TEXT_SEC, false);
                canvas.drawText(String.valueOf(i + 1) + ".", x0 + PADDING + 8f, cy, numP);

                // Deudor → acreedor
                float tx = x0 + PADDING + 60f;
                Paint deudorP = textPaint(34f, COLOR_RED, true);
                canvas.drawText(abreviar(deudor, 12), tx, cy, deudorP);

                Paint arrowP = textPaint(34f, COLOR_TEXT_SEC, false);
                canvas.drawText("  →  ", tx + 220f, cy, arrowP);

                Paint acreedorP = textPaint(34f, COLOR_GREEN, true);
                canvas.drawText(abreviar(acreedor, 12), tx + 360f, cy, acreedorP);

                // Importe
                Paint montoP = textPaint(36f, COLOR_ACCENT, true);
                montoP.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(FMT_MONEDA.format(b.montoPendiente) + " €",
                        x1 - PADDING, cy, montoP);

                rowY += ROW_H;
            }
        }

        return cardBot + 24f;
    }

    // ── PIE DE PÁGINA ─────────────────────────────────────────────────────────

    /**
     * Dibuja el pie con el branding de MoneyMaster.
     */
    private static void dibujarPie(Canvas canvas, float y, int alturaTotal) {
        float cy = y + 60f;

        // Línea separadora
        Paint sep = new Paint();
        sep.setColor(COLOR_DIVIDER);
        sep.setStrokeWidth(2f);
        canvas.drawLine(MARGIN, cy - 20f, IMG_W - MARGIN, cy - 20f, sep);

        Paint emoji = textPaint(52f, COLOR_TEXT_PRIMARY, false);
        emoji.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("💸", IMG_W / 2f - 120f, cy + 36f, emoji);

        Paint nombre = textPaint(40f, COLOR_ACCENT, true);
        nombre.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("MoneyMaster", IMG_W / 2f + 20f, cy + 36f, nombre);

        Paint sub = textPaint(28f, COLOR_TEXT_SEC, false);
        sub.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Tu gestor de gastos privado", IMG_W / 2f, cy + 76f, sub);
    }

    // =========================================================================
    // Primitivas de dibujo
    // =========================================================================

    /** Dibuja una tarjeta blanca con sombra simulada. */
    private static void drawCard(Canvas canvas, float x0, float y0, float x1, float y1) {
        // Sombra (offset simple)
        Paint sombra = new Paint(Paint.ANTI_ALIAS_FLAG);
        sombra.setColor(Color.parseColor("#19000000"));
        canvas.drawRoundRect(new RectF(x0 + 4f, y0 + 6f, x1 + 4f, y1 + 6f), CARD_RADIUS, CARD_RADIUS, sombra);

        // Tarjeta blanca
        Paint card = new Paint(Paint.ANTI_ALIAS_FLAG);
        card.setColor(COLOR_CARD_BG);
        canvas.drawRoundRect(new RectF(x0, y0, x1, y1), CARD_RADIUS, CARD_RADIUS, card);
    }

    /** Dibuja un avatar circular con la inicial del nombre. */
    private static void drawAvatar(Canvas canvas, float cx, float cy, float radio,
                                   int color, String inicial) {
        Paint fondo = new Paint(Paint.ANTI_ALIAS_FLAG);
        fondo.setColor(ajustarAlpha(color, 40));
        canvas.drawCircle(cx, cy, radio, fondo);

        Paint borde = new Paint(Paint.ANTI_ALIAS_FLAG);
        borde.setColor(color);
        borde.setStyle(Paint.Style.STROKE);
        borde.setStrokeWidth(3f);
        canvas.drawCircle(cx, cy, radio, borde);

        Paint txt = textPaint(radio * 0.9f, color, true);
        txt.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(inicial, cx, cy + radio * 0.35f, txt);
    }

    /** Dibuja una píldora de estado (badge redondeado). */
    private static void drawPildora(Canvas canvas, float x, float y, float w, float h,
                                    int bgColor, String texto, int txtColor) {
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(bgColor);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), h / 2f, h / 2f, bg);

        Paint txt = textPaint(26f, txtColor, true);
        txt.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(texto, x + w / 2f, y + h * 0.72f, txt);
    }

    /** Dibuja el título de sección con línea de acento. */
    private static float dibujarTituloSeccion(Canvas canvas, String titulo, float y) {
        float topMargin = 32f;
        Paint p = textPaint(38f, COLOR_TEXT_PRIMARY, true);
        canvas.drawText(titulo, MARGIN, y + topMargin + 36f, p);

        // Línea de acento bajo el título
        Paint linea = new Paint();
        linea.setColor(COLOR_ACCENT);
        linea.setStrokeWidth(4f);
        canvas.drawLine(MARGIN, y + topMargin + 50f, MARGIN + 120f, y + topMargin + 50f, linea);

        return y + topMargin + 72f;
    }

    // =========================================================================
    // Persistencia
    // =========================================================================

    /**
     * Guarda el Bitmap como PNG en el directorio de caché de la app.
     * El nombre del archivo lleva el nombre del grupo y timestamp.
     *
     * @return File creado, o null si falló la escritura.
     */
    private static File guardarEnCache(Context context, Bitmap bitmap, String nombreGrupo) {
        // Usar directorio cache/resumen_grupo/ (declarado en file_provider_paths.xml)
        File dir = new File(context.getCacheDir(), "resumen_grupo");
        if (!dir.exists() && !dir.mkdirs()) return null;

        // Nombre único: resumen_{nombreGrupo}_{timestamp}.png
        String nombre = "resumen_" + sanitizarNombre(nombreGrupo) + "_"
                + System.currentTimeMillis() + ".png";
        File archivo = new File(dir, nombre);

        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos);
            fos.flush();
            return archivo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            bitmap.recycle();
        }
    }

    // =========================================================================
    // Utilidades internas
    // =========================================================================

    /** Crea un Paint para texto con las opciones dadas. */
    private static Paint textPaint(float sp, int color, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(sp);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        p.setAntiAlias(true);
        return p;
    }

    /** Suma el totalPagado de todos los miembros. */
    private static double calcularTotal(List<EntradaMiembro> miembros) {
        double t = 0;
        for (EntradaMiembro m : miembros) t += m.totalPagado;
        return t;
    }

    /** Cuenta las liquidaciones con montoPendiente > 0 y liquidado == 0. */
    private static int contarLiquidacionesPendientes(DatosGrupo datos) {
        int n = 0;
        for (BalanceGrupo b : datos.balances) {
            if (b.liquidado == 0 && b.montoPendiente > 0.009) n++;
        }
        return n;
    }

    /** Obtiene la inicial en mayúsculas de un nombre. */
    private static String inicialDe(String nombre) {
        if (nombre == null || nombre.isEmpty()) return "?";
        return String.valueOf(nombre.charAt(0)).toUpperCase();
    }

    /** Recorta el nombre con "…" si supera la longitud dada. */
    private static String abreviar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() > max ? texto.substring(0, max - 1) + "…" : texto;
    }

    /** Elimina caracteres no válidos para nombres de archivo. */
    private static String sanitizarNombre(String nombre) {
        if (nombre == null) return "grupo";
        return nombre.replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase();
    }

    /** Aplica un valor alfa (0–255) a un color RGB. */
    private static int ajustarAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
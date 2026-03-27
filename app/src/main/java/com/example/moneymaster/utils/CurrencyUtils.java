package com.example.moneymaster.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utilidades globales para el manejo de moneda en MoneyMaster.
 *
 * Uso:
 *   String simbolo = CurrencyUtils.getCurrencySymbol(context);
 *   String formateado = CurrencyUtils.formatAmount(context, 1234.56);
 *
 * La moneda se persiste en SharedPreferences "moneymaster_config"
 * con las claves KEY_MONEDA y KEY_MONEDA_NOMBRE.
 */
public class CurrencyUtils {

    // ── SharedPreferences ─────────────────────────────────────────────────────
    public static final String PREFS_CONFIG      = "moneymaster_config";
    public static final String KEY_MONEDA        = "moneda_simbolo";
    public static final String KEY_MONEDA_NOMBRE = "moneda_nombre";

    // ── Catálogo de monedas disponibles ───────────────────────────────────────
    public static final String[] NOMBRES  = {
            "Euro", "Dólar USD", "Libra esterlina",
            "Yen japonés", "Franco suizo", "Peso mexicano",
            "Dólar canadiense", "Dólar australiano"
    };

    public static final String[] SIMBOLOS = {
            "€", "$", "£",
            "¥", "₣", "$",
            "CA$", "A$"
    };

    // ── Valores por defecto ───────────────────────────────────────────────────
    private static final String DEFAULT_SIMBOLO = "€";
    private static final String DEFAULT_NOMBRE  = "Euro";

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Devuelve el símbolo de moneda actualmente seleccionado.
     * Método global que debe usarse en toda la app para mostrar montos.
     *
     * @param context Contexto de la aplicación o Activity/Fragment.
     * @return Símbolo de moneda, por defecto "€".
     */
    public static String getCurrencySymbol(Context context) {
        return getPrefs(context).getString(KEY_MONEDA, DEFAULT_SIMBOLO);
    }

    /**
     * Devuelve el nombre completo de la moneda seleccionada.
     *
     * @param context Contexto.
     * @return Nombre de la moneda, por defecto "Euro".
     */
    public static String getCurrencyName(Context context) {
        return getPrefs(context).getString(KEY_MONEDA_NOMBRE, DEFAULT_NOMBRE);
    }

    /**
     * Formatea un importe con el símbolo de moneda activo.
     * Ejemplo: formatAmount(ctx, 1234.5) → "1.234,50 €"
     *
     * @param context Contexto.
     * @param amount  Importe a formatear.
     * @return Cadena formateada lista para mostrar en UI.
     */
    public static String formatAmount(Context context, double amount) {
        String simbolo = getCurrencySymbol(context);
        return String.format("%,.2f %s", amount, simbolo);
    }

    /**
     * Versión de formatAmount con signo explícito para balances.
     * Positivo → "+1.234,50 €"  |  Negativo → "-1.234,50 €"
     *
     * @param context Contexto.
     * @param amount  Importe (puede ser negativo).
     * @return Cadena formateada con signo.
     */
    public static String formatAmountSigned(Context context, double amount) {
        String simbolo = getCurrencySymbol(context);
        String signo   = amount >= 0 ? "+" : "";
        return String.format("%s%,.2f %s", signo, amount, simbolo);
    }

    /**
     * Persiste la moneda seleccionada en SharedPreferences.
     *
     * @param context     Contexto.
     * @param simbolo     Símbolo de moneda, ej: "€".
     * @param nombreMoneda Nombre completo, ej: "Euro".
     */
    public static void saveCurrency(Context context, String simbolo, String nombreMoneda) {
        getPrefs(context).edit()
                .putString(KEY_MONEDA, simbolo)
                .putString(KEY_MONEDA_NOMBRE, nombreMoneda)
                .apply();
    }

    /**
     * Devuelve el índice de la moneda actualmente seleccionada en los arrays
     * NOMBRES / SIMBOLOS, útil para preseleccionar el item en un diálogo.
     *
     * @param context Contexto.
     * @return Índice en el array, 0 (Euro) si no se encuentra.
     */
    public static int getCurrentIndex(Context context) {
        String actual = getCurrencyName(context);
        for (int i = 0; i < NOMBRES.length; i++) {
            if (NOMBRES[i].equals(actual)) return i;
        }
        return 0;
    }

    // ── Privado ───────────────────────────────────────────────────────────────
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_CONFIG, Context.MODE_PRIVATE);
    }

    // Clase de utilidades: no instanciar
    private CurrencyUtils() {}
}
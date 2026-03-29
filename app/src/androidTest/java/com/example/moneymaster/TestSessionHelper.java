package com.example.moneymaster;

import android.content.Context;

/**
 * TestSessionHelper — Card #61
 *
 * Inyecta una sesión de usuario falsa en SharedPreferences antes de
 * lanzar cualquier test de UI. Esto evita que la app redirija al
 * Splash/Login durante los tests.
 *
 * Usar en @Before de cada test class:
 *   TestSessionHelper.injectFakeSession(InstrumentationRegistry.getInstrumentation().getTargetContext());
 */
public class TestSessionHelper {

    public static final int FAKE_USER_ID = 1;
    public static final String FAKE_USER_NAME = "Test User";

    /**
     * Prefs de sesión — mismo nombre que usa SessionManager
     */
    private static final String PREFS_SESSION = "moneymaster_session";
    private static final String KEY_USER_ID = "usuario_id";
    private static final String KEY_USER_NAME = "nombre";
    private static final String KEY_LOGGED_IN = "logged_in";

    /**
     * Prefs legacy usadas en AddExpenseActivity
     */
    private static final String PREFS_LEGACY = "MoneyMasterPrefs";
    private static final String KEY_LEGACY_ID = "userId";

    /**
     * Escribe una sesión de usuario válida en ambos SharedPreferences
     * que usa la app para comprobar si hay sesión activa.
     */
    public static void injectFakeSession(Context context) {
        // Prefs de sesión principal
        context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_USER_ID, FAKE_USER_ID)
                .putString(KEY_USER_NAME, FAKE_USER_NAME)
                .putBoolean(KEY_LOGGED_IN, true)
                .apply();

        // Prefs legacy (AddExpenseActivity usa "MoneyMasterPrefs" / "userId")
        context.getSharedPreferences(PREFS_LEGACY, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_LEGACY_ID, FAKE_USER_ID)
                .apply();
    }

    /**
     * Limpia la sesión al terminar los tests.
     */
    public static void clearSession(Context context) {
        context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
                .edit().clear().apply();
        context.getSharedPreferences(PREFS_LEGACY, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}

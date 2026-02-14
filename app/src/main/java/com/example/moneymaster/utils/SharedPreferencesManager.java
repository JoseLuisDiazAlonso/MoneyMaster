package com.example.moneymaster.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**Lo que hace este archivo es gestionar las preferencias de usuario y controlar
 * si es la primera vez aque el usuario actual inicia sesión.
 * **/

public class SharedPreferencesManager {

    private static final String PREF_NAME = "MoneyMasterPrefs";
    private static final String KEY_IS_FIRST_TIME = "isFirstTime";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    /**
     * Constructor
     * @parm context Contexto de la Aplicación
     * *
     * */

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**Verificamos si es la primera vez que el usuario abre la app
     * return true si es la primera vez y false si ya se abrió anteriormente
     * */

    public boolean isFirstTime() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true);
    }
    /**Marcamos si el usuario ya completó el onboarding*
     * */

    public void setFirstTimeDone() {
        editor.putBoolean(KEY_IS_FIRST_TIME, false);
        editor.apply();
    }
    /**Verificamos si hay una sesión activa
     * @return true si el usuario está logeado
     * **/
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    /**Guradamos la sesión del usuario al hacer login**/

    public void saveUserSession (long userId, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
    /**Cerramos la sesión de usuario**/
    public void logout() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }
    /**Obtenemos el ID del usuario actual
     * retur ID del usuario o -1 si no hay sesión*
     * */
    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }
    /**Obtenemos el ID del usuario actual
     * return Email del usuario o null si no hay sesión*
     * */
    public String getKeyUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }
    /**Limpiamos todas las preferencias**/
    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}

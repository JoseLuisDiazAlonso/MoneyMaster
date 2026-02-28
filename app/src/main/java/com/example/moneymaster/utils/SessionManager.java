package com.example.moneymaster.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**Esta clase gestiona la sesión activa con SharedPreferences. Centraliza todas las claves
 * en un mismo lugar
 * */

public class SessionManager {

    private static final String PREFS_NAME = "MoneyMasterSession";
    private static final String KEY_LOGGED = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "userName";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    //Guardamos sesión

    public void saveSession (int userId, String email, String fullName) {
        editor.putBoolean(KEY_LOGGED, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NAME, fullName);
        editor.apply();
    }

    //Cerramos sesión

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    //Getters

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED, false);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public String getFullName() {
        return preferences.getString(KEY_NAME, null);
    }
}

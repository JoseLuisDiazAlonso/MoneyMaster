package com.example.moneymaster.ui.auth;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.User;
import com.example.moneymaster.utils.EmailSender;
import com.example.moneymaster.utils.SecurityUtils;

import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecuperarContrasenaViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "recovery_prefs";
    private static final String KEY_CODE   = "recovery_code";
    private static final String KEY_EMAIL  = "recovery_email";
    private static final String KEY_EXPIRY = "recovery_expiry";
    private static final long TWO_HOURS_MS = 2 * 60 * 60 * 1000L;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AppDatabase db;
    private final SharedPreferences prefs;

    // Estados para la UI
    private final MutableLiveData<Boolean> emailEnviado        = new MutableLiveData<>();
    private final MutableLiveData<Boolean> codigoVerificado    = new MutableLiveData<>();
    private final MutableLiveData<Boolean> contrasenaActualizada = new MutableLiveData<>();
    private final MutableLiveData<String>  errorMessage        = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading           = new MutableLiveData<>(false);

    public RecuperarContrasenaViewModel(@NonNull Application application) {
        super(application);
        db    = AppDatabase.getInstance(application);
        prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }


    // PASO 1: Verificar email y enviar código

    public void solicitarCodigo(String email) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Por favor ingresa un email válido");
            return;
        }

        isLoading.setValue(true);

        executor.execute(() -> {
            // 1. Verificar que el email existe en la DB
            User user = db.userDao().getByEmail(email.trim().toLowerCase());

            if (user == null) {
                errorMessage.postValue("No existe una cuenta con ese email");
                isLoading.postValue(false);
                return;
            }

            // 2. Generar código de 6 dígitos con SecureRandom
            String codigo = generarCodigo6Digitos();
            long expiracion = System.currentTimeMillis() + TWO_HOURS_MS;

            // 3. Guardar código y expiración en SharedPreferences
            prefs.edit()
                    .putString(KEY_CODE, codigo)
                    .putString(KEY_EMAIL, email.trim().toLowerCase())
                    .putLong(KEY_EXPIRY, expiracion)
                    .apply();

            // 4. Enviar email con el código
            boolean enviado = EmailSender.sendRecoveryCode(email, codigo);

            if (enviado) {
                emailEnviado.postValue(true);
            } else {
                errorMessage.postValue("Error al enviar el email. Verifica tu conexión.");
                prefs.edit().clear().apply();
            }

            isLoading.postValue(false);
        });
    }


    // PASO 2: Verificar código ingresado por el usuario

    public void verificarCodigo(String codigoIngresado) {
        String codigoGuardado = prefs.getString(KEY_CODE, null);
        long expiracion       = prefs.getLong(KEY_EXPIRY, 0);

        if (codigoGuardado == null) {
            errorMessage.setValue("No hay un código activo. Solicita uno nuevo.");
            return;
        }

        if (System.currentTimeMillis() > expiracion) {
            errorMessage.setValue("El código ha expirado. Solicita uno nuevo.");
            prefs.edit().clear().apply();
            return;
        }

        if (codigoIngresado.trim().equals(codigoGuardado)) {
            codigoVerificado.setValue(true);
        } else {
            errorMessage.setValue("Código incorrecto. Inténtalo de nuevo.");
        }
    }


    // PASO 3: Hashear y guardar la nueva contraseña en la DB

    public void actualizarContrasena(String nuevaContrasena, String confirmarContrasena) {
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            errorMessage.setValue("Las contraseñas no coinciden");
            return;
        }

        if (nuevaContrasena.length() < 6) {
            errorMessage.setValue("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        String email = prefs.getString(KEY_EMAIL, null);
        if (email == null) {
            errorMessage.setValue("Sesión de recuperación expirada. Vuelve a empezar.");
            return;
        }

        isLoading.setValue(true);

        executor.execute(() -> {
            // SecurityUtils.hashPassword() genera el salt internamente
            // y devuelve el formato "salt:sha256hash" listo para guardar
            String nuevoHash = SecurityUtils.hashPassword(nuevaContrasena);

            // Actualizar en la DB buscando por email
            db.userDao().updatePasswordByEmail(email, nuevoHash);

            // Limpiar todos los datos de recuperación de SharedPreferences
            prefs.edit().clear().apply();

            contrasenaActualizada.postValue(true);
            isLoading.postValue(false);
        });
    }


    // Helpers

    private String generarCodigo6Digitos() {
        SecureRandom random = new SecureRandom();
        int codigo = 100000 + random.nextInt(900000); // Rango: 100000–999999
        return String.valueOf(codigo);
    }

    public long getTiempoExpiracion() {
        return prefs.getLong(KEY_EXPIRY, 0);
    }


    // Getters LiveData

    public LiveData<Boolean> getEmailEnviado()          { return emailEnviado; }
    public LiveData<Boolean> getCodigoVerificado()      { return codigoVerificado; }
    public LiveData<Boolean> getContrasenaActualizada() { return contrasenaActualizada; }
    public LiveData<String>  getErrorMessage()          { return errorMessage; }
    public LiveData<Boolean> getIsLoading()             { return isLoading; }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
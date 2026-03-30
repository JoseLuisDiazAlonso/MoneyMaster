package com.example.moneymaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymaster.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.User;
import com.example.moneymaster.utils.SecurityUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ForgotPasswordActivity
 *
 * Flujo en 2 pasos:
 *   Paso 1 — El usuario introduce su email. Si existe en BD se avanza al paso 2.
 *   Paso 2 — Se muestra la pregunta de seguridad asociada a ese email.
 *            Si la respuesta es correcta, el usuario puede establecer una nueva contraseña.
 *
 * La actividad gestiona ambos pasos con un único layout usando visibilidad de vistas.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    // ── Preguntas de seguridad disponibles ────────────────
    public static final String[] SECURITY_QUESTIONS = {
            "¿Cuál es el nombre de tu primera mascota?",
            "¿En qué ciudad naciste?",
            "¿Cuál es el apellido de tu madre?"
    };

    // ── Paso 1 — Email ────────────────────────────────────
    private TextInputLayout  tilFpEmail;
    private TextInputEditText etFpEmail;
    private MaterialButton   btnFpNext;

    // ── Paso 2 — Pregunta + nueva contraseña ──────────────
    private View              step2Container;
    private TextView          tvSecurityQuestion;
    private TextInputLayout  tilFpAnswer;
    private TextInputEditText etFpAnswer;
    private TextInputLayout  tilFpNewPassword;
    private TextInputEditText etFpNewPassword;
    private TextInputLayout  tilFpConfirmPassword;
    private TextInputEditText etFpConfirmPassword;
    private MaterialButton   btnFpSave;

    // ── Estado ────────────────────────────────────────────
    private User            foundUser;
    private AppDatabase     db;
    private ExecutorService executor;

    // ─────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db       = AppDatabase.getDatabase(this);
        executor = Executors.newSingleThreadExecutor();

        bindViews();
        setupListeners();
    }

    // ── 1. Enlazar vistas ─────────────────────────────────
    private void bindViews() {
        tilFpEmail           = findViewById(R.id.tilFpEmail);
        etFpEmail            = findViewById(R.id.etFpEmail);
        btnFpNext            = findViewById(R.id.btnFpNext);

        step2Container       = findViewById(R.id.step2Container);
        tvSecurityQuestion   = findViewById(R.id.tvSecurityQuestion);
        tilFpAnswer          = findViewById(R.id.tilFpAnswer);
        etFpAnswer           = findViewById(R.id.etFpAnswer);
        tilFpNewPassword     = findViewById(R.id.tilFpNewPassword);
        etFpNewPassword      = findViewById(R.id.etFpNewPassword);
        tilFpConfirmPassword = findViewById(R.id.tilFpConfirmPassword);
        etFpConfirmPassword  = findViewById(R.id.etFpConfirmPassword);
        btnFpSave            = findViewById(R.id.btnFpSave);

        step2Container.setVisibility(View.GONE);
    }

    // ── 2. Listeners ──────────────────────────────────────
    private void setupListeners() {

        // Botón "Continuar" — Paso 1
        btnFpNext.setOnClickListener(v -> handleEmailStep());

        // Botón "Guardar contraseña" — Paso 2
        btnFpSave.setOnClickListener(v -> handleSavePassword());

        // Link "Volver al inicio de sesión"
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    // ── 3. Paso 1: verificar email ────────────────────────
    private void handleEmailStep() {
        String email = getTextFrom(etFpEmail);

        tilFpEmail.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilFpEmail.setError("Introduce tu email");
            return;
        }

        executor.execute(() -> {
            User user = db.usuarioDao().getByEmail(email);

            runOnUiThread(() -> {
                if (user == null) {
                    tilFpEmail.setError("No existe ninguna cuenta con ese email");
                    return;
                }
                foundUser = user;
                showStep2();
            });
        });
    }

    // ── 4. Mostrar paso 2 ─────────────────────────────────
    private void showStep2() {
        // Bloquear campo email
        etFpEmail.setEnabled(false);
        btnFpNext.setEnabled(false);

        // Mostrar la pregunta de seguridad asociada al usuario
        int qIndex = foundUser.securityQuestion;
        if (qIndex < 0 || qIndex >= SECURITY_QUESTIONS.length) qIndex = 0;
        tvSecurityQuestion.setText(SECURITY_QUESTIONS[qIndex]);

        step2Container.setVisibility(View.VISIBLE);
    }

    // ── 5. Paso 2: validar respuesta y guardar contraseña ─
    private void handleSavePassword() {
        String answer          = getTextFrom(etFpAnswer);
        String newPassword     = getTextFrom(etFpNewPassword);
        String confirmPassword = getTextFrom(etFpConfirmPassword);

        tilFpAnswer.setError(null);
        tilFpNewPassword.setError(null);
        tilFpConfirmPassword.setError(null);

        boolean valid = true;

        if (TextUtils.isEmpty(answer)) {
            tilFpAnswer.setError("Introduce la respuesta");
            valid = false;
        }
        if (TextUtils.isEmpty(newPassword)) {
            tilFpNewPassword.setError("Introduce la nueva contraseña");
            valid = false;
        } else if (newPassword.length() < 6) {
            tilFpNewPassword.setError("Mínimo 6 caracteres");
            valid = false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            tilFpConfirmPassword.setError("Confirma la contraseña");
            valid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            tilFpConfirmPassword.setError("Las contraseñas no coinciden");
            valid = false;
        }

        if (!valid) return;

        // Verificar respuesta de seguridad (insensible a mayúsculas y espacios)
        String normalizedAnswer = answer.trim().toLowerCase();
        boolean answerOk = SecurityUtils.verifyPassword(normalizedAnswer, foundUser.securityAnswerHash);

        if (!answerOk) {
            tilFpAnswer.setError("Respuesta incorrecta");
            return;
        }

        // Actualizar contraseña en BD usando el método especializado del DAO
        String newHash = SecurityUtils.hashPassword(newPassword);

        executor.execute(() -> {
            db.usuarioDao().updatePasswordByEmail(foundUser.email, newHash);

            runOnUiThread(() -> {
                navigateToLogin();
            });
        });
    }

    // ── 6. Navegar a Login con mensaje ────────────────────
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("password_reset_success", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ── Utilidad ──────────────────────────────────────────
    private String getTextFrom(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
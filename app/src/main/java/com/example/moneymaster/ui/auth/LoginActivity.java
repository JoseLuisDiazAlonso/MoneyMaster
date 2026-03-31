package com.example.moneymaster.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymaster.ActivityRegister;
import com.example.moneymaster.ForgotPasswordActivity;
import com.example.moneymaster.MainActivity;
import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.User;
import com.example.moneymaster.utils.SecurityUtils;
import com.example.moneymaster.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    // UI Views
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private MaterialButton btnLogin;
    private TextView tvLoginError, tvGoToRegister, tvForgotPassword;

    // Infraestructura
    private AppDatabase db;
    private SharedPreferences prefs;
    private ExecutorService executor;

    // Constantes SharedPreferences (solo para "Recuérdame")
    private static final String PREFS_NAME      = "MoneyMasterPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db       = AppDatabase.getDatabase(this);
        prefs    = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        executor = Executors.newSingleThreadExecutor();

        if (checkExistingSession()) return;

        setContentView(R.layout.activity_login);
        initViews();
        setupListeners();

        if (getIntent().getBooleanExtra("password_reset_success", false)) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Contraseña actualizada. Ya puedes iniciar sesión.",
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }

    // ── Listeners ─────────────────────────────────────────

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityRegister.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                hideGeneralError();
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                if (count > 0) hideGeneralError();
            }
        });
    }

    // ── Lógica de login ───────────────────────────────────

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        boolean remember = cbRememberMe.isChecked();

        tilEmail.setError(null);
        tilPassword.setError(null);
        hideGeneralError();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El email es obligatorio");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es obligatoria");
            etPassword.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Formato de email no válido");
            etEmail.requestFocus();
            return;
        }

        setLoadingState(true);

        executor.execute(() -> {
            User user = db.usuarioDao().getByEmail(email);

            runOnUiThread(() -> {
                if (user == null) {
                    handleLoginError();
                    return;
                }

                boolean passwordOk = SecurityUtils.verifyPassword(
                        password,
                        user.passwordHash
                );

                if (passwordOk) {
                    handleLoginSuccess(user, remember);
                } else {
                    handleLoginError();
                }
            });
        });
    }

    // ── Éxito ─────────────────────────────────────────────

    private void handleLoginSuccess(User user, boolean remember) {
        // FIX: SessionManager centraliza la sesión en "MoneyMasterSession"
        // para que todos los fragments y ViewModels lean del mismo fichero
        new SessionManager(this).saveSession(user.id, user.email, user.fullName);

        // "Recuérdame" se sigue guardando en MoneyMasterPrefs — solo lo usa LoginActivity
        prefs.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();

        setLoadingState(false);
        navigateToMainActivity();
    }

    // ── Error ─────────────────────────────────────────────

    private void handleLoginError() {
        setLoadingState(false);
        etPassword.setText("");
        showGeneralError("Correo o contraseña incorrectos");
        etPassword.requestFocus();
    }

    // ── Helpers UI ────────────────────────────────────────

    private void showGeneralError(String message) {
        tvLoginError.setText(message);
        tvLoginError.setVisibility(View.VISIBLE);
    }

    private void hideGeneralError() {
        tvLoginError.setVisibility(View.GONE);
    }

    private void setLoadingState(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Verificando..." : "Iniciar Sesión");
    }

    // ── Vistas ────────────────────────────────────────────

    private void initViews() {
        tilEmail         = findViewById(R.id.tilEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        cbRememberMe     = findViewById(R.id.cbRememberMe);
        btnLogin         = findViewById(R.id.btnLogin);
        tvLoginError     = findViewById(R.id.tvLoginError);
        tvGoToRegister   = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    // ── Sesión ────────────────────────────────────────────

    private boolean checkExistingSession() {
        boolean rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false);
        SessionManager session = new SessionManager(this);

        // FIX: leer isLoggedIn desde SessionManager en lugar de MoneyMasterPrefs
        if (session.isLoggedIn() && rememberMe) {
            navigateToMainActivity();
            return true;
        }
        return false;
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
package com.example.moneymaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.User;
import com.example.moneymaster.databinding.ActivityRegisterBinding;
import com.example.moneymaster.utils.SecurityUtils;
import com.example.moneymaster.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase para validar el nombre, email, contraseña y validación de contraseña.
 * Card #9 — Registro de usuario.
 */
public class ActivityRegister extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AppDatabase    db;
    private SessionManager session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Corrección Card #62: getDatabase() en lugar de getInstance()
        db      = AppDatabase.getDatabase(this);
        session = new SessionManager(this);

        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void setupListeners() {
        binding.btnRegistrar.setOnClickListener(v -> attemptRegister());

        binding.etNombre.addTextChangedListener(clearErrorTextWatcher(binding.tilNombre));
        binding.etEmail.addTextChangedListener(clearErrorTextWatcher(binding.tilEmail));
        binding.etPassword.addTextChangedListener(clearErrorTextWatcher(binding.tilPassword));
        binding.etPasswordConfirm.addTextChangedListener(
                clearErrorTextWatcher(binding.tisPasswordConfirm));
    }

    private void attemptRegister() {
        String nombre          = binding.etNombre.getText().toString().trim();
        String email           = binding.etEmail.getText().toString().trim();
        String password        = binding.etPassword.getText().toString();
        String passwordConfirm = binding.etPasswordConfirm.getText().toString();

        boolean valid = true;

        if (nombre.isEmpty()) {
            binding.tilNombre.setError("El nombre es obligatorio");
            valid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("El email es obligatorio");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("El email no es válido");
            valid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("La contraseña es obligatoria");
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            valid = false;
        }

        if (!password.equals(passwordConfirm)) {
            binding.tisPasswordConfirm.setError("Las contraseñas no coinciden");
            valid = false;
        }

        if (valid) {
            processRegistration(nombre, email, password);
        }
    }

    private void processRegistration(String nombre, String email, String password) {
        executor.execute(() -> {
            try {
                String passwordHash = SecurityUtils.hashPassword(password);

                long userId = db.usuarioDao().insertUser(
                        new User(nombre, email, passwordHash)
                );

                // Insertar categorías del sistema si no existen
                sembrarCategoriasSiNecesario();

                runOnUiThread(() -> {
                    session.saveSession((int) userId, email, nombre);
                    Intent intent = new Intent(ActivityRegister.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        binding.tilEmail.setError("Este email ya está registrado")
                );
            }
        });
    }

    private void sembrarCategoriasSiNecesario() {
        if (db.categoriaGastoDao().countCategoriasDelSistema() > 0) return;

        java.util.List<com.example.moneymaster.data.model.CategoriaGasto> gastos =
                java.util.Arrays.asList(
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Alimentación",  "ic_restaurant",     "#FF5722"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Transporte",    "ic_directions_car", "#2196F3"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Vivienda",      "ic_home",           "#4CAF50"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Salud",         "ic_local_hospital", "#F44336"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Ocio",          "ic_sports_esports", "#9C27B0"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Ropa",          "ic_checkroom",      "#E91E63"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Educación",     "ic_school",         "#3F51B5"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Tecnología",    "ic_devices",        "#00BCD4"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Viajes",        "ic_flight",         "#FF9800"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "Otros",         "ic_category",       "#607D8B")
                );
        db.categoriaGastoDao().insertarVarias(gastos);

        java.util.List<com.example.moneymaster.data.model.CategoriaIngreso> ingresos =
                java.util.Arrays.asList(
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "Salario",       "ic_work",           "#4CAF50"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "Freelance",     "ic_laptop",         "#2196F3"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "Inversiones",   "ic_trending_up",    "#FF9800"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "Alquiler",      "ic_home",           "#9C27B0"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "Regalo",        "ic_card_giftcard",  "#E91E63"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "Otros",         "ic_category",       "#607D8B")
                );
        db.categoriaIngresoDao().insertarVarias(ingresos);
    }

    private TextWatcher clearErrorTextWatcher(TextInputLayout layout) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(android.text.Editable s) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }
        };
    }
}
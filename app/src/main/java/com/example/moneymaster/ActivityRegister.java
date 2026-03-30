package com.example.moneymaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

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
 * Actualización: pregunta de seguridad para recuperación de contraseña.
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

        db      = AppDatabase.getDatabase(this);
        session = new SessionManager(this);

        setupSecurityQuestionDropdown();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // ── Poblar dropdown con las 3 preguntas de seguridad ──
    private void setupSecurityQuestionDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                ForgotPasswordActivity.SECURITY_QUESTIONS
        );
        binding.actvSecurityQuestion.setAdapter(adapter);
        // Seleccionar la primera por defecto
        binding.actvSecurityQuestion.setText(
                ForgotPasswordActivity.SECURITY_QUESTIONS[0], false);
    }

    private void setupListeners() {
        binding.btnRegistrar.setOnClickListener(v -> attemptRegister());

        binding.etNombre.addTextChangedListener(clearErrorTextWatcher(binding.tilNombre));
        binding.etEmail.addTextChangedListener(clearErrorTextWatcher(binding.tilEmail));
        binding.etPassword.addTextChangedListener(clearErrorTextWatcher(binding.tilPassword));
        binding.etPasswordConfirm.addTextChangedListener(
                clearErrorTextWatcher(binding.tisPasswordConfirm));
        binding.etSecurityAnswer.addTextChangedListener(
                clearErrorTextWatcher(binding.tilSecurityAnswer));
    }

    private void attemptRegister() {
        String nombre          = binding.etNombre.getText().toString().trim();
        String email           = binding.etEmail.getText().toString().trim();
        String password        = binding.etPassword.getText().toString();
        String passwordConfirm = binding.etPasswordConfirm.getText().toString();
        String answer          = binding.etSecurityAnswer.getText().toString().trim();

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

        if (answer.isEmpty()) {
            binding.tilSecurityAnswer.setError("La respuesta es obligatoria");
            valid = false;
        }

        if (valid) {
            // Calcular índice de la pregunta seleccionada
            String selectedQuestion = binding.actvSecurityQuestion.getText().toString();
            int questionIndex = 0;
            for (int i = 0; i < ForgotPasswordActivity.SECURITY_QUESTIONS.length; i++) {
                if (ForgotPasswordActivity.SECURITY_QUESTIONS[i].equals(selectedQuestion)) {
                    questionIndex = i;
                    break;
                }
            }
            processRegistration(nombre, email, password, answer, questionIndex);
        }
    }

    private void processRegistration(String nombre, String email, String password,
                                     String answer, int questionIndex) {
        executor.execute(() -> {
            try {
                String passwordHash = SecurityUtils.hashPassword(password);

                // Hashear respuesta normalizada (insensible a mayúsculas y espacios)
                String answerHash = SecurityUtils.hashPassword(answer.toLowerCase());

                User newUser = new User(nombre, email, passwordHash);
                newUser.securityQuestion   = questionIndex;
                newUser.securityAnswerHash = answerHash;

                long userId = db.usuarioDao().insertUser(newUser);

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
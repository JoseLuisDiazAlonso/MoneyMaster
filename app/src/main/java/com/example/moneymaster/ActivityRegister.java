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

    private void setupSecurityQuestionDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                ForgotPasswordActivity.SECURITY_QUESTIONS
        );
        binding.actvSecurityQuestion.setAdapter(adapter);
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
            binding.tilNombre.setError(getString(R.string.error_nombre_obligatorio));
            valid = false;
        }
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_amount_required));
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_amount_invalid));
            valid = false;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_amount_required));
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_min));
            valid = false;
        }
        if (!password.equals(passwordConfirm)) {
            binding.tisPasswordConfirm.setError(getString(R.string.error_passwords_no_coinciden));
            valid = false;
        }
        if (answer.isEmpty()) {
            binding.tilSecurityAnswer.setError(getString(R.string.error_amount_required));
            valid = false;
        }

        if (valid) {
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
                String answerHash   = SecurityUtils.hashPassword(answer.toLowerCase());

                User newUser = new User(nombre, email, passwordHash);
                newUser.securityQuestion   = questionIndex;
                newUser.securityAnswerHash = answerHash;

                long userId = db.usuarioDao().insertUser(newUser);
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
                        binding.tilEmail.setError(getString(R.string.error_email_registrado))
                );
            }
        });
    }

    /**
     * FIX: nombres de categoría como claves (ej. "cat_alimentacion")
     * que se traducen en CategoryAdapter según el idioma del dispositivo.
     */
    private void sembrarCategoriasSiNecesario() {
        if (db.categoriaGastoDao().countCategoriasDelSistema() > 0) return;

        java.util.List<com.example.moneymaster.data.model.CategoriaGasto> gastos =
                java.util.Arrays.asList(
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_alimentacion", "ic_restaurant",     "#FF5722"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_transporte",   "ic_directions_car", "#2196F3"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_vivienda",     "ic_home",           "#4CAF50"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_salud",        "ic_local_hospital", "#F44336"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_ocio",         "ic_sports_esports", "#9C27B0"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_ropa",         "ic_checkroom",      "#E91E63"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_educacion",    "ic_school",         "#3F51B5"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_tecnologia",   "ic_devices",        "#00BCD4"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_viajes",       "ic_flight",         "#FF9800"),
                        com.example.moneymaster.data.model.CategoriaGasto.crearSistema(
                                "cat_otros",        "ic_category",       "#607D8B")
                );
        db.categoriaGastoDao().insertarVarias(gastos);

        java.util.List<com.example.moneymaster.data.model.CategoriaIngreso> ingresos =
                java.util.Arrays.asList(
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "cat_salario",    "ic_work",          "#4CAF50"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "cat_freelance",  "ic_laptop",        "#2196F3"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "cat_inversiones","ic_trending_up",   "#FF9800"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "cat_alquiler",   "ic_home",          "#9C27B0"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "cat_regalo",     "ic_card_giftcard", "#E91E63"),
                        com.example.moneymaster.data.model.CategoriaIngreso.crearSistema(
                                "cat_otros",      "ic_category",      "#607D8B")
                );
        db.categoriaIngresoDao().insertarVarias(ingresos);
    }

    private TextWatcher clearErrorTextWatcher(TextInputLayout layout) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }
        };
    }
}
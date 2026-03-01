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

    // ViewBinding
    private ActivityRegisterBinding binding;

    // Dependencias
    private AppDatabase   db;
    private SessionManager session;

    // Room no permite operaciones en el hilo principal
    private final ExecutorService executor = Executors.newSingleThreadExecutor();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db      = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // liberar recursos
    }


    private void setupListeners() {

        // Botón principal de registro
        binding.btnRegistrar.setOnClickListener(v -> attemptRegister());

        // Limpiar errores mientras el usuario escribe
        binding.etNombre.addTextChangedListener(
                clearErrorTextWatcher(binding.tilNombre));
        binding.etEmail.addTextChangedListener(
                clearErrorTextWatcher(binding.tilEmail));
        binding.etPassword.addTextChangedListener(
                clearErrorTextWatcher(binding.tilPassword));
        binding.etPasswordConfirm.addTextChangedListener(
                clearErrorTextWatcher(binding.tisPasswordConfirm)); // ← corregido: "til" no "tis"
    }


    private void attemptRegister() {
        String nombre          = binding.etNombre.getText().toString().trim();
        String email           = binding.etEmail.getText().toString().trim();
        String password        = binding.etPassword.getText().toString();
        String passwordConfirm = binding.etPasswordConfirm.getText().toString();

        boolean valid = true;

        // Validación 1: nombre vacío
        if (nombre.isEmpty()) {
            binding.tilNombre.setError("El nombre es obligatorio");
            valid = false;
        }

        // Validación 2: email vacío o formato incorrecto
        if (email.isEmpty()) {
            binding.tilEmail.setError("El email es obligatorio");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("El email no es válido");
            valid = false;
        }

        // Validación 3: contraseña vacía o menor de 6 caracteres
        if (password.isEmpty()) {
            binding.tilPassword.setError("La contraseña es obligatoria");
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            valid = false;
        }

        // Validación 4: contraseñas no coinciden
        if (!password.equals(passwordConfirm)) {
            binding.tisPasswordConfirm.setError("Las contraseñas no coinciden"); // ← corregido: "til" no "tis"
            valid = false;
        }

        // Si todas las validaciones pasan → procesar en hilo de fondo
        if (valid) {
            processRegistration(nombre, email, password);
        }
    }



    private void processRegistration(String nombre, String email, String password) {
        executor.execute(() -> {
            try {
                // Paso 1: hashear contraseña (SHA-256 + sal)
                String passwordHash = SecurityUtils.hashPassword(password);

                // Paso 2: insertar usuario en Room
                long userId = db.userDao().insertUser(
                        new User(nombre, email, passwordHash)
                );

                // Paso 3: guardar sesión + navegar a MainActivity
                runOnUiThread(() -> {
                    session.saveSession((int) userId, email, nombre);

                    Intent intent = new Intent(ActivityRegister.this, MainActivity.class);
                    // Limpiar back-stack: el usuario no puede volver al registro pulsando "atrás"
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                // Room lanza excepción si el email ya existe (índice UNIQUE en la tabla)
                runOnUiThread(() ->
                        binding.tilEmail.setError("Este email ya está registrado")
                );
            }
        });
    }

    /**
     * Devuelve un TextWatcher que borra el error de un TextInputLayout
     * en cuanto el usuario empieza a escribir en el campo.
     * Se reutiliza para todos los campos del formulario.
     */
    private TextWatcher clearErrorTextWatcher(TextInputLayout layout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesario
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null); // limpiar error en cuanto el usuario escribe
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // No necesario
            }
        };
    }
}
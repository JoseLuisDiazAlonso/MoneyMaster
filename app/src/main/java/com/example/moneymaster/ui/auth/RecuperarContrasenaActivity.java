package com.example.moneymaster.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.moneymaster.databinding.ActivityRecuperarContrasenaBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.TimeUnit;

public class RecuperarContrasenaActivity extends AppCompatActivity {

    private ActivityRecuperarContrasenaBinding binding;
    private RecuperarContrasenaViewModel viewModel;
    private CountDownTimer countDownTimer;
    private String emailActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperarContrasenaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RecuperarContrasenaViewModel.class);
        
        setupToolbar();
        setupObservers();
        setupClickListeners();
    }
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    //Observer-Reacciona ante cambios del ViewModel
    private void setupObservers() {
        //Email enviado. Mostrar pantalla de código
        viewModel.getEmailEnviado().observe(this, enviado -> {
            if (enviado != null && enviado) {
                mostrarPantalla(2);
                binding.tvEmailDestino.setText("Código enviado a: " + emailActual);
                iniciarTimer();
                mostrarSnackbar("Código enviado a " + emailActual);
            }
        });

        //Código verificado. Mostrar pantalla de nueva contraseña
        viewModel.getCodigoVerificado().observe(this, verificado -> {
            if (verificado != null && verificado) {
                detenerTimer();
                mostrarPantalla(3);
                mostrarSnackbar("Código correcto. Crea tu nueva contraseña.");
            }
        });
        
        //Contraseña actualizada. éxito y navegar a Login
        viewModel.getContrasenaActualizada().observe(this, actualizada -> {
            if (actualizada != null && actualizada) {
                mostrarSnackbar("¡Contraseña actualizada exitosamente!");
                //Delay para que el usuario vea el mensaje antes de navegar
                binding.getRoot().postDelayed(() -> {
                    navegarALogin();
                }, 1500);
            }
        });
        //Errores
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                mostrarError(error);
            }
        });
        //Loading state
        viewModel.getIsLoading().observe(this, cargando -> {
            binding.progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            //Desabilitar botones durante la carga
            binding.btnEnviarCodigo.setEnabled(!cargando);
            binding.btnVerificarCodigo.setEnabled(!cargando);
            binding.btnActualizarContrasena.setEnabled(!cargando);
        });
    }
    
    //Click Listener
    private void setupClickListeners() {
        //Botón: Enviar código al email
        binding.btnEnviarCodigo.setOnClickListener(v -> {
            emailActual = binding.etEmail.getText().toString().trim();
            limpiarErrores();
            viewModel.solicitarCodigo(emailActual);
        });

        //Botón: Verificar código ingresado
        binding.btnEnviarCodigo.setOnClickListener(v -> {
            String codigo = binding.etCodigo.getText().toString().trim();
            if (codigo.isEmpty() || codigo.length() != 6) {
                binding.tilCodigo.setError("Ingresa un código válido de 6 dígitos");
                return;
            }
            limpiarErrores();
            viewModel.verificarCodigo(codigo);
        });
        //Botón: Reenviar código
        binding.btnReenviarCodigo.setOnClickListener(v -> {
            detenerTimer();
            mostrarPantalla(1);
            binding.etEmail.setText(emailActual);
        });
        //Botón: Actualizar contraseña
        binding.btnActualizarContrasena.setOnClickListener(v -> {
            String nueva = binding.etNuevaContrasena.getText().toString();
            String confirmar = binding.etConfirmarContrasena.getText().toString();
            limpiarErrores();
            viewModel.actualizarContrasena(nueva, confirmar);
        });

    }
    //Timer
    private void iniciarTimer() {
        long tiempoRestante = viewModel.getTiempoExpiracion() - System.currentTimeMillis();
        if (tiempoRestante <= 0) tiempoRestante = 2 * 60 * 60 * 1000L; // ← Error 1: constante inline

        countDownTimer = new CountDownTimer(tiempoRestante, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { // ← Error 2: debe ser parámetro del método
                long horas   = millisUntilFinished / 3600000;
                long minutos = (millisUntilFinished % 3600000) / 60000;
                long segundos = (millisUntilFinished % 60000) / 1000;

                // ← Error 3: Locale con L mayúscula, importado de java.util
                String tiempo = String.format(java.util.Locale.getDefault(),
                        "⏰ Expira en: %02d:%02d:%02d", horas, minutos, segundos);

                binding.tvTimer.setText(tiempo);
            }

            @Override
            public void onFinish() {
                binding.tvTimer.setText("⚠️ Código expirado");
                mostrarSnackbar("El código ha expirado. Solicita uno nuevo.");
                binding.getRoot().postDelayed(() -> mostrarPantalla(1), 2000);
            }
        }.start();
    }



    private void detenerTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    //Navegación entre Pantallas (1-email, 2-código, 3-nueva contraseña)
    private void mostrarPantalla(int i) {
        binding.layoutEmail.setVisibility(i == 1 ? View.VISIBLE : View.GONE);
        binding.layoutCodigo.setVisibility(i == 2 ? View.VISIBLE : View.GONE);
        binding.layoutNuevaContrasena.setVisibility(i == 3 ? View.VISIBLE : View.GONE);
    }
    private void navegarALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    //UI Helper Methods
    private void mostrarSnackbar(String s) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), s, Snackbar.LENGTH_LONG);
    }

    private void mostrarError(String error) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), "⚠️ " + error, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(com.google.android.material.R.color.design_default_color_error, getTheme()));
        snackbar.show();
    }

    private void limpiarErrores() {
        binding.tilEmail.setError(null);
        binding.tilCodigo.setError(null);
        binding.tilNuevaContrasena.setError(null);
        binding.tilConfirmarContrasena.setError(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerTimer();
    }
}

package com.example.moneymaster.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymaster.MainActivity;
import com.example.moneymaster.R;
import com.example.moneymaster.ui.auth.LoginActivity;
import com.example.moneymaster.utils.SharedPreferencesManager;

/**Esto es la primera pantalla de la apicación. Muestra el logo con animación fade-in,
 * Verifica si es la primera vez del usuario,
 * Redirige, en función del estado....OnboardingActivity, LoginActivity o a la sesión iniciada
 * **/

public class Splash extends AppCompatActivity {

    //Duración del Splash en milisegundos
    private static final long SPLASH_DURATION = 2000;

    private SharedPreferencesManager preferencesManager;
    private ImageView ivLogo;
    private TextView titulo;
    private TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Inicializamos el SharedPreferencesManager
        preferencesManager = new SharedPreferencesManager(this);

        //Referenciamos las vistas
        ivLogo = findViewById(R.id.ivLogo);
        titulo = findViewById(R.id.titulo);
        description = findViewById(R.id.description);

        //Iniciamos la animación del fade-in
        startAnimation();

        //Navegamos al siguiente activity después del delay
        navigateAfeterDelay();
    }

    //Inicializamos la animación del fade-in en el logo y los textos
private void startAnimation() {
     //cargar animación
    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
    //Aplicamos la animación al logo
    ivLogo.startAnimation(fadeIn);
    //Aplicamos animación al título con ligero delay
    titulo.postDelayed(() -> titulo.startAnimation(fadeIn), 200);
    //Aplicamos animación a la descripción con más delay
    description.postDelayed(() -> description.startAnimation(fadeIn), 400);
}
//Navegamos al siguiente activity después del delay de 2 segundos
    private void navigateAfeterDelay() {
     new Handler(Looper.getMainLooper()).postDelayed(() -> {
         Intent intent;

         //CASO 1: Primera Vez
         if (preferencesManager.isFirstTime()) {
             intent = new Intent(Splash.this, OnboardingActivity.class);
         }
         //CASO 2: Sesión activa - Ir al Main
         else if (preferencesManager.isLoggedIn()) {
             intent = new Intent(Splash.this, MainActivity.class);
         }
         //CASO 3: Ya se produjo el Onboarding pero no está logeado - Ir al login
         else {
             intent = new Intent(Splash.this, LoginActivity.class);
         }
         startActivity(intent);
         finish(); //Cerrar Splash para que no vuelva con Back

         //Transicción suave entre Activities
         overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

     }, SPLASH_DURATION);
    }
}

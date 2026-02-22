package com.example.moneymaster.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moneymaster.R;
import com.google.android.material.button.MaterialButton;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

/**
 * Activity principal del flujo de Onboarding.
 * Gestiona la navegación entre las 3 pantallas de introducción
 * y controla la finalización del onboarding.
 */
public class OnboardingActivity extends AppCompatActivity {

    // Vistas principales
    private ViewPager2 viewPager;
    private DotsIndicator dotsIndicator;
    private MaterialButton btnSkip, btnNext, btnStart;
    private OnboardingAdapter adapter;

    // SharedPreferences para guardar el estado del onboarding
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MoneyMasterPrefs";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Inicializar vistas
        initViews();

        // Configurar ViewPager2
        setupViewPager();

        // Configurar listeners de botones
        setupListeners();
    }

    /**
     * Inicializa todas las referencias a las vistas
     */
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dots_indicator);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);
        btnStart = findViewById(R.id.btnStart);
    }

    /**
     * Configura el ViewPager2 con su adapter y animaciones
     */
    private void setupViewPager() {
        // Crear y asignar el adapter
        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        // Vincular el indicador de puntos con el ViewPager
        dotsIndicator.attachTo(viewPager);

        // Configurar animaciones de transición entre páginas
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                // position: -1 (izquierda fuera), 0 (centro), 1 (derecha fuera)

                if (position < -1 || position > 1) {
                    // Página completamente fuera de la pantalla
                    page.setAlpha(0);
                } else {
                    // Efecto de fade mientras se desliza
                    page.setAlpha(1 - Math.abs(position));
                    page.setTranslationX(page.getWidth() * -position);

                    // Efecto de escala suave (opcional)
                    float scaleFactor = Math.max(0.85f, 1 - Math.abs(position) * 0.15f);
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                }
            }
        });

        // Listener para detectar cambios de página
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Actualizar visibilidad de botones según la página actual
                updateButtonsVisibility(position);
            }
        });
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        // Botón "Siguiente" - Avanza a la siguiente página
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem < adapter.getItemCount() - 1) {
                    // Ir a la siguiente página
                    viewPager.setCurrentItem(currentItem + 1, true);
                }
            }
        });

        // Botón "Saltar" - Termina el onboarding inmediatamente
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOnboarding();
            }
        });

        // Botón "Comenzar" - Termina el onboarding y va a la app
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOnboarding();
            }
        });
    }

    /**
     * Actualiza la visibilidad de los botones según la página actual
     * Páginas 1-2: Muestra "Siguiente" y "Saltar"
     * Página 3: Muestra solo "Comenzar"
     *
     * @param position Posición actual en el ViewPager (0, 1, 2)
     */
    private void updateButtonsVisibility(int position) {
        if (position == adapter.getItemCount() - 1) {
            // ÚLTIMA PÁGINA (índice 2)
            // Ocultar botones "Siguiente" y "Saltar"
            animateViewOut(btnSkip);
            animateViewOut(btnNext);

            // Mostrar botón "Comenzar"
            animateViewIn(btnStart);
        } else {
            // PÁGINAS 1 y 2 (índices 0 y 1)
            // Ocultar botón "Comenzar" si está visible
            if (btnStart.getVisibility() == View.VISIBLE) {
                animateViewOut(btnStart);
            }

            // Mostrar botones "Siguiente" y "Saltar"
            if (btnSkip.getVisibility() != View.VISIBLE) {
                animateViewIn(btnSkip);
            }
            if (btnNext.getVisibility() != View.VISIBLE) {
                animateViewIn(btnNext);
            }
        }
    }

    /**
     * Anima la desaparición de una vista con fade-out
     * @param view Vista a ocultar
     */
    private void animateViewOut(View view) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // No hacer nada
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // No hacer nada
            }
        });
        view.startAnimation(fadeOut);
    }

    /**
     * Anima la aparición de una vista con fade-in
     * @param view Vista a mostrar
     */
    private void animateViewIn(View view) {
        view.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(200);
        view.startAnimation(fadeIn);
    }

    /**
     * Finaliza el onboarding y navega a la siguiente pantalla
     * Guarda en SharedPreferences que el onboarding fue completado
     */
    private void finishOnboarding() {
        // Marcar onboarding como completado
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
        editor.apply();

        // TODO: Navegar a LoginActivity cuando la crees
        // Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        // startActivity(intent);

        // Por ahora, simplemente cerramos la activity
        finish();

        // Mensaje temporal para confirmar que funciona
        Toast.makeText(this,
                "¡Onboarding completado! Próximamente: LoginActivity",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Maneja el botón "Atrás" del dispositivo
     * - En la primera página: Sale de la app
     * - En otras páginas: Vuelve a la página anterior
     */
    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // Si está en la primera página, comportamiento por defecto (salir)
            super.onBackPressed();
        } else {
            // Retroceder a la página anterior
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}
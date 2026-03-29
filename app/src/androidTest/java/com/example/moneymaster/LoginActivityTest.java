package com.example.moneymaster;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.moneymaster.R;
import com.example.moneymaster.TestSessionHelper;
import com.example.moneymaster.ui.auth.LoginActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * LoginActivityTest — Card #61
 *
 * Tests de estructura y entrada de datos en LoginActivity.
 * Los tests de validación de errores en TextInputLayout se omiten
 * porque requieren IdlingResource para sincronizar con el executor
 * de base de datos — complejidad fuera del alcance de este card.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private Intent intent;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestSessionHelper.clearSession(context);
        intent = new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class);
    }

    // ─── Estructura básica ────────────────────────────────────────────────────

    @Test
    public void loginActivity_carga_campoEmailVisible() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void loginActivity_carga_campoPasswordVisible() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void loginActivity_carga_botonLoginVisible() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void loginActivity_carga_enlaceRecuperarPasswordVisible() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvForgotPassword)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void loginActivity_carga_checkboxRecordarmeMostrado() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.cbRememberMe)).check(matches(isDisplayed()));
        }
    }

    // ─── Entrada de datos ─────────────────────────────────────────────────────

    @Test
    public void loginActivity_campoEmail_aceptaTexto() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etEmail))
                    .perform(click(), typeText("test@test.com"), closeSoftKeyboard());
            onView(withId(R.id.etEmail))
                    .check(matches(withText("test@test.com")));
        }
    }

    @Test
    public void loginActivity_campoPassword_estaHabilitado() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etPassword)).check(matches(isEnabled()));
        }
    }

    @Test
    public void loginActivity_botonLogin_estaHabilitado() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnLogin)).check(matches(isEnabled()));
        }
    }
}

package com.example.moneymaster;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.moneymaster.R;
import com.example.moneymaster.TestSessionHelper;
import com.example.moneymaster.ui.expenses.AddExpenseActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

/**
 * AddExpenseActivityTest — Card #61 (corregido)
 *
 * Usa Intent explícito para lanzar Activities con android:exported="false".
 * ActivityScenario.launch(Clase.class) falla con Activities no exportadas
 * porque intenta lanzarlas desde el paquete de test (.test) en lugar del
 * paquete de la app (.moneymaster).
 *
 * Solución: construir el Intent manualmente con el contexto de la app.
 */
@RunWith(AndroidJUnit4.class)
public class AddExpenseActivityTest {

    private Intent intent;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestSessionHelper.injectFakeSession(context);

        // Intent explícito con el contexto de la app — funciona con exported="false"
        intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddExpenseActivity.class);
    }

    // ─── Estructura básica ────────────────────────────────────────────────────

    @Test
    public void addExpenseActivity_carga_camposVisibles() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.etCantidad))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.etDescripcion))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.etFecha))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.btnGuardar))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void addExpenseActivity_toolbar_esVisible() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void addExpenseActivity_campoFecha_tieneValorPorDefecto() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.etFecha))
                    .check(matches(not(withText(""))));
        }
    }

    // ─── Entrada de datos ─────────────────────────────────────────────────────

    @Test
    public void addExpenseActivity_campoCantidad_aceptaTexto() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.etCantidad))
                    .perform(click(), typeText("25.50"), closeSoftKeyboard());

            onView(withId(R.id.etCantidad))
                    .check(matches(withText("25.50")));
        }
    }

    @Test
    public void addExpenseActivity_campoDescripcion_aceptaTexto() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.etDescripcion))
                    .perform(click(), typeText("Compra supermercado"), closeSoftKeyboard());

            onView(withId(R.id.etDescripcion))
                    .check(matches(withText("Compra supermercado")));
        }
    }

    @Test
    public void addExpenseActivity_campoCantidad_aceptaDecimal() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.etCantidad))
                    .perform(click(), typeText("99.99"), closeSoftKeyboard());

            onView(withId(R.id.etCantidad))
                    .check(matches(withText("99.99")));
        }
    }

    // ─── Validación ───────────────────────────────────────────────────────────

    @Test
    public void addExpenseActivity_guardarSinMonto_muestraError() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.btnGuardar)).perform(click());

            onView(withId(R.id.tilCantidad))
                    .check(matches(hasDescendant(
                            withText(R.string.error_cantidad_requerida))));
        }
    }

    @Test
    public void addExpenseActivity_guardarSinCategoria_muestraError() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.etCantidad))
                    .perform(click(), typeText("50"), closeSoftKeyboard());

            onView(withId(R.id.btnGuardar)).perform(click());

            onView(withId(R.id.tilCategoria))
                    .check(matches(hasDescendant(
                            withText(R.string.error_categoria_requerida))));
        }
    }

    @Test
    public void addExpenseActivity_guardarConMontoNegativo_muestraError() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.etCantidad))
                    .perform(click(), typeText("-10"), closeSoftKeyboard());

            onView(withId(R.id.btnGuardar)).perform(click());

            onView(withId(R.id.tilCantidad))
                    .check(matches(hasDescendant(
                            withText(R.string.error_cantidad_positiva))));
        }
    }

    // ─── Botón foto ───────────────────────────────────────────────────────────

    @Test
    public void addExpenseActivity_botonFoto_estaVisible() {
        try (ActivityScenario<AddExpenseActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.btnFoto))
                    .check(matches(isDisplayed()));
        }
    }

    // ─── Navegación hacia atrás ───────────────────────────────────────────────

    @Test
    public void addExpenseActivity_botonAtras_cierraLaActivity() {
        ActivityScenario<AddExpenseActivity> scenario =
                ActivityScenario.launch(intent);

        // pressBack cierra la Activity — no usar try-with-resources ni
        // interactuar con vistas después porque la app ya no tiene Activity activa
        scenario.onActivity(activity -> activity.onBackPressed());

        // Verificar que la Activity está destruida
        assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED,
                scenario.getState());
    }
}

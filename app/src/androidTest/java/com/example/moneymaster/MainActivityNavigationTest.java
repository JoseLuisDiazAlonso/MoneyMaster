package com.example.moneymaster;

import android.content.Context;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * MainActivityNavigationTest — Card #61
 *
 * Tests de navegación entre las 4 pestañas del BottomNavigationView
 * de MainActivity usando Espresso.
 *
 * Verifica:
 *  - La pestaña Home carga el fragmentContainer
 *  - La pestaña Grupos es seleccionable
 *  - La pestaña Estadísticas es seleccionable
 *  - La pestaña Perfil es seleccionable
 *  - El FAB aparece en Home y desaparece en otras pestañas
 *  - El toolbar tiene título visible
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestSessionHelper.injectFakeSession(context);
    }

    // ─── Estructura básica ────────────────────────────────────────────────────

    @Test
    public void mainActivity_carga_bottomNavigationVisible() {
        onView(withId(R.id.bottomNavigationView))
                .check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_carga_fragmentContainerVisible() {
        onView(withId(R.id.fragmentContainer))
                .check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_carga_toolbarVisible() {
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    @Test
    public void mainActivity_carga_fabVisible() {
        onView(withId(R.id.fabAddExpense))
                .check(matches(isDisplayed()));
    }

    // ─── Navegación por pestañas ──────────────────────────────────────────────

    @Test
    public void navegacion_alPulsarGrupos_seleccionaGrupos() {
        onView(withId(R.id.nav_groups))
                .perform(click());

        onView(withId(R.id.nav_groups))
                .check(matches(isSelected()));
    }

    @Test
    public void navegacion_alPulsarEstadisticas_seleccionaEstadisticas() {
        onView(withId(R.id.nav_stats))
                .perform(click());

        onView(withId(R.id.nav_stats))
                .check(matches(isSelected()));
    }

    @Test
    public void navegacion_alPulsarPerfil_seleccionaPerfil() {
        onView(withId(R.id.nav_profile))
                .perform(click());

        onView(withId(R.id.nav_profile))
                .check(matches(isSelected()));
    }

    @Test
    public void navegacion_alVolverAHome_seleccionaHome() {
        // Ir a grupos y volver a home
        onView(withId(R.id.nav_groups)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());

        onView(withId(R.id.nav_home))
                .check(matches(isSelected()));
    }

    // ─── FAB visibilidad ──────────────────────────────────────────────────────

    @Test
    public void fab_enGrupos_estaOculto() {
        onView(withId(R.id.nav_groups)).perform(click());

        // El FAB se oculta con hide() — comprobamos que no está visible
        onView(withId(R.id.fabAddExpense))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void fab_alVolverAHome_esVisible() {
        onView(withId(R.id.nav_groups)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());

        onView(withId(R.id.fabAddExpense))
                .check(matches(isDisplayed()));
    }
}

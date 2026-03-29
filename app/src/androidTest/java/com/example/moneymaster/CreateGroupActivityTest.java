package com.example.moneymaster;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.moneymaster.R;
import com.example.moneymaster.TestSessionHelper;
import com.example.moneymaster.ui.groups.CreateGroupActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

/**
 * CreateGroupActivityTest — Card #61 (corregido)
 *
 * IDs reales del layout activity_create_group.xml:
 *  - editTextGroupName      (TextInputEditText del nombre)
 *  - inputLayoutGroupName   (TextInputLayout contenedor)
 *  - editTextGroupDescription
 *  - buttonCreateGroup
 *  - buttonAddMember
 *  - recyclerViewMembers
 *  - textViewMemberCount
 *  - textViewMemberWarning
 *
 * El error de validación se setea como texto literal en validateAndCreate():
 *  "El nombre del grupo es obligatorio"
 * No existe como string resource, así que se compara con withText() directo.
 */
@RunWith(AndroidJUnit4.class)
public class CreateGroupActivityTest {

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestSessionHelper.injectFakeSession(context);
    }

    // ─── Estructura básica ────────────────────────────────────────────────────

    @Test
    public void createGroupActivity_carga_campoNombreVisible() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.editTextGroupName))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void createGroupActivity_carga_botonCrearVisible() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.buttonCreateGroup))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void createGroupActivity_carga_botonAnadirMiembroVisible() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.buttonAddMember))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void createGroupActivity_carga_campoDescripcionVisible() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.editTextGroupDescription))
                    .check(matches(isDisplayed()));
        }
    }

    // ─── Entrada de datos ─────────────────────────────────────────────────────

    @Test
    public void createGroupActivity_campoNombre_aceptaTexto() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.editTextGroupName))
                    .perform(click(), typeText("Viaje Roma"), closeSoftKeyboard());

            onView(withId(R.id.editTextGroupName))
                    .check(matches(withText("Viaje Roma")));
        }
    }

    @Test
    public void createGroupActivity_campoDescripcion_aceptaTexto() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.editTextGroupDescription))
                    .perform(click(), typeText("Vacaciones de verano"), closeSoftKeyboard());

            onView(withId(R.id.editTextGroupDescription))
                    .check(matches(withText("Vacaciones de verano")));
        }
    }

    // ─── Validación ───────────────────────────────────────────────────────────

    @Test
    public void createGroupActivity_crearSinNombre_muestraError() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            // Pulsar crear sin nombre — el error se setea como texto literal
            onView(withId(R.id.buttonCreateGroup)).perform(click());

            onView(withId(R.id.inputLayoutGroupName))
                    .check(matches(hasDescendant(
                            withText("El nombre del grupo es obligatorio"))));
        }
    }

    // ─── Añadir miembro ───────────────────────────────────────────────────────

    @Test
    public void createGroupActivity_pulsarAnadirMiembro_añadeFilaAlRecycler() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.buttonAddMember)).perform(click());

            onView(withId(R.id.recyclerViewMembers))
                    .check(matches(hasMinimumChildCount(1)));
        }
    }

    @Test
    public void createGroupActivity_pulsarAnadirMiembroDosveces_añadeDosFilas() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.buttonAddMember)).perform(click());
            onView(withId(R.id.buttonAddMember)).perform(click());

            onView(withId(R.id.recyclerViewMembers))
                    .check(matches(hasMinimumChildCount(2)));
        }
    }

    @Test
    public void createGroupActivity_contador_actualizaAlAnadirMiembro() {
        try (ActivityScenario<CreateGroupActivity> scenario =
                     ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(), CreateGroupActivity.class))) {

            onView(withId(R.id.buttonAddMember)).perform(click());

            // Tras añadir 1 miembro, el contador debe mostrar "1 miembro"
            onView(withId(R.id.textViewMemberCount))
                    .check(matches(withText("1 miembro")));
        }
    }
}

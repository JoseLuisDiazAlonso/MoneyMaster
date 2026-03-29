package com.example.moneymaster;

import android.view.View;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * TextInputLayoutErrorMatcher — Card #61
 *
 * Matcher de Espresso para verificar el texto de error de un TextInputLayout.
 * hasDescendant(withText(...)) no funciona con TextInputLayout porque el error
 * se muestra en una vista interna no accesible directamente. Este matcher
 * lee el error directamente de TextInputLayout.getError().
 *
 * Uso:
 *   onView(withId(R.id.tilEmail))
 *       .check(matches(hasTextInputLayoutError("El email es obligatorio")));
 */
public class TextInputLayoutErrorMatcher {

    public static Matcher<View> hasTextInputLayoutError(final String expectedError) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) return false;
                CharSequence error = ((TextInputLayout) view).getError();
                if (error == null) return false;
                return expectedError.equals(error.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("TextInputLayout con error: " + expectedError);
            }
        };
    }
}

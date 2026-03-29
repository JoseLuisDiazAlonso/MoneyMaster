package com.example.moneymaster.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymaster.databinding.EmptyStateBinding;


public class EmptyStateHelper {

    private final EmptyStateBinding binding;
    private static final int ANIM_DURATION_MS = 300;

    public EmptyStateHelper(EmptyStateBinding binding) {
        this.binding = binding;
    }

    /**
     * Configura el empty state con icono, textos y acción del botón CTA.
     * Llamar una sola vez en onViewCreated().
     *
     * @param iconRes      Drawable del icono ilustrativo
     * @param titleRes     String resource del título principal
     * @param subtitleRes  String resource del subtítulo descriptivo
     * @param ctaRes       String resource del texto del botón CTA
     * @param ctaListener  OnClickListener para el botón CTA
     */
    public void setup(
            @DrawableRes int iconRes,
            @StringRes int titleRes,
            @StringRes int subtitleRes,
            @StringRes int ctaRes,
            View.OnClickListener ctaListener) {

        binding.ivEmptyIcon.setImageResource(iconRes);
        binding.tvEmptyTitle.setText(titleRes);
        binding.tvEmptySubtitle.setText(subtitleRes);
        binding.btnEmptyCta.setText(ctaRes);
        binding.btnEmptyCta.setOnClickListener(ctaListener);
    }

    /**
     * Muestra u oculta el empty state según si la lista está vacía,
     * animando la transición con fade.
     *
     * @param recyclerView El RecyclerView asociado (se oculta cuando hay empty state)
     * @param isEmpty      true para mostrar el empty state, false para ocultarlo
     */
    public void toggle(RecyclerView recyclerView, boolean isEmpty) {
        if (isEmpty) {
            // Ocultar RecyclerView, mostrar empty state
            recyclerView.setVisibility(View.GONE);
            if (binding.layoutEmptyState.getVisibility() != View.VISIBLE) {
                fadeIn(binding.layoutEmptyState);
            }
        } else {
            // Mostrar RecyclerView, ocultar empty state
            binding.layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Versión sin RecyclerView — útil cuando la pantalla no tiene lista
     * sino que muestra/oculta secciones completas (ej. Estadísticas).
     *
     * @param contentView Vista de contenido principal a alternar con el empty state
     * @param isEmpty     true para mostrar el empty state
     */
    public void toggle(View contentView, boolean isEmpty) {
        if (isEmpty) {
            contentView.setVisibility(View.GONE);
            if (binding.layoutEmptyState.getVisibility() != View.VISIBLE) {
                fadeIn(binding.layoutEmptyState);
            }
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
        }
    }

    /** Hace visible el empty state directamente sin animación. */
    public void show() {
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }

    /** Oculta el empty state directamente sin animación. */
    public void hide() {
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    //Animación interna

    private void fadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(ANIM_DURATION_MS);
        view.startAnimation(anim);
    }
}

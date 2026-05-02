package com.example.moneymaster.utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.example.moneymaster.R;


public class TransitionHelper {

    // -------------------------------------------------------------------------
    // Activity Transitions
    // -------------------------------------------------------------------------

    /**
     * Inicia una Activity con transición de slide hacia adelante (izquierda).
     * Usar para navegar hacia una pantalla de detalle/formulario.
     */
    public static void startWithSlideForward(Activity from, Intent intent) {
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Inicia una Activity con transición de slide hacia adelante y espera resultado.
     */
    public static void startForResultWithSlideForward(Activity from, Intent intent, int requestCode) {
        from.startActivityForResult(intent, requestCode);
        from.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Inicia una Activity con transición de fade.
     * Usar para pantallas modales o de configuración.
     */
    public static void startWithFade(Activity from, Intent intent) {
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Llama a este método en onBackPressed() o en el botón de navegación hacia atrás
     * para aplicar la transición de regreso correcta.
     */
    public static void finishWithSlideBack(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Finaliza con transición fade.
     */
    public static void finishWithFade(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    // Shared Element Transitions (fotos)


    /**
     * Inicia una Activity con shared element transition para fotos.
     * La vista de origen (imageView) debe tener un transitionName establecido.
     *
     * Uso en el adaptador:
     *   TransitionHelper.startWithSharedPhoto(activity, intent, imageView, "photo_transition");
     *
     * En el layout del item del adaptador:
     *   android:transitionName="photo_transition"
     *
     * En ImageViewerActivity, la ImageView destino debe tener el mismo transitionName.
     */
    public static void startWithSharedPhoto(Activity from, Intent intent, View sharedView, String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedView.setTransitionName(transitionName);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    from,
                    sharedView,
                    transitionName
            );
            from.startActivity(intent, options.toBundle());
        } else {
            startWithFade(from, intent);
        }
    }

    /**
     * Inicia una Activity con múltiples shared elements (imagen + título).
     */
    @SafeVarargs
    public static void startWithMultipleSharedElements(Activity from, Intent intent,
                                                       Pair<View, String>... sharedElements) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    from, sharedElements);
            from.startActivity(intent, options.toBundle());
        } else {
            startWithFade(from, intent);
        }
    }


    // FAB Animations


    /**
     * Aplica animación de rotación + escala al FAB al abrirse.
     */
    public static void animateFabOpen(Context context, View fab) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_open);
        fab.startAnimation(anim);
    }

    /**
     * Aplica animación de rotación + escala al FAB al cerrarse.
     */
    public static void animateFabClose(Context context, View fab) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_close);
        fab.startAnimation(anim);
    }

    /**
     * Anima el FAB con bounce al pulsar (para uso en acciones únicas, sin toggle).
     * Escala a 1.2x y vuelve a 1.0x con efecto de rebote.
     */
    public static void animateFabPress(View fab) {
        fab.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction(() ->
                        fab.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(150)
                                .start()
                )
                .start();
    }


    // RecyclerView Item Animations (usadas desde el Adapter/Fragment)


    /**
     * Carga y devuelve la animación de entrada de un item del RecyclerView.
     * Llamar en onBindViewHolder para cada item nuevo.
     */
    public static Animation getItemAddAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.item_add);
    }

    /**
     * Carga y devuelve la animación de salida/eliminación de un item.
     * Llamar antes de notifyItemRemoved() con un listener en onAnimationEnd.
     */
    public static Animation getItemRemoveAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.item_remove);
    }

    /**
     * Aplica animación de entrada escalonada a una vista en función de su posición.
     * Genera un delay proporcional a la posición para un efecto cascada.
     *
     * Uso:
     *   TransitionHelper.animateItemEntry(holder.itemView, position);
     */
    public static void animateItemEntry(View itemView, int position) {
        itemView.setAlpha(0f);
        itemView.setTranslationY(-30f);
        itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(350)
                .setStartDelay(position * 40L)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }


    // View Animations (genéricas)


    /**
     * Hace aparecer una vista con fade in.
     */
    public static void fadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    /**
     * Hace desaparecer una vista con fade out y la oculta al terminar.
     */
    public static void fadeOut(View view) {
        view.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Anima una vista con un ligero "pulse" para llamar la atención.
     */
    public static void pulse(View view) {
        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .start()
                )
                .start();
    }
}

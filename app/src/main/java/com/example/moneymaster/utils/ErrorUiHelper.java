package com.example.moneymaster.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;


public class ErrorUiHelper {

    private static final String TAG = "ErrorUiHelper";


    // API para Fragment (busca la raíz de la vista automáticamente)


    /**
     * Muestra un Snackbar de error (rojo) en el Fragment.
     */
    public static void showError(Fragment fragment, String mensaje) {
        View root = getRootView(fragment);
        if (root != null) {
            showErrorSnackbar(root, mensaje);
        } else {
            showToastError(fragment.requireContext(), mensaje);
        }
    }

    /**
     * Muestra un Snackbar de éxito (verde) en el Fragment.
     */
    public static void showSuccess(Fragment fragment, String mensaje) {
        View root = getRootView(fragment);
        if (root != null) {
            showSuccessSnackbar(root, mensaje);
        } else {
            showToast(fragment.requireContext(), mensaje);
        }
    }

    /**
     * Muestra un Snackbar de error con botón "Reintentar".
     */
    public static void showErrorConReintentar(Fragment fragment, String mensaje,
                                              View.OnClickListener onReintentar) {
        View root = getRootView(fragment);
        if (root != null) {
            Snackbar.make(root, mensaje, Snackbar.LENGTH_LONG)
                    .setAction("Reintentar", onReintentar)
                    .setBackgroundTint(resolveColor(fragment.requireContext(),
                            com.google.android.material.R.attr.colorErrorContainer))
                    .setTextColor(resolveColor(fragment.requireContext(),
                            com.google.android.material.R.attr.colorOnErrorContainer))
                    .setActionTextColor(resolveColor(fragment.requireContext(),
                            com.google.android.material.R.attr.colorPrimary))
                    .show();
        } else {
            showToastError(fragment.requireContext(), mensaje);
        }
    }

    /**
     * Muestra un Snackbar informativo (neutro).
     */
    public static void showInfo(Fragment fragment, String mensaje) {
        View root = getRootView(fragment);
        if (root != null) {
            Snackbar.make(root, mensaje, Snackbar.LENGTH_SHORT).show();
        } else {
            showToast(fragment.requireContext(), mensaje);
        }
    }


    // API para Activity


    public static void showError(Activity activity, View anchorView, String mensaje) {
        showErrorSnackbar(anchorView, mensaje);
    }

    public static void showSuccess(Activity activity, View anchorView, String mensaje) {
        showSuccessSnackbar(anchorView, mensaje);
    }

    public static void showInfo(Activity activity, View anchorView, String mensaje) {
        Snackbar.make(anchorView, mensaje, Snackbar.LENGTH_SHORT).show();
    }


    // Toast (fallback cuando no hay View)


    public static void showToastError(Context context, String mensaje) {
        AppLogger.w(TAG, "Mostrando Toast error (sin View): " + mensaje);
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }


    // Mensajes de error estándar reutilizables


    public static void showErrorGuardado(Fragment fragment) {
        showError(fragment, "No se pudo guardar. Inténtalo de nuevo.");
    }

    public static void showErrorCargaDatos(Fragment fragment) {
        showError(fragment, "Error al cargar los datos.");
    }

    public static void showErrorExportacion(Fragment fragment) {
        showError(fragment, "Error durante la exportación.");
    }

    public static void showErrorFoto(Fragment fragment) {
        showError(fragment, "No se pudo procesar la foto.");
    }

    public static void showErrorCampoObligatorio(Fragment fragment) {
        showError(fragment, "Por favor, completa todos los campos obligatorios.");
    }


    // Helpers privados

    private static void showErrorSnackbar(View root, String mensaje) {
        Context ctx = root.getContext();
        Snackbar.make(root, mensaje, Snackbar.LENGTH_LONG)
                .setBackgroundTint(resolveColor(ctx,
                        com.google.android.material.R.attr.colorErrorContainer))
                .setTextColor(resolveColor(ctx,
                        com.google.android.material.R.attr.colorOnErrorContainer))
                .show();
    }

    private static void showSuccessSnackbar(View root, String mensaje) {
        Context ctx = root.getContext();
        Snackbar.make(root, mensaje, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(resolveColor(ctx,
                        com.google.android.material.R.attr.colorSecondaryContainer))
                .setTextColor(resolveColor(ctx,
                        com.google.android.material.R.attr.colorOnSecondaryContainer))
                .show();
    }

    private static View getRootView(Fragment fragment) {
        if (fragment == null || fragment.getView() == null) return null;
        return fragment.getView();
    }

    private static int resolveColor(Context context, int attrResId) {
        android.util.TypedValue tv = new android.util.TypedValue();
        context.getTheme().resolveAttribute(attrResId, tv, true);
        return tv.data;
    }
}

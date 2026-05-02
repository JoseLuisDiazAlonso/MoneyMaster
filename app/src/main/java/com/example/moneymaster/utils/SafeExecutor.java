package com.example.moneymaster.utils;

import androidx.fragment.app.Fragment;


public class SafeExecutor {


    // Interfaces

    @FunctionalInterface
    public interface Operation {
        void execute() throws Exception;
    }

    @FunctionalInterface
    public interface OperationWithResult<T> {
        T execute() throws Exception;
    }


    // Sin UI (ViewModel / Repository / hilo de fondo)


    /**
     * Ejecuta la operación logueando cualquier excepción como ERROR.
     *
     * @return true si completó sin excepciones, false si falló.
     */
    public static boolean run(String tag, String operacion, Operation op) {
        try {
            op.execute();
            return true;
        } catch (Exception e) {
            AppLogger.e(tag, "Error en operación [" + operacion + "]", e);
            return false;
        }
    }

    /**
     * Ejecuta la operación y devuelve su resultado.
     *
     * @param defaultValue Valor a devolver si hay excepción.
     */
    public static <T> T runForResult(String tag, String operacion,
                                     OperationWithResult<T> op, T defaultValue) {
        try {
            return op.execute();
        } catch (Exception e) {
            AppLogger.e(tag, "Error en operación [" + operacion + "]", e);
            return defaultValue;
        }
    }


    // Con UI (Fragment — ejecutar siempre en hilo principal)


    /**
     * Ejecuta la operación y muestra un Snackbar de error si falla.
     *
     * @param mensajeError Texto a mostrar al usuario en caso de error.
     * @return true si completó sin excepciones.
     */
    public static boolean runWithUi(Fragment fragment, String tag,
                                    String operacion, Operation op,
                                    String mensajeError) {
        try {
            op.execute();
            return true;
        } catch (Exception e) {
            AppLogger.e(tag, "Error en operación [" + operacion + "]", e);
            ErrorUiHelper.showError(fragment, mensajeError);
            return false;
        }
    }

    /**
     * Ejecuta la operación, muestra éxito o error según el resultado.
     *
     * @param mensajeExito  Texto Snackbar si todo fue bien.
     * @param mensajeError  Texto Snackbar si falló.
     * @return true si completó sin excepciones.
     */
    public static boolean runWithUiFeedback(Fragment fragment, String tag,
                                            String operacion, Operation op,
                                            String mensajeExito, String mensajeError) {
        try {
            op.execute();
            ErrorUiHelper.showSuccess(fragment, mensajeExito);
            return true;
        } catch (Exception e) {
            AppLogger.e(tag, "Error en operación [" + operacion + "]", e);
            ErrorUiHelper.showError(fragment, mensajeError);
            return false;
        }
    }
}

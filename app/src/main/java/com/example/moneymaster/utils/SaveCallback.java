package com.example.moneymaster.utils;


public interface SaveCallback {

    /** Llamado cuando la operación se completa sin errores. */
    void onSuccess();

    /**
     * Llamado cuando la operación falla.
     *
     * @param e excepción que causó el fallo
     */
    void onError(Exception e);
}

package com.example.moneymaster.utils;

/**
 * Callback genérico para operaciones de escritura en Room.
 *
 * <p>Room ejecuta las operaciones de BD en un hilo secundario
 * ({@code databaseWriteExecutor}). Este callback permite notificar
 * el resultado al hilo principal sin usar RxJava ni Coroutines.</p>
 *
 * <p>Uso típico en el Repository:</p>
 * <pre>
 *     AppDatabase.databaseWriteExecutor.execute(() -> {
 *         try {
 *             dao.insert(entity);
 *             callback.onSuccess();
 *         } catch (Exception e) {
 *             callback.onError(e);
 *         }
 *     });
 * </pre>
 *
 * <p><b>Importante:</b> los métodos del callback se invocan en el hilo de BD.
 * Usa {@code new Handler(Looper.getMainLooper()).post(...)} en la Activity/Fragment
 * para actualizar la UI.</p>
 */
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

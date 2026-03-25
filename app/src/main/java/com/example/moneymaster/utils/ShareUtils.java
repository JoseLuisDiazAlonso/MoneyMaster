package com.example.moneymaster.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ShareUtils - Utilidades para compartir contenido desde MoneyMaster.
 *
 * Proporciona métodos estáticos para compartir texto, imágenes y archivos
 * (Excel/PDF) a través del sistema de Intents de Android, con soporte
 * específico para WhatsApp.
 *
 * Uso básico:
 *   ShareUtils.shareText(context, "Mi resumen de gastos");
 *   ShareUtils.shareFile(context, archivoExcel, "application/vnd.ms-excel");
 *
 * Requisito: FileProvider configurado en AndroidManifest.xml con la
 * authority "com.example.moneymaster.fileprovider".
 *
 * Card #36 - Sprint 5: Compartir
 */
public class ShareUtils {

    // Authority del FileProvider declarado en AndroidManifest.xml
    private static final String FILE_PROVIDER_AUTHORITY =
            "com.example.moneymaster.fileprovider";

    // Package name oficial de WhatsApp
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";

    // Package name de WhatsApp Business (alternativa)
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    // Evitar instanciación — clase de utilidades pura
    private ShareUtils() {}

    // =========================================================================
    // shareText — Compartir texto plano
    // =========================================================================

    /**
     * Abre el selector de aplicaciones del sistema para compartir texto plano.
     *
     * Uso típico: resúmenes de movimientos, totales de gastos, balances de grupo.
     *
     * @param context  Contexto de la Activity o Fragment desde el que se llama.
     * @param text     Texto a compartir. No debe ser null ni vacío.
     * @param subject  Asunto opcional (visible en apps como Gmail). Puede ser null.
     * @param chooserTitle Título del selector de apps mostrado al usuario.
     */
    public static void shareText(Context context, String text,
                                 String subject, String chooserTitle) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(context, "No hay texto para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        if (subject != null && !subject.isEmpty()) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        // createChooser fuerza el selector aunque el usuario haya elegido app por defecto
        context.startActivity(Intent.createChooser(intent,
                chooserTitle != null ? chooserTitle : "Compartir vía"));
    }

    /**
     * Versión simplificada sin asunto ni título personalizado.
     *
     * @param context Contexto actual.
     * @param text    Texto a compartir.
     */
    public static void shareText(Context context, String text) {
        shareText(context, text, null, "Compartir vía");
    }

    // =========================================================================
    // shareImage — Compartir una sola imagen
    // =========================================================================

    /**
     * Comparte una imagen (foto de recibo, gráfico estadístico) a través del
     * selector de apps del sistema.
     *
     * El archivo DEBE estar dentro del directorio configurado en FileProvider
     * (generalmente getFilesDir() o getExternalFilesDir()). Imágenes fuera de
     * ese directorio causarán SecurityException.
     *
     * @param context   Contexto actual.
     * @param imageFile Archivo de imagen a compartir (jpg, png).
     * @param mimeType  MIME type: "image/jpeg" o "image/png".
     * @param subject   Asunto opcional. Puede ser null.
     * @param chooserTitle Título del selector. Puede ser null.
     */
    public static void shareImage(Context context, File imageFile,
                                  String mimeType, String subject,
                                  String chooserTitle) {
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(context, "El archivo de imagen no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri imageUri = getFileUri(context, imageFile);
        if (imageUri == null) return;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType != null ? mimeType : "image/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);

        if (subject != null && !subject.isEmpty()) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        // FLAG obligatorio para que la app receptora tenga permiso temporal de lectura
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent,
                chooserTitle != null ? chooserTitle : "Compartir imagen"));
    }

    /**
     * Versión simplificada para compartir un JPEG sin parámetros opcionales.
     *
     * @param context   Contexto actual.
     * @param imageFile Archivo de imagen a compartir.
     */
    public static void shareImage(Context context, File imageFile) {
        shareImage(context, imageFile, "image/jpeg", null, "Compartir imagen");
    }

    // =========================================================================
    // shareMultipleImages — Compartir varias imágenes
    // =========================================================================

    /**
     * Comparte una lista de imágenes en un solo Intent (ACTION_SEND_MULTIPLE).
     *
     * Útil para compartir todos los recibos de un período o un conjunto de
     * capturas de estadísticas. Si la lista tiene un solo elemento, delega
     * automáticamente a shareImage() para mayor compatibilidad.
     *
     * @param context    Contexto actual.
     * @param imageFiles Lista de archivos de imagen. No debe ser null ni vacía.
     * @param mimeType   MIME type común: "image/jpeg", "image/png" o "image/*".
     * @param chooserTitle Título del selector.
     */
    public static void shareMultipleImages(Context context, List<File> imageFiles,
                                           String mimeType, String chooserTitle) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            Toast.makeText(context, "No hay imágenes para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optimización: si solo hay una imagen, usar ACTION_SEND (mejor compatibilidad)
        if (imageFiles.size() == 1) {
            shareImage(context, imageFiles.get(0), mimeType, null, chooserTitle);
            return;
        }

        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : imageFiles) {
            if (file != null && file.exists()) {
                Uri uri = getFileUri(context, file);
                if (uri != null) {
                    uris.add(uri);
                }
            }
        }

        if (uris.isEmpty()) {
            Toast.makeText(context, "No se pudieron acceder a las imágenes", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType(mimeType != null ? mimeType : "image/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent,
                chooserTitle != null ? chooserTitle : "Compartir imágenes"));
    }

    /**
     * Versión simplificada para compartir varias imágenes JPEG.
     *
     * @param context    Contexto actual.
     * @param imageFiles Lista de archivos de imagen.
     */
    public static void shareMultipleImages(Context context, List<File> imageFiles) {
        shareMultipleImages(context, imageFiles, "image/jpeg", "Compartir imágenes");
    }

    // =========================================================================
    // shareFile — Compartir archivo (Excel, PDF)
    // =========================================================================

    /**
     * Comparte un archivo genérico (Excel .xlsx, PDF, CSV) a través del selector
     * de apps. El receptor necesita tener una app capaz de abrir ese tipo de archivo.
     *
     * MIME types comunes para MoneyMaster:
     *   - Excel:  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
     *   - PDF:    "application/pdf"
     *   - CSV:    "text/csv"
     *
     * @param context      Contexto actual.
     * @param file         Archivo a compartir.
     * @param mimeType     MIME type del archivo.
     * @param subject      Asunto opcional (visible en Gmail). Puede ser null.
     * @param extraText    Texto adicional que acompaña al archivo. Puede ser null.
     * @param chooserTitle Título del selector.
     */
    public static void shareFile(Context context, File file, String mimeType,
                                 String subject, String extraText,
                                 String chooserTitle) {
        if (file == null || !file.exists()) {
            Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri fileUri = getFileUri(context, file);
        if (fileUri == null) return;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType != null ? mimeType : "*/*");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        if (subject != null && !subject.isEmpty()) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        if (extraText != null && !extraText.isEmpty()) {
            intent.putExtra(Intent.EXTRA_TEXT, extraText);
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent,
                chooserTitle != null ? chooserTitle : "Compartir archivo"));
    }

    /**
     * Versión simplificada para compartir un archivo con MIME type y sin extras.
     *
     * @param context  Contexto actual.
     * @param file     Archivo a compartir.
     * @param mimeType MIME type del archivo.
     */
    public static void shareFile(Context context, File file, String mimeType) {
        shareFile(context, file, mimeType, null, null, "Compartir archivo");
    }

    // =========================================================================
    // shareViaWhatsApp — Compartir directamente en WhatsApp
    // =========================================================================

    /**
     * Intenta compartir texto directamente en WhatsApp, saltándose el selector.
     * Si WhatsApp no está instalado, cae en el selector genérico del sistema.
     *
     * Para compartir en WhatsApp Business, pasar useWhatsAppBusiness = true.
     *
     * @param context             Contexto actual.
     * @param text                Texto a compartir.
     * @param useWhatsAppBusiness true para intentar primero WhatsApp Business.
     */
    public static void shareTextViaWhatsApp(Context context, String text,
                                            boolean useWhatsAppBusiness) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(context, "No hay texto para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetPackage = useWhatsAppBusiness ? WHATSAPP_BUSINESS_PACKAGE : WHATSAPP_PACKAGE;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        if (isAppInstalled(context, targetPackage)) {
            intent.setPackage(targetPackage);
            context.startActivity(intent);
        } else {
            // WhatsApp no instalado → selector genérico
            Toast.makeText(context, "WhatsApp no está instalado. Elige otra aplicación.",
                    Toast.LENGTH_SHORT).show();
            context.startActivity(Intent.createChooser(intent, "Compartir vía"));
        }
    }

    /**
     * Versión simplificada: intenta WhatsApp normal; si no está instalado, usa selector.
     *
     * @param context Contexto actual.
     * @param text    Texto a compartir.
     */
    public static void shareTextViaWhatsApp(Context context, String text) {
        shareTextViaWhatsApp(context, text, false);
    }

    /**
     * Intenta compartir una imagen directamente en WhatsApp.
     * Si WhatsApp no está instalado, cae en el selector genérico.
     *
     * @param context             Contexto actual.
     * @param imageFile           Archivo de imagen a compartir.
     * @param captionText         Texto/pie de foto opcional. Puede ser null.
     * @param useWhatsAppBusiness true para intentar primero WhatsApp Business.
     */
    public static void shareImageViaWhatsApp(Context context, File imageFile,
                                             String captionText,
                                             boolean useWhatsAppBusiness) {
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(context, "El archivo de imagen no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri imageUri = getFileUri(context, imageFile);
        if (imageUri == null) return;

        String targetPackage = useWhatsAppBusiness ? WHATSAPP_BUSINESS_PACKAGE : WHATSAPP_PACKAGE;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (captionText != null && !captionText.isEmpty()) {
            intent.putExtra(Intent.EXTRA_TEXT, captionText);
        }

        if (isAppInstalled(context, targetPackage)) {
            intent.setPackage(targetPackage);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "WhatsApp no está instalado. Elige otra aplicación.",
                    Toast.LENGTH_SHORT).show();
            context.startActivity(Intent.createChooser(intent, "Compartir imagen"));
        }
    }

    /**
     * Versión simplificada para compartir imagen en WhatsApp sin pie de foto.
     *
     * @param context   Contexto actual.
     * @param imageFile Archivo de imagen.
     */
    public static void shareImageViaWhatsApp(Context context, File imageFile) {
        shareImageViaWhatsApp(context, imageFile, null, false);
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    /**
     * Convierte un File en un Uri seguro usando FileProvider.
     * Retorna null y muestra Toast si la conversión falla.
     *
     * FileProvider es obligatorio desde Android 7 (API 24) para compartir archivos
     * privados de la app. Sin él, se lanza FileUriExposedException.
     *
     * @param context Contexto actual.
     * @param file    Archivo a convertir.
     * @return Uri generada por FileProvider, o null si hay error.
     */
    private static Uri getFileUri(Context context, File file) {
        try {
            return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
        } catch (IllegalArgumentException e) {
            // El archivo no está dentro del directorio autorizado en file_provider_paths.xml
            Toast.makeText(context,
                    "Error: el archivo no está en una ruta autorizada",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si una aplicación está instalada en el dispositivo.
     *
     * @param context     Contexto actual.
     * @param packageName Package name a verificar (ej. "com.whatsapp").
     * @return true si la app está instalada y disponible.
     */
    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
package com.example.moneymaster.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Card #30 — Integración de Cámara y Galería
 *
 * Gestor centralizado para capturar fotos con la cámara del dispositivo
 * o seleccionarlas desde la galería. Genera nombres únicos, comprime
 * imágenes y guarda en directorio interno de la app.
 *
 * Uso básico:
 *   CameraGalleryManager manager = new CameraGalleryManager(activity, authority);
 *   manager.showSelectionDialog();
 */
public class CameraGalleryManager {

    private static final String TAG = "CameraGalleryManager";

    // Authority debe coincidir con AndroidManifest.xml
    public static final String FILE_PROVIDER_AUTHORITY =
            "com.example.moneymaster.fileprovider";
    // Nombre del subdirectorio dentro de getFilesDir()
    private static final String RECEIPTS_DIR = "receipts";

    // Códigos de permiso runtime
    public static final int REQUEST_CAMERA_PERMISSION  = 201;
    public static final int REQUEST_GALLERY_PERMISSION = 202;

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final Activity    mActivity;
    private final String      mAuthority;
    private Callback          mCallback;

    // URI del archivo temporal creado para la foto de cámara
    private Uri mCurrentPhotoUri;

    // Launchers inyectados desde la Activity/Fragment
    private ActivityResultLauncher<Intent> mCameraLauncher;
    private ActivityResultLauncher<Intent> mGalleryLauncher;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param activity  Activity que hospeda los launchers
     * @param authority Valor de android:authorities del FileProvider en el Manifest
     */
    public CameraGalleryManager(Activity activity, String authority) {
        this.mActivity  = activity;
        this.mAuthority = authority;
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    /** Registra el callback que recibirá los resultados. */
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    /** Inyecta el launcher de cámara registrado en la Activity. */
    public void setCameraLauncher(ActivityResultLauncher<Intent> launcher) {
        this.mCameraLauncher = launcher;
    }

    /** Inyecta el launcher de galería registrado en la Activity. */
    public void setGalleryLauncher(ActivityResultLauncher<Intent> launcher) {
        this.mGalleryLauncher = launcher;
    }

    // ── Diálogo selector ─────────────────────────────────────────────────────

    /**
     * Muestra el diálogo de selección Cámara / Galería.
     * Llama a este método desde el click del FAB o cualquier trigger.
     */
    public void showSelectionDialog() {
        new android.app.AlertDialog.Builder(mActivity)
                .setTitle("Adjuntar recibo")
                .setItems(new String[]{"📷  Tomar foto", "🖼️  Seleccionar de galería"},
                        (dialog, which) -> {
                            if (which == 0) {
                                launchCamera();
                            } else {
                                launchGallery();
                            }
                        })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── Cámara ────────────────────────────────────────────────────────────────

    /** Solicita permiso de cámara si es necesario, luego abre la cámara. */
    public void launchCamera() {
        if (hasCameraPermission()) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(
                    mActivity,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                mActivity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Abre la cámara del sistema.
     * Crea un archivo temporal con nombre único y lo expone vía FileProvider.
     */
    private void openCamera() {
        if (mCameraLauncher == null) {
            Log.e(TAG, "CameraLauncher no configurado. Llama a setCameraLauncher().");
            return;
        }
        try {
            File photoFile = createUniquePhotoFile();
            mCurrentPhotoUri = FileProvider.getUriForFile(
                    mActivity, mAuthority, photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
            // Necesario para que la cámara pueda escribir en la URI
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            mCameraLauncher.launch(intent);

        } catch (IOException e) {
            Log.e(TAG, "Error al crear archivo de foto", e);
            if (mCallback != null) mCallback.onError("No se pudo preparar la cámara.");
        }
    }

    // ── Galería ───────────────────────────────────────────────────────────────

    /**
     * Solicita permiso de lectura de medios si es necesario (API 33+ usa
     * READ_MEDIA_IMAGES; API < 33 usa READ_EXTERNAL_STORAGE), luego abre galería.
     */
    public void launchGallery() {
        if (hasGalleryPermission()) {
            openGallery();
        } else {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(
                    mActivity,
                    new String[]{permission},
                    REQUEST_GALLERY_PERMISSION);
        }
    }

    private boolean hasGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    mActivity, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(
                    mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void openGallery() {
        if (mGalleryLauncher == null) {
            Log.e(TAG, "GalleryLauncher no configurado. Llama a setGalleryLauncher().");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        mGalleryLauncher.launch(intent);
    }

    // ── Gestión de archivos ───────────────────────────────────────────────────

    /**
     * Crea un archivo vacío con nombre único en el directorio interno de la app.
     * Formato: receipt_YYYYMMDD_HHmmss_<random4>.jpg
     *
     * Ejemplo: receipt_20250315_143022_7f3a.jpg
     *
     * @return File apuntando al archivo creado (vacío, la cámara lo rellenará)
     * @throws IOException si no se puede crear el archivo o el directorio
     */
    public File createUniquePhotoFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        // 4 chars hex aleatorios para evitar colisiones en la misma app
        String random = Integer.toHexString((int)(Math.random() * 0xFFFF));
        String fileName = "receipt_" + timestamp + "_" + random + ".jpg";

        File receiptsDir = getReceiptsDirectory();
        File photoFile   = new File(receiptsDir, fileName);

        // createNewFile() crea el archivo vacío en disco
        if (!photoFile.createNewFile()) {
            throw new IOException("El archivo ya existe: " + photoFile.getAbsolutePath());
        }
        Log.d(TAG, "Archivo de foto creado: " + photoFile.getAbsolutePath());
        return photoFile;
    }

    /**
     * Devuelve (creando si no existe) el directorio interno para recibos.
     * Ruta: /data/data/<packageId>/files/receipts/
     */
    public File getReceiptsDirectory() throws IOException {
        File dir = new File(mActivity.getFilesDir(), RECEIPTS_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio: " + dir.getAbsolutePath());
        }
        return dir;
    }

    /**
     * Elimina un archivo de recibo del directorio interno.
     *
     * @param filePath Ruta absoluta del archivo a eliminar
     * @return true si se eliminó correctamente
     */
    public static boolean deletePhoto(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
        File file = new File(filePath);
        boolean deleted = file.delete();
        Log.d(TAG, "Foto eliminada (" + deleted + "): " + filePath);
        return deleted;
    }

    // ── Procesamiento de resultados ───────────────────────────────────────────

    /**
     * Llama desde la Activity cuando el resultado de la cámara llega.
     * La foto ya está guardada en mCurrentPhotoUri (EXTRA_OUTPUT).
     *
     * @param resultOk true si Activity.RESULT_OK
     */
    public void handleCameraResult(boolean resultOk) {
        if (resultOk && mCurrentPhotoUri != null) {
            // La foto está ya escrita en el archivo. Comprimimos en background.
            String absolutePath = getAbsolutePathFromUri(mCurrentPhotoUri);
            compressAndDeliver(absolutePath);
        } else {
            Log.d(TAG, "Cámara cancelada o fallo");
            // Si el usuario canceló, borramos el archivo temporal vacío
            if (mCurrentPhotoUri != null) {
                String path = getAbsolutePathFromUri(mCurrentPhotoUri);
                if (path != null) new File(path).delete();
            }
            mCurrentPhotoUri = null;
        }
    }

    /**
     * Llama desde la Activity cuando el resultado de galería llega.
     *
     * @param resultOk true si Activity.RESULT_OK
     * @param data     Intent con getData() = URI de la imagen seleccionada
     */
    public void handleGalleryResult(boolean resultOk, Intent data) {
        if (resultOk && data != null && data.getData() != null) {
            Uri sourceUri = data.getData();
            // Copiamos la imagen de galería al directorio interno y comprimimos
            copyFromGalleryAndDeliver(sourceUri);
        } else {
            Log.d(TAG, "Galería cancelada o fallo");
        }
    }

    /**
     * Maneja la respuesta de requestPermissions.
     * Llama desde onRequestPermissionsResult de la Activity.
     */
    public void handlePermissionResult(int requestCode,
                                       String[] permissions,
                                       int[] grantResults) {
        boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (granted) {
                openCamera();
            } else {
                showPermissionDeniedMessage("cámara");
            }
        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (granted) {
                openGallery();
            } else {
                showPermissionDeniedMessage("galería");
            }
        }
    }

    // ── Compresión ────────────────────────────────────────────────────────────

    /**
     * Comprime el archivo en un hilo de fondo y notifica al callback en el hilo principal.
     * Reduce la imagen a máx. 1024px en el lado mayor y calidad JPEG 80%.
     */
    private void compressAndDeliver(String absolutePath) {
        if (absolutePath == null) {
            if (mCallback != null) mCallback.onError("No se pudo determinar la ruta del archivo.");
            return;
        }
        new Thread(() -> {
            String compressedPath = ImageCompressor.compress(absolutePath, 1024, 80);
            mActivity.runOnUiThread(() -> {
                if (compressedPath != null && mCallback != null) {
                    mCallback.onImageReady(compressedPath);
                } else if (mCallback != null) {
                    mCallback.onError("Error al procesar la imagen.");
                }
            });
        }).start();
    }

    /**
     * Copia una URI de galería al directorio interno de la app y comprime.
     */
    private void copyFromGalleryAndDeliver(Uri sourceUri) {
        new Thread(() -> {
            try {
                File destFile = createUniquePhotoFile();
                ImageCompressor.copyUriToFile(mActivity, sourceUri, destFile);
                String compressedPath = ImageCompressor.compress(
                        destFile.getAbsolutePath(), 1024, 80);
                mActivity.runOnUiThread(() -> {
                    if (compressedPath != null && mCallback != null) {
                        mCallback.onImageReady(compressedPath);
                    } else if (mCallback != null) {
                        mCallback.onError("Error al copiar imagen de galería.");
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Error copiando de galería", e);
                mActivity.runOnUiThread(() -> {
                    if (mCallback != null) mCallback.onError("Error al leer la imagen.");
                });
            }
        }).start();
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private String getAbsolutePathFromUri(Uri uri) {
        // Para FileProvider URIs (file://... o content://...) usamos el path del archivo
        // que creamos nosotros mismos, así que podemos extraerlo del last path segment
        // Alternativa más robusta: guardamos el File al crear y lo recuperamos
        if (uri == null) return null;
        // El archivo fue creado por nosotros en getReceiptsDirectory()
        // URI content://...fileprovider/receipt_photos/receipt_XXXX.jpg
        // El path real es getFilesDir()/receipts/receipt_XXXX.jpg
        String lastSegment = uri.getLastPathSegment(); // "receipt_photos/receipt_XXXX.jpg"
        if (lastSegment == null) return null;
        String fileName = lastSegment.contains("/")
                ? lastSegment.substring(lastSegment.lastIndexOf('/') + 1)
                : lastSegment;
        return new File(mActivity.getFilesDir(), RECEIPTS_DIR + "/" + fileName)
                .getAbsolutePath();
    }

    private void showPermissionDeniedMessage(String type) {
        mActivity.runOnUiThread(() ->
                android.widget.Toast.makeText(
                        mActivity,
                        "Permiso de " + type + " denegado. Actívalo en Ajustes.",
                        android.widget.Toast.LENGTH_LONG).show()
        );
    }

    /** Devuelve la URI del archivo temporal de la última foto de cámara. */
    public Uri getCurrentPhotoUri() {
        return mCurrentPhotoUri;
    }

    // ── Interfaz Callback ─────────────────────────────────────────────────────

    /**
     * Interfaz que la Activity/Fragment implementa para recibir resultados.
     */
    public interface Callback {
        /**
         * Llamado cuando la imagen está lista y comprimida en el almacenamiento interno.
         *
         * @param absolutePath Ruta absoluta del archivo .jpg resultante.
         *                     Guarda esta ruta en la entidad FotoRecibo.
         */
        void onImageReady(String absolutePath);

        /** Llamado cuando ocurre un error durante la captura o procesamiento. */
        void onError(String errorMessage);
    }
}

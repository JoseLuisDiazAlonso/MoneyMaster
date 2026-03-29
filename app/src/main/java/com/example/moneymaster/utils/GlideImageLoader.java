package com.example.moneymaster.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.moneymaster.R;

import java.io.File;

/**
 * Card #63 – Optimización de imágenes
 *
 * Helper centralizado para cargar imágenes con Glide aplicando:
 *  - Lazy loading automático
 *  - Thumbnails separados (150×150) en listas
 *  - Caché en disco para evitar re-decodificaciones
 *  - Liberación de recursos al destruir vistas
 */
public class GlideImageLoader {

    // ─────────────────────────────────────────────────────────────────────────
    // Carga de thumbnail (listas / RecyclerView)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Carga el thumbnail 150×150 de la imagen indicada por {@code imagePath}.
     * Primero intenta la versión "thumb_*"; si no existe, carga la original
     * escalada por Glide.
     *
     * @param context   Contexto (Fragment/Activity).
     * @param imagePath Ruta de la imagen original.
     * @param imageView ImageView de destino.
     */
    public static void loadThumbnail(Context context, String imagePath, ImageView imageView) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_photo_placeholder);
            return;
        }

        // Preferir el thumbnail pre-generado
        String thumbPath = ImageOptimizer.buildThumbnailPath(imagePath);
        File thumbFile = new File(thumbPath);
        String loadPath = thumbFile.exists() ? thumbPath : imagePath;

        Glide.with(context)
                .load(new File(loadPath))
                .apply(thumbnailOptions())
                .into(imageView);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carga de imagen completa (detalle / visor)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Carga la imagen a tamaño completo usando el thumbnail como placeholder
     * mientras se decodifica la versión completa (progresivo).
     *
     * @param context   Contexto.
     * @param imagePath Ruta de la imagen original.
     * @param imageView ImageView de destino.
     */
    public static void loadFullImage(Context context, String imagePath, ImageView imageView) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_photo_placeholder);
            return;
        }

        File imageFile = new File(imagePath);

        // Thumbnail como placeholder de baja calidad durante la carga
        String thumbPath = ImageOptimizer.buildThumbnailPath(imagePath);
        File thumbFile = new File(thumbPath);

        com.bumptech.glide.RequestBuilder<android.graphics.drawable.Drawable> fullRequest =
                Glide.with(context)
                        .load(imageFile)
                        .apply(fullImageOptions());

        if (thumbFile.exists()) {
            fullRequest
                    .thumbnail(
                            Glide.with(context)
                                    .load(thumbFile)
                                    .apply(thumbnailOptions()))
                    .into(imageView);
        } else {
            fullRequest.into(imageView);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Limpieza
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cancela las peticiones pendientes y libera la referencia del ImageView.
     * Llamar desde {@code onDestroyView()} o cuando el ítem del adapter se recicle.
     *
     * @param context   Contexto.
     * @param imageView ImageView cuyas peticiones se quieren cancelar.
     */
    public static void clear(Context context, ImageView imageView) {
        Glide.with(context).clear(imageView);
    }

    /**
     * Limpia la caché en disco de Glide (ejecutar en hilo secundario).
     */
    public static void clearDiskCache(Context context) {
        Glide.get(context).clearDiskCache();
    }

    /**
     * Limpia la caché en memoria de Glide (ejecutar en el hilo principal).
     */
    public static void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RequestOptions reutilizables
    // ─────────────────────────────────────────────────────────────────────────

    private static RequestOptions thumbnailOptions() {
        return new RequestOptions()
                .override(ImageOptimizer.THUMBNAIL_SIZE_PX, ImageOptimizer.THUMBNAIL_SIZE_PX)
                .centerCrop()
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE); // Cachea el thumbnail transformado
    }

    private static RequestOptions fullImageOptions() {
        return new RequestOptions()
                .fitCenter()
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.DATA); // Cachea los bytes originales
    }
}

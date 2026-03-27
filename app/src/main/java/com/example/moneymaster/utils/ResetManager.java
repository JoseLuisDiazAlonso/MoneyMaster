package com.example.moneymaster.utils;

import android.content.Context;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el reseteo completo de la app al estado inicial de fábrica.
 *
 * Secuencia de operaciones (todas en hilo de fondo):
 *   1. clearAllTables()  — vacía todas las tablas Room de golpe
 *   2. eliminarFotos()   — borra recursivamente la carpeta de fotos internas
 *   3. repoblarCategorias() — inserta las categorías predefinidas del sistema
 *
 * IMPORTANTE: llamar siempre desde databaseWriteExecutor, nunca desde el hilo principal.
 * Después de llamar a reset() es responsabilidad del llamador cerrar la sesión y
 * navegar a LoginActivity.
 */
public class ResetManager {

    private static final String FOTOS_DIR = "fotos_recibos";

    /**
     * Ejecuta el reset completo de la app.
     * Borra datos, fotos y repuebla categorías.
     *
     * @param context Contexto de la aplicación.
     * @return true si todo fue exitoso, false si hubo algún error.
     */
    public static boolean resetApp(Context context) {
        try {
            AppDatabase db = AppDatabase.getDatabase(context);

            // ── 1. Borrar todas las tablas ────────────────────────────────
            db.clearAllTables();

            // ── 2. Eliminar fotos de almacenamiento interno ───────────────
            eliminarFotos(context);

            // ── 3. Repoblar categorías predefinidas ───────────────────────
            repoblarCategorias(db);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Eliminar fotos ────────────────────────────────────────────────────────

    /**
     * Elimina recursivamente la carpeta de fotos de recibos del almacenamiento interno.
     * Si la carpeta no existe, no hace nada.
     */
    private static void eliminarFotos(Context context) {
        File fotosDir = new File(context.getFilesDir(), FOTOS_DIR);
        if (fotosDir.exists()) {
            deleteRecursive(fotosDir);
        }
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    // ── Repoblar categorías ───────────────────────────────────────────────────

    /**
     * Inserta las categorías predefinidas del sistema en las tablas
     * categorias_gasto y categorias_ingreso.
     * Equivalente al seedCallback de AppDatabase pero invocable en cualquier momento.
     */
    private static void repoblarCategorias(AppDatabase db) {

        // ── Categorías de GASTO ──────────────────────────────────────────────
        List<CategoriaGasto> gastos = new ArrayList<>();
        gastos.add(CategoriaGasto.crearSistema("Alimentación",  "ic_restaurant",    "#FF5722"));
        gastos.add(CategoriaGasto.crearSistema("Transporte",    "ic_directions_car","#2196F3"));
        gastos.add(CategoriaGasto.crearSistema("Vivienda",      "ic_home",          "#4CAF50"));
        gastos.add(CategoriaGasto.crearSistema("Salud",         "ic_local_hospital","#F44336"));
        gastos.add(CategoriaGasto.crearSistema("Educación",     "ic_school",        "#9C27B0"));
        gastos.add(CategoriaGasto.crearSistema("Ocio",          "ic_sports_esports","#FF9800"));
        gastos.add(CategoriaGasto.crearSistema("Ropa",          "ic_checkroom",     "#E91E63"));
        gastos.add(CategoriaGasto.crearSistema("Tecnología",    "ic_devices",       "#00BCD4"));
        gastos.add(CategoriaGasto.crearSistema("Viajes",        "ic_flight",        "#3F51B5"));
        gastos.add(CategoriaGasto.crearSistema("Mascotas",      "ic_pets",          "#795548"));
        gastos.add(CategoriaGasto.crearSistema("Restaurantes",  "ic_local_dining",  "#FF7043"));
        gastos.add(CategoriaGasto.crearSistema("Supermercado",  "ic_shopping_cart", "#66BB6A"));
        gastos.add(CategoriaGasto.crearSistema("Seguros",       "ic_security",      "#607D8B"));
        gastos.add(CategoriaGasto.crearSistema("Suscripciones", "ic_subscriptions", "#AB47BC"));
        gastos.add(CategoriaGasto.crearSistema("Otros gastos",  "ic_more_horiz",    "#9E9E9E"));
        db.categoriaGastoDao().insertarVarias(gastos);

        // ── Categorías de INGRESO ────────────────────────────────────────────
        List<CategoriaIngreso> ingresos = new ArrayList<>();
        ingresos.add(CategoriaIngreso.crearSistema("Salario",        "ic_work",          "#4CAF50"));
        ingresos.add(CategoriaIngreso.crearSistema("Freelance",      "ic_laptop",        "#2196F3"));
        ingresos.add(CategoriaIngreso.crearSistema("Inversiones",    "ic_trending_up",   "#FF9800"));
        ingresos.add(CategoriaIngreso.crearSistema("Alquiler",       "ic_home",          "#9C27B0"));
        ingresos.add(CategoriaIngreso.crearSistema("Regalo",         "ic_card_giftcard", "#E91E63"));
        ingresos.add(CategoriaIngreso.crearSistema("Reembolso",      "ic_replay",        "#00BCD4"));
        ingresos.add(CategoriaIngreso.crearSistema("Venta",          "ic_sell",          "#FF5722"));
        ingresos.add(CategoriaIngreso.crearSistema("Otros ingresos", "ic_more_horiz",    "#9E9E9E"));
        db.categoriaIngresoDao().insertarVarias(ingresos);
    }

    // Constructor privado: clase de utilidades
    private ResetManager() {}
}
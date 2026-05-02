package com.example.moneymaster.utils;

import android.content.Context;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResetManager {

    private static final String FOTOS_DIR = "fotos_recibos";

    public static boolean resetApp(Context context) {
        try {
            AppDatabase db = AppDatabase.getDatabase(context);
            db.clearAllTables();
            eliminarFotos(context);
            repoblarCategorias(db);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void eliminarFotos(Context context) {
        File fotosDir = new File(context.getFilesDir(), FOTOS_DIR);
        if (fotosDir.exists()) deleteRecursive(fotosDir);
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        file.delete();
    }

    /**
     * FIX: los nombres de categoría son ahora claves (ej. "cat_alimentacion")
     * que se resuelven a texto traducido en CategoryAdapter según el idioma del dispositivo.
     */
    private static void repoblarCategorias(AppDatabase db) {

        List<CategoriaGasto> gastos = new ArrayList<>();
        gastos.add(CategoriaGasto.crearSistema("cat_alimentacion",  "ic_restaurant",    "#FF5722"));
        gastos.add(CategoriaGasto.crearSistema("cat_transporte",    "ic_directions_car","#2196F3"));
        gastos.add(CategoriaGasto.crearSistema("cat_vivienda",      "ic_home",          "#4CAF50"));
        gastos.add(CategoriaGasto.crearSistema("cat_salud",         "ic_local_hospital","#F44336"));
        gastos.add(CategoriaGasto.crearSistema("cat_educacion",     "ic_school",        "#9C27B0"));
        gastos.add(CategoriaGasto.crearSistema("cat_ocio",          "ic_sports_esports","#FF9800"));
        gastos.add(CategoriaGasto.crearSistema("cat_ropa",          "ic_checkroom",     "#E91E63"));
        gastos.add(CategoriaGasto.crearSistema("cat_tecnologia",    "ic_devices",       "#00BCD4"));
        gastos.add(CategoriaGasto.crearSistema("cat_viajes",        "ic_flight",        "#3F51B5"));
        gastos.add(CategoriaGasto.crearSistema("cat_mascotas",      "ic_pets",          "#795548"));
        gastos.add(CategoriaGasto.crearSistema("cat_restaurantes",  "ic_local_dining",  "#FF7043"));
        gastos.add(CategoriaGasto.crearSistema("cat_supermercado",  "ic_shopping_cart", "#66BB6A"));
        gastos.add(CategoriaGasto.crearSistema("cat_seguros",       "ic_security",      "#607D8B"));
        gastos.add(CategoriaGasto.crearSistema("cat_suscripciones", "ic_subscriptions", "#AB47BC"));
        gastos.add(CategoriaGasto.crearSistema("cat_otros_gastos",  "ic_more_horiz",    "#9E9E9E"));
        db.categoriaGastoDao().insertarVarias(gastos);

        List<CategoriaIngreso> ingresos = new ArrayList<>();
        ingresos.add(CategoriaIngreso.crearSistema("cat_salario",       "ic_work",          "#4CAF50"));
        ingresos.add(CategoriaIngreso.crearSistema("cat_freelance",     "ic_laptop",        "#2196F3"));
        ingresos.add(CategoriaIngreso.crearSistema("cat_inversiones",   "ic_trending_up",   "#FF9800"));
        ingresos.add(CategoriaIngreso.crearSistema("cat_alquiler",      "ic_home",          "#9C27B0"));
        ingresos.add(CategoriaIngreso.crearSistema("cat_regalo",        "ic_card_giftcard", "#E91E63"));
        ingresos.add(CategoriaIngreso.crearSistema("cat_reembolso",     "ic_replay",        "#00BCD4"));
        ingresos.add(CategoriaIngreso.crearSistema("cat_venta",         "ic_sell",          "#FF5722"));
        ingresos.add(CategoriaIngreso.crearSistema("cat_otros_ingresos","ic_more_horiz",    "#9E9E9E"));
        db.categoriaIngresoDao().insertarVarias(ingresos);
    }

    private ResetManager() {}
}
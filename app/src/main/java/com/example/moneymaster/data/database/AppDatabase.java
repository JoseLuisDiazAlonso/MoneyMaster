package com.example.moneymaster.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.moneymaster.data.dao.*;
import com.example.moneymaster.data.model.*;

@Database(
        entities = {
                GastoPersonal.class,
                IngresoPersonal.class,
                CategoriaGasto.class,
                CategoriaIngreso.class,
                Grupo.class,
                GastoGrupo.class,
                User.class,
                PreferenciasUsuario.class,
                BalanceGrupo.class,
                MiembroGrupo.class
        },
        version = 7,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public abstract GastoPersonalDao       gastoPersonalDao();
    public abstract IngresoPersonalDao     ingresoPersonalDao();
    public abstract CategoriaGastoDao      categoriaGastoDao();
    public abstract CategoriaIngresoDao    categoriaIngresoDao();
    public abstract GrupoDao               grupoDao();
    public abstract GastoGrupoDao          gastoGrupoDao();
    public abstract UserDao                usuarioDao();
    public abstract PreferenciasUsuarioDao preferenciasUsuarioDao();
    public abstract BalanceGrupoDao        balanceGrupoDao();
    public abstract MiembroGrupoDao        miembroGrupoDao();

    // FIX: columna correcta es "es_sistema" (no "es_predefinida")
    private static final RoomDatabase.Callback migrarCategoriasCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    databaseWriteExecutor.execute(() -> migrarNombresCategoriasAClaves(db));
                }
            };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "moneymaster_database"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(migrarCategoriasCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static synchronized void resetInstance() {
        INSTANCE = null;
    }

    private static void migrarNombresCategoriasAClaves(SupportSQLiteDatabase db) {
        // FIX: usar "es_sistema = 1" en lugar de "es_predefinida = 1"
        String[][] gastosMapping = {
                {"Alimentación",  "cat_alimentacion"},
                {"Transporte",    "cat_transporte"},
                {"Vivienda",      "cat_vivienda"},
                {"Salud",         "cat_salud"},
                {"Educación",     "cat_educacion"},
                {"Ocio",          "cat_ocio"},
                {"Ropa",          "cat_ropa"},
                {"Tecnología",    "cat_tecnologia"},
                {"Viajes",        "cat_viajes"},
                {"Mascotas",      "cat_mascotas"},
                {"Restaurantes",  "cat_restaurantes"},
                {"Supermercado",  "cat_supermercado"},
                {"Seguros",       "cat_seguros"},
                {"Suscripciones", "cat_suscripciones"},
                {"Otros gastos",  "cat_otros_gastos"},
                {"Otros",         "cat_otros"},
        };

        for (String[] entry : gastosMapping) {
            db.execSQL(
                    "UPDATE categorias_gasto SET nombre = ? WHERE nombre = ? AND es_sistema = 1",
                    new Object[]{entry[1], entry[0]}
            );
        }

        String[][] ingresosMapping = {
                {"Salario",        "cat_salario"},
                {"Freelance",      "cat_freelance"},
                {"Inversiones",    "cat_inversiones"},
                {"Alquiler",       "cat_alquiler"},
                {"Regalo",         "cat_regalo"},
                {"Reembolso",      "cat_reembolso"},
                {"Venta",          "cat_venta"},
                {"Otros ingresos", "cat_otros_ingresos"},
                {"Otros",          "cat_otros"},
        };

        for (String[] entry : ingresosMapping) {
            db.execSQL(
                    "UPDATE categorias_ingreso SET nombre = ? WHERE nombre = ? AND es_sistema = 1",
                    new Object[]{entry[1], entry[0]}
            );
        }
    }
}
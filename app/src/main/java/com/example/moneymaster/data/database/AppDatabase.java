package com.example.moneymaster.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.moneymaster.data.dao.BalanceGrupoDao;
import com.example.moneymaster.data.dao.CategoriaGastoDao;
import com.example.moneymaster.data.dao.CategoriaIngresoDao;
import com.example.moneymaster.data.dao.FotoReciboDao;
import com.example.moneymaster.data.dao.GastoGrupoDao;
import com.example.moneymaster.data.dao.GastoPersonalDao;
import com.example.moneymaster.data.dao.GrupoDao;
import com.example.moneymaster.data.dao.IngresoPersonalDao;
import com.example.moneymaster.data.dao.MiembroGrupoDao;
import com.example.moneymaster.data.dao.PreferenciasUsuarioDao;
import com.example.moneymaster.data.dao.UserDao;

import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.data.model.PreferenciasUsuario;
import com.example.moneymaster.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Punto de entrada único a la base de datos Room de MoneyMaster.
 */
@Database(
        entities = {
                User.class,                 // Entidad existente del Sprint anterior
                PreferenciasUsuario.class,
                CategoriaGasto.class,
                CategoriaIngreso.class,
                GastoPersonal.class,
                IngresoPersonal.class,
                Grupo.class,
                MiembroGrupo.class,
                GastoGrupo.class,
                FotoRecibo.class,
                BalanceGrupoDao.class
        },
        version = 2,            // Subimos de 1 a 2 porque agregamos tablas nuevas
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // SINGLETON


    private static volatile AppDatabase INSTANCE;

    /**
     * Pool de 4 hilos para operaciones de escritura en background.
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    /**
     * Obtiene la instancia singleton de la base de datos.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "moneymaster.db"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    // DAOs — Room genera la implementación automáticamente en compilación


    // ---- Existente del Sprint anterior ----
    public abstract UserDao userDao();

    // ---- Nuevos de Card #13 ----
    public abstract PreferenciasUsuarioDao preferenciasUsuarioDao();
    public abstract CategoriaGastoDao categoriaGastoDao();
    public abstract CategoriaIngresoDao categoriaIngresoDao();
    public abstract GastoPersonalDao gastoPersonalDao();
    public abstract IngresoPersonalDao ingresoPersonalDao();
    public abstract GrupoDao grupoDao();
    public abstract MiembroGrupoDao miembroGrupoDao();
    public abstract GastoGrupoDao gastoGrupoDao();
    public abstract FotoReciboDao fotoReciboDao();
    public abstract BalanceGrupoDao balanceGrupoDao();


    // CALLBACK — Seed de datos al crear la BD por primera vez


    /**
     * Se ejecuta UNA SOLA VEZ cuando la base de datos se crea desde cero.
     */
    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null) {
                    insertarCategoriasSistema(INSTANCE);
                }
            });
        }
    };

    /**
     * Inserta las categorías del sistema para gastos e ingresos.
     */
    private static void insertarCategoriasSistema(AppDatabase db) {


        // Categorías de GASTO del sistema
        // Íconos: archivos .xml en res/drawable/ → fonts.google.com/icons

        if (db.categoriaGastoDao().countSistema() == 0) {
            List<CategoriaGasto> gastoCats = new ArrayList<>();
            gastoCats.add(CategoriaGasto.crearSistema("Alimentación",    "ic_restaurant",     "#F44336"));
            gastoCats.add(CategoriaGasto.crearSistema("Transporte",      "ic_directions_car", "#2196F3"));
            gastoCats.add(CategoriaGasto.crearSistema("Entretenimiento", "ic_movie",          "#9C27B0"));
            gastoCats.add(CategoriaGasto.crearSistema("Salud",           "ic_local_hospital", "#4CAF50"));
            gastoCats.add(CategoriaGasto.crearSistema("Ropa",            "ic_checkroom",      "#FF9800"));
            gastoCats.add(CategoriaGasto.crearSistema("Casa",            "ic_home",           "#795548"));
            gastoCats.add(CategoriaGasto.crearSistema("Educación",       "ic_school",         "#3F51B5"));
            gastoCats.add(CategoriaGasto.crearSistema("Suscripciones",   "ic_subscriptions",  "#009688"));
            gastoCats.add(CategoriaGasto.crearSistema("Viajes",          "ic_flight",         "#00BCD4"));
            gastoCats.add(CategoriaGasto.crearSistema("Otros",           "ic_category",       "#607D8B"));
            db.categoriaGastoDao().insertCategorias(gastoCats);
        }


        // Categorías de INGRESO del sistema

        if (db.categoriaIngresoDao().countSistema() == 0) {
            List<CategoriaIngreso> ingresoCats = new ArrayList<>();
            ingresoCats.add(CategoriaIngreso.crearSistema("Salario",     "ic_work",           "#4CAF50"));
            ingresoCats.add(CategoriaIngreso.crearSistema("Freelance",   "ic_laptop",         "#2196F3"));
            ingresoCats.add(CategoriaIngreso.crearSistema("Inversiones", "ic_trending_up",    "#FF9800"));
            ingresoCats.add(CategoriaIngreso.crearSistema("Regalo",      "ic_card_giftcard",  "#E91E63"));
            ingresoCats.add(CategoriaIngreso.crearSistema("Renta",       "ic_apartment",      "#795548"));
            ingresoCats.add(CategoriaIngreso.crearSistema("Bono",        "ic_star",           "#FFC107"));
            ingresoCats.add(CategoriaIngreso.crearSistema("Venta",       "ic_sell",           "#9C27B0"));
            ingresoCats.add(CategoriaIngreso.crearSistema("Otros",       "ic_attach_money",   "#607D8B"));
            db.categoriaIngresoDao().insertCategorias(ingresoCats);
        }
    }
}
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
import com.example.moneymaster.data.model.BalanceGrupo;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton de la base de datos Room.
 *
 * Versión 2: esquema completo con las 11 entidades.
 * El Callback de creación siembra las categorías predefinidas.
 */
@Database(
        entities = {
                User.class,
                PreferenciasUsuario.class,
                CategoriaGasto.class,
                CategoriaIngreso.class,
                GastoPersonal.class,
                IngresoPersonal.class,
                Grupo.class,
                MiembroGrupo.class,
                GastoGrupo.class,
                FotoRecibo.class,
                BalanceGrupo.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase getInstance(Context context) {
        return getDatabase(context);
    }

    // ─── DAOs ─────────────────────────────────────────────────────────────────

    public abstract UserDao userDao();
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

    // ─── Executor compartido por todos los Repositories ───────────────────────

    /** Pool de 4 hilos para escrituras en la BD sin bloquear el hilo principal. */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    // ─── Singleton con Double-Checked Locking ─────────────────────────────────

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "moneymaster_db")
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ─── Seed: categorías predefinidas ────────────────────────────────────────

    /**
     * Se ejecuta una sola vez al crear la BD.
     * Inserta las categorías de sistema para gastos e ingresos.
     */
    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE == null) return;

                CategoriaGastoDao gastoDao     = INSTANCE.categoriaGastoDao();
                CategoriaIngresoDao ingresoDao = INSTANCE.categoriaIngresoDao();

                // Evita duplicados si el callback se llamara más de una vez
                if (gastoDao.countCategoriasDelSistema() > 0) return;

                // ── Categorías de GASTO ──────────────────────────────────────
                java.util.List<CategoriaGasto> categoriasGasto = new java.util.ArrayList<>();
                categoriasGasto.add(CategoriaGasto.crearSistema("Alimentación",   "ic_restaurant",    "#FF5722"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Transporte",     "ic_directions_car","#2196F3"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Vivienda",       "ic_home",          "#4CAF50"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Salud",          "ic_local_hospital","#F44336"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Educación",      "ic_school",        "#9C27B0"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Ocio",           "ic_sports_esports","#FF9800"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Ropa",           "ic_checkroom",     "#E91E63"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Tecnología",     "ic_devices",       "#00BCD4"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Viajes",         "ic_flight",        "#3F51B5"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Mascotas",       "ic_pets",          "#795548"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Restaurantes",   "ic_local_dining",  "#FF7043"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Supermercado",   "ic_shopping_cart", "#66BB6A"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Seguros",        "ic_security",      "#607D8B"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Suscripciones",  "ic_subscriptions", "#AB47BC"));
                categoriasGasto.add(CategoriaGasto.crearSistema("Otros gastos",   "ic_more_horiz",    "#9E9E9E"));
                gastoDao.insertarVarias(categoriasGasto);

                // ── Categorías de INGRESO ────────────────────────────────────
                java.util.List<CategoriaIngreso> categoriasIngreso = new java.util.ArrayList<>();
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Salario",        "ic_work",          "#4CAF50"));
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Freelance",      "ic_laptop",        "#2196F3"));
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Inversiones",    "ic_trending_up",   "#FF9800"));
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Alquiler",       "ic_home",          "#9C27B0"));
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Regalo",         "ic_card_giftcard", "#E91E63"));
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Reembolso",      "ic_replay",        "#00BCD4"));
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Venta",          "ic_sell",          "#FF5722"));
                categoriasIngreso.add(CategoriaIngreso.crearSistema("Otros ingresos", "ic_more_horiz",    "#9E9E9E"));
                ingresoDao.insertarVarias(categoriasIngreso);
            });
        }
    };

}



package com.example.moneymaster.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

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
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public abstract GastoPersonalDao    gastoPersonalDao();
    public abstract IngresoPersonalDao  ingresoPersonalDao();
    public abstract CategoriaGastoDao   categoriaGastoDao();
    public abstract CategoriaIngresoDao categoriaIngresoDao();
    public abstract GrupoDao            grupoDao();
    public abstract GastoGrupoDao       gastoGrupoDao();
    public abstract UserDao             usuarioDao();
    public abstract PreferenciasUsuarioDao preferenciasUsuarioDao();
    public abstract BalanceGrupoDao     balanceGrupoDao();
    public abstract MiembroGrupoDao     miembroGrupoDao();

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
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static synchronized void resetInstance() {
        INSTANCE = null;
    }
}
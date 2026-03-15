package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.PreferenciasUsuario;

@Dao
public interface PreferenciasUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(PreferenciasUsuario prefs);

    @Update
    void actualizar(PreferenciasUsuario prefs);

    /** Preferencias del usuario en sesión. */
    @Query("SELECT * FROM preferencias_usuario WHERE usuarioId = :usuarioId LIMIT 1")
    LiveData<PreferenciasUsuario> getPreferencias(long usuarioId);

    /** Versión síncrona para leer la moneda/tema al arrancar la app. */
    @Query("SELECT * FROM preferencias_usuario WHERE usuarioId = :usuarioId LIMIT 1")
    PreferenciasUsuario getPreferenciasSync(long usuarioId);
}
package com.example.moneymaster.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "preferencias_usuario",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "usuarioId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = { @Index(value = "usuarioId", unique = true) }
)
public class PreferenciasUsuario {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /** FK → users.id. Propietario de estas preferencias. */
    @ColumnInfo(name = "usuarioId")
    public int usuarioId;

    /** Código de moneda ISO 4217. Ej: "MXN", "USD", "EUR". */
    @ColumnInfo(name = "moneda", defaultValue = "MXN")
    public String moneda = "MXN";

    /** "light" o "dark". */
    @ColumnInfo(name = "tema", defaultValue = "light")
    public String tema = "light";

    /** Idioma del sistema. Ej: "es", "en". */
    @ColumnInfo(name = "idioma", defaultValue = "es")
    public String idioma = "es";

    /** 1 = activadas, 0 = desactivadas. */
    @ColumnInfo(name = "notificaciones_activas", defaultValue = "1")
    public int notificacionesActivas = 1;

    /** Timestamp de la última vez que se modificaron preferencias. */
    @ColumnInfo(name = "ultima_actualizacion")
    public long ultimaActualizacion;
}
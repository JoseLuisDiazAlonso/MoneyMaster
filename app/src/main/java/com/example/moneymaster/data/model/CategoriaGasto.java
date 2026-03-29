package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "categorias_gasto",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = { @Index(value = "usuario_id") }
)
public class CategoriaGasto {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /** NULL = categoría del sistema. INT = categoría personalizada del usuario. */
    @Nullable
    @ColumnInfo(name = "usuario_id")
    public Integer usuarioId;

    @ColumnInfo(name = "nombre")
    public String nombre;

    /** Nombre del drawable (ej: "ic_restaurant"). Para setImageResource() dinámico. */
    @ColumnInfo(name = "icono")
    public String icono;

    /** Color hex (ej: "#FF5722"). Para tinting con setColorFilter(). */
    @ColumnInfo(name = "color")
    public String color;

    /** 1 = categoría del sistema (no editable), 0 = personalizada. */
    @ColumnInfo(name = "es_sistema", defaultValue = "0")
    public int esSistema = 0;

    /** 1 = activa, 0 = desactivada (borrado suave). */
    @ColumnInfo(name = "activo", defaultValue = "1")
    public int activo = 1;
    public int esPredefinida;

    /** Factory method para categorías del sistema (usuario_id = null). */
    public static CategoriaGasto crearSistema(String nombre, String icono, String color) {
        CategoriaGasto c = new CategoriaGasto();
        c.nombre    = nombre;
        c.icono     = icono;
        c.color     = color;
        c.esSistema = 1;
        c.usuarioId = null;
        c.activo    = 1;
        return c;
    }

    /** Factory method para categorías personalizadas de un usuario. */
    public static CategoriaGasto crearPersonalizada(int usuarioId, String nombre, String icono, String color) {
        CategoriaGasto c = new CategoriaGasto();
        c.usuarioId = usuarioId;
        c.nombre    = nombre;
        c.icono     = icono;
        c.color     = color;
        c.esSistema = 0;
        c.activo    = 1;
        return c;
    }
}

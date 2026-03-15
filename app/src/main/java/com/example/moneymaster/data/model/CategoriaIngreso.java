package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room — tabla "categorias_ingreso".
 *
 * Misma lógica que CategoriaGasto: categorías del sistema (usuario_id = NULL)
 * y categorías personalizadas (usuario_id = X).
 */
@Entity(
        tableName = "categorias_ingreso",
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
public class CategoriaIngreso {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /** NULL = categoría del sistema. INT = categoría personalizada del usuario. */
    @Nullable
    @ColumnInfo(name = "usuario_id")
    public Integer usuarioId;

    @ColumnInfo(name = "nombre")
    public String nombre;

    /** Nombre del drawable (ej: "ic_salary"). Para setImageResource() dinámico. */
    @ColumnInfo(name = "icono")
    public String icono;

    /** Color hex (ej: "#4CAF50"). Para tinting con setColorFilter(). */
    @ColumnInfo(name = "color")
    public String color;

    /** 1 = categoría del sistema (no editable), 0 = personalizada. */
    @ColumnInfo(name = "es_sistema", defaultValue = "0")
    public int esSistema = 0;

    /** 1 = activa, 0 = desactivada (borrado suave). */
    @ColumnInfo(name = "activo", defaultValue = "1")
    public int activo = 1;

    /** Factory method para crear categorías del sistema con usuario_id = null. */
    public static CategoriaIngreso crearSistema(String nombre, String icono, String color) {
        CategoriaIngreso c = new CategoriaIngreso();
        c.nombre = nombre;
        c.icono = icono;
        c.color = color;
        c.esSistema = 1;
        c.usuarioId = null;
        c.activo = 1;
        return c;
    }

    /** Factory method para crear categorías personalizadas de un usuario. */
    public static CategoriaIngreso crearPersonalizada(int usuarioId, String nombre, String icono, String color) {
        CategoriaIngreso c = new CategoriaIngreso();
        c.usuarioId = usuarioId;
        c.nombre = nombre;
        c.icono = icono;
        c.color = color;
        c.esSistema = 0;
        c.activo = 1;
        return c;
    }
}

package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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


    @Nullable
    @ColumnInfo(name = "usuario_id")
    public Integer usuarioId;

    @ColumnInfo(name = "nombre")
    public String nombre;


    @ColumnInfo(name = "icono")
    public String icono;


    @ColumnInfo(name = "color")
    public String color;


    @ColumnInfo(name = "es_sistema", defaultValue = "0")
    public int esSistema = 0;


    @ColumnInfo(name = "activo", defaultValue = "1")
    public int activo = 1;
    public int esPredefinida;

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

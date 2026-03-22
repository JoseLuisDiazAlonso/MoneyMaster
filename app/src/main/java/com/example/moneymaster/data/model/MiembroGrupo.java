package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "miembros_grupo",
        foreignKeys = {
                @ForeignKey(
                        entity = Grupo.class,
                        parentColumns = "id",
                        childColumns = "grupoId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "usuarioId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"grupoId", "usuarioId"}, unique = true),
                @Index(value = "usuarioId")
        }
)
public class MiembroGrupo {

    public static final String ROL_ADMIN   = "ADMIN";
    public static final String ROL_MIEMBRO = "MIEMBRO";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "grupoId")
    public int grupoId;

    @ColumnInfo(name = "usuarioId")
    public int usuarioId;

    /** Nombre visible del miembro en el grupo. Ej: "Ana", "Pedro". */
    @Nullable
    @ColumnInfo(name = "nombre")
    public String nombre;

    /** Color hex asignado al miembro. Ej: "#F44336". */
    @Nullable
    @ColumnInfo(name = "color")
    public String color;

    @ColumnInfo(name = "rol", defaultValue = "MIEMBRO")
    public String rol = ROL_MIEMBRO;

    @ColumnInfo(name = "activo", defaultValue = "1")
    public int activo = 1;

    @ColumnInfo(name = "fecha_union")
    public long fechaUnion;
}
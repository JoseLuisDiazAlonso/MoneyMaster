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
                )
        },
        indices = {
                @Index(value = "grupoId")
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

    /** Ya no es FK — es simplemente un identificador opcional del usuario de la app. */
    @ColumnInfo(name = "usuarioId")
    public int usuarioId = 0;

    /** Nombre visible del miembro en el grupo.  */
    @Nullable
    @ColumnInfo(name = "nombre")
    public String nombre;

    /** Color hex asignado al miembro.  */
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
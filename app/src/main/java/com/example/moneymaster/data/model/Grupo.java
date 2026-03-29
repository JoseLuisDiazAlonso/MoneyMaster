package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(
        tableName = "grupos",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "creador_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = "creador_id"),
                @Index(value = "fecha_creacion")
        }
)
public class Grupo {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /**
     * FK nullable → users.id. Usuario que creó el grupo.
     * Nullable con SET_NULL: si el creador borra su cuenta, el grupo no se elimina.
     */
    @Nullable
    @ColumnInfo(name = "creador_id")
    public Integer creadorId;

    /** Nombre del grupo. Ej: "Viaje Cancún", "Piso compartido". */
    @ColumnInfo(name = "nombre")
    public String nombre;

    /** Descripción opcional del grupo. */
    @Nullable
    @ColumnInfo(name = "descripcion")
    public String descripcion;

    /** Código de moneda del grupo. Ej: "MXN". Todos los gastos del grupo usan esta moneda. */
    @ColumnInfo(name = "moneda", defaultValue = "MXN")
    public String moneda = "MXN";

    /** 1 = activo, 0 = archivado (borrado suave). */
    @ColumnInfo(name = "activo", defaultValue = "1")
    public int activo = 1;

    /** Timestamp Unix en milisegundos de creación del grupo. */
    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;
}

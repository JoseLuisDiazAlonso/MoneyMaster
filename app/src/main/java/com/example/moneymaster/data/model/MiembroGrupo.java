package com.example.moneymaster.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room — tabla "miembros_grupo".
 *
 * Tabla de unión N:M entre User y Grupo.
 * Un usuario puede pertenecer a muchos grupos, y un grupo tiene muchos miembros.
 *
 * El índice compuesto único en (grupo_id, usuario_id) garantiza que
 * un usuario no pueda ser añadido dos veces al mismo grupo.
 *
 * Roles: ADMIN puede editar/archivar el grupo y gestionar miembros.
 *        MIEMBRO solo puede añadir gastos y ver balances.
 */
@Entity(
        tableName = "miembros_grupo",
        foreignKeys = {
                @ForeignKey(
                        entity = Grupo.class,
                        parentColumns = "id",
                        childColumns = "grupo_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"grupo_id", "usuario_id"}, unique = true),
                @Index(value = "usuario_id")
        }
)
public class MiembroGrupo {

    public static final String ROL_ADMIN   = "ADMIN";
    public static final String ROL_MIEMBRO = "MIEMBRO";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /** FK → grupos.id. Grupo al que pertenece el miembro. */
    @ColumnInfo(name = "grupo_id")
    public int grupoId;

    /** FK → users.id. Usuario que es miembro del grupo. */
    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    /** ROL_ADMIN o ROL_MIEMBRO. */
    @ColumnInfo(name = "rol", defaultValue = "MIEMBRO")
    public String rol = ROL_MIEMBRO;

    /** 1 = activo en el grupo, 0 = dado de baja (borrado suave). */
    @ColumnInfo(name = "activo", defaultValue = "1")
    public int activo = 1;

    /** Timestamp Unix en milisegundos de cuándo se unió al grupo. */
    @ColumnInfo(name = "fecha_union")
    public long fechaUnion;
}

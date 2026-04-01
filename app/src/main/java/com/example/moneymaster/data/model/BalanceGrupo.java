package com.example.moneymaster.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "balance_grupo",
        foreignKeys = {
                @ForeignKey(
                        entity = Grupo.class,
                        parentColumns = "id",
                        childColumns = "grupo_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"grupo_id", "usuario_deudor_id", "usuario_acreedor_id"}, unique = true),
                @Index(value = "usuario_deudor_id"),
                @Index(value = "usuario_acreedor_id"),
                @Index(value = "ultima_actualizacion")
        }
)
public class BalanceGrupo {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "grupo_id")
    public int grupoId;

    /** ID del miembro deudor — referencia a miembros_grupo.id, sin FK para permitir miembros sin cuenta. */
    @ColumnInfo(name = "usuario_deudor_id")
    public int usuarioDeudorId;

    /** ID del miembro acreedor — referencia a miembros_grupo.id, sin FK para permitir miembros sin cuenta. */
    @ColumnInfo(name = "usuario_acreedor_id")
    public int usuarioAcreedorId;

    @ColumnInfo(name = "monto_pendiente", defaultValue = "0.0")
    public double montoPendiente = 0.0;

    @ColumnInfo(name = "ultima_actualizacion")
    public long ultimaActualizacion;

    @ColumnInfo(name = "liquidado", defaultValue = "0")
    public int liquidado = 0;
}
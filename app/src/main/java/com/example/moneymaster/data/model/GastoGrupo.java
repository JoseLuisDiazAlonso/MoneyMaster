package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "gastos_grupo",
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
                        childColumns = "pagado_por_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = CategoriaGasto.class,
                        parentColumns = "id",
                        childColumns = "categoria_id",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = FotoRecibo.class,
                        parentColumns = "id",
                        childColumns = "foto_recibo_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = "grupo_id"),
                @Index(value = "pagado_por_id"),
                @Index(value = "categoria_id"),
                @Index(value = "fecha"),
                @Index(value = "foto_recibo_id")
        }
)
public class GastoGrupo {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "grupo_id")
    public int grupoId;

    @ColumnInfo(name = "pagado_por_id")
    public int pagadoPorId;

    @ColumnInfo(name = "categoria_id")
    public int categoriaId;

    @Nullable
    @ColumnInfo(name = "foto_recibo_id")
    public Integer fotoReciboId;

    /** Monto total del gasto. Siempre positivo. */
    @ColumnInfo(name = "monto")
    public double monto;

    @Nullable
    @ColumnInfo(name = "descripcion")
    public String descripcion;

    @ColumnInfo(name = "fecha")
    public long fecha;

    @ColumnInfo(name = "dividir_igual", defaultValue = "1")
    public int dividirIgual = 1;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    /**
     * Nombre del miembro que pagó, guardado como texto plano.
     * Evita JOIN con miembros_grupo ya que los miembros no tienen
     * cuenta de usuario vinculada en este sprint.
     * Ej: "Ana", "Pedro".
     */
    @Nullable
    @ColumnInfo(name = "pagado_por_nombre")
    public String pagadoPorNombre;
}

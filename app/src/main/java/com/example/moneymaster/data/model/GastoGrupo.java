package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room — tabla "gastos_grupo".
 *
 * Representa un gasto realizado dentro de un grupo.
 * pagadoPorId identifica al miembro que adelantó el dinero.
 * El campo dividirIgual indica si el gasto se reparte equitativamente
 * entre todos los miembros activos del grupo (base para el cálculo de BalanceGrupo).
 *
 * Contiene 4 FKs: grupo, usuario que pagó, categoría y foto (nullable).
 */
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

    /** FK → grupos.id. Grupo al que pertenece este gasto. */
    @ColumnInfo(name = "grupo_id")
    public int grupoId;

    /** FK → users.id. Miembro que adelantó el dinero. */
    @ColumnInfo(name = "pagado_por_id")
    public int pagadoPorId;

    /** FK → categorias_gasto.id. Clasificación del gasto. */
    @ColumnInfo(name = "categoria_id")
    public int categoriaId;

    /**
     * FK nullable → fotos_recibo.id.
     * Null = el gasto no tiene comprobante fotográfico.
     */
    @Nullable
    @ColumnInfo(name = "foto_recibo_id")
    public Integer fotoReciboId;

    /** Monto total del gasto. Siempre positivo. */
    @ColumnInfo(name = "monto")
    public double monto;

    /** Descripción del gasto. Ej: "Cena restaurante La Mar". */
    @Nullable
    @ColumnInfo(name = "descripcion")
    public String descripcion;

    /** Timestamp Unix en milisegundos de cuándo ocurrió el gasto. */
    @ColumnInfo(name = "fecha")
    public long fecha;

    /**
     * 1 = dividir el monto en partes iguales entre todos los miembros activos.
     * 0 = división personalizada (implementación futura).
     */
    @ColumnInfo(name = "dividir_igual", defaultValue = "1")
    public int dividirIgual = 1;

    /** Timestamp Unix en milisegundos de cuándo se registró. */
    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;
}

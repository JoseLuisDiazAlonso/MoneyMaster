package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room — tabla "gastos_personales".
 *
 * Entidad central del tracking individual.
 * Contiene 3 FKs: usuario, categoría y foto (esta última nullable).
 *
 * El campo "fecha" está indexado para acelerar las queries de rango temporal
 * (mes actual, semana, año) que son las más frecuentes en los reportes.
 *
 * Flujo de inserción:
 *   1. Si hay foto: insertar FotoRecibo → obtener ID generado.
 *   2. Construir GastoPersonal con fotoReciboId (o null si no hay foto).
 *   3. Insertar GastoPersonal → Room retorna ID generado.
 *
 * Flujo de borrado:
 *   1. Leer fotoReciboId antes de borrar.
 *   2. Borrar GastoPersonal (Room NO hace CASCADE hacia FotoRecibo).
 *   3. Si había foto: borrar FotoRecibo + archivo físico del disco.
 */
@Entity(
        tableName = "gastos_personales",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
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
                @Index(value = "usuario_id"),
                @Index(value = "categoria_id"),
                @Index(value = "fecha"),
                @Index(value = "foto_recibo_id")
        }
)
public class GastoPersonal {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /** FK → users.id. Propietario del gasto. */
    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

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

    /** Monto del gasto. Siempre positivo. */
    @ColumnInfo(name = "monto")
    public double monto;

    /** Descripción opcional del gasto. Ej: "Gasolina viaje fin de semana". */
    @Nullable
    @ColumnInfo(name = "descripcion")
    public String descripcion;

    /** Timestamp Unix en milisegundos de cuándo ocurrió el gasto. */
    @ColumnInfo(name = "fecha")
    public long fecha;

    /** Timestamp Unix en milisegundos de cuándo se registró en la app. */
    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;
}
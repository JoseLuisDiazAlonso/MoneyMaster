package com.example.moneymaster.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room — tabla "ingresos_personales".
 *
 * Estructura análoga a GastoPersonal, pero para registrar ingresos.
 * Referencia a CategoriaIngreso en lugar de CategoriaGasto.
 * No incluye foto de recibo (los ingresos raramente tienen comprobante fotográfico),
 * aunque se puede añadir en el futuro.
 */
@Entity(
        tableName = "ingresos_personales",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = CategoriaIngreso.class,
                        parentColumns = "id",
                        childColumns = "categoria_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = "usuario_id"),
                @Index(value = "categoria_id"),
                @Index(value = "fecha")
        }
)
public class IngresoPersonal {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /** FK → users.id. Propietario del ingreso. */
    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    /** FK → categorias_ingreso.id. Clasificación del ingreso. */
    @ColumnInfo(name = "categoria_id")
    public int categoriaId;

    /** Monto del ingreso. Siempre positivo. */
    @ColumnInfo(name = "monto")
    public double monto;

    /** Descripción opcional. Ej: "Nómina marzo 2024". */
    @Nullable
    @ColumnInfo(name = "descripcion")
    public String descripcion;

    /** Timestamp Unix en milisegundos de cuándo se recibió el ingreso. */
    @ColumnInfo(name = "fecha")
    public long fecha;

    /** Timestamp Unix en milisegundos de cuándo se registró en la app. */
    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;
}

package com.example.moneymaster.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad GastoPersonal — Card #62: índices añadidos en fecha y categoria_id
 * para acelerar las consultas de estadísticas, home y export.
 */
@Entity(
        tableName = "gastos_personales",
        indices = {
                @Index(value = {"fecha"}),
                @Index(value = {"categoria_id"})
        },
        foreignKeys = @ForeignKey(
                entity = CategoriaGasto.class,
                parentColumns = "id",
                childColumns = "categoria_id",
                onDelete = ForeignKey.SET_NULL
        )
)
public class GastoPersonal {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId;
    public double monto;
    public String descripcion;
    public long fecha;          // Unix timestamp ms
    public Integer categoria_id; // nullable FK
    public String fotoRuta;
    public int tieneFoto;       // 0 = no, 1 = sí
}
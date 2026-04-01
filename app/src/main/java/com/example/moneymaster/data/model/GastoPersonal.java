package com.example.moneymaster.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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
    public long fecha;
    public Integer categoria_id;
}
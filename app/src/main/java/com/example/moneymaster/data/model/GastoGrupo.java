package com.example.moneymaster.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "gastos_grupo",
        indices = {
                @Index(value = {"fecha"}),
                @Index(value = {"grupoId"})
        },
        foreignKeys = @ForeignKey(
                entity = Grupo.class,
                parentColumns = "id",
                childColumns = "grupoId",
                onDelete = ForeignKey.CASCADE
        )
)
public class GastoGrupo {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int grupoId;
    public double monto;
    public String descripcion;
    public long fecha;
    public int pagadoPorId;
    public String pagadoPorNombre;
    public Integer categoria_id;
    public int dividirIgual;
}
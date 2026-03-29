package com.example.moneymaster.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad GastoGrupo — Card #62: índices añadidos en fecha y grupoId.
 *
 * Campos originales conservados:
 *  - categoria_id   → Integer nullable (FK opcional a categorías)
 *  - foto_recibo_id → Integer nullable (FK opcional a FotoRecibo)
 *  - tieneFoto      → Integer nullable (puede ser NULL según DAO: SET tieneFoto = NULL)
 *  - pagadoPorId    → int primitivo (nunca null)
 */
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
    public long fecha;              // Unix timestamp ms

    public int pagadoPorId;         // primitivo int, nunca null
    public String pagadoPorNombre;  // desnormalizado para display rápido

    public Integer categoria_id;    // nullable — FK opcional
    public Integer foto_recibo_id;  // nullable — FK opcional a FotoRecibo
    public String fotoRuta;         // ruta local del archivo de foto
    public Integer tieneFoto;       // nullable: NULL = sin foto, 1 = con foto
    public int dividirIgual;        // 0 = no dividir, 1 = dividir entre miembros
}
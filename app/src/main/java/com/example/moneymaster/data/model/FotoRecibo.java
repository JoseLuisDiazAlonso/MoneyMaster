package com.example.moneymaster.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(
        tableName = "fotos_recibo",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "usuario_id"),
                @Index(value = "fecha_captura")
        }
)
public class FotoRecibo {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    /** FK → users.id. Usuario propietario de la foto. **/
    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    /** Ruta absoluta al archivo en el almacenamiento interno.**/
    @ColumnInfo(name = "ruta_archivo")
    public String rutaArchivo;

    /** Nombre original del archivo.**/
    @ColumnInfo(name = "nombre_archivo")
    public String nombreArchivo;

    /** Tamaño del archivo en bytes.  */
    @ColumnInfo(name = "tamanio_bytes")
    public long tamanioBytes;

    /** Timestamp Unix en milisegundos del momento de captura/importación.**/
    @ColumnInfo(name = "fecha_captura")
    public long fechaCaptura;

    /**
     * Ruta a la miniatura generada (thumbnail).
     * Null si aún no se generó. La miniatura se crea en background al insertar.
     */
    @ColumnInfo(name = "miniatura_ruta")
    public String miniaturaRuta;
}

package com.example.moneymaster.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room — tabla "fotos_recibo".
 *
 * Diseño offline-first: almacena la RUTA LOCAL del archivo en el almacenamiento
 * interno del dispositivo, nunca el BLOB binario en la base de datos.
 * Esto mantiene la BD ligera y el acceso a imágenes rápido.
 *
 * Flujo de uso:
 *   1. El usuario toma/elige una foto.
 *   2. La app copia el archivo a getFilesDir() o similar.
 *   3. Se inserta FotoRecibo con la ruta resultante → Room devuelve el ID generado.
 *   4. Ese ID se asigna a GastoPersonal.fotoReciboId o GastoGrupo.fotoReciboId.
 *
 * Limpieza de huérfanas:
 *   Usar FotoReciboDao.getFotosHuerfanas() periódicamente para detectar
 *   fotos sin gasto asociado y borrar tanto la fila como el archivo físico.
 */
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

    /** FK → users.id. Usuario propietario de la foto. */
    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    /** Ruta absoluta al archivo en el almacenamiento interno. Ej: /data/data/.../files/recibos/foto_123.jpg */
    @ColumnInfo(name = "ruta_archivo")
    public String rutaArchivo;

    /** Nombre original del archivo. Ej: "recibo_2024_03_15.jpg". */
    @ColumnInfo(name = "nombre_archivo")
    public String nombreArchivo;

    /** Tamaño del archivo en bytes. Para mostrar info al usuario. */
    @ColumnInfo(name = "tamanio_bytes")
    public long tamanioBytes;

    /** Timestamp Unix en milisegundos del momento de captura/importación. */
    @ColumnInfo(name = "fecha_captura")
    public long fechaCaptura;

    /**
     * Ruta a la miniatura generada (thumbnail).
     * Null si aún no se generó. La miniatura se crea en background al insertar.
     */
    @ColumnInfo(name = "miniatura_ruta")
    public String miniaturaRuta;
}

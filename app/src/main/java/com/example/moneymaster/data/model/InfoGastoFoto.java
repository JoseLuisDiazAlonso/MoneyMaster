package com.example.moneymaster.data.model;

/**
 * Card #35 — POJO de presentación para el visor de imagen.
 * Combina la info del gasto (personal o de grupo) con la foto.
 * NO es una entidad Room.
 */
public class InfoGastoFoto {

    public enum TipoGasto { PERSONAL, GRUPO }

    public TipoGasto tipo;
    public double    monto;
    public String    descripcion;
    public String    nombreCategoria;
    public long      fecha;
    public String    pagadoPor;      // null para gastos personales

    public InfoGastoFoto() {}
}

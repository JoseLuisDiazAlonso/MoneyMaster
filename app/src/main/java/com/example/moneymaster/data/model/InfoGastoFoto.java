package com.example.moneymaster.data.model;


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

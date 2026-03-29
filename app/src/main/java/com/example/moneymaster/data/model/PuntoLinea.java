package com.example.moneymaster.data.model;

import androidx.room.Ignore;

public class PuntoLinea {

    public String etiqueta;
    public int    posicion;
    public double total;

    @Ignore
    public double totalAcumulado;

    public PuntoLinea() {}

    @Ignore
    public PuntoLinea(String etiqueta, int posicion, double total) {
        this.etiqueta = etiqueta;
        this.posicion = posicion;
        this.total    = total;
    }
}
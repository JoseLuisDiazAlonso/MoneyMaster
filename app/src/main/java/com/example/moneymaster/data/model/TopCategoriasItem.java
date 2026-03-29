package com.example.moneymaster.data.model;

public class TopCategoriasItem {

    /** Nombre de la categoría */
    public String nombreCategoria;

    /** Nombre del drawable vector  */
    public String icono;

    /** Color hex de la categoría ( */
    public String color;

    /** Suma total gastada en esta categoría en el período */
    public double total;

    //Constructor vacío requerido por Room

    public TopCategoriasItem() {}

    public TopCategoriasItem(String nombreCategoria, String icono, String color, double total) {
        this.nombreCategoria = nombreCategoria;
        this.icono           = icono;
        this.color           = color;
        this.total           = total;
    }
}
package com.example.moneymaster.data.model;

public class ResumenMensual {

    public int mes;    // 1 = enero, 12 = diciembre
    public int anio;
    public double total;

    public ResumenMensual() {}

    public ResumenMensual(int mes, int anio, double total) {
        this.mes   = mes;
        this.anio  = anio;
        this.total = total;
    }

    public String getEtiquetaMes() {
        String[] nombres = {"Ene","Feb","Mar","Abr","May","Jun",
                "Jul","Ago","Sep","Oct","Nov","Dic"};
        if (mes >= 1 && mes <= 12) return nombres[mes - 1];
        return String.valueOf(mes);
    }

    public String getEtiquetaConAnio() {
        return getEtiquetaMes() + " " + String.valueOf(anio).substring(2);
    }
}

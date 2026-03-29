package com.example.moneymaster.ui.groups.model;


public class MemberBalanceItem {

    public String nombreMiembro;
    public String colorMiembro;  // hex: "#F44336"
    public double totalPagado;

    public MemberBalanceItem(String nombreMiembro, String colorMiembro, double totalPagado) {
        this.nombreMiembro = nombreMiembro;
        this.colorMiembro  = colorMiembro;
        this.totalPagado   = totalPagado;
    }
}

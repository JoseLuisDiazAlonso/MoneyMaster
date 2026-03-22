package com.example.moneymaster.ui.groups.model;

/**
 * POJO de UI que representa el balance neto de un miembro en el grupo.
 *
 * balanceNeto > 0 → acreedor: recupera dinero (color verde)
 * balanceNeto < 0 → deudor:   debe dinero (color rojo)
 * balanceNeto ≈ 0 → equilibrado (color gris)
 */
public class MiembroBalanceItem {

    public String nombreMiembro;
    public String colorMiembro;   // hex: "#F44336"
    public double totalPagado;
    public double cuotaIdeal;
    public double balanceNeto;    // totalPagado - cuotaIdeal

    public MiembroBalanceItem(String nombreMiembro, String colorMiembro,
                              double totalPagado, double cuotaIdeal, double balanceNeto) {
        this.nombreMiembro = nombreMiembro;
        this.colorMiembro  = colorMiembro;
        this.totalPagado   = totalPagado;
        this.cuotaIdeal    = cuotaIdeal;
        this.balanceNeto   = balanceNeto;
    }
}
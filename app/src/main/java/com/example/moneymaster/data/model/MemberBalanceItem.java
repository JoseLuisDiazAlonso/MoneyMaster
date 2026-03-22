package com.example.moneymaster.ui.groups.model;

/**
 * POJO de UI (no entidad Room) que representa el resumen
 * de gasto de un miembro dentro de un grupo.
 *
 * Se calcula en GroupExpensesViewModel agrupando los gastos
 * por pagadoPorNombre.
 */
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

package com.example.moneymaster.ui.groups.model;

/**
 * POJO de UI que representa una transacción sugerida para saldar deudas.
 * Ejemplo: "Carlos debe 30€ a Ana"
 *
 * pagado es un flag en memoria (no se persiste en Room en este sprint).
 * Al marcar como pagado se oculta la tarjeta visualmente.
 */
public class DeudaItem {

    public String nombreDeudor;
    public String colorDeudor;   // hex del color del deudor
    public String nombreAcreedor;
    public double monto;
    public boolean pagado;

    public DeudaItem(String nombreDeudor, String colorDeudor,
                     String nombreAcreedor, double monto, boolean pagado) {
        this.nombreDeudor   = nombreDeudor;
        this.colorDeudor    = colorDeudor;
        this.nombreAcreedor = nombreAcreedor;
        this.monto          = monto;
        this.pagado         = pagado;
    }
}

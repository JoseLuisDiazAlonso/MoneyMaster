package com.example.moneymaster.ui.groups.model;

/**
 * Modelo de UI (no es entidad Room) que representa un miembro
 * mientras se está editando en el formulario de creación de grupo.
 *
 * Contiene el nombre que el usuario escribe y el color asignado
 * automáticamente al añadir la fila.
 */
public class MemberInputItem {

    public String nombre;
    public String color; // hex: "#F44336"

    public MemberInputItem(String nombre, String color) {
        this.nombre = nombre;
        this.color  = color;
    }
}

package com.example.moneymaster.ui.groups.model;

/**
 * Modelo de datos para cada fila del RecyclerView dinámico de entrada
 * de miembros en la pantalla de creación de grupos.
 *
 * Los campos son públicos y mutables porque MemberInputAdapter los
 * modifica directamente desde el TextWatcher en tiempo real.
 */
public class MemberInputItem {

    public String nombre;
    public String color;

    public MemberInputItem(String nombre, String color) {
        this.nombre = nombre != null ? nombre : "";
        this.color  = color  != null ? color  : "#6200EE";
    }
}
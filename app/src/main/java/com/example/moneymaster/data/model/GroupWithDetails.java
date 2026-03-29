package com.example.moneymaster.data.model;


public class GroupWithDetails {

    // Campos del Grupo base
    public int id;
    public String nombre;
    public String descripcion;
    public long fechaCreacion;

    // Campos calculados por la consulta SQL
    public int numMiembros;
    public double balanceTotal; // suma de importes del grupo (puede ser negativo)

    // Constructor vacío requerido por Room para mapear resultados de @Query
    public GroupWithDetails() {}
}
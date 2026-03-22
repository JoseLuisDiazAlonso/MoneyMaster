package com.example.moneymaster.data.model;

/**
 * POJO (Plain Old Java Object) que combina los datos de un Grupo
 * con información calculada: número de miembros y balance total.
 *
 * Este objeto NO es una entidad Room; lo devuelve la consulta
 * mediante una @Query personalizada en GrupoDao con JOIN + COUNT.
 */
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
package com.example.moneymaster.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Define la tabla users en la base de datos SQLite */
@Entity(
        tableName = "users",
        indices = { @Index(value = "email", unique = true) }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    /** 1 = activo, 0 = desactivado (borrado suave). */
    @ColumnInfo(name = "activo", defaultValue = "1")
    public int activo = 1;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "fecha_registro")
    public long fechaRegistro;

    // Constructor vacío requerido por Room
    public User() {}

    public User(String fullName, String email, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = System.currentTimeMillis();
        this.fechaRegistro = System.currentTimeMillis();
        this.activo = 1;
    }
}
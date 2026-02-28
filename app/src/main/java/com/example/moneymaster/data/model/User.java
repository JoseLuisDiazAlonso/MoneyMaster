package com.example.moneymaster.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**Define la tabla users en la base de datos SQLite*/


    @Entity(
            tableName = "users",
            indices = {@Index(value = "email", unique = true)}
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

        @ColumnInfo(name = "created_at")
        public long createdAt;

//Constructor
        public User() {}

        public User(String fullName, String email, String passwordHash) {
            this.fullName = fullName;
            this.email = email;
            this.passwordHash = passwordHash;
            this.createdAt = System.currentTimeMillis();
        }
    }

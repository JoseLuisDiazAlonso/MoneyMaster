package com.example.moneymaster.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.moneymaster.data.model.User;

/**Define las consultas SQL para insertar y buscar usuarios*/
@Dao
public interface UserDao {

    /**Inserta un Usuario. ABORT lanza una excepción si el email ya existe.
     * Devuelve el id generado*/

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    /**Busca el usuario por email. Verifica si existen duplicados en el registro,
     * autentifica en login. Devuelve null si no existe*/

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    /**Busca usuario por email.*/

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    /**Actualiza contraseña para la opción de "olvidé mi contraseña".*/

    @Query("UPDATE users SET password_hash = :newHash WHERE id = :userId")
    void updatePasswordHash(int userId, String newHash);



}

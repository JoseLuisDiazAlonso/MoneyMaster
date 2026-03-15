package com.example.moneymaster.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moneymaster.data.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    @Update
    int updateUser(User user);

    @Query("UPDATE users SET activo = 0 WHERE id = :id")
    int desactivarUser(int id);

    @Query("UPDATE users SET password_hash = :passwordHash WHERE email = :email")
    int updatePasswordByEmail(String email, String passwordHash);

    @Query("SELECT * FROM users WHERE email = :email AND activo = 1 LIMIT 1")
    User getByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(int id);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email AND activo = 1")
    int countByEmail(String email);

    @Query("SELECT * FROM users WHERE activo = 1")
    LiveData<List<User>> getAllActiveUsers();
}
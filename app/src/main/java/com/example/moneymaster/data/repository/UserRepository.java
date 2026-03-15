package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.UserDao;
import com.example.moneymaster.data.model.User;

import java.util.List;

/**
 * Repositorio de usuarios.
 *
 * Actúa como única fuente de verdad para los datos de User.
 * Las Activities y ViewModels nunca acceden al DAO directamente —
 * siempre pasan por aquí.
 *
 * Las operaciones de escritura (insert, update, desactivar) se ejecutan
 * en un hilo de background usando AppDatabase.databaseWriteExecutor.
 * Las lecturas que devuelven LiveData las maneja Room automáticamente
 * en background — no necesitan executor.
 */
public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
    }

    // ---- ESCRITURAS (background thread) ----

    public void insertUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.insertUser(user));
    }

    public void updateUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.updateUser(user));
    }

    public void desactivarUser(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.desactivarUser(id));
    }

    // ---- LECTURAS SINCRÓNICAS (llamar desde background thread o Executor) ----

    public User getByEmail(String email) {
        return userDao.getByEmail(email);
    }

    public User getById(int id) {
        return userDao.getById(id);
    }

    public int countByEmail(String email) {
        return userDao.countByEmail(email);
    }

    // ---- LECTURAS REACTIVAS (LiveData — Room las ejecuta en background) ----

    public LiveData<List<User>> getAllActiveUsers() {
        return userDao.getAllActiveUsers();
    }
}
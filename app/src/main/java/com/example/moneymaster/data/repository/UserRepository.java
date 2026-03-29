package com.example.moneymaster.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.dao.UserDao;
import com.example.moneymaster.data.model.User;

import java.util.List;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        userDao = db.usuarioDao();
    }

    //ESCRITURAS

    public void insertUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                userDao.insertUser(user));
    }

    public void insertUserYObtenerId(User user, SaveCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = userDao.insertUser(user);
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onSaved(id));
        });
    }

    public void updateUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                userDao.updateUser(user));
    }

    public void desactivarUser(int id) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                userDao.desactivarUser(id));
    }

    public void updatePasswordByEmail(String email, String passwordHash) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                userDao.updatePasswordByEmail(email, passwordHash));
    }

    //LECTURAS SÍNCRONAS (llamar desde background thread)

    public User getByEmail(String email) {
        return userDao.getByEmail(email);
    }

    public User getById(int id) {
        return userDao.getById(id);
    }

    public int countByEmail(String email) {
        return userDao.countByEmail(email);
    }

    //LECTURAS REACTIVAS

    public LiveData<List<User>> getAllActiveUsers() {
        return userDao.getAllActiveUsers();
    }

    //CALLBACK

    public interface SaveCallback<T> {
        void onSaved(T result);
    }
}
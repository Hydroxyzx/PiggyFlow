package com.example.mymoneymanager.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mymoneymanager.data.dao.AccountDao;
import com.example.mymoneymanager.data.dao.CategoryDao;
import com.example.mymoneymanager.data.dao.TxnDao;
import com.example.mymoneymanager.data.entity.Account;
import com.example.mymoneymanager.data.entity.Category;
import com.example.mymoneymanager.data.entity.Txn;

@Database(entities = {Account.class, Category.class, Txn.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AccountDao accountDao();
    public abstract CategoryDao categoryDao();
    public abstract TxnDao txnDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            ctx.getApplicationContext(),
                            AppDatabase.class, "money.db")
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}

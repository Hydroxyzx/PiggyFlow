package com.example.mymoneymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mymoneymanager.data.entity.Account;
import java.util.List;

@Dao
public interface AccountDao {
    @Insert long insert(Account a);
    @Update void update(Account a);
    @Delete void delete(Account a);

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    LiveData<List<Account>> getAll();

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    List<Account> getAllSync();

    @Query("SELECT COUNT(*) FROM accounts")
    int count();
}

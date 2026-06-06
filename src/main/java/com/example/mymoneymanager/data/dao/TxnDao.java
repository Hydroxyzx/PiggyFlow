package com.example.mymoneymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mymoneymanager.data.entity.Txn;
import java.util.List;

@Dao
public interface TxnDao {
    @Insert long insert(Txn t);
    @Update void update(Txn t);
    @Delete void delete(Txn t);

    @Query("SELECT * FROM transactions WHERE dateMillis BETWEEN :start AND :end ORDER BY dateMillis DESC")
    LiveData<List<Txn>> getInRange(long start, long end);

    @Query("SELECT * FROM transactions WHERE dateMillis BETWEEN :start AND :end ORDER BY dateMillis DESC")
    List<Txn> getInRangeSync(long start, long end);

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    LiveData<List<Txn>> getAll();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND dateMillis BETWEEN :s AND :e")
    LiveData<Double> sumByType(String type, long s, long e);

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accId AND type = 'INCOME'")
    double sumIncomeForAccount(long accId);

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accId AND type = 'EXPENSE'")
    double sumExpenseForAccount(long accId);

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accId AND type = 'TRANSFER'")
    double sumTransferOutForAccount(long accId);

    @Query("SELECT SUM(amount) FROM transactions WHERE toAccountId = :accId AND type = 'TRANSFER'")
    double sumTransferInForAccount(long accId);

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    Txn getByIdSync(long id);
}

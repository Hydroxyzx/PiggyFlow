package com.example.mymoneymanager.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Txn {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long dateMillis;
    public String type;          // INCOME / EXPENSE / TRANSFER
    public double amount;
    public long accountId;
    public long toAccountId;     // for transfers, else 0
    public long categoryId;      // 0 for transfers
    public String note;

    public Txn() {}
}

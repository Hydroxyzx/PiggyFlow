package com.example.mymoneymanager.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String type;          // CASH / BANK / CARD
    public double openingBalance;
    public int colorHex;

    public Account() {}
}

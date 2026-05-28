package com.example.mymoneymanager.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String type;          // INCOME / EXPENSE
    public int colorHex;
    public double monthlyBudget; // 0 = no budget

    public Category() {}
}

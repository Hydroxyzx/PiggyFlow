package com.example.mymoneymanager.data.db;

import android.content.Context;
import com.example.mymoneymanager.data.entity.Account;
import com.example.mymoneymanager.data.entity.Category;
import java.util.concurrent.Executors;

public class Seeder {
    public static void seedIfEmpty(Context ctx) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            if (db.accountDao().count() == 0) {
                Account cash = new Account();
                cash.name = "Cash";
                cash.type = "CASH";
                cash.openingBalance = 0;
                cash.colorHex = 0xFF4CAF50;
                db.accountDao().insert(cash);

                Account bank = new Account();
                bank.name = "Bank";
                bank.type = "BANK";
                bank.openingBalance = 0;
                bank.colorHex = 0xFF2196F3;
                db.accountDao().insert(bank);
            }
            if (db.categoryDao().count() == 0) {
                String[] expenses = {"Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other"};
                int[] expColors = {0xFFE57373, 0xFFFFB74D, 0xFFBA68C8, 0xFF7986CB, 0xFF4DB6AC, 0xFFAED581, 0xFFA1887F};
                for (int i = 0; i < expenses.length; i++) {
                    Category c = new Category();
                    c.name = expenses[i];
                    c.type = "EXPENSE";
                    c.colorHex = expColors[i];
                    db.categoryDao().insert(c);
                }
                String[] incomes = {"Salary", "Bonus", "Gift", "Other"};
                int[] incColors = {0xFF66BB6A, 0xFF26A69A, 0xFF42A5F5, 0xFF8D6E63};
                for (int i = 0; i < incomes.length; i++) {
                    Category c = new Category();
                    c.name = incomes[i];
                    c.type = "INCOME";
                    c.colorHex = incColors[i];
                    db.categoryDao().insert(c);
                }
            }
        });
    }
}

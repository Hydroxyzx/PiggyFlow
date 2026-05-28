package com.example.mymoneymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymoneymanager.R;
import com.example.mymoneymanager.data.db.AppDatabase;
import com.example.mymoneymanager.data.entity.Account;
import com.example.mymoneymanager.data.entity.Category;
import com.example.mymoneymanager.data.entity.Txn;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionsFragment extends Fragment {

    private TxnAdapter adapter = new TxnAdapter();
    private TextView tvIncome, tvExpense, tvNet;
    private LiveData<List<Txn>> currentTxns;

    private Map<Long, String> categoryMap = new HashMap<>();
    private Map<Long, String> accountMap = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        View v = i.inflate(R.layout.fragment_transactions, c, false);
        RecyclerView rv = v.findViewById(R.id.rv_txns);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        tvIncome = v.findViewById(R.id.tv_income);
        tvExpense = v.findViewById(R.id.tv_expense);
        tvNet = v.findViewById(R.id.tv_net);

        CalendarView cal = v.findViewById(R.id.calendar);
        cal.setOnDateChangeListener((view, y, m, d) -> loadForDay(y, m, d));

        // Load lookups; update local fields and refresh the adapter
        AppDatabase.get(getContext()).categoryDao().getAll().observe(getViewLifecycleOwner(), list -> {
            categoryMap.clear();
            if (list != null) for (Category cat : list) categoryMap.put(cat.id, cat.name);
            adapter.setLookups(categoryMap, accountMap);
        });
        AppDatabase.get(getContext()).accountDao().getAll().observe(getViewLifecycleOwner(), list -> {
            accountMap.clear();
            if (list != null) for (Account a : list) accountMap.put(a.id, a.name);
            adapter.setLookups(categoryMap, accountMap);
        });

        Calendar today = Calendar.getInstance();
        loadForDay(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        return v;
    }

    private void loadForDay(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, m, d, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        long end = start + 24L * 60 * 60 * 1000 - 1;

        if (currentTxns != null) currentTxns.removeObservers(getViewLifecycleOwner());
        currentTxns = AppDatabase.get(getContext()).txnDao().getInRange(start, end);
        currentTxns.observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
            double inc = 0, exp = 0;
            if (list != null) {
                for (Txn t : list) {
                    if ("INCOME".equals(t.type)) inc += t.amount;
                    else if ("EXPENSE".equals(t.type)) exp += t.amount;
                }
            }
            tvIncome.setText("Income: " + String.format(Locale.getDefault(), "%,.2f", inc));
            tvExpense.setText("Expense: " + String.format(Locale.getDefault(), "%,.2f", exp));
            tvNet.setText("Net: " + String.format(Locale.getDefault(), "%,.2f", inc - exp));
        });
    }
}

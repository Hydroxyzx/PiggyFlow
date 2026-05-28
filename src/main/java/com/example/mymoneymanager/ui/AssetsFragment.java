package com.example.mymoneymanager.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mymoneymanager.ManageAccountsActivity;
import com.example.mymoneymanager.R;
import com.example.mymoneymanager.data.db.AppDatabase;
import com.example.mymoneymanager.data.entity.Account;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AssetsFragment extends Fragment {

    private TextView tvTotal;
    private LinearLayout container;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        View v = i.inflate(R.layout.fragment_assets, c, false);
        tvTotal = v.findViewById(R.id.tv_total);
        container = v.findViewById(R.id.container);

        v.findViewById(R.id.btn_manage).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), ManageAccountsActivity.class)));

        AppDatabase.get(getContext()).accountDao().getAll()
                .observe(getViewLifecycleOwner(), this::render);
        return v;
    }

    private void render(List<Account> accounts) {
        container.removeAllViews();
        if (accounts == null || accounts.isEmpty()) {
            tvTotal.setText("Total: 0.00");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            double total = 0;
            double[] balances = new double[accounts.size()];
            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);
                double income = AppDatabase.get(getContext()).txnDao().sumIncomeForAccount(a.id);
                double expense = AppDatabase.get(getContext()).txnDao().sumExpenseForAccount(a.id);
                balances[i] = a.openingBalance + income - expense;
                total += balances[i];
            }
            final double finalTotal = total;
            requireActivity().runOnUiThread(() -> {
                tvTotal.setText(String.format(Locale.getDefault(), "Total: %,.2f", finalTotal));
                for (int i = 0; i < accounts.size(); i++) {
                    addRow(accounts.get(i), balances[i]);
                }
            });
        });
    }

    private void addRow(Account a, double balance) {
        View row = LayoutInflater.from(getContext())
                .inflate(R.layout.item_account, container, false);
        TextView name = row.findViewById(R.id.tv_name);
        TextView type = row.findViewById(R.id.tv_type);
        TextView bal = row.findViewById(R.id.tv_balance);
        View dot = row.findViewById(R.id.dot);

        name.setText(a.name);
        type.setText(a.type);
        bal.setText(String.format(Locale.getDefault(), "%,.2f", balance));

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(a.colorHex);
        dot.setBackground(gd);

        container.addView(row);
    }
}

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

    private List<Account> cachedAccounts = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        View v = i.inflate(R.layout.fragment_assets, c, false);
        tvTotal = v.findViewById(R.id.tv_total);
        container = v.findViewById(R.id.container);

        v.findViewById(R.id.btn_manage).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), ManageAccountsActivity.class)));

        // Refresh saat daftar akun berubah
        AppDatabase.get(getContext()).accountDao().getAll()
                .observe(getViewLifecycleOwner(), list -> {
                    cachedAccounts = list;
                    render(list);
                });
        // Refresh juga saat ada perubahan transaksi (tambah/edit/hapus)
        AppDatabase.get(getContext()).txnDao().getAll()
                .observe(getViewLifecycleOwner(), txns -> {
                    if (cachedAccounts != null) render(cachedAccounts);
                });
        return v;
    }

    private void render(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            container.removeAllViews();
            tvTotal.setText("Saldo total: 0.00");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            // Ambil SEMUA transaksi sekali, lalu hitung saldo tiap akun secara langsung
            // dengan iterasi. Lebih kuat dari SUM query — transfer ditangani eksplisit.
            List<com.example.mymoneymanager.data.entity.Txn> allTxns =
                    AppDatabase.get(getContext()).txnDao().getInRangeSync(0L, Long.MAX_VALUE);

            double total = 0;
            double[] balances = new double[accounts.size()];
            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);
                double bal = a.openingBalance;
                for (com.example.mymoneymanager.data.entity.Txn t : allTxns) {
                    if (t.accountId == a.id) {
                        // ini akun ASAL
                        if ("INCOME".equals(t.type))       bal += t.amount;
                        else if ("EXPENSE".equals(t.type)) bal -= t.amount;
                        else if ("TRANSFER".equals(t.type)) bal -= t.amount;  // uang keluar
                    } else if ("TRANSFER".equals(t.type) && t.toAccountId == a.id) {
                        // ini akun TUJUAN dari sebuah transfer → uang masuk
                        bal += t.amount;
                    }
                }
                balances[i] = bal;
                total += bal;
            }
            final double finalTotal = total;
            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() -> {
                // CLEAR + ADD secara atomik di UI thread.
                // Mencegah duplikasi kalau render() dipanggil dua kali bersamaan
                // (oleh observer akun & observer transaksi).
                container.removeAllViews();
                tvTotal.setText(String.format(Locale.getDefault(), "Saldo total: %,.2f", finalTotal));
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

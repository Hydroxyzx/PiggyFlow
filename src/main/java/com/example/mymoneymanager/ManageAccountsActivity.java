package com.example.mymoneymanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymoneymanager.data.db.AppDatabase;
import com.example.mymoneymanager.data.entity.Account;

import java.util.List;
import java.util.concurrent.Executors;

public class ManageAccountsActivity extends AppCompatActivity {

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manage_list);

        TextView title = findViewById(R.id.tv_title);
        title.setText("Accounts");
        Button btnAdd = findViewById(R.id.btn_add);
        btnAdd.setText("+ Add account");
        RecyclerView rv = findViewById(R.id.rv_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddDialog());

        AppDatabase.get(this).accountDao().getAll()
                .observe(this, list -> adapter.setItems(list));
    }

    private void showAddDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null);
        EditText etName = v.findViewById(R.id.et_name);
        EditText etBalance = v.findViewById(R.id.et_balance);

        new AlertDialog.Builder(this)
                .setTitle("New account")
                .setView(v)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    double balance = 0;
                    try { balance = Double.parseDouble(etBalance.getText().toString()); }
                    catch (Exception ignored) {}
                    Account a = new Account();
                    a.name = name;
                    a.type = "CASH";
                    a.openingBalance = balance;
                    a.colorHex = 0xFF4CAF50;

                    Executors.newSingleThreadExecutor().execute(() ->
                            AppDatabase.get(this).accountDao().insert(a));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        List<Account> items;
        void setItems(List<Account> l) { items = l; notifyDataSetChanged(); }

        @Override public VH onCreateViewHolder(ViewGroup p, int v) {
            View row = LayoutInflater.from(p.getContext())
                    .inflate(android.R.layout.simple_list_item_2, p, false);
            return new VH(row);
        }
        @Override public void onBindViewHolder(VH h, int i) {
            Account a = items.get(i);
            h.t1.setText(a.name);
            h.t2.setText(a.type + " — Opening: " + String.format("%.2f", a.openingBalance));
            h.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(ManageAccountsActivity.this)
                        .setMessage("Delete " + a.name + "?")
                        .setPositiveButton("Delete", (d, w) ->
                                Executors.newSingleThreadExecutor().execute(() ->
                                        AppDatabase.get(ManageAccountsActivity.this)
                                                .accountDao().delete(a)))
                        .setNegativeButton("Cancel", null).show();
                return true;
            });
        }
        @Override public int getItemCount() { return items == null ? 0 : items.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}

package com.example.mymoneymanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymoneymanager.data.db.AppDatabase;
import com.example.mymoneymanager.data.entity.Category;

import java.util.List;
import java.util.concurrent.Executors;

public class ManageCategoriesActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manage_list);

        TextView title = findViewById(R.id.tv_title);
        title.setText("Categories");
        Button btnAdd = findViewById(R.id.btn_add);
        btnAdd.setText("+ Add category");
        rv = findViewById(R.id.rv_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddDialog());

        AppDatabase.get(this).categoryDao().getAll()
                .observe(this, list -> adapter.setItems(list));
    }

    private void showAddDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = v.findViewById(R.id.et_name);
        RadioGroup rg = v.findViewById(R.id.rg_type);

        new AlertDialog.Builder(this)
                .setTitle("New category")
                .setView(v)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    Category c = new Category();
                    c.name = name;
                    c.type = rg.getCheckedRadioButtonId() == R.id.rb_income ? "INCOME" : "EXPENSE";
                    c.colorHex = 0xFF607D8B;
                    Executors.newSingleThreadExecutor().execute(() ->
                            AppDatabase.get(this).categoryDao().insert(c));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        List<Category> items;
        void setItems(List<Category> l) { items = l; notifyDataSetChanged(); }

        @Override public VH onCreateViewHolder(ViewGroup p, int v) {
            View row = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_manage_row, p, false);
            return new VH(row);
        }
        @Override public void onBindViewHolder(VH h, int i) {
            Category c = items.get(i);
            h.tvName.setText(c.name);
            h.tvSub.setText("INCOME".equals(c.type) ? "Pemasukan" : "Pengeluaran");
            h.btnDelete.setOnClickListener(v -> confirmDelete(c));
            h.itemView.setOnLongClickListener(v -> { confirmDelete(c); return true; });
        }
        @Override public int getItemCount() { return items == null ? 0 : items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvSub;
            Button btnDelete;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_name);
                tvSub = v.findViewById(R.id.tv_sub);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }

    private void confirmDelete(Category c) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus kategori?")
                .setMessage("\"" + c.name + "\" akan dihapus. Transaksi yang sudah terhubung dengan kategori ini akan tetap ada tapi tidak menampilkan nama kategori lagi.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (d, w) ->
                        Executors.newSingleThreadExecutor().execute(() ->
                                AppDatabase.get(this).categoryDao().delete(c)))
                .show();
    }
}

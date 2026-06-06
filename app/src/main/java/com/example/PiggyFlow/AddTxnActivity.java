package com.example.mymoneymanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mymoneymanager.data.db.AppDatabase;
import com.example.mymoneymanager.data.entity.Account;
import com.example.mymoneymanager.data.entity.Category;
import com.example.mymoneymanager.data.entity.Txn;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddTxnActivity extends AppCompatActivity {

    public static final String EXTRA_TXN_ID = "EXTRA_TXN_ID";

    private String type = "EXPENSE";
    private long dateMillis = System.currentTimeMillis();
    private List<Account> accounts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    private long editingTxnId = 0;        // 0 = mode tambah baru; > 0 = mode edit
    private Txn editingTxn = null;        // data transaksi yang sedang diedit

    private TabLayout tabs;
    private Button btnDate, btnSave, btnDelete;
    private EditText etAmount, etNote;
    private Spinner spAccount, spCategory, spToAccount;
    private View toAccountRow;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_add_txn);

        tabs = findViewById(R.id.type_tabs);
        btnDate = findViewById(R.id.btn_date);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        spAccount = findViewById(R.id.sp_account);
        spCategory = findViewById(R.id.sp_category);
        spToAccount = findViewById(R.id.sp_to_account);
        toAccountRow = findViewById(R.id.row_to_account);

        // Cek apakah dipanggil dari TransactionsFragment untuk edit
        editingTxnId = getIntent().getLongExtra(EXTRA_TXN_ID, 0);
        boolean isEdit = editingTxnId > 0;

        if (isEdit) {
            setTitle("Edit Transaksi");
            btnSave.setText("Perbarui");
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle("Tambah Transaksi");
        }

        tabs.getTabAt(1).select();
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab t) {
                type = new String[]{"INCOME", "EXPENSE", "TRANSFER"}[t.getPosition()];
                refreshTypeUi();
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });

        updateDateButton();
        btnDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());

        loadData();
    }

    private void updateDateButton() {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        btnDate.setText(fmt.format(new Date(dateMillis)));
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateMillis);
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar c2 = Calendar.getInstance();
            c2.set(y, m, d);
            dateMillis = c2.getTimeInMillis();
            updateDateButton();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            accounts = db.accountDao().getAllSync();
            if (editingTxnId > 0) {
                editingTxn = db.txnDao().getByIdSync(editingTxnId);
                if (editingTxn != null) {
                    type = editingTxn.type;
                    dateMillis = editingTxn.dateMillis;
                }
            }
            categories = db.categoryDao().getByTypeSync(type);
            runOnUiThread(() -> {
                populateSpinners();
                if (editingTxn != null) prefillFromEditing();
                updateDateButton();
            });
        });
    }

    private void populateSpinners() {
        List<String> accNames = new ArrayList<>();
        for (Account a : accounts) accNames.add(a.name);
        spAccount.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, accNames));
        spToAccount.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, accNames));
        refreshCategories();
        // Pilih tab sesuai tipe yang sedang diedit
        int idx = "INCOME".equals(type) ? 0 : ("TRANSFER".equals(type) ? 2 : 1);
        if (tabs.getSelectedTabPosition() != idx) tabs.getTabAt(idx).select();
        refreshTypeUi();
    }

    // Isi field dari data yang sedang diedit
    private void prefillFromEditing() {
        if (editingTxn == null) return;
        etAmount.setText(String.valueOf(editingTxn.amount));
        etNote.setText(editingTxn.note == null ? "" : editingTxn.note);
        // pilih account
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).id == editingTxn.accountId) {
                spAccount.setSelection(i);
                break;
            }
        }
        if ("TRANSFER".equals(editingTxn.type)) {
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).id == editingTxn.toAccountId) {
                    spToAccount.setSelection(i);
                    break;
                }
            }
        }
    }

    private void refreshCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if ("TRANSFER".equals(type)) {
                categories = new ArrayList<>();
            } else {
                categories = AppDatabase.get(this).categoryDao().getByTypeSync(type);
            }
            runOnUiThread(() -> {
                List<String> names = new ArrayList<>();
                for (Category c : categories) names.add(c.name);
                spCategory.setAdapter(new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item, names));
                // Pulihkan pilihan kategori jika sedang edit dan tipe cocok
                if (editingTxn != null && type.equals(editingTxn.type) && !"TRANSFER".equals(type)) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).id == editingTxn.categoryId) {
                            spCategory.setSelection(i);
                            break;
                        }
                    }
                }
            });
        });
    }

    private void refreshTypeUi() {
        boolean isTransfer = "TRANSFER".equals(type);
        spCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
        findViewById(R.id.lbl_category).setVisibility(isTransfer ? View.GONE : View.VISIBLE);
        toAccountRow.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        refreshCategories();
    }

    private void save() {
        String amtStr = etAmount.getText().toString().trim();
        if (amtStr.isEmpty()) {
            Toast.makeText(this, "Masukkan jumlah", Toast.LENGTH_SHORT).show();
            return;
        }
        if (accounts.isEmpty()) {
            Toast.makeText(this, "Tambah akun dulu", Toast.LENGTH_SHORT).show();
            return;
        }
        double amt;
        try {
            amt = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi tambahan untuk transfer
        if ("TRANSFER".equals(type)) {
            if (accounts.size() < 2) {
                Toast.makeText(this, "Transfer butuh minimal 2 akun", Toast.LENGTH_SHORT).show();
                return;
            }
            long fromId = accounts.get(spAccount.getSelectedItemPosition()).id;
            long toId = accounts.get(spToAccount.getSelectedItemPosition()).id;
            if (fromId == toId) {
                Toast.makeText(this, "Akun asal dan tujuan harus berbeda", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final boolean isUpdate = (editingTxnId > 0 && editingTxn != null);
        final Txn t = isUpdate ? editingTxn : new Txn();
        t.type = type;
        t.dateMillis = dateMillis;
        t.amount = amt;
        t.accountId = accounts.get(spAccount.getSelectedItemPosition()).id;
        if ("TRANSFER".equals(type)) {
            t.toAccountId = accounts.get(spToAccount.getSelectedItemPosition()).id;
            t.categoryId = 0;
        } else {
            t.toAccountId = 0;
            t.categoryId = categories.isEmpty() ? 0
                    : categories.get(spCategory.getSelectedItemPosition()).id;
        }
        t.note = etNote.getText().toString();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (isUpdate) AppDatabase.get(this).txnDao().update(t);
            else AppDatabase.get(this).txnDao().insert(t);
            runOnUiThread(() -> {
                Toast.makeText(this, isUpdate ? "Diperbarui" : "Tersimpan",
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void confirmDelete() {
        if (editingTxn == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Hapus transaksi?")
                .setMessage("Transaksi ini akan dihapus permanen. Lanjutkan?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (d, w) -> doDelete())
                .show();
    }

    private void doDelete() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.get(this).txnDao().delete(editingTxn);
            runOnUiThread(() -> {
                Toast.makeText(this, "Dihapus", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

package com.example.mymoneymanager;

import android.app.DatePickerDialog;
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

    private String type = "EXPENSE";
    private long dateMillis = System.currentTimeMillis();
    private List<Account> accounts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    private TabLayout tabs;
    private Button btnDate, btnSave;
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
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        spAccount = findViewById(R.id.sp_account);
        spCategory = findViewById(R.id.sp_category);
        spToAccount = findViewById(R.id.sp_to_account);
        toAccountRow = findViewById(R.id.row_to_account);

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
            categories = db.categoryDao().getByTypeSync(type);
            runOnUiThread(this::populateSpinners);
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
        refreshTypeUi();
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
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }
        if (accounts.isEmpty()) {
            Toast.makeText(this, "Add an account first", Toast.LENGTH_SHORT).show();
            return;
        }
        double amt;
        try {
            amt = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        Txn t = new Txn();
        t.type = type;
        t.dateMillis = dateMillis;
        t.amount = amt;
        t.accountId = accounts.get(spAccount.getSelectedItemPosition()).id;
        if ("TRANSFER".equals(type)) {
            t.toAccountId = accounts.get(spToAccount.getSelectedItemPosition()).id;
            t.categoryId = 0;
        } else {
            if (!categories.isEmpty()) {
                t.categoryId = categories.get(spCategory.getSelectedItemPosition()).id;
            }
        }
        t.note = etNote.getText().toString();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.get(this).txnDao().insert(t);
            runOnUiThread(() -> {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

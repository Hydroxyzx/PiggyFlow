package com.example.mymoneymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mymoneymanager.data.db.Seeder;
import com.example.mymoneymanager.ui.AssetsFragment;
import com.example.mymoneymanager.ui.MoreFragment;
import com.example.mymoneymanager.ui.StatsFragment;
import com.example.mymoneymanager.ui.TransactionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navLeft, navRight;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        // Buat kategori & rekening default saat pertama kali dibuka
        Seeder.seedIfEmpty(this);

        navLeft = findViewById(R.id.bottom_nav_left);
        navRight = findViewById(R.id.bottom_nav_right);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        if (s == null) {
            loadFragment(new TransactionsFragment());
            navLeft.setSelectedItemId(R.id.nav_trans);
        }

        // Sisi kiri: Trans / Stats
        navLeft.setOnItemSelectedListener(item -> {
            clearOtherSelection(navRight);
            int id = item.getItemId();
            if (id == R.id.nav_trans) loadFragment(new TransactionsFragment());
            else if (id == R.id.nav_stats) loadFragment(new StatsFragment());
            return true;
        });

        // Sisi kanan: Accts / More
        navRight.setOnItemSelectedListener(item -> {
            clearOtherSelection(navLeft);
            int id = item.getItemId();
            if (id == R.id.nav_assets) loadFragment(new AssetsFragment());
            else if (id == R.id.nav_more) loadFragment(new MoreFragment());
            return true;
        });

        // Tombol + di tengah -> buka layar tambah transaksi
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddTxnActivity.class)));
    }

    // Hilangkan highlight di bar satunya, supaya hanya satu tab aktif
    private boolean suppress = false;
    private void clearOtherSelection(BottomNavigationView other) {
        if (suppress) return;
        suppress = true;
        Menu m = other.getMenu();
        m.setGroupCheckable(0, true, false);   // boleh tidak ada yang tercentang
        for (int i = 0; i < m.size(); i++) m.getItem(i).setChecked(false);
        m.setGroupCheckable(0, true, true);
        suppress = false;
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f).commit();
    }
}

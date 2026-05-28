package com.example.mymoneymanager.ui;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.mymoneymanager.R;
import com.example.mymoneymanager.data.db.AppDatabase;
import com.example.mymoneymanager.data.entity.Category;
import com.example.mymoneymanager.data.entity.Txn;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsFragment extends Fragment {

    // Mode periode
    private static final int WEEK = 0, MONTH = 1, YEAR = 2, CUSTOM = 3;
    private int mode = MONTH;                 // default: per bulan
    private Calendar anchor = Calendar.getInstance();  // titik acuan untuk minggu/bulan/tahun
    private long customStart = 0, customEnd = 0;       // untuk mode "Pilih"

    private final Locale ID = new Locale("id", "ID");

    private TextView tvSummary, tvRange;
    private Button btnPrev, btnNext;
    private LinearLayout container;
    private Map<Long, Category> categoryById = new HashMap<>();
    private List<Txn> latestList = new ArrayList<>();
    private LiveData<List<Txn>> currentTxns;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        View v = i.inflate(R.layout.fragment_stats, c, false);
        tvSummary = v.findViewById(R.id.tv_summary);
        tvRange = v.findViewById(R.id.tv_range);
        container = v.findViewById(R.id.container);
        btnPrev = v.findViewById(R.id.btn_prev);
        btnNext = v.findViewById(R.id.btn_next);
        TabLayout tabs = v.findViewById(R.id.period_tabs);

        // Kategori (untuk nama & warna) — dimuat sekali, lalu gambar ulang
        AppDatabase.get(getContext()).categoryDao().getAll().observe(getViewLifecycleOwner(), list -> {
            categoryById.clear();
            if (list != null) for (Category cat : list) categoryById.put(cat.id, cat);
            render(latestList);
        });

        // Tombol panah: geser periode mundur / maju
        btnPrev.setOnClickListener(view -> shift(-1));
        btnNext.setOnClickListener(view -> shift(1));

        // Klik label rentang saat mode "Pilih" -> buka pemilih tanggal lagi
        tvRange.setOnClickListener(view -> { if (mode == CUSTOM) pickCustomRange(); });

        // Pindah tab
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab t) {
                mode = t.getPosition();
                anchor = Calendar.getInstance();   // reset acuan ke hari ini
                if (mode == CUSTOM) {
                    pickCustomRange();
                } else {
                    reload();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {
                if (mode == CUSTOM) pickCustomRange();
            }
        });

        // Pilih tab default = Bulan
        tabs.getTabAt(MONTH).select();
        reload();
        return v;
    }

    // Geser periode (hanya untuk minggu/bulan/tahun)
    private void shift(int dir) {
        if (mode == WEEK) anchor.add(Calendar.DAY_OF_MONTH, 7 * dir);
        else if (mode == MONTH) anchor.add(Calendar.MONTH, dir);
        else if (mode == YEAR) anchor.add(Calendar.YEAR, dir);
        else return; // mode CUSTOM tidak pakai panah
        reload();
    }

    // Mode "Pilih": pilih tanggal awal, lalu tanggal akhir
    private void pickCustomRange() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            Calendar from = Calendar.getInstance();
            from.set(y, m, d);
            startOfDay(from);
            customStart = from.getTimeInMillis();

            // pemilih kedua untuk tanggal akhir
            new DatePickerDialog(requireContext(), (view2, y2, m2, d2) -> {
                Calendar to = Calendar.getInstance();
                to.set(y2, m2, d2);
                endOfDay(to);
                customEnd = to.getTimeInMillis();
                if (customEnd < customStart) {       // kalau kebalik, tukar
                    long tmp = customStart; customStart = customEnd; customEnd = tmp;
                }
                reload();
            }, y, m, d).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Hitung rentang [start, end] sesuai mode + acuan
    private long[] computeRange() {
        if (mode == CUSTOM) {
            if (customStart == 0 || customEnd == 0) {
                // belum dipilih: pakai bulan ini sebagai default
                Calendar c = Calendar.getInstance();
                c.set(Calendar.DAY_OF_MONTH, 1); startOfDay(c);
                long s = c.getTimeInMillis();
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)); endOfDay(c);
                return new long[]{s, c.getTimeInMillis()};
            }
            return new long[]{customStart, customEnd};
        }
        Calendar c = (Calendar) anchor.clone();
        if (mode == WEEK) {
            c.setFirstDayOfWeek(Calendar.MONDAY);
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            startOfDay(c);
            long s = c.getTimeInMillis();
            c.add(Calendar.DAY_OF_MONTH, 6);
            endOfDay(c);
            return new long[]{s, c.getTimeInMillis()};
        } else if (mode == MONTH) {
            c.set(Calendar.DAY_OF_MONTH, 1); startOfDay(c);
            long s = c.getTimeInMillis();
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)); endOfDay(c);
            return new long[]{s, c.getTimeInMillis()};
        } else { // YEAR
            c.set(Calendar.MONTH, Calendar.JANUARY); c.set(Calendar.DAY_OF_MONTH, 1); startOfDay(c);
            long s = c.getTimeInMillis();
            c.set(Calendar.MONTH, Calendar.DECEMBER); c.set(Calendar.DAY_OF_MONTH, 31); endOfDay(c);
            return new long[]{s, c.getTimeInMillis()};
        }
    }

    private String rangeLabel(long start, long end) {
        if (mode == WEEK) {
            SimpleDateFormat f = new SimpleDateFormat("dd MMM");
            SimpleDateFormat fy = new SimpleDateFormat("dd MMM yyyy");
            return f.format(new Date(start)) + " – " + fy.format(new Date(end));
        } else if (mode == MONTH) {
            return new SimpleDateFormat("MMMM yyyy").format(new Date(start));
        } else if (mode == YEAR) {
            return new SimpleDateFormat("yyyy").format(new Date(start));
        } else {
            SimpleDateFormat f = new SimpleDateFormat("dd MMM yyyy");
            return f.format(new Date(start)) + " – " + f.format(new Date(end));
        }
    }

    // Ambil ulang data sesuai periode terpilih
    private void reload() {
        long[] r = computeRange();
        tvRange.setText(rangeLabel(r[0], r[1]));

        // panah disembunyikan di mode "Pilih"
        boolean showArrows = (mode != CUSTOM);
        btnPrev.setVisibility(showArrows ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(showArrows ? View.VISIBLE : View.INVISIBLE);

        if (currentTxns != null) currentTxns.removeObservers(getViewLifecycleOwner());
        currentTxns = AppDatabase.get(getContext()).txnDao().getInRange(r[0], r[1]);
        currentTxns.observe(getViewLifecycleOwner(), this::render);
    }

    private void render(List<Txn> list) {
        latestList = list == null ? new ArrayList<>() : list;
        double income = 0, expense = 0;
        Map<Long, Double> byCat = new HashMap<>();
        for (Txn t : latestList) {
            if ("INCOME".equals(t.type)) income += t.amount;
            else if ("EXPENSE".equals(t.type)) {
                expense += t.amount;
                byCat.merge(t.categoryId, t.amount, Double::sum);
            }
        }
        tvSummary.setText(String.format(Locale.getDefault(),
                "Income: %,.2f\nExpense: %,.2f\nMargin: %,.2f",
                income, expense, income - expense));

        container.removeAllViews();
        if (byCat.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("There are no expenses in this period.");
            empty.setTextColor(Color.GRAY);
            empty.setPadding(0, 16, 0, 0);
            container.addView(empty);
            return;
        }

        double max = 0;
        for (double d : byCat.values()) if (d > max) max = d;

        for (Map.Entry<Long, Double> e : byCat.entrySet()) {
            Category cat = categoryById.get(e.getKey());
            String name = cat != null ? cat.name : "Lainnya";
            int color = cat != null ? cat.colorHex : Color.GRAY;
            double amt = e.getValue();
            double pct = expense > 0 ? (amt / expense * 100) : 0;

            View row = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_stat_row, container, false);
            TextView tvName = row.findViewById(R.id.tv_name);
            TextView tvAmount = row.findViewById(R.id.tv_amount);
            ProgressBar bar = row.findViewById(R.id.progress);
            View dot = row.findViewById(R.id.dot);

            tvName.setText(name);
            tvAmount.setText(String.format(Locale.getDefault(), "%,.2f (%.1f%%)", amt, pct));
            bar.setMax((int) (max * 100));
            bar.setProgress((int) (amt * 100));
            bar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(color);
            dot.setBackground(gd);

            container.addView(row);
        }
    }

    private void startOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
    }
    private void endOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999);
    }
}

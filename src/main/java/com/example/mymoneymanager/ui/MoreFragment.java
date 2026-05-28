package com.example.mymoneymanager.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mymoneymanager.ManageAccountsActivity;
import com.example.mymoneymanager.ManageCategoriesActivity;
import com.example.mymoneymanager.R;
import com.example.mymoneymanager.data.db.AppDatabase;
import com.example.mymoneymanager.data.entity.Txn;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.Executors;

public class MoreFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        View v = i.inflate(R.layout.fragment_more, c, false);

        v.findViewById(R.id.btn_categories).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), ManageCategoriesActivity.class)));

        v.findViewById(R.id.btn_accounts).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), ManageAccountsActivity.class)));

        v.findViewById(R.id.btn_currency).setOnClickListener(view -> showCurrencyDialog());

        v.findViewById(R.id.btn_export).setOnClickListener(view -> exportCsv());

        v.findViewById(R.id.btn_about).setOnClickListener(view ->
                new AlertDialog.Builder(getContext())
                        .setTitle("About")
                        .setMessage("My Money Manager\nVersion 1.0\n\nA simple personal finance tracker.")
                        .setPositiveButton("OK", null).show());

        return v;
    }

    private void showCurrencyDialog() {
        SharedPreferences sp = requireContext().getSharedPreferences("p", Context.MODE_PRIVATE);
        EditText et = new EditText(getContext());
        et.setText(sp.getString("currency", "USD"));
        new AlertDialog.Builder(getContext())
                .setTitle("Currency code (e.g. USD, IDR, EUR)")
                .setView(et)
                .setPositiveButton("Save", (d, w) -> {
                    sp.edit().putString("currency", et.getText().toString().trim()).apply();
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void exportCsv() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Txn> all = AppDatabase.get(getContext()).txnDao()
                        .getInRangeSync(0, System.currentTimeMillis());
                File dir = requireContext().getExternalFilesDir(null);
                File f = new File(dir, "export.csv");
                FileWriter w = new FileWriter(f);
                w.write("date,type,amount,accountId,categoryId,note\n");
                for (Txn t : all) {
                    w.write(t.dateMillis + "," + t.type + "," + t.amount + ","
                            + t.accountId + "," + t.categoryId + ","
                            + (t.note == null ? "" : t.note.replace(",", " ")) + "\n");
                }
                w.close();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Exported to " + f.getAbsolutePath(),
                                Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Export failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        });
    }
}

package com.example.mymoneymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymoneymanager.R;
import com.example.mymoneymanager.data.entity.Txn;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TxnAdapter extends RecyclerView.Adapter<TxnAdapter.VH> {

    public interface OnClick { void onClick(Txn t); }
    private OnClick onClick;

    private List<Txn> items;
    private Map<Long, String> categoryNames = new HashMap<>();
    private Map<Long, String> accountNames = new HashMap<>();
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    public void setOnClick(OnClick l) { this.onClick = l; }

    public void setItems(List<Txn> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    public void setLookups(Map<Long, String> cats, Map<Long, String> accs) {
        this.categoryNames = cats;
        this.accountNames = accs;
        notifyDataSetChanged();
    }

    @Override public VH onCreateViewHolder(ViewGroup p, int v) {
        View row = LayoutInflater.from(p.getContext()).inflate(R.layout.item_txn, p, false);
        return new VH(row);
    }

    @Override public void onBindViewHolder(VH h, int i) {
        Txn t = items.get(i);
        h.itemView.setOnClickListener(v -> { if (onClick != null) onClick.onClick(t); });
        String catName = categoryNames.getOrDefault(t.categoryId, "");
        if (catName.isEmpty() && "TRANSFER".equals(t.type)) catName = "Transfer";
        h.tvCategory.setText(catName);
        h.tvNote.setText(t.note == null ? "" : t.note);
        h.tvAccount.setText(accountNames.getOrDefault(t.accountId, ""));
        h.tvDate.setText(timeFmt.format(new Date(t.dateMillis)));

        String sign;
        int color;
        if ("INCOME".equals(t.type)) { sign = "+"; color = 0xFF2E7D32; }
        else if ("EXPENSE".equals(t.type)) { sign = "-"; color = 0xFFC62828; }
        else { sign = ""; color = 0xFF1565C0; }
        h.tvAmount.setText(sign + String.format(Locale.getDefault(), "%,.2f", t.amount));
        h.tvAmount.setTextColor(color);
    }

    @Override public int getItemCount() { return items == null ? 0 : items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCategory, tvNote, tvAmount, tvAccount, tvDate;
        VH(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_category);
            tvNote = v.findViewById(R.id.tv_note);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvAccount = v.findViewById(R.id.tv_account);
            tvDate = v.findViewById(R.id.tv_date);
        }
    }
}

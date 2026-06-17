package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public static class HistoryItem {
        private final String date;
        private final String status; // "Selesai", "Berlangsung", "Gagal"
        private final String productName;
        private final int totalPrice;
        private final String imageUrl;

        public HistoryItem(String date, String status, String productName, int totalPrice, String imageUrl) {
            this.date = date;
            this.status = status;
            this.productName = productName;
            this.totalPrice = totalPrice;
            this.imageUrl = imageUrl;
        }

        public String getDate() { return date; }
        public String getStatus() { return status; }
        public String getProductName() { return productName; }
        public int getTotalPrice() { return totalPrice; }
        public String getImageUrl() { return imageUrl; }
    }

    private final Context context;
    private final List<HistoryItem> listHistory;

    public HistoryAdapter(Context context, List<HistoryItem> listHistory) {
        this.context = context;
        this.listHistory = listHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = listHistory.get(position);

        holder.tvDate.setText(item.getDate());
        holder.tvProductName.setText(item.getProductName());

        // Format Price to Rupiah
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvTotalPrice.setText(context.getString(R.string.total_price_format, formatRupiah.format(item.getTotalPrice())));

        // Set Status Badge Style
        holder.tvStatus.setText(item.getStatus());
        switch (item.getStatus().toLowerCase()) {
            case "selesai":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_completed);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed));
                holder.btnActionPrimary.setVisibility(View.VISIBLE);
                holder.btnActionPrimary.setText("Beri Ulasan");
                break;
            case "berlangsung":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_paid);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_paid));
                holder.btnActionPrimary.setVisibility(View.VISIBLE);
                holder.btnActionPrimary.setText("Lacak Pesanan");
                break;
            case "gagal":
            case "dibatalkan":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_gray);
                holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red for failed
                holder.btnActionPrimary.setVisibility(View.GONE); // No actions for failed order
                break;
        }

        // Load image with Glide
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivProductThumb);

        // Actions
        holder.btnActionSecondary.setOnClickListener(v -> {
            Toast.makeText(context, "Membeli ulang: " + item.getProductName(), Toast.LENGTH_SHORT).show();
        });

        holder.btnActionPrimary.setOnClickListener(v -> {
            Toast.makeText(context, holder.btnActionPrimary.getText() + " untuk " + item.getProductName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvProductName, tvTotalPrice;
        ImageView ivProductThumb;
        MaterialButton btnActionSecondary, btnActionPrimary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            ivProductThumb = itemView.findViewById(R.id.ivProductThumb);
            btnActionSecondary = itemView.findViewById(R.id.btnActionSecondary);
            btnActionPrimary = itemView.findViewById(R.id.btnActionPrimary);
        }
    }
}

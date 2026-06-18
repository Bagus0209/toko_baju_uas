package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.model.OrderModel;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderModel> listHistory;

    public HistoryAdapter(Context context, List<OrderModel> listHistory) {
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
        OrderModel item = listHistory.get(position);

        // Header Info
        holder.tvDate.setText(item.getTanggal());
        
        // Detailed Product Name/Summary
        // If the backend doesn't provide product names in OrderModel, 
        // we'll show a more descriptive ID and summary
        holder.tvProductName.setText("Order #" + item.getIdTransaksi() + " - Premium Collection");

        // Format Price to Rupiah
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvTotalPrice.setText("Total Payment: " + formatRupiah.format(item.getTotalHarga()));

        // Set Status Badge Style with premium colors
        String status = item.getStatus() != null ? item.getStatus() : "Berlangsung";
        holder.tvStatus.setText(status.toUpperCase());
        
        switch (status.toLowerCase()) {
            case "selesai":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_completed);
                holder.tvStatus.setTextColor(Color.parseColor("#10B981")); // Emerald Green
                holder.btnActionPrimary.setVisibility(View.VISIBLE);
                holder.btnActionPrimary.setText("Buy Again");
                break;
            case "berlangsung":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_paid);
                holder.tvStatus.setTextColor(Color.parseColor("#F59E0B")); // Amber/Gold
                holder.btnActionPrimary.setVisibility(View.VISIBLE);
                holder.btnActionPrimary.setText("Track Order");
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_cancelled);
                holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red
                holder.btnActionPrimary.setVisibility(View.GONE);
                break;
        }

        // Action Buttons logic
        holder.btnActionSecondary.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            // Navigate back to Shop
            Intent intent = new Intent(context, com.bagus.toko_baju_uas.PengunjungActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        });

        holder.btnDeleteHistory.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            showDeleteConfirmDialog(item, holder.getBindingAdapterPosition());
        });
    }

    private void showDeleteConfirmDialog(OrderModel item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Hapus Riwayat")
                .setMessage("Apakah Anda yakin ingin menghapus riwayat transaksi ini?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteHistory(item.getIdTransaksi(), position))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteHistory(int idTransaksi, int position) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        // Using the same delete order endpoint for customer to delete from their view
        api.deleteOrder(idTransaksi).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show();
                    listHistory.remove(position);
                    notifyItemRemoved(position);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvProductName, tvTotalPrice;
        ImageView ivProductThumb;
        MaterialButton btnActionSecondary, btnActionPrimary, btnDeleteHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            ivProductThumb = itemView.findViewById(R.id.ivProductThumb);
            btnActionSecondary = itemView.findViewById(R.id.btnActionSecondary);
            btnActionPrimary = itemView.findViewById(R.id.btnActionPrimary);
            btnDeleteHistory = itemView.findViewById(R.id.btnDeleteHistory);
        }
    }
}

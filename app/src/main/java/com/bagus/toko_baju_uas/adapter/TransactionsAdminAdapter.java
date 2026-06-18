package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.model.OrderModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsAdminAdapter extends RecyclerView.Adapter<TransactionsAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderModel> list;
    private final Runnable onStatusUpdated;

    public TransactionsAdminAdapter(Context context, List<OrderModel> list, Runnable onStatusUpdated) {
        this.context = context;
        this.list = list;
        this.onStatusUpdated = onStatusUpdated;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = list.get(position);

        holder.tvOrderId.setText("ORD-" + order.getIdTransaksi());
        holder.tvOrderDate.setText(order.getTanggal());
        holder.tvCustomerName.setText(order.getNama());
        holder.tvCustomerEmail.setText(order.getEmail() != null ? order.getEmail() : "-");
        holder.tvShippingAddress.setText(order.getAlamat() != null ? order.getAlamat() : "-");
        holder.tvStatusBadge.setText(order.getStatus());

        // Dynamic status badge color
        switch (order.getStatus().toLowerCase()) {
            case "selesai":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_status_completed);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_completed));
                break;
            case "berlangsung":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_status_paid);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_paid));
                break;
            case "gagal":
            case "dibatalkan":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_status_cancelled);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_cancelled));
                break;
        }

        Locale localeID = Locale.forLanguageTag("id-ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvTotalAmount.setText(formatRupiah.format(order.getTotalHarga()));

        holder.itemView.setOnClickListener(v -> {
            if (order.getStatus().equalsIgnoreCase("Berlangsung")) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Konfirmasi Transaksi")
                        .setMessage("Apakah Anda ingin menyelesaikan atau membatalkan transaksi ini?")
                        .setPositiveButton("Selesai", (dialog, which) -> {
                            updateStatus(order.getIdTransaksi(), "Selesai");
                        })
                        .setNegativeButton("Batalkan", (dialog, which) -> {
                            updateStatus(order.getIdTransaksi(), "Dibatalkan");
                        })
                        .setNeutralButton("Tutup", null)
                        .show();
            } else {
                Toast.makeText(context, "Status transaksi ini: " + order.getStatus(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(int idTransaksi, String status) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateTransaksi(idTransaksi, status).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(context, "Transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    if (onStatusUpdated != null) {
                        onStatusUpdated.run();
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Gagal memperbarui transaksi";
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Koneksi internet error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvStatusBadge, tvCustomerName, tvCustomerEmail, tvShippingAddress, tvTotalAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvShippingAddress = itemView.findViewById(R.id.tvShippingAddress);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }
    }
}
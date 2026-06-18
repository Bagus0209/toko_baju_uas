package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.model.OrderModel;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsAdminAdapter extends RecyclerView.Adapter<TransactionsAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderModel> list;

    public TransactionsAdminAdapter(Context context, List<OrderModel> list) {
        this.context = context;
        this.list = list;
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
        holder.tvStatusBadge.setText(order.getStatus());

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvTotalAmount.setText(formatRupiah.format(order.getTotalHarga()));

        holder.btnUpdateStatus.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            showStatusUpdateDialog(order, position);
        });

        holder.btnDeleteOrder.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            showDeleteConfirmDialog(order, position);
        });
    }

    private void showStatusUpdateDialog(OrderModel order, int position) {
        String[] statuses = {"Selesai", "Berlangsung", "Gagal"};
        new AlertDialog.Builder(context)
                .setTitle("Update Order Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    updateStatus(order.getIdTransaksi(), newStatus, position);
                })
                .show();
    }

    private void updateStatus(int idTransaksi, String status, int position) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateOrderStatus(idTransaksi, status).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(context, "Status updated!", Toast.LENGTH_SHORT).show();
                    // In a real app, you might want to update the local list and notify
                    // Or reload the whole list from Activity
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(OrderModel order, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Order")
                .setMessage("Are you sure you want to delete this order record?")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrder(order.getIdTransaksi(), position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteOrder(int idTransaksi, int position) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.deleteOrder(idTransaksi).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(context, "Order deleted!", Toast.LENGTH_SHORT).show();
                    list.remove(position);
                    notifyItemRemoved(position);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvStatusBadge, tvCustomerName, tvTotalAmount;
        MaterialButton btnUpdateStatus, btnDeleteOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnDeleteOrder = itemView.findViewById(R.id.btnDeleteOrder);
        }
    }
}
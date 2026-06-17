package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.model.OrderModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvStatusBadge, tvCustomerName, tvTotalAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }
    }
}
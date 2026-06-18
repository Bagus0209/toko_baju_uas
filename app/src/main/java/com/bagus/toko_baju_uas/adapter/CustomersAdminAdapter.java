package com.bagus.toko_baju_uas.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.model.UserModel;

import java.util.List;

public class CustomersAdminAdapter extends RecyclerView.Adapter<CustomersAdminAdapter.ViewHolder> {

    private final List<UserModel> list;

    public CustomersAdminAdapter(List<UserModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = list.get(position);
        holder.tvName.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());
        
        // Show Customer ID
        holder.tvCustomerId.setText("ID: #" + user.getIdUser());

        // Set Avatar Initial
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            holder.tvAvatarInitial.setText(user.getUsername().substring(0, 1).toUpperCase());
        }

        // Set Role Badge
        if (user.getRole() != null) {
            String role = user.getRole().toUpperCase();
            holder.tvRoleBadge.setText(role);
            
            if (role.equals("ADMIN")) {
                holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_pill_status_paid);
                holder.tvRoleBadge.setTextColor(Color.parseColor("#854D0E")); // Dark Gold
            } else {
                holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_pill_gray);
                holder.tvRoleBadge.setTextColor(Color.parseColor("#374151")); // Gray
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvAvatarInitial, tvRoleBadge, tvCustomerId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCustName);
            tvEmail = itemView.findViewById(R.id.tvCustEmail);
            tvAvatarInitial = itemView.findViewById(R.id.tvAvatarInitial);
            tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
            tvCustomerId = itemView.findViewById(R.id.tvCustomerId);
        }
    }
}
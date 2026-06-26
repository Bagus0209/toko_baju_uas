package com.bagus.toko_baju_uas.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.model.UserModel;
import com.bagus.toko_baju_uas.util.AnimationUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                holder.btnDelete.setVisibility(View.GONE); // Admin cannot delete other admins from here easily
            } else {
                holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_pill_gray);
                holder.tvRoleBadge.setTextColor(Color.parseColor("#374151")); // Gray
                holder.btnDelete.setVisibility(View.VISIBLE);
            }
        }

        holder.btnDelete.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Hapus Pelanggan")
                    .setMessage("Apakah Anda yakin ingin menghapus pelanggan " + user.getUsername() + "?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        deleteUser(user.getUidFirebase(), currentPos, v.getContext());
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void deleteUser(String uid, int position, android.content.Context context) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.deleteCustomer(uid).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(context, "Pelanggan berhasil dihapus", Toast.LENGTH_SHORT).show();
                    list.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, list.size());
                } else {
                    Toast.makeText(context, "Gagal menghapus pelanggan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Kesalahan jaringan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvAvatarInitial, tvRoleBadge, tvCustomerId;
        ImageButton btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCustName);
            tvEmail = itemView.findViewById(R.id.tvCustEmail);
            tvAvatarInitial = itemView.findViewById(R.id.tvAvatarInitial);
            tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
            tvCustomerId = itemView.findViewById(R.id.tvCustomerId);
            btnDelete = itemView.findViewById(R.id.btnDeleteCustomer);
        }
    }
}
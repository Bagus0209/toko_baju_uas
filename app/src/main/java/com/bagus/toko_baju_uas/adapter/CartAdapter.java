package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.model.CartModel;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartModel> cartList;
    private final OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(Context context, List<CartModel> cartList, OnCartChangeListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartModel item = cartList.get(position);

        holder.tvCartName.setText(item.getNamaBarang());
        holder.tvCartQty.setText(String.valueOf(item.getJumlah()));

        Locale localeID = Locale.forLanguageTag("id-ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvCartPrice.setText(formatRupiah.format(item.getHarga()));

        Glide.with(context)
                .load(item.getGambar())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivCartItem);

        // Click Listeners with Animations
        holder.btnPlus.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            updateCartQuantity(item.getIdCart(), "increase", holder, item, holder.getBindingAdapterPosition());
        });

        holder.btnMinus.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            updateCartQuantity(item.getIdCart(), "decrease", holder, item, holder.getBindingAdapterPosition());
        });

        holder.btnDelete.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            deleteFromCart(item.getIdCart(), holder.getBindingAdapterPosition());
        });
    }

    private void updateCartQuantity(int idCart, String action, ViewHolder holder, CartModel item, int position) {
        if (position == RecyclerView.NO_POSITION) return;

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateCartQty(idCart, action).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        int currentQty = item.getJumlah();
                        int newQty = action.equals("increase") ? currentQty + 1 : currentQty - 1;
                        item.setJumlah(newQty);
                        holder.tvCartQty.setText(String.valueOf(newQty));
                        if (listener != null) listener.onCartChanged();
                    } else {
                        android.widget.Toast.makeText(context, response.body().getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.widget.Toast.makeText(context, "Gagal memperbarui jumlah", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                android.widget.Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFromCart(int idCart, int position) {
        if (position == RecyclerView.NO_POSITION) return;
        
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.deleteCart(idCart).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    if (position < cartList.size()) {
                        cartList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, cartList.size());
                        if (listener != null) listener.onCartChanged();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {}
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartItem;
        TextView tvCartName, tvCartPrice, tvCartQty;
        ImageButton btnDelete, btnPlus, btnMinus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartItem = itemView.findViewById(R.id.ivCartItem);
            tvCartName = itemView.findViewById(R.id.tvCartName);
            tvCartPrice = itemView.findViewById(R.id.tvCartPrice);
            tvCartQty = itemView.findViewById(R.id.tvCartQty);
            btnDelete = itemView.findViewById(R.id.btnDeleteCart);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}
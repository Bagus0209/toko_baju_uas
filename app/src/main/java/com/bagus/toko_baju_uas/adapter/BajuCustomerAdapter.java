package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BajuCustomerAdapter extends RecyclerView.Adapter<BajuCustomerAdapter.ViewHolder> {

    private final Context context;
    private final List<BajuModel> listBaju;

    public BajuCustomerAdapter(Context context, List<BajuModel> listBaju) {
        this.context = context;
        this.listBaju = listBaju;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BajuModel baju = listBaju.get(position);

        holder.tvProductName.setText(baju.getNamaBarang());

        // Format price to Rupiah
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvProductPrice.setText(formatRupiah.format(baju.getHarga()));

        // Load image using Glide
        Glide.with(context)
                .load(baju.getGambar())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivProductImage);

        // Add action button logic
        holder.btnAdd.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            Toast.makeText(context, context.getString(R.string.buy_toast_format, baju.getNamaBarang()), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listBaju.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        ImageButton btnAdd;
        TextView tvProductName, tvProductPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}

package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BajuUserAdapter extends RecyclerView.Adapter<BajuUserAdapter.ViewHolder> {

    private final Context context;
    private final List<BajuModel> listBaju;

    public BajuUserAdapter(Context context, List<BajuModel> listBaju) {
        this.context = context;
        this.listBaju = listBaju;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menyambungkan dengan desain kartu Pengunjung
        View view = LayoutInflater.from(context).inflate(R.layout.item_baju_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BajuModel baju = listBaju.get(position);

        holder.tvNamaBaju.setText(baju.getNamaBarang());
        holder.tvStokBaju.setText(context.getString(R.string.stock_format, baju.getStok()));

        // Format angka menjadi Rupiah
        Locale localeID = Locale.forLanguageTag("id-ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvHargaBaju.setText(formatRupiah.format(baju.getHarga()));

        // Menampilkan Gambar dengan Glide
        Glide.with(context)
                .load(baju.getGambar())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivGambarBaju);

        // Aksi Tombol Beli (Untuk saat ini kita beri efek Toast saja)
        holder.btnBeli.setOnClickListener(v -> {
            Toast.makeText(context, context.getString(R.string.buy_toast_format, baju.getNamaBarang()), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listBaju.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaBaju, tvHargaBaju, tvStokBaju;
        ImageView ivGambarBaju;
        MaterialButton btnBeli;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inisialisasi ID yang sesuai dengan item_baju_user.xml
            tvNamaBaju = itemView.findViewById(R.id.tvNamaBajuUser);
            tvHargaBaju = itemView.findViewById(R.id.tvHargaBajuUser);
            tvStokBaju = itemView.findViewById(R.id.tvStokBajuUser);
            ivGambarBaju = itemView.findViewById(R.id.ivGambarBajuUser);
            btnBeli = itemView.findViewById(R.id.btnBeli);
        }
    }
}
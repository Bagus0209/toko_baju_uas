package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class BajuAdminAdapter extends RecyclerView.Adapter<BajuAdminAdapter.ViewHolder> {

    private Context context;
    private List<BajuModel> listBaju;

    public BajuAdminAdapter(Context context, List<BajuModel> listBaju) {
        this.context = context;
        this.listBaju = listBaju;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menyambungkan adapter dengan cetakan XML item_baju_admin
        View view = LayoutInflater.from(context).inflate(R.layout.item_baju_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Mengambil data per baris
        BajuModel baju = listBaju.get(position);

        // Memasukkan teks ke dalam UI
        holder.tvIdBaju.setText("#PRD-" + baju.getIdBarang());
        holder.tvNamaBaju.setText(baju.getNamaBarang());
        holder.tvStokBaju.setText(String.valueOf(baju.getStok()));

        // Format angka ke Rupiah agar terlihat profesional
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvHargaBaju.setText(formatRupiah.format(baju.getHarga()));

        // Logika Pintar untuk Indikator Stok (Warna Titik)
        if (baju.getStok() < 10) {
            holder.cvIndikatorStok.setCardBackgroundColor(Color.parseColor("#EF4444")); // Merah (Kritis)
        } else if (baju.getStok() < 30) {
            holder.cvIndikatorStok.setCardBackgroundColor(Color.parseColor("#F59E0B")); // Oranye (Peringatan)
        } else {
            holder.cvIndikatorStok.setCardBackgroundColor(Color.parseColor("#10B981")); // Hijau (Aman)
        }

        // Memuat gambar menggunakan Glide (Sementara pakai ikon bawaan sampai folder gambar di XAMPP siap)
        Glide.with(context)
                .load(baju.getGambar())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivGambarBaju);

        // Aksi ketika tombol Edit diklik (Nanti disambungkan ke halaman Edit)
        holder.btnEdit.setOnClickListener(v -> {
            Toast.makeText(context, "Membuka edit untuk: " + baju.getNamaBarang(), Toast.LENGTH_SHORT).show();
        });

        // Aksi ketika tombol Hapus diklik (Nanti disambungkan ke API Hapus)
        holder.btnHapus.setOnClickListener(v -> {
            Toast.makeText(context, "Fitur hapus belum aktif untuk: " + baju.getNamaBarang(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listBaju.size();
    }

    // Class untuk mengenalkan komponen ID dari XML ke Java
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdBaju, tvNamaBaju, tvHargaBaju, tvStokBaju;
        ImageView ivGambarBaju;
        CardView cvIndikatorStok;
        ImageButton btnEdit, btnHapus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdBaju = itemView.findViewById(R.id.tvIdBaju);
            tvNamaBaju = itemView.findViewById(R.id.tvNamaBaju);
            tvHargaBaju = itemView.findViewById(R.id.tvHargaBaju);
            tvStokBaju = itemView.findViewById(R.id.tvStokBaju);
            ivGambarBaju = itemView.findViewById(R.id.ivGambarBaju);
            cvIndikatorStok = itemView.findViewById(R.id.cvIndikatorStok);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnHapus = itemView.findViewById(R.id.btnHapus);
        }
    }
}
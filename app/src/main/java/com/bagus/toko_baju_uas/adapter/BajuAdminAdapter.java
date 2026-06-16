package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.EditProdukActivity;
import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_baju_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BajuModel baju = listBaju.get(position);

        holder.tvIdBaju.setText("#PRD-" + baju.getIdBarang());
        holder.tvNamaBaju.setText(baju.getNamaBarang());
        holder.tvStokBaju.setText(String.valueOf(baju.getStok()));

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvHargaBaju.setText(formatRupiah.format(baju.getHarga()));

        if (baju.getStok() < 10) {
            holder.cvIndikatorStok.setCardBackgroundColor(Color.parseColor("#EF4444"));
        } else if (baju.getStok() < 30) {
            holder.cvIndikatorStok.setCardBackgroundColor(Color.parseColor("#F59E0B"));
        } else {
            holder.cvIndikatorStok.setCardBackgroundColor(Color.parseColor("#10B981"));
        }

        Glide.with(context)
                .load(baju.getGambar())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivGambarBaju);

        // TOMBOL EDIT (Kirim data ke EditProdukActivity)
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditProdukActivity.class);
            intent.putExtra("id_barang", baju.getIdBarang());
            intent.putExtra("nama_barang", baju.getNamaBarang());
            intent.putExtra("harga", baju.getHarga());
            intent.putExtra("stok", baju.getStok());
            context.startActivity(intent);
        });

        // TOMBOL HAPUS
        holder.btnHapus.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            BajuModel bajuTarget = listBaju.get(currentPosition);

            new AlertDialog.Builder(context)
                    .setTitle("Hapus Produk")
                    .setMessage("Apakah Anda yakin ingin menghapus " + bajuTarget.getNamaBarang() + "?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
                        api.hapusBarang(bajuTarget.getIdBarang()).enqueue(new Callback<BaseResponse>() {
                            @Override
                            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                    Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    listBaju.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                } else {
                                    Toast.makeText(context, "Gagal menghapus produk", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<BaseResponse> call, Throwable t) {
                                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listBaju.size();
    }

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
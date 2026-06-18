package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProdukActivity extends AppCompatActivity {

    private TextInputEditText etEditNamaBaju, etEditHarga, etEditStok, etEditGambar;
    private int idBarang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produk);

        // UI Initialization
        etEditNamaBaju = findViewById(R.id.etEditNamaBaju);
        etEditHarga = findViewById(R.id.etEditHarga);
        etEditStok = findViewById(R.id.etEditStok);
        etEditGambar = findViewById(R.id.etEditGambar);
        ImageView ivEditPreview = findViewById(R.id.ivEditPreview);
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan);

        // Load Data from Intent
        if (getIntent().getExtras() != null) {
            idBarang = getIntent().getIntExtra("id_barang", 0);
            etEditNamaBaju.setText(getIntent().getStringExtra("nama_barang"));
            etEditHarga.setText(String.valueOf(getIntent().getIntExtra("harga", 0)));
            etEditStok.setText(String.valueOf(getIntent().getIntExtra("stok", 0)));
            
            String gambar = getIntent().getStringExtra("gambar");
            etEditGambar.setText(gambar);
            
            // Premium Preview
            String baseUrl = ApiClient.IP_LAPTOP; // Assuming this is used for full URL construction
            Glide.with(this)
                .load("http://" + baseUrl + "/api_tokobaju/images/" + gambar)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivEditPreview);
        }

        // Back Button
        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            onBackPressed();
        });

        // Save Button
        btnSimpanPerubahan.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            saveChanges();
        });
    }

    private void saveChanges() {
        String namaBaru = Objects.requireNonNull(etEditNamaBaju.getText()).toString().trim();
        String hargaStr = Objects.requireNonNull(etEditHarga.getText()).toString().trim();
        String stokStr = Objects.requireNonNull(etEditStok.getText()).toString().trim();
        String gambarBaru = Objects.requireNonNull(etEditGambar.getText()).toString().trim();

        if (namaBaru.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int hargaBaru = Integer.parseInt(hargaStr);
            int stokBaru = Integer.parseInt(stokStr);
            if (gambarBaru.isEmpty()) gambarBaru = "default.jpg";

            ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
            api.updateBarang(idBarang, namaBaru, hargaBaru, stokBaru, gambarBaru).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Toast.makeText(EditProdukActivity.this, "Collection updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProdukActivity.this, "Failed to update collection", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                    Toast.makeText(EditProdukActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahProdukActivity extends AppCompatActivity {

    private TextInputEditText etNamaBaju, etHarga, etStok, etGambar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_tambah_produk);

        etNamaBaju = findViewById(R.id.etNamaBaju);
        etHarga = findViewById(R.id.etHarga);
        etStok = findViewById(R.id.etStok);
        etGambar = findViewById(R.id.etGambar);
        MaterialButton btnSimpan = findViewById(R.id.btnSimpan);

        btnSimpan.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            String nama = etNamaBaju.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();
            String stokStr = etStok.getText().toString().trim();
            String gambar = etGambar.getText().toString().trim();

            if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(this, "Nama, Harga, dan Stok wajib diisi!", Toast.LENGTH_SHORT).show();
            } else {
                int harga = Integer.parseInt(hargaStr);
                int stok = Integer.parseInt(stokStr);
                if (gambar.isEmpty()) gambar = "default.jpg";

                ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
                api.tambahBarang(nama, harga, stok, gambar).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Toast.makeText(TambahProdukActivity.this, "Berhasil tambah barang!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(TambahProdukActivity.this, "Gagal tambah barang", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(TambahProdukActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
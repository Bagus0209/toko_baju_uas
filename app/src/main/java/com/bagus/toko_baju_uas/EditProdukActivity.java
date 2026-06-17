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

public class EditProdukActivity extends AppCompatActivity {

    private TextInputEditText etEditNamaBaju, etEditHarga, etEditStok, etEditGambar;
    private int idBarang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_edit_produk);

        etEditNamaBaju = findViewById(R.id.etEditNamaBaju);
        etEditHarga = findViewById(R.id.etEditHarga);
        etEditStok = findViewById(R.id.etEditStok);
        etEditGambar = findViewById(R.id.etEditGambar);
        MaterialButton btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan);

        if (getIntent().getExtras() != null) {
            idBarang = getIntent().getIntExtra("id_barang", 0);
            etEditNamaBaju.setText(getIntent().getStringExtra("nama_barang"));
            etEditHarga.setText(String.valueOf(getIntent().getIntExtra("harga", 0)));
            etEditStok.setText(String.valueOf(getIntent().getIntExtra("stok", 0)));
            etEditGambar.setText(getIntent().getStringExtra("gambar"));
        }

        btnSimpanPerubahan.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            String namaBaru = etEditNamaBaju.getText().toString().trim();
            String hargaStr = etEditHarga.getText().toString().trim();
            String stokStr = etEditStok.getText().toString().trim();
            String gambarBaru = etEditGambar.getText().toString().trim();

            if (namaBaru.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(this, "Semua kolom tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            } else {
                int hargaBaru = Integer.parseInt(hargaStr);
                int stokBaru = Integer.parseInt(stokStr);
                if (gambarBaru.isEmpty()) gambarBaru = "default.jpg";

                ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
                api.updateBarang(idBarang, namaBaru, hargaBaru, stokBaru, gambarBaru).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Toast.makeText(EditProdukActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditProdukActivity.this, "Gagal memperbarui data produk", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(EditProdukActivity.this, "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProdukActivity extends AppCompatActivity {

    private TextInputEditText etEditNamaBaju, etEditHarga, etEditStok;
    private MaterialButton btnSimpanPerubahan;
    private int idBarang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produk);

        // 1. Inisialisasi Komponen UI
        etEditNamaBaju = findViewById(R.id.etEditNamaBaju);
        etEditHarga = findViewById(R.id.etEditHarga);
        etEditStok = findViewById(R.id.etEditStok);
        btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan);

        // 2. Tangkap data lama yang dikirim dari Adapter
        if (getIntent().getExtras() != null) {
            idBarang = getIntent().getIntExtra("id_barang", 0);
            String namaLama = getIntent().getStringExtra("nama_barang");
            int hargaLama = getIntent().getIntExtra("harga", 0);
            int stokLama = getIntent().getIntExtra("stok", 0);

            // Tampilkan data lama ke dalam kolom input form
            etEditNamaBaju.setText(namaLama);
            etEditHarga.setText(String.valueOf(hargaLama));
            etEditStok.setText(String.valueOf(stokLama));
        }

        // 3. Logika Klik Tombol Simpan Perubahan
        btnSimpanPerubahan.setOnClickListener(v -> {
            String namaBaru = etEditNamaBaju.getText().toString();
            String hargaStr = etEditHarga.getText().toString();
            String stokStr = etEditStok.getText().toString();

            if (namaBaru.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(this, "Semua kolom tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            } else {
                int hargaBaru = Integer.parseInt(hargaStr);
                int stokBaru = Integer.parseInt(stokStr);

                // Jalankan perintah update ke database lewat API
                ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
                api.updateBarang(idBarang, namaBaru, hargaBaru, stokBaru).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Toast.makeText(EditProdukActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            finish(); // Tutup halaman dan kembali ke dasbor
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
package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.BajuAdminAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bagus.toko_baju_uas.model.BarangResponse;
import com.google.android.material.button.MaterialButton;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvBajuAdmin;
    private MaterialButton btnTambahProduk;
    private ImageButton btnMenu;
    private DrawerLayout drawerLayout;
    private BajuAdminAdapter adapter;
    private List<BajuModel> listBaju = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // 1. Kenalkan komponen dari XML
        rvBajuAdmin = findViewById(R.id.rvBajuAdmin);
        btnTambahProduk = findViewById(R.id.btnTambahProduk);
        btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);

        // 2. Atur bentuk daftar menjadi vertikal (atas ke bawah)
        rvBajuAdmin.setLayoutManager(new LinearLayoutManager(this));

        // 3. Panggil data dari server (XAMPP)
        fetchDataBarang();

        // 4. Perintah untuk tombol Tambah Produk
        btnTambahProduk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, TambahProdukActivity.class);
                startActivity(intent);
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
            }
        });
    }

    private void fetchDataBarang() {
        // Memanggil API get_barang.php
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<BarangResponse> call = apiInterface.getBarang();

        call.enqueue(new Callback<BarangResponse>() {
            @Override
            public void onResponse(Call<BarangResponse> call, Response<BarangResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    boolean status = response.body().isStatus();

                    if (status) {
                        // Jika berhasil, ambil datanya dan masukkan ke List
                        listBaju = response.body().getData();

                        // Pasang List ke dalam Adapter
                        adapter = new BajuAdminAdapter(AdminActivity.this, listBaju);

                        // Pasang Adapter ke RecyclerView
                        rvBajuAdmin.setAdapter(adapter);
                    } else {
                        Toast.makeText(AdminActivity.this, "Belum ada data produk", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminActivity.this, "Gagal mengambil data dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BarangResponse> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fungsi ini agar saat kita kembali dari halaman tambah/edit, data otomatis diperbarui
    @Override
    protected void onResume() {
        super.onResume();
        fetchDataBarang();
    }
}
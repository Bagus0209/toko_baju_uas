package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.BajuAdminAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bagus.toko_baju_uas.model.BarangResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView rvBajuAdmin;
    private DrawerLayout drawerLayout;
    private BajuAdminAdapter adapter;
    private List<BajuModel> listBaju = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        rvBajuAdmin = findViewById(R.id.rvBajuAdmin);
        MaterialButton btnTambahProduk = findViewById(R.id.btnTambahProduk);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);

        rvBajuAdmin.setLayoutManager(new LinearLayoutManager(this));
        
        fetchDataBarang();

        btnTambahProduk.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(InventoryActivity.this, TambahProdukActivity.class));
        });

        btnMenu.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            drawerLayout.openDrawer(GravityCompat.START);
        });

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminActivity.class));
                finish();
            } else if (id == R.id.nav_inventory) {
                // Already here
                fetchDataBarang();
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, TransactionsAdminActivity.class));
            } else if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomersAdminActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void fetchDataBarang() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        apiInterface.getBarang().enqueue(new Callback<BarangResponse>() {
            @Override
            public void onResponse(@NonNull Call<BarangResponse> call, @NonNull Response<BarangResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listBaju = response.body().getData();
                    adapter = new BajuAdminAdapter(InventoryActivity.this, listBaju);
                    rvBajuAdmin.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(@NonNull Call<BarangResponse> call, @NonNull Throwable t) {
                Toast.makeText(InventoryActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(InventoryActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataBarang();
    }
}
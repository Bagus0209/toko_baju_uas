package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.bagus.toko_baju_uas.model.AdminStatsResponse;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bagus.toko_baju_uas.model.BarangResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvBajuAdmin;
    private DrawerLayout drawerLayout;
    private BajuAdminAdapter adapter;
    private List<BajuModel> listBaju = new ArrayList<>();
    
    private TextView tvTotalProduk, tvTotalOrders, tvTotalRevenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_admin);

        rvBajuAdmin = findViewById(R.id.rvBajuAdmin);
        MaterialButton btnTambahProduk = findViewById(R.id.btnTambahProduk);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        
        tvTotalProduk = findViewById(R.id.tvTotalProduk); 
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);

        rvBajuAdmin.setLayoutManager(new LinearLayoutManager(this));
        
        loadAdminDashboard();
        fetchDataBarang();

        btnTambahProduk.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            startActivity(new Intent(AdminActivity.this, TambahProdukActivity.class));
        });

        btnMenu.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            drawerLayout.openDrawer(GravityCompat.START);
        });

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_dashboard) {
                // Already here, just refresh
                loadAdminDashboard();
                fetchDataBarang();
            } else if (id == R.id.nav_inventory) {
                // Focus on inventory list
                rvBajuAdmin.smoothScrollToPosition(0);
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, TransactionsAdminActivity.class));
            } else if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomersAdminActivity.class));
            } else if (id == R.id.nav_logout) {
                logout();
            } else {
                Toast.makeText(this, "Fitur " + item.getTitle() + " akan segera hadir!", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadAdminDashboard() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getAdminStats().enqueue(new Callback<AdminStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminStatsResponse> call, @NonNull Response<AdminStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    AdminStatsResponse.Data data = response.body().getData();
                    tvTotalProduk.setText(String.valueOf(data.total_products));
                    tvTotalOrders.setText(String.valueOf(data.total_orders));
                    
                    Locale localeID = new Locale("in", "ID");
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                    tvTotalRevenue.setText(formatRupiah.format(data.total_revenue));
                }
            }
            @Override
            public void onFailure(@NonNull Call<AdminStatsResponse> call, @NonNull Throwable t) {}
        });
    }

    private void fetchDataBarang() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        apiInterface.getBarang().enqueue(new Callback<BarangResponse>() {
            @Override
            public void onResponse(@NonNull Call<BarangResponse> call, @NonNull Response<BarangResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listBaju = response.body().getData();
                    adapter = new BajuAdminAdapter(AdminActivity.this, listBaju);
                    rvBajuAdmin.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(@NonNull Call<BarangResponse> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataBarang();
        loadAdminDashboard();
    }
}
package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.BajuCustomerAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bagus.toko_baju_uas.model.BarangResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PengunjungActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private ChipGroup chipGroup;
    private RecyclerView rvProducts;
    
    private BajuCustomerAdapter adapter;
    private List<BajuModel> allProducts = new ArrayList<>();
    private List<BajuModel> filteredProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pengunjung);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Elements
        etSearch = findViewById(R.id.etSearch);
        chipGroup = findViewById(R.id.chipGroup);
        rvProducts = findViewById(R.id.rvProducts);

        // Setup RecyclerView Adapter
        adapter = new BajuCustomerAdapter(this, filteredProducts);
        rvProducts.setAdapter(adapter);

        // Setup Bottom Navigation View
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_shop);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_history) {
                Intent intent = new Intent(this, PaymentHistoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_shop) {
                return true;
            } else if (itemId == R.id.nav_search || itemId == R.id.nav_bag || itemId == R.id.nav_profile) {
                Toast.makeText(this, item.getTitle() + " (Fitur segera hadir!)", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Fetch Data from Server
        loadProducts();

        // Register Filter and Search Listeners
        setupListeners();
    }

    private void loadProducts() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getBarang().enqueue(new Callback<BarangResponse>() {
            @Override
            public void onResponse(@NonNull Call<BarangResponse> call, @NonNull Response<BarangResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    allProducts = response.body().getData();
                    filteredProducts.clear();
                    filteredProducts.addAll(allProducts);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(PengunjungActivity.this, "Gagal mengambil data produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BarangResponse> call, @NonNull Throwable t) {
                Toast.makeText(PengunjungActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Real-time search watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip selection changes
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterProducts());
    }

    private void filterProducts() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase().trim() : "";
        
        int checkedChipId = chipGroup.getCheckedChipId();
        String category = "all";
        Chip selectedChip = chipGroup.findViewById(checkedChipId);
        if (selectedChip != null) {
            category = selectedChip.getText().toString().toLowerCase();
        }

        filteredProducts.clear();
        for (BajuModel product : allProducts) {
            boolean matchesSearch = product.getNamaBarang().toLowerCase().contains(query);
            boolean matchesCategory = true;

            if (!category.equals("all")) {
                String productName = product.getNamaBarang().toLowerCase();
                if (category.equals("jackets")) {
                    matchesCategory = productName.contains("jacket") || productName.contains("jaket");
                } else if (category.equals("shirts")) {
                    matchesCategory = productName.contains("shirt") || productName.contains("kemeja") || productName.contains("kaos") || productName.contains("t-shirt");
                } else if (category.equals("pants")) {
                    matchesCategory = productName.contains("celana") || productName.contains("pant") || productName.contains("trouser");
                } else if (category.equals("dresses")) {
                    matchesCategory = productName.contains("dress") || productName.contains("gaun") || productName.contains("rok");
                } else {
                    matchesCategory = false;
                }
            }

            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_shop);
        }
    }
}
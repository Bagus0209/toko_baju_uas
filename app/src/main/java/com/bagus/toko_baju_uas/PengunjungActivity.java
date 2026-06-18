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
import com.bagus.toko_baju_uas.util.AnimationUtil;
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
    private BajuCustomerAdapter adapter;
    private List<BajuModel> allProducts = new ArrayList<>();
    private final List<BajuModel> filteredProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        super.setContentView(R.layout.activity_pengunjung);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI Initialization
        etSearch = findViewById(R.id.etSearch);
        chipGroup = findViewById(R.id.chipGroup);
        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabBag = findViewById(R.id.fabBag);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Adapter Setup
        adapter = new BajuCustomerAdapter(this, filteredProducts);
        rvProducts.setAdapter(adapter);

        // FAB Cart
        if (fabBag != null) {
            fabBag.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                startActivity(new Intent(this, CartActivity.class));
            });
        }

        // Bottom Nav
        bottomNavigation.setSelectedItemId(R.id.nav_shop);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_shop) {
                if (etSearch != null) {
                    etSearch.clearFocus();
                }
                return true;
            } else if (itemId == R.id.nav_search) {
                focusSearchField();
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, PaymentHistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_bag) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });

        loadProducts();
        setupListeners();
        
        handleIntent(getIntent());
    }

    private void loadProducts() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getBarang().enqueue(new Callback<BarangResponse>() {
            @Override
            public void onResponse(@NonNull Call<BarangResponse> call, @NonNull Response<BarangResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    allProducts = response.body().getData();
                    filterProducts();
                } else {
                    Toast.makeText(PengunjungActivity.this, "Gagal mengambil data produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BarangResponse> call, @NonNull Throwable t) {
                Toast.makeText(PengunjungActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterProducts(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            BottomNavigationView navigation = findViewById(R.id.bottomNavigation);
            if (navigation != null) {
                if (hasFocus) {
                    navigation.setSelectedItemId(R.id.nav_search);
                } else {
                    navigation.setSelectedItemId(R.id.nav_shop);
                }
            }
        });
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterProducts());
    }

    private void filterProducts() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase().trim() : "";
        int checkedChipId = chipGroup.getCheckedChipId();
        String category = "all";
        Chip selectedChip = findViewById(checkedChipId);
        if (selectedChip != null) {
            category = selectedChip.getText().toString().toLowerCase();
        }

        filteredProducts.clear();
        for (BajuModel product : allProducts) {
            String name = product.getNamaBarang().toLowerCase();
            boolean matchesSearch = name.contains(query);
            boolean matchesCategory = category.equals("all");

            if (!matchesCategory) {
                if (category.contains("jacket") && (name.contains("jacket") || name.contains("jaket"))) matchesCategory = true;
                else if (category.contains("shirt") && (name.contains("shirt") || name.contains("kemeja") || name.contains("kaos"))) matchesCategory = true;
                else if (category.contains("pant") && (name.contains("pant") || name.contains("celana") || name.contains("trouser"))) matchesCategory = true;
                else if (category.contains("dress") && (name.contains("dress") || name.contains("gaun") || name.contains("rok"))) matchesCategory = true;
            }

            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void focusSearchField() {
        if (etSearch != null) {
            etSearch.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("focus_search", false)) {
            focusSearchField();
            BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_search);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            if (etSearch != null && etSearch.hasFocus()) {
                bottomNavigation.setSelectedItemId(R.id.nav_search);
            } else {
                bottomNavigation.setSelectedItemId(R.id.nav_shop);
            }
        }
    }
}
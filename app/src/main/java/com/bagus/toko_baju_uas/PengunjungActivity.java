package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.bagus.toko_baju_uas.model.UserModel;
import com.bagus.toko_baju_uas.model.UsersResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PengunjungActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private ChipGroup chipGroup;
    private BajuCustomerAdapter adapter;
    private List<BajuModel> allProducts = new ArrayList<>();
    private final List<BajuModel> filteredProducts = new ArrayList<>();
    private boolean isTourRunning = false; // Flag pencegah penumpukan tour

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

        // Help Tour Button
        View btnHelpTour = findViewById(R.id.btnHelpTour);
        if (btnHelpTour != null) {
            btnHelpTour.setOnClickListener(v -> {
                AnimationUtil.animateButtonClick(v);
                startProductTour();
            });
        }

        // Bottom Nav
        bottomNavigation.setSelectedItemId(R.id.nav_shop);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_shop) {
                if (etSearch != null) etSearch.clearFocus();
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
        checkOnboardingStatus();
        handleIntent(getIntent());
    }

    private void checkOnboardingStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getAdminCustomers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (UserModel u : response.body().getData()) {
                        if (u.getEmail() != null && u.getEmail().equals(user.getEmail())) {
                            processOnboardingLogic(u);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {
                android.util.Log.e("OnboardingCheck", "Gagal koneksi: " + t.getMessage());
            }
        });
    }

    private void processOnboardingLogic(UserModel user) {
        // Cek Local Cache dulu: Jika baru saja selesai, jangan balik lagi walau server belum update
        boolean isFinishedLocally = getSharedPreferences("app_settings", MODE_PRIVATE)
                .getBoolean("onboarding_finished_locally", false);
        
        if (isFinishedLocally || user.isOnboardingCompleted() || user.isOnboardingSkipped()) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        if (!today.equals(user.getOnboardingLastSeen()) && user.getOnboardingSeenCount() < 3) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        }
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
                navigation.setSelectedItemId(hasFocus ? R.id.nav_search : R.id.nav_shop);
            }
        });
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterProducts());
    }

    private void filterProducts() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase().trim() : "";
        int checkedChipId = chipGroup.getCheckedChipId();
        String category = "all";
        Chip selectedChip = findViewById(checkedChipId);
        if (selectedChip != null) category = selectedChip.getText().toString().toLowerCase();

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

            if (matchesSearch && matchesCategory) filteredProducts.add(product);
        }
        adapter.notifyDataSetChanged();
    }

    private void focusSearchField() {
        if (etSearch != null) {
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getBooleanExtra("focus_search", false)) {
                focusSearchField();
                BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_search);
            }
            
            if (intent.getBooleanExtra("start_tour", false)) {
                // Beri sedikit delay agar UI siap
                new android.os.Handler().postDelayed(this::startProductTour, 1000);
            }
        }
    }

    private void startProductTour() {
        if (isTourRunning) return; // Jangan jalankan jika sudah jalan
        
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        if (nav == null) return;

        // Mendapatkan view dari masing-masing menu bottom navigation
        View shop = nav.findViewById(R.id.nav_shop);
        View search = nav.findViewById(R.id.nav_search);
        View bag = nav.findViewById(R.id.nav_bag);
        View history = nav.findViewById(R.id.nav_history);
        View profile = nav.findViewById(R.id.nav_profile);

        // Jika view belum ter-inflate (jarang terjadi di onCreate), tour dibatalkan agar tidak crash
        if (shop == null) return;

        isTourRunning = true; // Tandai tour sedang berjalan
        new TapTargetSequence(this)
                .targets(
                    TapTarget.forView(shop, "Katalog Produk", "Lihat semua koleksi fashion mewah kami di sini.")
                            .outerCircleColor(R.color.luxe_gold)
                            .targetRadius(50)
                            .transparentTarget(true)
                            .drawShadow(true),
                    TapTarget.forView(search, "Pencarian Pintar", "Cari baju impian Anda dengan filter kategori.")
                            .outerCircleColor(R.color.luxe_black)
                            .targetRadius(50)
                            .transparentTarget(true),
                    TapTarget.forView(bag, "Keranjang Belanja", "Kelola item yang ingin Anda beli sebelum checkout.")
                            .outerCircleColor(R.color.luxe_gold)
                            .targetRadius(50)
                            .transparentTarget(true),
                    TapTarget.forView(history, "Riwayat Transaksi", "Pantau status pesanan dan pembayaran Anda.")
                            .outerCircleColor(R.color.luxe_black)
                            .targetRadius(50)
                            .transparentTarget(true),
                    TapTarget.forView(profile, "Akun & Alamat", "Atur profil dan alamat pengiriman Anda.")
                            .outerCircleColor(R.color.luxe_gold)
                            .targetRadius(50)
                            .transparentTarget(true)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        isTourRunning = false;
                        Toast.makeText(PengunjungActivity.this, "Tur selesai! Selamat berbelanja.", Toast.LENGTH_SHORT).show();
                        updateOnboardingStatusOnServer("complete");
                    }
                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {}
                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        isTourRunning = false;
                        updateOnboardingStatusOnServer("skip");
                    }
                })
                .start();
    }

    private void updateOnboardingStatusOnServer(String action) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateOnboarding(user.getUid(), action).enqueue(new Callback<com.bagus.toko_baju_uas.model.BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Response<com.bagus.toko_baju_uas.model.BaseResponse> response) {}
            @Override
            public void onFailure(@NonNull Call<com.bagus.toko_baju_uas.model.BaseResponse> call, @NonNull Throwable t) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId((etSearch != null && etSearch.hasFocus()) ? R.id.nav_search : R.id.nav_shop);
        }
    }
}

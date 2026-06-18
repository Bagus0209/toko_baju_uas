package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.HistoryAdapter;
import com.bagus.toko_baju_uas.adapter.HistoryAdapter.HistoryItem;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryActivity extends AppCompatActivity {

    private ImageButton btnBack, btnNotif;
    private ChipGroup chipGroup;
    private RecyclerView rvHistory;
    
    private HistoryAdapter adapter;
    private final List<HistoryItem> allHistory = new ArrayList<>();
    private final List<HistoryItem> filteredHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_payment_history);

        btnBack = findViewById(R.id.btnBack);
        btnNotif = findViewById(R.id.btnNotif);
        chipGroup = findViewById(R.id.tabScroll).findViewById(R.id.chipGroup); // ChipGroup is direct child of ScrollView
        if (chipGroup == null) {
            chipGroup = findViewById(R.id.chipGroup);
        }
        rvHistory = findViewById(R.id.rvHistory);

        // Set LayoutManager
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // Connect Adapter
        adapter = new HistoryAdapter(this, filteredHistory, () -> {
            com.google.firebase.auth.FirebaseUser u = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (u != null) {
                loadRealHistoryData(u.getUid());
            }
        });
        rvHistory.setAdapter(adapter);

        // Listeners
        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });
        btnNotif.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            Toast.makeText(this, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show();
        });
        
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterHistory());

        // Initial Filter
        filterHistory();
    }

    private void generateMockData() {
        String baseUrl = "http://" + com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP + "/toko%20baju/images/";
        allHistory.add(new HistoryItem(1001, "16 Juni 2026", "Selesai", "Midnight Velvet Jacket", 850000, baseUrl + "jacket.jpg"));
        allHistory.add(new HistoryItem(1002, "17 Juni 2026", "Berlangsung", "Classic Oxford Shirt", 450000, baseUrl + "shirt.jpg"));
        allHistory.add(new HistoryItem(1003, "12 Mei 2026", "Gagal", "Vintage Denim Jacket", 750000, baseUrl + "denim.jpg"));
        allHistory.add(new HistoryItem(1004, "05 Mei 2026", "Selesai", "Silk Elegance Dress", 1200000, baseUrl + "dress.jpg"));
    }

    private void filterHistory() {
        int checkedChipId = chipGroup.getCheckedChipId();
        String selectedFilter = "semua";
        Chip selectedChip = chipGroup.findViewById(checkedChipId);
        if (selectedChip != null) {
            selectedFilter = selectedChip.getText().toString().toLowerCase();
        }

        filteredHistory.clear();
        for (HistoryItem item : allHistory) {
            boolean matches = false;
            if (selectedFilter.equals("semua")) {
                matches = true;
            } else if (selectedFilter.equals("berlangsung")) {
                matches = item.getStatus().equalsIgnoreCase("berlangsung");
            } else if (selectedFilter.equals("selesai")) {
                matches = item.getStatus().equalsIgnoreCase("selesai");
            } else if (selectedFilter.equals("gagal")) {
                matches = item.getStatus().equalsIgnoreCase("gagal") || item.getStatus().equalsIgnoreCase("dibatalkan");
            }

            if (matches) {
                filteredHistory.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadRealHistoryData(user.getUid());
        } else {
            generateMockData();
            filterHistory();
        }
    }

    private void loadRealHistoryData(String uid) {
        com.bagus.toko_baju_uas.api.ApiInterface api = com.bagus.toko_baju_uas.api.ApiClient.getClient().create(com.bagus.toko_baju_uas.api.ApiInterface.class);
        api.getHistory(uid).enqueue(new retrofit2.Callback<com.bagus.toko_baju_uas.model.HistoryResponse>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.bagus.toko_baju_uas.model.HistoryResponse> call, @androidx.annotation.NonNull retrofit2.Response<com.bagus.toko_baju_uas.model.HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    allHistory.clear();
                    List<com.bagus.toko_baju_uas.model.HistoryResponse.Data> dataList = response.body().getData();
                    if (dataList != null) {
                        String baseUrl = "http://" + com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP + "/toko%20baju/images/";
                        for (com.bagus.toko_baju_uas.model.HistoryResponse.Data tx : dataList) {
                            String label = "Pesanan #" + tx.id_transaksi;
                            String img = baseUrl + "default.jpg";
                            allHistory.add(new HistoryItem(
                                    tx.id_transaksi,
                                    tx.tanggal,
                                    tx.status,
                                    label,
                                    tx.total_harga,
                                    img
                            ));
                        }
                    }
                    filterHistory();
                } else {
                    Toast.makeText(PaymentHistoryActivity.this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show();
                    generateMockData();
                    filterHistory();
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.bagus.toko_baju_uas.model.HistoryResponse> call, @androidx.annotation.NonNull Throwable t) {
                Toast.makeText(PaymentHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                generateMockData();
                filterHistory();
            }
        });
    }
}

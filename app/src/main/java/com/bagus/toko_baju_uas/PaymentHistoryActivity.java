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
        adapter = new HistoryAdapter(this, filteredHistory);
        rvHistory.setAdapter(adapter);

        // Load Mock Data
        generateMockData();

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
        String baseUrl = "http://" + com.bagus.toko_baju_uas.api.ApiClient.IP_LAPTOP + "/api_tokobaju/images/";
        allHistory.add(new HistoryItem("16 Juni 2026", "Selesai", "Midnight Velvet Jacket", 850000, baseUrl + "jacket.jpg"));
        allHistory.add(new HistoryItem("17 Juni 2026", "Berlangsung", "Classic Oxford Shirt", 450000, baseUrl + "shirt.jpg"));
        allHistory.add(new HistoryItem("12 Mei 2026", "Gagal", "Vintage Denim Jacket", 750000, baseUrl + "denim.jpg"));
        allHistory.add(new HistoryItem("05 Mei 2026", "Selesai", "Silk Elegance Dress", 1200000, baseUrl + "dress.jpg"));
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
}

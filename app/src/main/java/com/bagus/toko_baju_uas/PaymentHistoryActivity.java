package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.HistoryAdapter;
import com.bagus.toko_baju_uas.adapter.HistoryAdapter.HistoryItem;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.HistoryResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentHistoryActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private RecyclerView rvHistory;
    private LinearLayout layoutEmptyState;
    
    private HistoryAdapter adapter;
    private final List<HistoryItem> allHistory = new ArrayList<>();
    private final List<HistoryItem> filteredHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnNotif = findViewById(R.id.btnNotif);
        layoutEmptyState = findViewById(R.id.layoutEmptyState); // Pastikan ID ini ada di XML
        
        chipGroup = findViewById(R.id.chipGroup);
        if (chipGroup == null && findViewById(R.id.tabScroll) != null) {
            chipGroup = findViewById(R.id.tabScroll).findViewById(R.id.chipGroup);
        }
        
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(this, filteredHistory, this::refreshData);
        rvHistory.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnNotif.setOnClickListener(v -> Toast.makeText(this, "Tidak ada notifikasi", Toast.LENGTH_SHORT).show());
        
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterHistory());
        }
    }

    private void refreshData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadRealHistoryData(user.getUid());
        }
    }

    private void filterHistory() {
        if (chipGroup == null) return;
        
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

            if (matches) filteredHistory.add(item);
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyStateVisibility();
    }

    private void updateEmptyStateVisibility() {
        if (filteredHistory.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bersihkan data lama agar tidak tertukar saat ganti akun
        allHistory.clear();
        filteredHistory.clear();
        adapter.notifyDataSetChanged();
        
        refreshData();
    }

    private void loadRealHistoryData(String uid) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getHistory(uid).enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistoryResponse> call, @NonNull Response<HistoryResponse> response) {
                allHistory.clear();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<HistoryResponse.Data> dataList = response.body().getData();
                    if (dataList != null && !dataList.isEmpty()) {
                        String baseUrl = "http://" + ApiClient.IP_LAPTOP + "/api_tokobaju/images/";
                        for (HistoryResponse.Data tx : dataList) {
                            allHistory.add(new HistoryItem(
                                    tx.id_transaksi,
                                    tx.tanggal,
                                    tx.status,
                                    "Pesanan #" + tx.id_transaksi,
                                    tx.total_harga,
                                    baseUrl + "default.jpg"
                            ));
                        }
                    }
                }
                filterHistory();
            }

            @Override
            public void onFailure(@NonNull Call<HistoryResponse> call, @NonNull Throwable t) {
                allHistory.clear();
                filterHistory();
                Toast.makeText(PaymentHistoryActivity.this, "Gagal memuat data dari server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

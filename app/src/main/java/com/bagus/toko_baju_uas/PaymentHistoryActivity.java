package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.HistoryAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.OrderModel;
import com.bagus.toko_baju_uas.model.OrderResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentHistoryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ChipGroup chipGroup;
    private RecyclerView rvHistory;
    
    private HistoryAdapter adapter;
    private final List<OrderModel> allHistory = new ArrayList<>();
    private final List<OrderModel> filteredHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        btnBack = findViewById(R.id.btnBack);
        chipGroup = findViewById(R.id.chipGroup);
        rvHistory = findViewById(R.id.rvHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(this, filteredHistory);
        rvHistory.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });
        
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterHistory());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Session expired, please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getUserHistory(uid).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        allHistory.clear();
                        allHistory.addAll(response.body().getData());
                        filterHistory();
                    } else {
                        allHistory.clear();
                        filterHistory();
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(PaymentHistoryActivity.this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterHistory() {
        int checkedChipId = chipGroup.getCheckedChipId();
        String selectedFilter = "semua";
        Chip selectedChip = chipGroup.findViewById(checkedChipId);
        if (selectedChip != null) {
            selectedFilter = selectedChip.getText().toString().toLowerCase();
        }

        filteredHistory.clear();
        for (OrderModel item : allHistory) {
            boolean matches = false;
            String status = item.getStatus() != null ? item.getStatus().toLowerCase() : "berlangsung";
            
            if (selectedFilter.equals("semua")) {
                matches = true;
            } else if (selectedFilter.equals("berlangsung")) {
                matches = status.equals("berlangsung");
            } else if (selectedFilter.equals("selesai")) {
                matches = status.equals("selesai");
            } else if (selectedFilter.equals("gagal")) {
                matches = status.equals("gagal") || status.equals("dibatalkan");
            }

            if (matches) {
                filteredHistory.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}

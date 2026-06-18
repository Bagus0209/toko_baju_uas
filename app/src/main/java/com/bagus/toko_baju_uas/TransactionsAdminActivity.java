package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.TransactionsAdminAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.OrderModel;
import com.bagus.toko_baju_uas.model.OrderResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsAdminActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TransactionsAdminAdapter adapter;
    private List<OrderModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_transactions_admin);

        ImageButton btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrdersAdmin);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        loadOrders();
    }

    private void loadOrders() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getAdminOrders().enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    list = response.body().getData();
                    adapter = new TransactionsAdminAdapter(TransactionsAdminActivity.this, list, () -> loadOrders());
                    rvOrders.setAdapter(adapter);
                } else {
                    Toast.makeText(TransactionsAdminActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                Toast.makeText(TransactionsAdminActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
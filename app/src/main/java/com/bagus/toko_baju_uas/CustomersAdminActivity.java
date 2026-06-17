package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.CustomersAdminAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.UsersResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomersAdminActivity extends AppCompatActivity {

    private RecyclerView rvCustomers;
    private CustomersAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_customers_admin);

        ImageButton btnBack = findViewById(R.id.btnBack);
        rvCustomers = findViewById(R.id.rvCustomersAdmin);
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        loadCustomers();
    }

    private void loadCustomers() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getAdminCustomers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    adapter = new CustomersAdminAdapter(response.body().getData());
                    rvCustomers.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {
                Toast.makeText(CustomersAdminActivity.this, "Koneksi terputus: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
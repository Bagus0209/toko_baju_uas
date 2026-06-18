package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.CustomersAdminAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.UserModel;
import com.bagus.toko_baju_uas.model.UsersResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomersAdminActivity extends AppCompatActivity {

    private RecyclerView rvCustomers;
    private CustomersAdminAdapter adapter;
    private List<UserModel> allCustomers = new ArrayList<>();
    private List<UserModel> filteredCustomers = new ArrayList<>();
    private TextView tvTotalCount;
    private TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_admin);

        ImageButton btnBack = findViewById(R.id.btnBack);
        rvCustomers = findViewById(R.id.rvCustomersAdmin);
        tvTotalCount = findViewById(R.id.tvTotalCustomersCount);
        etSearch = findViewById(R.id.etSearchCustomers);

        rvCustomers.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        setupSearch();
        loadCustomers();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        filteredCustomers.clear();
        if (query.isEmpty()) {
            filteredCustomers.addAll(allCustomers);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (UserModel user : allCustomers) {
                if (user.getUsername().toLowerCase().contains(lowerQuery) || 
                    user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filteredCustomers.add(user);
                }
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void loadCustomers() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getAdminCustomers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    allCustomers = response.body().getData();
                    filteredCustomers.clear();
                    filteredCustomers.addAll(allCustomers);
                    
                    tvTotalCount.setText(String.valueOf(allCustomers.size()));
                    
                    adapter = new CustomersAdminAdapter(filteredCustomers);
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
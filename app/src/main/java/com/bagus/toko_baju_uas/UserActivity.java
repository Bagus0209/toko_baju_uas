package com.bagus.toko_baju_uas;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.BajuUserAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BajuModel;
import com.bagus.toko_baju_uas.model.BarangResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    private RecyclerView rvBajuUser;
    private BajuUserAdapter adapter;
    private List<BajuModel> listBaju = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_user);

        // Kenalkan RecyclerView
        rvBajuUser = findViewById(R.id.rvBajuUser);
        rvBajuUser.setLayoutManager(new LinearLayoutManager(this));

        // Panggil data barang dari server
        ambilDataBarang();
    }

    private void ambilDataBarang() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<BarangResponse> call = api.getBarang();

        call.enqueue(new Callback<BarangResponse>() {
            @Override
            public void onResponse(Call<BarangResponse> call, Response<BarangResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        // Jika sukses, masukkan data ke Adapter
                        listBaju = response.body().getData();
                        adapter = new BajuUserAdapter(UserActivity.this, listBaju);
                        rvBajuUser.setAdapter(adapter);
                    } else {
                        Toast.makeText(UserActivity.this, "Data kosong", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BarangResponse> call, Throwable t) {
                Toast.makeText(UserActivity.this, "Gagal memuat katalog: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
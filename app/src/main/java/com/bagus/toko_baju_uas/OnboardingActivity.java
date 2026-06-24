package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnNext, btnSkip;
    private String[] titles = {"Selamat Datang", "Koleksi Eksklusif", "Mudah Digunakan", "Keuntungan Maksimal", "Siap Memulai?"};
    private String[] descriptions = {
        "Selamat datang di LuxeThreads, aplikasi e-commerce fashion premium portofolio saya.",
        "Jelajahi berbagai koleksi baju mewah dengan sistem stok yang selalu update.",
        "Cukup pilih baju, masukkan keranjang, dan lakukan pembayaran dengan mudah.",
        "Nikmati keamanan transaksi dengan Google Sign-In dan tracking pesanan real-time.",
        "Ayo mulai pengalaman belanja mewah Anda sekarang juga!"
    };
    private int[] images = {R.drawable.ic_home, R.drawable.ic_inventory, R.drawable.ic_bag, R.drawable.ic_search, R.drawable.ic_person};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        viewPager.setAdapter(new OnboardingAdapter());
        new TabLayoutMediator(findViewById(R.id.tabLayout), viewPager, (tab, position) -> {}).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                btnNext.setText(position == titles.length - 1 ? "Mulai Belanja" : "Lanjutkan");
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < titles.length - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                saveStatusAndExit("complete");
            }
        });

        btnSkip.setOnClickListener(v -> saveStatusAndExit("skip"));
        
        // Catat tampilan hari ini ke server secara background
        updateStatusOnServer("seen");
    }

    private void saveStatusAndExit(String action) {
        // 1. Simpan di Local Cache dulu agar PengunjungActivity tidak bingung (Safety Net)
        getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_finished_locally", true)
                .apply();

        // 2. Tampilkan loading singkat
        btnNext.setEnabled(false);
        btnSkip.setEnabled(false);
        Toast.makeText(this, "Menyiapkan toko...", Toast.LENGTH_SHORT).show();

        // 3. Kirim ke server, tunggu response baru pindah halaman
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            goToMain(action);
            return;
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateOnboarding(uid, action).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                goToMain(action);
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                // Jika koneksi gagal, tetap pindah agar user tidak stuck
                goToMain(action);
            }
        });
    }

    private void goToMain(String action) {
        Intent intent = new Intent(OnboardingActivity.this, PengunjungActivity.class);
        if ("complete".equals(action)) intent.putExtra("start_tour", true);
        startActivity(intent);
        finish();
    }

    private void updateStatusOnServer(String action) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        ApiClient.getClient().create(ApiInterface.class).updateOnboarding(uid, action).enqueue(new Callback<BaseResponse>() {
            @Override public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {}
            @Override public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {}
        });
    }

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvTitle.setText(titles[position]);
            holder.tvDesc.setText(descriptions[position]);
            holder.ivImage.setImageResource(images[position]);
        }
        @Override public int getItemCount() { return titles.length; }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc; ImageView ivImage;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDesc = itemView.findViewById(R.id.tvDesc);
                ivImage = itemView.findViewById(R.id.ivImage);
            }
        }
    }
}

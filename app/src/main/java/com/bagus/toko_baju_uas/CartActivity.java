package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.adapter.CartAdapter;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bagus.toko_baju_uas.model.CartModel;
import com.bagus.toko_baju_uas.model.CartResponse;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
    private TextView tvTotalHarga, tvSubtotal;
    private LinearLayout emptyState, bottomLayout;
    private View cartContent;
    private CartAdapter adapter;
    private final List<CartModel> cartList = new ArrayList<>();
    private int totalBayar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_cart);

        // Initialize UI Elements
        rvCart = findViewById(R.id.rvCart);
        tvTotalHarga = findViewById(R.id.tvTotalHarga);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        emptyState = findViewById(R.id.emptyState);
        cartContent = findViewById(R.id.cartContent);
        bottomLayout = findViewById(R.id.bottomLayout);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);
        MaterialButton btnStartShopping = findViewById(R.id.btnStartShopping);

        // Setup RecyclerView
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, cartList, this);
        rvCart.setAdapter(adapter);

        // Button Listeners
        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnCheckout.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            if (!cartList.isEmpty()) {
                // Show shipping address input dialog
                android.widget.EditText inputAlamat = new android.widget.EditText(this);
                inputAlamat.setHint("Masukkan alamat lengkap pengiriman");
                android.content.SharedPreferences spSettings = getSharedPreferences("app_settings", MODE_PRIVATE);
                String savedAddress = spSettings.getString("user_default_address", "Jl. Luxe Threads No. 8, Jakarta Pusat");
                inputAlamat.setText(savedAddress);

                android.widget.FrameLayout container = new android.widget.FrameLayout(this);
                android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                int margin = (int) (16 * getResources().getDisplayMetrics().density);
                params.setMargins(margin, margin / 2, margin, margin / 2);
                inputAlamat.setLayoutParams(params);
                container.addView(inputAlamat);

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Konfirmasi Alamat Pengiriman")
                        .setMessage("Silakan lengkapi alamat pengiriman Anda:")
                        .setView(container)
                        .setPositiveButton("Lanjutkan Pembayaran", (dialog, which) -> {
                            String alamat = inputAlamat.getText().toString().trim();
                            if (alamat.isEmpty()) {
                                Toast.makeText(this, "Alamat wajib diisi!", Toast.LENGTH_SHORT).show();
                            } else {
                                prosesCheckout(alamat);
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            } else {
                Toast.makeText(this, "Keranjang belanja masih kosong!", Toast.LENGTH_SHORT).show();
            }
        });

        btnStartShopping.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        loadCartData();
    }

    private void loadCartData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            showCart(false);
            return;
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getCart(uid).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    cartList.clear();
                    if (response.body().getData() != null) {
                        cartList.addAll(response.body().getData());
                    }
                    adapter.notifyDataSetChanged();
                    updatePriceCalculation();
                    showCart(!cartList.isEmpty());
                } else {
                    showCart(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Toast.makeText(CartActivity.this, "Gagal memuat data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showCart(false);
            }
        });
    }

    private void updatePriceCalculation() {
        int subtotal = 0;
        for (CartModel item : cartList) {
            subtotal += (item.getHarga() * item.getJumlah());
        }
        
        int serviceFee = subtotal > 0 ? 2000 : 0;
        totalBayar = subtotal + serviceFee;

        Locale localeID = Locale.forLanguageTag("id-ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        
        if (tvSubtotal != null) tvSubtotal.setText(formatRupiah.format(subtotal));
        if (tvTotalHarga != null) tvTotalHarga.setText(formatRupiah.format(totalBayar));
    }

    private void showCart(boolean hasItems) {
        if (hasItems) {
            if (cartContent != null) cartContent.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (bottomLayout != null) bottomLayout.setVisibility(View.VISIBLE);
        } else {
            if (cartContent != null) cartContent.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (bottomLayout != null) bottomLayout.setVisibility(View.GONE);
        }
    }

    private void prosesCheckout(String alamat) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.checkout(uid, totalBayar, alamat).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        // Pass details to Success screen to print receipt
                        Intent intent = new Intent(CartActivity.this, CheckoutSuccessActivity.class);
                        String itemsJson = new com.google.gson.Gson().toJson(cartList);
                        intent.putExtra("purchase_items_json", itemsJson);
                        intent.putExtra("total_bayar", totalBayar);
                        intent.putExtra("alamat", alamat);
                        
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CartActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(CartActivity.this, "Gagal memproses pembayaran.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(CartActivity.this, "Koneksi ke server gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCartChanged() {
        updatePriceCalculation();
        if (cartList.isEmpty()) showCart(false);
    }
}
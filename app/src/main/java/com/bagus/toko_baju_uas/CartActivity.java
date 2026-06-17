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
    private TextView tvTotalHarga;
    private LinearLayout emptyState, cartContent;
    private CartAdapter adapter;
    private List<CartModel> cartList = new ArrayList<>();
    private int totalBayar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        tvTotalHarga = findViewById(R.id.tvTotalHarga);
        emptyState = findViewById(R.id.emptyState);
        cartContent = findViewById(R.id.cartContent);
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);
        MaterialButton btnStartShopping = findViewById(R.id.btnStartShopping);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, cartList, this);
        rvCart.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            finish();
        });

        btnCheckout.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Keranjang Anda kosong!", Toast.LENGTH_SHORT).show();
            } else {
                prosesCheckout();
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
        if (uid == null) return;

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getCart(uid).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    cartList.clear();
                    cartList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    updateTotalPrice();
                    showCart(true);
                } else {
                    showCart(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Toast.makeText(CartActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showCart(false);
            }
        });
    }

    private void updateTotalPrice() {
        totalBayar = 0;
        for (CartModel item : cartList) {
            totalBayar += (item.getHarga() * item.getJumlah());
        }
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        tvTotalHarga.setText(formatRupiah.format(totalBayar));
    }

    private void showCart(boolean hasItems) {
        if (hasItems) {
            cartContent.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            findViewById(R.id.bottomCheckout).setVisibility(View.VISIBLE);
        } else {
            cartContent.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            findViewById(R.id.bottomCheckout).setVisibility(View.GONE);
        }
    }

    private void prosesCheckout() {
        String uid = FirebaseAuth.getInstance().getUid();
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.checkout(uid, totalBayar).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(CartActivity.this, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(CartActivity.this, CheckoutSuccessActivity.class));
                    finish();
                } else {
                    Toast.makeText(CartActivity.this, "Gagal Checkout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCartChanged() {
        updateTotalPrice();
        if (cartList.isEmpty()) showCart(false);
    }
}